package org.brava.usecase;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.brava.application.dto.PaymentResult;
import org.brava.application.dto.ProcessPaymentCommand;
import org.brava.application.usecase.ProcessPaymentUseCase;
import org.brava.domain.enums.PaymentStatus;
import org.brava.domain.models.Payment;
import org.brava.domain.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("ProcessPaymentUseCase Tests")
public class ProcessPaymentUseCaseTest {
    @Inject
    ProcessPaymentUseCase useCase;

    @InjectMock
    PaymentRepository paymentRepository;

    private ProcessPaymentCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = new ProcessPaymentCommand(
                "idempotency-key-123",
                "payer-456",
                "payee-789",
                BigDecimal.valueOf(1000.00),
                "USD",
                "Test payment"
        );
    }

    @Nested
    @DisplayName("When payment is new")
    class NewPaymentTests {

        @Test
        @DisplayName("Should approve payment when amount is below limit")
        void shouldApprovePaymentWhenAmountBelowLimit() {
            // Arrange
            when(paymentRepository.findByIdempotencyKey(validCommand.idempotencyKey()))
                    .thenReturn(Optional.empty());

            Payment savedPayment = createApprovedPayment();
            when(paymentRepository.save(any(Payment.class)))
                    .thenReturn(savedPayment);

            // Act
            PaymentResult result = useCase.execute(validCommand);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(result.transactionId()).startsWith("txn-");
            assertThat(result.message()).isEqualTo("Payment processed successfully");

            verify(paymentRepository).findByIdempotencyKey(validCommand.idempotencyKey());
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should decline payment when amount exceeds limit")
        void shouldDeclinePaymentWhenAmountExceedsLimit() {
            // Arrange
            ProcessPaymentCommand highAmountCommand = new ProcessPaymentCommand(
                    "idempotency-key-999",
                    "payer-456",
                    "payee-789",
                    BigDecimal.valueOf(15000.00),
                    "USD",
                    "High value payment"
            );

            when(paymentRepository.findByIdempotencyKey(highAmountCommand.idempotencyKey()))
                    .thenReturn(Optional.empty());

            Payment declinedPayment = createDeclinedPayment();
            when(paymentRepository.save(any(Payment.class)))
                    .thenReturn(declinedPayment);

            // Act
            PaymentResult result = useCase.execute(highAmountCommand);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(PaymentStatus.DECLINED);
            assertThat(result.message()).contains("Insufficient funds or amount exceeds limit");

            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should save payment with correct data")
        void shouldSavePaymentWithCorrectData() {
            // Arrange
            when(paymentRepository.findByIdempotencyKey(validCommand.idempotencyKey()))
                    .thenReturn(Optional.empty());

            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            Payment savedPayment = createApprovedPayment();
            when(paymentRepository.save(paymentCaptor.capture()))
                    .thenReturn(savedPayment);

            // Act
            useCase.execute(validCommand);

            // Assert
            Payment capturedPayment = paymentCaptor.getValue();
            assertThat(capturedPayment.getIdempotencyKey()).isEqualTo(validCommand.idempotencyKey());
            assertThat(capturedPayment.getPayerId()).isEqualTo(validCommand.payerId());
            assertThat(capturedPayment.getPayeeId()).isEqualTo(validCommand.payeeId());
            assertThat(capturedPayment.getAmount()).isEqualByComparingTo(validCommand.amount());
            assertThat(capturedPayment.getCurrency()).isEqualTo(validCommand.currency());
            assertThat(capturedPayment.getDescription()).isEqualTo(validCommand.description());
        }

        @Test
        @DisplayName("Should generate unique transaction ID")
        void shouldGenerateUniqueTransactionId() {
            // Arrange
            when(paymentRepository.findByIdempotencyKey(validCommand.idempotencyKey()))
                    .thenReturn(Optional.empty());

            Payment savedPayment = createApprovedPayment();
            when(paymentRepository.save(any(Payment.class)))
                    .thenReturn(savedPayment);

            // Act
            PaymentResult result = useCase.execute(validCommand);

            // Assert
            assertThat(result.transactionId())
                    .isNotNull()
                    .startsWith("txn-")
                    .hasSizeGreaterThan(4);
        }
    }

    @Nested
    @DisplayName("When payment already exists (idempotency)")
    class IdempotencyTests {

        @Test
        @DisplayName("Should return existing payment when idempotency key matches")
        void shouldReturnExistingPaymentWhenIdempotencyKeyMatches() {
            // Arrange
            Payment existingPayment = createApprovedPayment();
            when(paymentRepository.findByIdempotencyKey(validCommand.idempotencyKey()))
                    .thenReturn(Optional.of(existingPayment));

            // Act
            PaymentResult result = useCase.execute(validCommand);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(result.transactionId()).isEqualTo(existingPayment.getTransactionId());
            assertThat(result.message()).isEqualTo(existingPayment.getMessage());

            verify(paymentRepository, never()).save(any(Payment.class));
            verify(paymentRepository, times(1)).findByIdempotencyKey(validCommand.idempotencyKey());
        }

        @Test
        @DisplayName("Should not create duplicate payment with same idempotency key")
        void shouldNotCreateDuplicatePaymentWithSameIdempotencyKey() {
            // Arrange
            Payment existingPayment = createApprovedPayment();
            when(paymentRepository.findByIdempotencyKey(validCommand.idempotencyKey()))
                    .thenReturn(Optional.of(existingPayment));

            // Act
            PaymentResult result1 = useCase.execute(validCommand);
            PaymentResult result2 = useCase.execute(validCommand);

            // Assert
            assertThat(result1.transactionId()).isEqualTo(result2.transactionId());
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should return declined payment if it was previously declined")
        void shouldReturnDeclinedPaymentIfPreviouslyDeclined() {
            // Arrange
            Payment existingDeclinedPayment = createDeclinedPayment();
            when(paymentRepository.findByIdempotencyKey(validCommand.idempotencyKey()))
                    .thenReturn(Optional.of(existingDeclinedPayment));

            // Act
            PaymentResult result = useCase.execute(validCommand);

            // Assert
            assertThat(result.status()).isEqualTo(PaymentStatus.DECLINED);
            verify(paymentRepository, never()).save(any(Payment.class));
        }
    }

    private Payment createApprovedPayment() {
        Payment payment = Payment.create(
                "idempotency-key-123",
                "payer-456",
                "payee-789",
                BigDecimal.valueOf(1000.00),
                "USD",
                "Test payment"
        );
        payment.approve("txn-" + System.currentTimeMillis());
        return payment;
    }

    private Payment createDeclinedPayment() {
        Payment payment = Payment.create(
                "idempotency-key-123",
                "payer-456",
                "payee-789",
                BigDecimal.valueOf(15000.00),
                "USD",
                "Test payment"
        );
        payment.decline("Insufficient funds or amount exceeds limit");
        return payment;
    }

}
