package com.hr_management.hr.exception;

import org.springframework.http.HttpStatus;

public class LeaveAPIException extends RuntimeException{
    private HttpStatus status;
    private String message;
    
    public LeaveAPIException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
    public LeaveAPIException(String message, HttpStatus status, String message2) {
        super(message);
        this.status = status;
        message = message2;
    }
    public HttpStatus getStatus() {
        return status;
    }
    public String getMessage() {
        return message;
    }

    
}
