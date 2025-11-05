package org.brava.infrastructure.persistence.mappers;

import org.brava.domain.models.Payment;
import org.brava.infrastructure.persistence.entities.PaymentEntity;

public class PaymentEntityMapper {

    public static PaymentEntity toEntity(Payment domain) {
        PaymentEntity entity = new PaymentEntity();
        entity.id = domain.getId();
        entity.idempotencyKey = domain.getIdempotencyKey();
        entity.transactionId = domain.getTransactionId();
        entity.payerId = domain.getPayerId();
        entity.payeeId = domain.getPayeeId();
        entity.amount = domain.getAmount();
        entity.currency = domain.getCurrency();
        entity.description = domain.getDescription();
        entity.status = domain.getStatus();
        entity.message = domain.getMessage();
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        return entity;
    }

    public static Payment toDomain(PaymentEntity entity) {
        Payment payment = Payment.create(
                entity.idempotencyKey,
                entity.payerId,
                entity.payeeId,
                entity.amount,
                entity.currency,
                entity.description
        );

//        payment.setId(entity.id);

        return payment;
    }
}
