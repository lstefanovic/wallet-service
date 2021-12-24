package com.playtomic.tests.wallet.api.exception;

public class WalletControllerException extends RuntimeException {

    public WalletControllerException(String message, Throwable throwable) {
	super(message, throwable);
    }
}
