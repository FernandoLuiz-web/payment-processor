package org.brava.shell;

import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.brava.core.Payment;
import org.brava.core.PaymentDecision;
import org.brava.core.RiskContext;
import org.brava.core.policies.PaymentPolicy;
import org.jboss.logging.Logger;

import java.util.Optional;

@ApplicationScoped
public class ProcessPaymentHandler {

    private static final Logger LOG = Logger.getLogger(ProcessPaymentHandler.class);

    @Inject
    PaymentRepository repository;

    @Inject
    RiskPolicyEngine riskEngine;

    @Inject
    PaymentPolicy composedPolicy;

    @Transactional
    public Payment handle(ProcessPaymentCommand command) {
        LOG.infof("Processing payment - IdempotencyKey: %s, Amount: %.2f %s",
                command.idempotencyKey(),
                command.amount(),
                command.currency());

        Optional<Payment> existing = repository.findByIdempotencyKey(command.idempotencyKey());
        if (existing.isPresent()) {
            LOG.infof("Payment already processed: %s", existing.get().transactionId());
            return existing.get();
        }

        Payment payment = Payment.createPending(
                command.idempotencyKey(),
                command.payerId(),
                command.payeeId(),
                command.amount(),
                command.currency(),
                command.description()
        );

        RiskContext context = riskEngine.buildContext(payment);

        PaymentDecision decision = composedPolicy.evaluate(context);

        LOG.infof("Policy decision: %s", decision);

        Payment finalPayment = switch (decision) {
            case PaymentDecision.Approved(String txnId, String reason) -> {
                LOG.infof("Payment approved - %s", reason);
                yield payment.approve(txnId);
            }
            case PaymentDecision.Declined(String reason, int score) -> {
                LOG.warnf("Payment declined (risk: %d) - %s", score, reason);
                yield payment.decline(reason);
            }
        };

        Payment saved = repository.save(finalPayment);

        LOG.infof("Payment persisted - ID: %d, Status: %s", saved.id(), saved.status());

        return saved;
    }

}
