package com.dkb.bankingsystem.repositories

import com.dkb.bankingsystem.model.TransferHistory
import org.springframework.data.jpa.repository.JpaRepository

interface TransactionHistoryRepository : JpaRepository<TransferHistory, Int> {


    fun findByFromAccount(fromAccount: String): List<TransferHistory>

    fun findByToAccount(toAccount: String): List<TransferHistory>

}