package org.brava.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.brava.application.dto.PaymentResult;
import org.brava.application.dto.ProcessPaymentCommand;
import org.brava.domain.models.Payment;
import org.brava.domain.repositories.PaymentRepository;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.Optional;

@ApplicationScoped
public class ProcessPaymentUseCase {

    private static final BigDecimal MAX_APPROVED_AMOUNT = BigDecimal.valueOf(10000);
    private static final Logger LOG = Logger.getLogger(ProcessPaymentUseCase.class);

    @Inject
    PaymentRepository paymentRepository;

    @Transactional
    public PaymentResult execute(ProcessPaymentCommand command) {
        LOG.infof("Executing payment processing for idempotencyKey: %s", command.idempotencyKey());

        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(command.idempotencyKey());
        if (existing.isPresent()) {
            LOG.infof("Payment already processed: %s", existing.get().getTransactionId());
            return PaymentResult.fromPayment(existing.get());
        }

        Payment payment = Payment.create(
                command.idempotencyKey(),
                command.payerId(),
                command.payeeId(),
                command.amount(),
                command.currency(),
                command.description()
        );

        if (shouldApprovePayment(payment)) {
           String transactionId = generateTransactionId();
           payment.approve(transactionId);
        }else {
            payment.decline("Insufficient funds or amount exceeds limit");
        }

        Payment saved = paymentRepository.save(payment);

        LOG.infof("Payment processed successfully: %s - %s",
                saved.getTransactionId(),
                saved.getStatus());

        return PaymentResult.fromPayment(saved);
    }

    private boolean shouldApprovePayment(Payment payment) {
        return payment.getAmount().compareTo(MAX_APPROVED_AMOUNT) < 0;
    }

    private String generateTransactionId() {
        return "txn-" + System.currentTimeMillis();
    }
}
