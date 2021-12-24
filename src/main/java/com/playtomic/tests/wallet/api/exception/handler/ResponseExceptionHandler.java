package com.playtomic.tests.wallet.api.exception.handler;

import com.playtomic.tests.wallet.api.exception.WalletControllerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice(basePackages = "com.playtomic.tests.wallet")
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String WALLET_ERROR_TITTLE = "Problem occurred during the work with the Wallet";

    @ExceptionHandler({WalletControllerException.class})
    public ResponseEntity<Object> handleWalletException(WalletControllerException ex, WebRequest request) {
	HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
	ExceptionDetails exceptionDetails = ExceptionDetails.builder()
		.title(WALLET_ERROR_TITTLE)
		.message(ex.getMessage()).status(status).build();
	return super.handleExceptionInternal(ex, exceptionDetails, new HttpHeaders(), status, request);
    }
}
