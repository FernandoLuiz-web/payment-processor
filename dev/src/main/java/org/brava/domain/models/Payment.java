package org.brava.domain.models;

import org.brava.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    String paymentProcessedSuccessfully = "Payment processed successfully";

    String onlyPendingPaymentsCanBeApproved = "Only pending payments can be approved";
    String onlyPendingPaymentsCanBeDeclined = "Only pending payments can be declined";

    private Long id;
    private String idempotencyKey;
    private String transactionId;
    private String payerId;
    private String payeeId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private PaymentStatus status;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Payment() {}

    public static Payment create(String idempotencyKey, String payerId, String payeeId,
                                 BigDecimal amount, String currency, String description) {
        Payment payment = new Payment();
        payment.idempotencyKey = idempotencyKey;
        payment.payerId = payerId;
        payment.payeeId = payeeId;
        payment.amount = amount;
        payment.currency = currency;
        payment.description = description;
        payment.status = PaymentStatus.PENDING;
        payment.createdAt = LocalDateTime.now();
        payment.updatedAt = LocalDateTime.now();
        return payment;
    }

    public void approve(String transactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException(onlyPendingPaymentsCanBeApproved);
        }
        this.transactionId = transactionId;
        this.status = PaymentStatus.APPROVED;
        this.message = paymentProcessedSuccessfully;
        this.updatedAt = LocalDateTime.now();
    }

    public void decline(String reason) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException(onlyPendingPaymentsCanBeDeclined);
        }
        this.status = PaymentStatus.DECLINED;
        this.message = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getTransactionId() { return transactionId; }
    public String getPayerId() { return payerId; }
    public String getPayeeId() { return payeeId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
    public PaymentStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    void setId(Long id) { this.id = id; }
}
