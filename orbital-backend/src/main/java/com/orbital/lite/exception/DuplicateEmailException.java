package com.orbital.lite.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("Employee email already exists: " + email);
    }
}
