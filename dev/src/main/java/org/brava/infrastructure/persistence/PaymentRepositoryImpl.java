package org.brava.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import org.brava.core.Payment;
import org.brava.shell.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PaymentRepositoryImpl implements PaymentRepository {

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = toEntity(payment);
        entity.persist();
        return toDomain(entity);
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return PaymentEntity.find("idempotencyKey", idempotencyKey)
                .firstResultOptional()
                .map(e -> toDomain((PaymentEntity) e));
    }

    @Override
    public List<Payment> findByPayerIdAndCreatedAtAfter(String payerId, LocalDateTime after) {
        return PaymentEntity.find("payerId = ?1 and createdAt > ?2", payerId, after)
                .stream()
                .map(e -> toDomain((PaymentEntity) e))
                .toList();
    }

    @Override
    public List<Payment> findByPayerIdAndPayeeIdAndCreatedAtAfter(String payerId, String payeeId, LocalDateTime after) {
        return PaymentEntity.find("payerId = ?1 and payeeId = ?2 and createdAt > ?3", payerId, payeeId, after)
                .stream()
                .map(e -> toDomain((PaymentEntity) e))
                .toList();
    }

    private PaymentEntity toEntity(Payment domain) {
        PaymentEntity entity = new PaymentEntity();
        entity.id = domain.id();
        entity.idempotencyKey = domain.idempotencyKey();
        entity.transactionId = domain.transactionId();
        entity.payerId = domain.payerId();
        entity.payeeId = domain.payeeId();
        entity.amount = domain.amount();
        entity.currency = domain.currency();
        entity.description = domain.description();
        entity.status = domain.status();
        entity.message = domain.message();
        entity.createdAt = domain.createdAt();
        entity.updatedAt = domain.updatedAt();
        return entity;
    }

    private Payment toDomain(PaymentEntity entity) {
        return new Payment(
                entity.id,
                entity.idempotencyKey,
                entity.payerId,
                entity.payeeId,
                entity.amount,
                entity.currency,
                entity.description,
                entity.status,
                entity.transactionId,
                entity.message,
                entity.createdAt,
                entity.updatedAt
        );
    }
}
