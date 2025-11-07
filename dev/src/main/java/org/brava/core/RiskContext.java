package org.brava.core;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RiskContext(
        String payerId,
        String payeeId,
        BigDecimal amount,
        String currency,
        int successfulPaymentsLast30Days,
        BigDecimal totalAmountLast30Days,
        int paymentsToSamePayeeLast7Days,
        LocalDateTime lastPaymentToSamePayee,
        boolean isFirstTransaction
) {
    public boolean hasHistory() {
        return !isFirstTransaction;
    }

    public boolean isHighValue() {
        return amount.compareTo(BigDecimal.valueOf(10000)) > 0;
    }
}
