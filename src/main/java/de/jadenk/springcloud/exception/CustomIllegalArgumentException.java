package de.jadenk.springcloud.exception;

public class CustomIllegalArgumentException extends IllegalArgumentException {

    public CustomIllegalArgumentException(String msg) {
        super(msg);
    }

}