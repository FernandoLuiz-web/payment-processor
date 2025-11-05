package org.brava.infrastructure.persistence.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.brava.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class PaymentEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank
    @Column(name = "idempotency_key", nullable = false, unique = true)
    public String idempotencyKey;

    @NotBlank
    @Column(name = "transaction_id", nullable = false, unique = true)
    public String transactionId;

    @NotBlank
    @Column(name = "payer_id", nullable = false)
    public String payerId;

    @NotBlank
    @Column(name = "payee_id", nullable = false)
    public String payeeId;

    @Positive
    @Column(nullable = false, precision = 19, scale = 2)
    public BigDecimal amount;

    @NotBlank
    @Column(nullable = false, length = 3)
    public String currency;

    @Column(length = 500)
    public String description;

    @NotBlank
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public PaymentStatus status;

    @Column(length = 500)
    public String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

}
