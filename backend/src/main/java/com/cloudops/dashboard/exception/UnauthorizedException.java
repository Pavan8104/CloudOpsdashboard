package com.cloudops.dashboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Unauthorized exception - 403 Forbidden ke liye.
 *
 * User logged in hai but uske paas yeh action karne ka permission nahi.
 * 401 Unauthorized (not authenticated) se alag hai yeh - 403 Forbidden (not authorized).
 * Spring Security khud 403 throw karta hai mostly, but custom logic ke liye yeh use karo.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    // Specific action aur resource ke liye - clear error message
    public UnauthorizedException(String action, String resource) {
        super(String.format("Aapko '%s' karne ka permission nahi hai '%s' ke liye", action, resource));
    }
}
