package org.brava.shell;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.brava.core.Payment;
import org.brava.core.PaymentStatus;
import org.brava.core.RiskContext;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class RiskPolicyEngine {

    private static final Logger LOG = Logger.getLogger(RiskPolicyEngine.class);

    @Inject
    PaymentRepository repository;

    public RiskContext buildContext(Payment payment) {
        LOG.debugf("Building risk context for payer: %s", payment.payerId());

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<Payment> recentPayments = repository.findByPayerIdAndCreatedAtAfter(
                payment.payerId(),
                thirtyDaysAgo
        );

        int successfulCount = (int) recentPayments.stream()
                .filter(p -> p.status() == PaymentStatus.APPROVED)
                .count();

        BigDecimal totalAmount = recentPayments.stream()
                .filter(p -> p.status() == PaymentStatus.APPROVED)
                .map(Payment::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Payment> paymentsToSamePayee = repository.findByPayerIdAndPayeeIdAndCreatedAtAfter(
                payment.payerId(),
                payment.payeeId(),
                sevenDaysAgo
        );

        LocalDateTime lastToSamePayee = paymentsToSamePayee.stream()
                .map(Payment::createdAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return new RiskContext(
                payment.payerId(),
                payment.payeeId(),
                payment.amount(),
                payment.currency(),
                successfulCount,
                totalAmount,
                paymentsToSamePayee.size(),
                lastToSamePayee,
                recentPayments.isEmpty()
        );
    }
}
