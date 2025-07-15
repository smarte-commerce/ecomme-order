package com.winnguyen1905.order.util;

import org.springframework.http.HttpStatus;

import com.winnguyen1905.order.secure.RestResponse;

/**
 * Utility class for creating standard RestResponse objects
 */
public class ResponseUtil {
    
    /**
     * Create a success response with message and data
     * 
     * @param <T> Type of response data
     * @param message Success message
     * @param data Response data
     * @return RestResponse with success status
     */
    public static <T> RestResponse<T> success(String message, T data) {
        return RestResponse.<T>builder()
                .statusCode(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * Create a success response with only a message
     * 
     * @param <T> Type of response data
     * @param message Success message
     * @return RestResponse with success status
     */
    public static <T> RestResponse<T> success(String message) {
        return RestResponse.<T>builder()
                .statusCode(HttpStatus.OK.value())
                .message(message)
                .build();
    }
    
    /**
     * Create an error response
     * 
     * @param <T> Type of response data
     * @param statusCode HTTP status code
     * @param error Error type
     * @param message Error message
     * @return RestResponse with error status
     */
    public static <T> RestResponse<T> error(int statusCode, String error, String message) {
        return RestResponse.<T>builder()
                .statusCode(statusCode)
                .error(error)
                .message(message)
                .build();
    }
} 
