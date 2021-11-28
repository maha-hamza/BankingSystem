package com.dkb.bankingsystem.exceptions

class AccountLockException(msg: String?) : RuntimeException(msg)

class AccountNotFoundException(msg: String?) : RuntimeException(msg)

class CustomerCannotOpenAccountException(msg: String?) : RuntimeException(msg)

class MissingParametersForAccountBalanceRetrievalException(msg: String?) : RuntimeException(msg)

class EmptyDepositException(msg: String?) : RuntimeException(msg)

class DepositToLockedAccountException(msg: String?) : RuntimeException(msg)
