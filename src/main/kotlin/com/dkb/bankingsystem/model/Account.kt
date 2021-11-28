package com.dkb.bankingsystem.model

import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "accounts", schema = "banking")
data class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @Column(name = "customer_id")
    val customerId: String,
    @Column(name = "account_number")
    val accountNumber: String,
    @Column(name = "account_type")
    val accountType: String,
    val iban: String,
    @Column(name = "date_opened")
    val dateOpened: LocalDate = LocalDate.now(),
    @Column(name = "date_closed")
    val dateClosed: LocalDate? = null,
    @Column(name = "last_modified")
    var lastModified: LocalDate? = null,
    val balance: BigDecimal = BigDecimal.ZERO,
    @Column(name = "transaction_pending")
    val transactionPending: Boolean = false,
    var locked: Boolean = false
)