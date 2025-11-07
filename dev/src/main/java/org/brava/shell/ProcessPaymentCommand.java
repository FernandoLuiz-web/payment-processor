package org.brava.shell;

import java.math.BigDecimal;

public record ProcessPaymentCommand(
        String idempotencyKey,
        String payerId,
        String payeeId,
        BigDecimal amount,
        String currency,
        String description
){
}
