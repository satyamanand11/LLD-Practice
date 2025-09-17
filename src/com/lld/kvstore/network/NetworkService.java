package com.lld.kvstore.network;

import com.lld.kvstore.interfaces.ByteKeyValueStore;
import com.lld.kvstore.metrics.MetricsCollector;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkService {
    private final ByteKeyValueStore kvStore;
    private final MetricsCollector metricsCollector;
    private final ExecutorService threadPool;
    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final AtomicBoolean running;
    private final int port;
    
    public NetworkService(ByteKeyValueStore kvStore, MetricsCollector metricsCollector) {
        this.kvStore = kvStore;
        this.metricsCollector = metricsCollector;
        this.threadPool = Executors.newFixedThreadPool(16, r -> {
            Thread t = new Thread(r, "NetworkService-Thread");
            t.setDaemon(true);
            return t;
        });
        this.running = new AtomicBoolean(false);
        this.port = 0; 
        
        try {
            this.serverChannel = ServerSocketChannel.open();
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize network service", e);
        }
    }
    
    public void start(int port) throws IOException {
        if (running.get()) {
            throw new IllegalStateException("Network service is already running");
        }
        
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        running.set(true);
        
        threadPool.submit(this::eventLoop);
        
        System.out.println("Network service started on port " + port);
    }
    
    public void stop() throws IOException {
        if (!running.get()) {
            return;
        }
        
        running.set(false);
        serverChannel.close();
        selector.close();
        threadPool.shutdown();
        
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Network service stopped");
    }
    
    private void eventLoop() {
        try {
            while (running.get()) {
                int readyChannels = selector.select(1000); 
                
                if (readyChannels == 0) {
                    continue;
                }
                
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    } else if (key.isWritable()) {
                        handleWrite(key);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error in event loop: " + e.getMessage());
        }
    }
    
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        
        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ, new ClientSession(clientChannel));
            System.out.println("New client connected: " + clientChannel.getRemoteAddress());
        }
    }
    
    private void handleRead(SelectionKey key) throws IOException {
        ClientSession session = (ClientSession) key.attachment();
        SocketChannel clientChannel = session.getChannel();
        
        ByteBuffer buffer = session.getReadBuffer();
        int bytesRead = clientChannel.read(buffer);
        
        if (bytesRead == -1) {
            
            clientChannel.close();
            key.cancel();
            System.out.println("Client disconnected: " + clientChannel.getRemoteAddress());
            return;
        }
        
        if (bytesRead > 0) {
            buffer.flip();
            
            while (buffer.hasRemaining()) {
                if (buffer.remaining() < 4) {
                    
                    buffer.compact();
                    return;
                }
                
                int messageLength = buffer.getInt();
                if (buffer.remaining() < messageLength) {
                    
                    buffer.position(buffer.position() - 4); 
                    buffer.compact();
                    return;
                }
                
                byte[] messageData = new byte[messageLength];
                buffer.get(messageData);
                
                threadPool.submit(() -> processMessage(session, messageData));
            }
            
            buffer.compact();
        }
    }
    
    private void handleWrite(SelectionKey key) throws IOException {
        ClientSession session = (ClientSession) key.attachment();
        SocketChannel clientChannel = session.getChannel();
        
        ByteBuffer writeBuffer = session.getWriteBuffer();
        if (writeBuffer.hasRemaining()) {
            clientChannel.write(writeBuffer);
        }
        
        if (!writeBuffer.hasRemaining()) {
            key.interestOps(SelectionKey.OP_READ);
        }
    }
    
    private void processMessage(ClientSession session, byte[] messageData) {
        try {
            Request request = Request.deserialize(messageData);
            Response response = handleRequest(request);
            sendResponse(session, response);
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            try {
                Response errorResponse = Response.error("Internal server error");
                sendResponse(session, errorResponse);
            } catch (IOException ioException) {
                System.err.println("Error sending error response: " + ioException.getMessage());
            }
        }
    }
    
    private Response handleRequest(Request request) {
        long startTime = System.nanoTime();
        
        try {
            switch (request.getType()) {
                case GET:
                    return handleGet(request);
                case PUT:
                    return handlePut(request);
                case DELETE:
                    return handleDelete(request);
                case BATCH:
                    return handleBatch(request);
                default:
                    return Response.error("Unknown request type");
            }
        } finally {
            long latency = (System.nanoTime() - startTime) / 1_000_000; 
            metricsCollector.recordOperation(request.getType().name(), latency);
        }
    }
    
    private Response handleGet(Request request) {
        ByteKeyValueStore.Result<byte[]> result = kvStore.get(request.getKey());
        if (result.isSuccess()) {
            return Response.success(result.getData().orElse(null));
        } else {
            return Response.error(result.getError().orElse("Unknown error"));
        }
    }
    
    private Response handlePut(Request request) {
        ByteKeyValueStore.Result<Boolean> result = kvStore.put(request.getKey(), request.getValue());
        if (result.isSuccess()) {
            return Response.success(result.getData().orElse(false));
        } else {
            return Response.error(result.getError().orElse("Unknown error"));
        }
    }
    
    private Response handleDelete(Request request) {
        ByteKeyValueStore.Result<Boolean> result = kvStore.delete(request.getKey());
        if (result.isSuccess()) {
            return Response.success(result.getData().orElse(false));
        } else {
            return Response.error(result.getError().orElse("Unknown error"));
        }
    }
    
    private Response handleBatch(Request request) {
        
        return Response.error("Batch operations not yet implemented");
    }
    
    private void sendResponse(ClientSession session, Response response) throws IOException {
        byte[] responseData = response.serialize();
        ByteBuffer writeBuffer = session.getWriteBuffer();
        
        writeBuffer.putInt(responseData.length);
        writeBuffer.put(responseData);
        writeBuffer.flip();
        
        session.getChannel().keyFor(selector).interestOps(SelectionKey.OP_WRITE);
    }
    
    private static class ClientSession {
        private final SocketChannel channel;
        private final ByteBuffer readBuffer;
        private final ByteBuffer writeBuffer;
        
        public ClientSession(SocketChannel channel) {
            this.channel = channel;
            this.readBuffer = ByteBuffer.allocate(64 * 1024); 
            this.writeBuffer = ByteBuffer.allocate(64 * 1024); 
        }
        
        public SocketChannel getChannel() { return channel; }
        public ByteBuffer getReadBuffer() { return readBuffer; }
        public ByteBuffer getWriteBuffer() { return writeBuffer; }
    }
    
    public enum RequestType {
        GET, PUT, DELETE, BATCH
    }
    
    public static class Request {
        private final RequestType type;
        private final byte[] key;
        private final byte[] value;
        private final java.util.List<ByteKeyValueStore.KeyValuePair> batch;
        
        public Request(RequestType type, byte[] key, byte[] value) {
            this.type = type;
            this.key = key;
            this.value = value;
            this.batch = null;
        }
        
        public Request(RequestType type, java.util.List<ByteKeyValueStore.KeyValuePair> batch) {
            this.type = type;
            this.key = null;
            this.value = null;
            this.batch = batch;
        }
        
        public byte[] serialize() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            dos.writeInt(type.ordinal());
            
            if (key != null) {
                dos.writeInt(key.length);
                dos.write(key);
            } else {
                dos.writeInt(0);
            }
            
            if (value != null) {
                dos.writeInt(value.length);
                dos.write(value);
            } else {
                dos.writeInt(0);
            }
            
            if (batch != null) {
                dos.writeInt(batch.size());
                for (ByteKeyValueStore.KeyValuePair pair : batch) {
                    dos.writeInt(pair.getKey().length);
                    dos.write(pair.getKey());
                    dos.writeInt(pair.getValue().length);
                    dos.write(pair.getValue());
                }
            } else {
                dos.writeInt(0);
            }
            
            return baos.toByteArray();
        }
        
        public static Request deserialize(byte[] data) throws IOException {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            
            RequestType type = RequestType.values()[dis.readInt()];
            
            int keyLength = dis.readInt();
            byte[] key = null;
            if (keyLength > 0) {
                key = new byte[keyLength];
                dis.readFully(key);
            }
            
            int valueLength = dis.readInt();
            byte[] value = null;
            if (valueLength > 0) {
                value = new byte[valueLength];
                dis.readFully(value);
            }
            
            int batchSize = dis.readInt();
            java.util.List<ByteKeyValueStore.KeyValuePair> batch = null;
            if (batchSize > 0) {
                batch = new java.util.ArrayList<>();
                for (int i = 0; i < batchSize; i++) {
                    int pairKeyLength = dis.readInt();
                    byte[] pairKey = new byte[pairKeyLength];
                    dis.readFully(pairKey);
                    
                    int pairValueLength = dis.readInt();
                    byte[] pairValue = new byte[pairValueLength];
                    dis.readFully(pairValue);
                    
                    batch.add(new ByteKeyValueStore.KeyValuePair(pairKey, pairValue));
                }
            }
            
            if (batch != null) {
                return new Request(type, batch);
            } else {
                return new Request(type, key, value);
            }
        }
        
        public RequestType getType() { return type; }
        public byte[] getKey() { return key; }
        public byte[] getValue() { return value; }
        public java.util.List<ByteKeyValueStore.KeyValuePair> getBatch() { return batch; }
    }
    
    public static class Response {
        private final boolean success;
        private final byte[] data;
        private final String error;
        
        private Response(boolean success, byte[] data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }
        
        public static Response success(byte[] data) {
            return new Response(true, data, null);
        }
        
        public static Response success(boolean success) {
            return new Response(true, success ? new byte[]{1} : new byte[]{0}, null);
        }
        
        public static Response error(String error) {
            return new Response(false, null, error);
        }
        
        public byte[] serialize() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            dos.writeBoolean(success);
            
            if (data != null) {
                dos.writeInt(data.length);
                dos.write(data);
            } else {
                dos.writeInt(0);
            }
            
            if (error != null) {
                byte[] errorBytes = error.getBytes("UTF-8");
                dos.writeInt(errorBytes.length);
                dos.write(errorBytes);
            } else {
                dos.writeInt(0);
            }
            
            return baos.toByteArray();
        }
        
        public boolean isSuccess() { return success; }
        public byte[] getData() { return data; }
        public String getError() { return error; }
    }
}
