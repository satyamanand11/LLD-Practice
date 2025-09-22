package com.lld.kvstore.network;

import com.lld.kvstore.interfaces.ByteKeyValueStore;
import com.lld.kvstore.models.Result;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkService {
    private final ByteKeyValueStore store;
    private final int port;
    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final ExecutorService clientExecutor;
    private final AtomicBoolean running;
    private final int maxClients;
    
    public NetworkService(ByteKeyValueStore store, int port) {
        this(store, port, 1000);
    }
    
    public NetworkService(ByteKeyValueStore store, int port, int maxClients) {
        this.store = store;
        this.port = port;
        this.maxClients = maxClients;
        this.running = new AtomicBoolean(false);
        this.clientExecutor = Executors.newFixedThreadPool(maxClients);
        
        try {
            this.serverChannel = ServerSocketChannel.open();
            this.serverChannel.bind(new InetSocketAddress(port));
            this.serverChannel.configureBlocking(false);
            this.selector = Selector.open();
            this.serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize network service", e);
        }
    }
    
    public void start() throws IOException {
        if (running.compareAndSet(false, true)) {
            System.out.println("Starting network service on port " + port);
            
            while (running.get()) {
                try {
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
                } catch (IOException e) {
                    System.err.println("Error in network service: " + e.getMessage());
                }
            }
        }
    }
    
    public void stop() {
        if (running.compareAndSet(true, false)) {
            try {
                selector.wakeup();
                serverChannel.close();
                clientExecutor.shutdown();
                clientExecutor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (IOException | InterruptedException e) {
                System.err.println("Error stopping network service: " + e.getMessage());
            }
        }
    }
    
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        
        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ, new ClientSession(clientChannel));
            System.out.println("Client connected: " + clientChannel.getRemoteAddress());
        }
    }
    
    private void handleRead(SelectionKey key) throws IOException {
        ClientSession session = (ClientSession) key.attachment();
        SocketChannel clientChannel = session.getChannel();
        
        try {
            ByteBuffer buffer = session.getReadBuffer();
            int bytesRead = clientChannel.read(buffer);
            
            if (bytesRead == -1) {
                closeClient(session);
                return;
            }
            
            if (bytesRead > 0) {
                buffer.flip();
                processRequest(session, buffer);
                buffer.clear();
            }
        } catch (IOException e) {
            closeClient(session);
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
    
    private void processRequest(ClientSession session, ByteBuffer buffer) {
        clientExecutor.submit(() -> {
            try {
                Request request = Request.deserialize(buffer);
                Response response = handleRequest(request);
                session.queueResponse(response);
                
                SelectionKey key = session.getChannel().keyFor(selector);
                if (key != null) {
                    key.interestOps(SelectionKey.OP_WRITE);
                }
            } catch (Exception e) {
                System.err.println("Error processing request: " + e.getMessage());
                closeClient(session);
            }
        });
    }
    
    private Response handleRequest(Request request) {
        try {
            switch (request.getType()) {
                case PUT:
                    Result<Boolean> putResult = store.put(request.getKey(), request.getValue());
                    return new Response(putResult.isSuccess(), putResult.getData().orElse(false), 
                                      putResult.getError().orElse(null));
                
                case GET:
                    Result<byte[]> getResult = store.get(request.getKey());
                    return new Response(getResult.isSuccess(), getResult.getData().orElse(null), 
                                      getResult.getError().orElse(null));
                
                case DELETE:
                    Result<Boolean> deleteResult = store.delete(request.getKey());
                    return new Response(deleteResult.isSuccess(), deleteResult.getData().orElse(false), 
                                      deleteResult.getError().orElse(null));
                
                case EXISTS:
                    Result<Boolean> existsResult = store.exists(request.getKey());
                    return new Response(existsResult.isSuccess(), existsResult.getData().orElse(false), 
                                      existsResult.getError().orElse(null));
                
                case SIZE:
                    Result<Long> sizeResult = store.size();
                    return new Response(sizeResult.isSuccess(), sizeResult.getData().orElse(0L), 
                                      sizeResult.getError().orElse(null));
                
                default:
                    return new Response(false, null, "Unknown request type: " + request.getType());
            }
        } catch (Exception e) {
            return new Response(false, null, "Error handling request: " + e.getMessage());
        }
    }
    
    private void closeClient(ClientSession session) {
        try {
            session.getChannel().close();
            System.out.println("Client disconnected: " + session.getChannel().getRemoteAddress());
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }
    
    public static class ClientSession {
        private final SocketChannel channel;
        private final ByteBuffer readBuffer;
        private final ByteBuffer writeBuffer;
        private final BlockingQueue<Response> responseQueue;
        
        public ClientSession(SocketChannel channel) {
            this.channel = channel;
            this.readBuffer = ByteBuffer.allocate(64 * 1024);
            this.writeBuffer = ByteBuffer.allocate(64 * 1024);
            this.responseQueue = new LinkedBlockingQueue<>();
        }
        
        public SocketChannel getChannel() { return channel; }
        public ByteBuffer getReadBuffer() { return readBuffer; }
        public ByteBuffer getWriteBuffer() { return writeBuffer; }
        
        public void queueResponse(Response response) {
            responseQueue.offer(response);
        }
        
        public Response pollResponse() {
            return responseQueue.poll();
        }
    }
    
    public static class Request {
        public enum Type { PUT, GET, DELETE, EXISTS, SIZE }
        
        private final Type type;
        private final byte[] key;
        private final byte[] value;
        
        public Request(Type type, byte[] key, byte[] value) {
            this.type = type;
            this.key = key;
            this.value = value;
        }
        
        public static Request deserialize(ByteBuffer buffer) {
            Type type = Type.values()[buffer.get()];
            int keyLength = buffer.getInt();
            byte[] key = new byte[keyLength];
            buffer.get(key);
            
            int valueLength = buffer.getInt();
            byte[] value = null;
            if (valueLength > 0) {
                value = new byte[valueLength];
                buffer.get(value);
            }
            
            return new Request(type, key, value);
        }
        
        public Type getType() { return type; }
        public byte[] getKey() { return key; }
        public byte[] getValue() { return value; }
    }
    
    public static class Response {
        private final boolean success;
        private final Object data;
        private final String error;
        
        public Response(boolean success, Object data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }
        
        public byte[] serialize() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (DataOutputStream dos = new DataOutputStream(baos)) {
                dos.writeBoolean(success);
                
                if (success) {
                    if (data instanceof byte[]) {
                        byte[] dataBytes = (byte[]) data;
                        dos.writeInt(dataBytes.length);
                        dos.write(dataBytes);
                    } else if (data instanceof Boolean) {
                        dos.writeInt(1);
                        dos.writeBoolean((Boolean) data);
                    } else if (data instanceof Long) {
                        dos.writeInt(8);
                        dos.writeLong((Long) data);
                    } else {
                        dos.writeInt(0);
                    }
                } else {
                    byte[] errorBytes = error.getBytes();
                    dos.writeInt(errorBytes.length);
                    dos.write(errorBytes);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize response", e);
            }
            return baos.toByteArray();
        }
        
        public boolean isSuccess() { return success; }
        public Object getData() { return data; }
        public String getError() { return error; }
    }
}
