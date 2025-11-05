package org.brava.application.dto;

import org.brava.domain.enums.PaymentStatus;
import org.brava.domain.models.Payment;

public record PaymentResult(
        String transactionId,
        PaymentStatus status,
        String message
) {
    public static PaymentResult fromPayment(Payment payment) {
        return new PaymentResult(
                payment.getTransactionId(),
                payment.getStatus(),
                payment.getMessage()
        );
    }
}