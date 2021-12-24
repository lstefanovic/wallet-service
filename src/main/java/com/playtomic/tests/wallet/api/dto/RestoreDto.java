package com.playtomic.tests.wallet.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RestoreDto {

    @NonNull()
    private String paymentId;
}
