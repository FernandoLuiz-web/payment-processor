package org.brava.core.policies;

import org.brava.core.PaymentDecision;
import org.brava.core.RiskContext;

import java.time.Duration;
import java.time.LocalDateTime;

public class FrequencyPolicy implements PaymentPolicy{

    private static final int MAX_PAYMENTS_TO_SAME_PAYEE_WEEK = 5;
    private static final long MIN_HOURS_BETWEEN_PAYMENTS = 2;

    @Override
    public PaymentDecision evaluate(RiskContext context) {
        if (context.paymentsToSamePayeeLast7Days() >= MAX_PAYMENTS_TO_SAME_PAYEE_WEEK) {
            return PaymentDecision.decline(
                    String.format("Too many payments to same recipient (%d in last 7 days)",
                            context.paymentsToSamePayeeLast7Days()),
                    90
            );
        }

        if (context.lastPaymentToSamePayee() != null) {
            Duration timeSince = Duration.between(
                    context.lastPaymentToSamePayee(),
                    LocalDateTime.now()
            );

            if (timeSince.toHours() < MIN_HOURS_BETWEEN_PAYMENTS) {
                return PaymentDecision.decline(
                        String.format("Please wait at least %d hours between payments to same recipient",
                                MIN_HOURS_BETWEEN_PAYMENTS),
                        60
                );
            }
        }

        return PaymentDecision.approve(
                generateTransactionId(),
                "Frequency checks passed"
        );
    }

    private String generateTransactionId() {
        return "txn-" + System.currentTimeMillis();
    }
}
