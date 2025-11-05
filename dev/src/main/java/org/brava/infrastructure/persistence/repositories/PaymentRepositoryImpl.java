package org.brava.infrastructure.persistence.repositories;

import jakarta.enterprise.context.ApplicationScoped;
import org.brava.domain.models.Payment;
import org.brava.domain.repositories.PaymentRepository;
import org.brava.infrastructure.persistence.entities.PaymentEntity;
import org.brava.infrastructure.persistence.mappers.PaymentEntityMapper;

import java.util.Optional;

@ApplicationScoped
public class PaymentRepositoryImpl implements PaymentRepository {
    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = PaymentEntityMapper.toEntity(payment);
        entity.persist();
        return PaymentEntityMapper.toDomain(entity);
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return PaymentEntity.find("idempotencyKey", idempotencyKey)
                .firstResultOptional()
                .map(entity -> PaymentEntityMapper.toDomain((PaymentEntity) entity));
    }

    @Override
    public Optional<Payment> findByTransactionId(String transactionId) {
        return PaymentEntity.find("transactionId", transactionId)
                .firstResultOptional()
                .map(entity -> PaymentEntityMapper.toDomain((PaymentEntity) entity));
    }
}
