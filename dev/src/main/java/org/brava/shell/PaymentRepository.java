package org.brava.shell;

import org.brava.core.Payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    List<Payment> findByPayerIdAndCreatedAtAfter(String payerId, LocalDateTime after);
    List<Payment> findByPayerIdAndPayeeIdAndCreatedAtAfter(String payerId, String payeeId, LocalDateTime after);
}
