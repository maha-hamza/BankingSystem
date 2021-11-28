package com.dkb.bankingsystem.repositories

import com.dkb.bankingsystem.model.Account
import com.dkb.bankingsystem.model.PendingTransaction
import org.springframework.data.jpa.repository.JpaRepository

interface PendingTransactionsRepository : JpaRepository<PendingTransaction, Long> {

}