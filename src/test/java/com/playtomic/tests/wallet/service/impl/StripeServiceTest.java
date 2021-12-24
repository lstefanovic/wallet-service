package com.playtomic.tests.wallet.service.impl;


import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.exception.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.exception.StripeRestTemplateResponseErrorHandler;
import com.playtomic.tests.wallet.service.exception.StripeServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This test is failing with the current implementation.
 * <p>
 * How would you test this?
 */
@ExtendWith(MockitoExtension.class)
public class StripeServiceTest {

    private static final URI testUri = URI.create("http://how-would-you-test-me.localhost");
    private static RestTemplate restTemplate;
    private static StripeService stripeService;

    @Test
    public void test_exception() {
	Assertions.assertThrows(StripeAmountTooSmallException.class, () -> {
	    StripeAmountTooSmallException ex = new StripeAmountTooSmallException();
	    when(restTemplate.postForObject(any(), any(), any())).thenThrow(ex);
	    stripeService.charge("4242 4242 4242 4242", new BigDecimal(5));
	    verify(restTemplate).postForObject(any(), any(), any());
	});
    }

    @Test
    public void test_ok() throws StripeServiceException {
	Object ob = new Object();

	when(restTemplate.postForObject(any(), any(), any())).thenReturn(ob);
	stripeService.charge("4242 4242 4242 4242", new BigDecimal(15));
	verify(restTemplate).postForObject(any(), any(), any());

    }

    @BeforeAll
    private static void setUp() {
	restTemplate = mock(RestTemplate.class);
	RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
	when(builder.errorHandler(any(StripeRestTemplateResponseErrorHandler.class))).thenReturn(builder);
	when(builder.build()).thenReturn(restTemplate);
	stripeService = new StripeService(testUri, testUri, builder);
	verify(builder).errorHandler(any(StripeRestTemplateResponseErrorHandler.class));
	verify(builder).build();
    }


}
