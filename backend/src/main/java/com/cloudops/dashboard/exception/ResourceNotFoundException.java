package com.cloudops.dashboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Resource not found exception - 404 ke liye.
 *
 * Jab koi ID se resource dhundha jaaye aur mila nahi toh yeh throw karo.
 * @ResponseStatus annotation automatically 404 HTTP status bhejta hai.
 * Controller mein manually check nahi karna padta - cleaner code.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    // Resource type aur ID ke saath readable message - debugging mein helpful
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s nahi mila with %s: %s", resourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
