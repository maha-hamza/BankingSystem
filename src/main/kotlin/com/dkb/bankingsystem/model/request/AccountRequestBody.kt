package com.dkb.bankingsystem.model.request

import com.dkb.bankingsystem.model.enum.AccountType
import java.math.BigDecimal

class IbanRequestRequestBody(val iban: String)

class AccountCreationRequestBody(
    val customerId: String,
    val accountType: AccountType
)

class AccountRequestByAccountTypeRequestBody(
    val customerId: String,
    val accountsType: List<AccountType>
)

class AccountBalanceRequestBody(
    val accountNumber: String? = null,
    val iban: String? = null
)

class AccountDepositRequestBody(
    val iban: String,
    val amount: BigDecimal
)

