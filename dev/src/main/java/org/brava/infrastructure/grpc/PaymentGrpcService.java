package org.brava.infrastructure.grpc;

import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;

@GrpcService
public class PaymentGrpcService extends org.brava.infrastructure.grpc.PaymentServiceGrpc.PaymentServiceImplBase {

//    @Override
//    public Uni<org.brava.infrastructure.grpc.PaymentResponse> processPayment(org.brava.infrastructure.grpc.PaymentRequest request) {
//        org.brava.infrastructure.grpc.PaymentResponse response = org.brava.infrastructure.grpc.PaymentResponse.newBuilder()
//                .setTransactionId("txn-" + System.currentTimeMillis())
//                .setStatus("APPROVED")
//                .setMessage("Payment processed successfully")
//                .build();
//
//        return Uni.createFrom().item(response);
//    }
}
