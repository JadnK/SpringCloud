package de.jadenk.springcloud.exception;

public class CustomRuntimeException extends RuntimeException {

    public CustomRuntimeException(String msg) {
        super(msg);
    }

}
