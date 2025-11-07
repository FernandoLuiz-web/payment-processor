package org.brava.core;

public sealed interface PaymentDecision {

    record Approved(String transactionId, String reason) implements PaymentDecision {}

    record Declined(String reason, int riskScore) implements PaymentDecision {}

    static PaymentDecision approve(String transactionId, String reason) {
        return new Approved(transactionId, reason);
    }

    static PaymentDecision decline(String reason, int riskScore) {
        return new Declined(reason, riskScore);
    }

    default boolean isApproved() {
        return this instanceof Approved;
    }
}
