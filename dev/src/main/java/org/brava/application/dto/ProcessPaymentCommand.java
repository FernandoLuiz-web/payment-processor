package org.brava.application.dto;

import java.math.BigDecimal;

public record ProcessPaymentCommand(
        String idempotencyKey,
        String payerId,
        String payeeId,
        BigDecimal amount,
        String currency,
        String description
) {}