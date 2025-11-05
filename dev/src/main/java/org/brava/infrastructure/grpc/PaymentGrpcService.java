package org.brava.infrastructure.grpc;

import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.brava.application.dto.PaymentResult;
import org.brava.application.dto.ProcessPaymentCommand;
import org.brava.application.usecase.ProcessPaymentUseCase;
import org.jboss.logging.Logger;

import java.math.BigDecimal;

@GrpcService
public class PaymentGrpcService extends MutinyPaymentServiceGrpc.PaymentServiceImplBase {

    private static final Logger LOG = Logger.getLogger(PaymentGrpcService.class);

    @Inject
    ProcessPaymentUseCase processPaymentUseCase;

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
            PaymentResult result = processPaymentUseCase.execute(command);

            return PaymentResponse.newBuilder()
                    .setTransactionId(result.transactionId())
                    .setStatus(result.status().name())
                    .setMessage(result.message())
                    .build();
        });
    }
}
