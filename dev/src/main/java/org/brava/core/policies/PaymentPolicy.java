package org.brava.core.policies;

import org.brava.core.PaymentDecision;
import org.brava.core.RiskContext;

@FunctionalInterface
public interface PaymentPolicy {

    PaymentDecision evaluate(RiskContext context);

    default PaymentPolicy and(PaymentPolicy other) {
        return context -> {
            PaymentDecision result = this.evaluate(context);
            if (!result.isApproved()) {
                return result;
            }
            return other.evaluate(context);
        };
    }

    default String getName() {
        return this.getClass().getSimpleName();
    }
}
