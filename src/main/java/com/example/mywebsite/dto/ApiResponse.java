package com.example.mywebsite.dto;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Integer count;
    private Integer updated;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(boolean success, String message, Integer count) {
        this.success = success;
        this.message = message;
        this.count = count;
    }

    public ApiResponse(boolean success, String message, Integer count, Integer updated) {
        this.success = success;
        this.message = message;
        this.count = count;
        this.updated = updated;
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> success(String message, Integer count) {
        return new ApiResponse<>(true, message, count);
    }

    public static <T> ApiResponse<T> success(String message, Integer count, Integer updated) {
        return new ApiResponse<>(true, message, count, updated);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getUpdated() {
        return updated;
    }

    public void setUpdated(Integer updated) {
        this.updated = updated;
    }
}
