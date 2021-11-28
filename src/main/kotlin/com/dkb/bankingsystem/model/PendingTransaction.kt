package com.dkb.bankingsystem.model

import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "pending_transactions", schema = "banking")
class PendingTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val iban: String,
    val amount: BigDecimal
)