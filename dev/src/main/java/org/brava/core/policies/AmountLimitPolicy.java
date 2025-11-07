package org.brava.core.policies;

import org.brava.core.PaymentDecision;
import org.brava.core.RiskContext;

import java.math.BigDecimal;

public class AmountLimitPolicy implements PaymentPolicy{

    private static final BigDecimal ABSOLUTE_MAX = BigDecimal.valueOf(100000);
    private static final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(0.01);

    @Override
    public PaymentDecision evaluate(RiskContext context) {
        BigDecimal amount = context.amount();

        if (amount.compareTo(ABSOLUTE_MAX) > 0) {
            return PaymentDecision.decline(
                    String.format("Amount %.2f exceeds maximum allowed %.2f", amount, ABSOLUTE_MAX),
                    100
            );
        }

        if (amount.compareTo(MIN_AMOUNT) < 0) {
            return PaymentDecision.decline(
                    String.format("Amount %.2f below minimum %.2f", amount, MIN_AMOUNT),
                    50
            );
        }

        return PaymentDecision.approve(
                generateTransactionId(),
                "Amount within acceptable range"
        );
    }

    private String generateTransactionId() {
        return "txn-" + System.currentTimeMillis();
    }
}
