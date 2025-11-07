package org.brava.infrastructure.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.brava.core.policies.AmountLimitPolicy;
import org.brava.core.policies.FrequencyPolicy;
import org.brava.core.policies.HistoryBasedPolicy;
import org.brava.core.policies.PaymentPolicy;

public class PolicyConfiguration {

    @Produces
    @ApplicationScoped
    public PaymentPolicy composedPolicy() {
        return new AmountLimitPolicy()
                .and(new HistoryBasedPolicy())
                .and(new FrequencyPolicy());
    }
}
