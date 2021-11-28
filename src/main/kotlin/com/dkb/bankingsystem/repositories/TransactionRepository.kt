package com.dkb.bankingsystem.repositories

import com.dkb.bankingsystem.model.Transfer
import org.springframework.data.jpa.repository.JpaRepository

interface TransactionRepository : JpaRepository<Transfer, Long> {
}