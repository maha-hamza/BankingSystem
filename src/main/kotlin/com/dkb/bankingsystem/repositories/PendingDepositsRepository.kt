package com.dkb.bankingsystem.repositories

import com.dkb.bankingsystem.model.PendingDeposit
import org.springframework.data.jpa.repository.JpaRepository

interface PendingDepositsRepository : JpaRepository<PendingDeposit, Long> {

}