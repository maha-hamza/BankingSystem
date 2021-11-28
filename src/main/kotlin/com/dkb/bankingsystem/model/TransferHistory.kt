package com.dkb.bankingsystem.model

import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "transfers_history", schema = "banking")
data class TransferHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @Column(name = "from_account")
    val fromAccount: String? = null,
    @Column(name = "to_account")
    val toAccount: String,
    @Column(name = "initiated_at")
    val initiatedAt: LocalDate,
    @Column(name = "finished_at")
    val finishedAt: LocalDate,
    val amount: BigDecimal,
    val status: String,
    @Column(name = "transaction_type")
    val transactionType: String,
    val comment: String? = null
)