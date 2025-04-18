package com.hr_management.hr.exception;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.hr_management.hr.payload.ErrorDetails;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
        // handle specific exceptions

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorDetails> handleResourceNotFountException(ResourceNotFoundException exception,
                        WebRequest webRequest) {
                ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(),
                                webRequest.getDescription(false));
                return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(LeaveAPIException.class)
        public ResponseEntity<ErrorDetails> handleBlogAPIException(LeaveAPIException exception,
                        WebRequest webRequest) {
                ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(),
                                webRequest.getDescription(false));
                return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
        }
        // global exceptions

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorDetails> handleGlobalException(Exception exception,
                        WebRequest webRequest) {
                ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(),
                                webRequest.getDescription(false));
                return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Override

        protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                        @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {

                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String message = error.getDefaultMessage();
                        errors.put(fieldName, message);
                });
                return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);

        }

        //
        // @ExceptionHandler(MethodArgumentNotValidException.class)
        // public ResponseEntity<Object>
        // handleMethodArgumentNotValidException(MethodArgumentNotValidException
        // exception,
        // WebRequest webRequest) {
        // Map<String, String> errors = new HashMap<>();
        // exception.getBindingResult().getAllErrors().forEach((error) -> {
        // String fieldName = ((FieldError) error).getField();
        // String message = error.getDefaultMessage();
        // errors.put(fieldName, message);
        // });
        // return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        // }

        // access denied exceptions

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorDetails> handleAccessDeniedException(AccessDeniedException exception,
                        WebRequest webRequest) {
                ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(),
                                webRequest.getDescription(false));
                return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
        }

}
