package com.zeng.zar.exception;

public class ClassFinderException extends RuntimeException{

    private static final long serialVersionUID = 2643645974076577393L;

    public ClassFinderException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ClassFinderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassFinderException(String message) {
        super(message);
    }

    public ClassFinderException(Throwable cause) {
        super(cause);
    }
    
}
