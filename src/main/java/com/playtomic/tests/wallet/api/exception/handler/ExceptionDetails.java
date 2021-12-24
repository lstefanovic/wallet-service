package com.playtomic.tests.wallet.api.exception.handler;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Builder
@Getter
public class ExceptionDetails {

    private String message;
    private HttpStatus status;
    private String title;
}
