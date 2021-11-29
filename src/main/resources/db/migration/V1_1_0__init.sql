create schema if not exists banking;

CREATE TABLE IF NOT EXISTS banking.accounts (
    id             int           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_id    VARCHAR(12)           NOT NULL,
    account_number VARCHAR(12)   NOT NULL,
    account_type   VARCHAR(20)   DEFAULT 'Checking Account',
    iban           varchar(50)   NOT NULL,
    date_opened    TIMESTAMP          NOT NULL,
    date_closed    TIMESTAMP,
    last_modified  TIMESTAMP,
    balance        DECIMAL(20,2) DEFAULT 0.00,
    transaction_pending        BOOLEAN       DEFAULT false,
    locked         BOOLEAN       DEFAULT false,
    UNIQUE KEY acct_number_UNIQUE (account_number)
);

CREATE TABLE IF NOT EXISTS banking.pending_deposits (
    id             int           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    iban           VARCHAR(50)   NOT NULL,
    amount        DECIMAL(20,2)  NOT NULL
);

CREATE TABLE IF NOT EXISTS banking.pending_transactions (
    id             int           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sender         VARCHAR(50)   NOT NULL,
    receiver       VARCHAR(50)   NOT NULL,
    amount         DECIMAL(20,2)  NOT NULL
);

CREATE TABLE IF NOT EXISTS banking.transfers_history
(
   id                VARCHAR(50)     PRIMARY KEY AUTO_INCREMENT NOT NULL,
   from_account      VARCHAR(50),
   to_account        VARCHAR(50)                 NOT NULL,
   initiated_at      TIMESTAMP                   Not NULL,
   finished_at       TIMESTAMP,
   amount            DECIMAL(20,2),
   status            VARCHAR(10),
   transaction_type  VARCHAR(255),
   transaction_code  VARCHAR(50),
   comment           VARCHAR(255)
);