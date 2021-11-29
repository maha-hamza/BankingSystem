package com.dkb.bankingsystem.model

import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "pending_deposits", schema = "banking")
class PendingDeposit(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val iban: String,
    val amount: BigDecimal
)