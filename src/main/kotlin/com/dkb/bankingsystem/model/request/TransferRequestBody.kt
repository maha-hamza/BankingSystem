package com.dkb.bankingsystem.model.request

import java.math.BigDecimal

class TransferRequestBody(
    val senderIban: String,
    val receiverIban: String,
    val amount: BigDecimal
)