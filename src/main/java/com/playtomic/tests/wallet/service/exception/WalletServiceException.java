package com.playtomic.tests.wallet.service.exception;

public class WalletServiceException extends RuntimeException {

    public WalletServiceException(String message) {
	super(message);
    }

    public WalletServiceException(String message, Throwable throwable) {
	super(message, throwable);
    }
}
