CREATE TABLE payments (
      id BIGSERIAL PRIMARY KEY,
      idempotency_key VARCHAR(255) NOT NULL UNIQUE,
      transaction_id VARCHAR(100) NOT NULL UNIQUE,
      payer_id VARCHAR(100) NOT NULL,
      payee_id VARCHAR(100) NOT NULL,
      amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
      currency VARCHAR(3) NOT NULL,
      description VARCHAR(500),
      status VARCHAR(20) NOT NULL,
      message VARCHAR(500),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_idempotency_key ON payments(idempotency_key);
CREATE INDEX idx_transaction_id ON payments(transaction_id);
CREATE INDEX idx_payer_id ON payments(payer_id);
CREATE INDEX idx_status ON payments(status);
CREATE INDEX idx_created_at ON payments(created_at);

COMMENT ON TABLE payments IS 'Tabela de pagamentos processados';
COMMENT ON COLUMN payments.idempotency_key IS 'Chave de idempotência para evitar duplicação';
COMMENT ON COLUMN payments.transaction_id IS 'ID único da transação gerado pelo sistema';
COMMENT ON COLUMN payments.status IS 'Status do pagamento: PENDING, APPROVED, DECLINED, FAILED, CANCELLED';