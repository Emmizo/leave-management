package com.hr_management.hr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class LeaveAPIException extends RuntimeException {
    
    private final HttpStatus status;
    
    public LeaveAPIException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }
    
    public LeaveAPIException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
    
    public HttpStatus getStatus() {
        return status;
    }
}
