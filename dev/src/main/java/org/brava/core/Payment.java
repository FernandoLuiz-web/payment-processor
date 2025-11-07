package org.brava.core;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Payment(
        Long id,
        String idempotencyKey,
        String payerId,
        String payeeId,
        BigDecimal amount,
        String currency,
        String description,
        PaymentStatus status,
        String transactionId,
        String message,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static Payment createPending(
            String idempotencyKey,
            String payerId,
            String payeeId,
            BigDecimal amount,
            String currency,
            String description
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new Payment(
                null,
                idempotencyKey,
                payerId,
                payeeId,
                amount,
                currency,
                description,
                PaymentStatus.PENDING,
                null,
                null,
                now,
                now
        );
    }

    public Payment approve(String transactionId) {
        return new Payment(
                id,
                idempotencyKey,
                payerId,
                payeeId,
                amount,
                currency,
                description,
                PaymentStatus.APPROVED,
                transactionId,
                "Payment processed successfully",
                createdAt,
                LocalDateTime.now()
        );
    }

    public Payment decline(String reason) {
        return new Payment(
                id,
                idempotencyKey,
                payerId,
                payeeId,
                amount,
                currency,
                description,
                PaymentStatus.DECLINED,
                null,
                reason,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Payment withId(Long id) {
        return new Payment(
                id,
                idempotencyKey,
                payerId,
                payeeId,
                amount,
                currency,
                description,
                status,
                transactionId,
                message,
                createdAt,
                updatedAt
        );
    }

}
