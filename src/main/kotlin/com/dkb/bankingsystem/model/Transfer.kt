package com.dkb.bankingsystem.model

import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "transfers", schema = "banking")
data class Transfer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    @Column(name = "from_account")
    val fromAccount: String,
    @Column(name = "to_account")
    val toAccount: String,
    @Column(name = "initiated_at")
    val initiatedAt: LocalDate,
    val amount: BigDecimal,
    val status: String,
    @Column(name = "transaction_type")
    val transactionType: String,
    val comment: String
)