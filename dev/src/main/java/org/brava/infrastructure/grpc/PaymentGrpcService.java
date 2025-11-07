package org.brava.infrastructure.grpc;

import io.quarkus.grpc.GrpcService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.brava.core.Payment;
import org.brava.shell.ProcessPaymentCommand;
import org.brava.shell.ProcessPaymentHandler;
import org.jboss.logging.Logger;

import java.math.BigDecimal;

@GrpcService
public class PaymentGrpcService extends MutinyPaymentServiceGrpc.PaymentServiceImplBase {

    private static final Logger LOG = Logger.getLogger(PaymentGrpcService.class);

    @Inject
    ProcessPaymentHandler handler;

    @Blocking
    @Override
    public Uni<PaymentResponse> processPayment(PaymentRequest request) {

        LOG.infof("Received gRPC request - IdempotencyKey: %s", request.getIdempotencyKey());

        ProcessPaymentCommand command = new ProcessPaymentCommand(
                request.getIdempotencyKey(),
                request.getPayerId(),
                request.getPayeeId(),
                BigDecimal.valueOf(request.getAmount()),
                request.getCurrency(),
                request.getDescription()
        );

        return Uni.createFrom().item(() -> {
            Payment result = handler.handle(command);

            return PaymentResponse.newBuilder()
                    .setTransactionId(result.transactionId() != null ? result.transactionId() : "")
                    .setStatus(result.status().name())
                    .setMessage(result.message() != null ? result.message() : "")
                    .build();
        });
    }
}
