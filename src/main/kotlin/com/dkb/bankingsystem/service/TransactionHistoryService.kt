package com.dkb.bankingsystem.service

import com.dkb.bankingsystem.model.TransferHistory
import com.dkb.bankingsystem.repositories.TransactionHistoryRepository
import org.springframework.stereotype.Service

@Service
class TransactionHistoryService(val repository: TransactionHistoryRepository) {

    fun getAllTransactionsHistoryForAccount(iban: String): List<TransferHistory> {
        return repository.findByFromAccount(iban)
    }
}