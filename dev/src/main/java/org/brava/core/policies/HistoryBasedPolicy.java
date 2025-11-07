package org.brava.core.policies;

import org.brava.core.PaymentDecision;
import org.brava.core.RiskContext;

import java.math.BigDecimal;

public class HistoryBasedPolicy implements PaymentPolicy{

    private static final BigDecimal LIMIT_NEW_USER = BigDecimal.valueOf(1000);
    private static final BigDecimal LIMIT_BASIC = BigDecimal.valueOf(5000);
    private static final BigDecimal LIMIT_TRUSTED = BigDecimal.valueOf(10000);
    private static final BigDecimal LIMIT_VIP = BigDecimal.valueOf(50000);


    @Override
    public PaymentDecision evaluate(RiskContext context) {
        BigDecimal limit = calculateLimit(context);

        if (context.amount().compareTo(limit) > 0) {
            return PaymentDecision.decline(
                    String.format("Amount %.2f exceeds history-based limit %.2f", context.amount(), limit),
                    70
            );
        }

        return PaymentDecision.approve(
                generateTransactionId(),
                String.format("Amount within history-based limit %.2f", limit)
        );
    }

    private BigDecimal calculateLimit(RiskContext context) {
        if (context.isFirstTransaction()) {
            return LIMIT_NEW_USER;
        }

        int history = context.successfulPaymentsLast30Days();
        BigDecimal total = context.totalAmountLast30Days();

        if (history >= 20 && total.compareTo(BigDecimal.valueOf(20000)) > 0) {
            return LIMIT_VIP;
        }

        if (history >= 10) {
            return LIMIT_TRUSTED;
        }

        if (history >= 3) {
            return LIMIT_BASIC;
        }

        return LIMIT_NEW_USER;
    }

    private String generateTransactionId() {
        return "txn-" + System.currentTimeMillis();
    }

}
