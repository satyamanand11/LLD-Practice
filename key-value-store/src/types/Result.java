package com.lld.kvstore.types;

import java.util.Optional;

public class Result<T> {
    private final boolean success;
    private final T data;
    private final String error;
    
    private Result(boolean success, T data, String error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }
    
    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null);
    }
    
    public static <T> Result<T> error(String error) {
        return new Result<>(false, null, error);
    }
    
    public boolean isOk() {
        return success;
    }
    
    public T get() {
        return data;
    }
    
    public String error() {
        return error;
    }
    
    public Optional<T> getData() {
        return Optional.ofNullable(data);
    }
    
    public Optional<String> getError() {
        return Optional.ofNullable(error);
    }
    
    @Override
    public String toString() {
        if (success) {
            return "Result{success=true, data=" + data + "}";
        } else {
            return "Result{success=false, error='" + error + "'}";
        }
    }
}
