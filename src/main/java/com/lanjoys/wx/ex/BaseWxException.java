package com.lanjoys.wx.ex;

public class BaseWxException extends Exception {
    public BaseWxException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}
