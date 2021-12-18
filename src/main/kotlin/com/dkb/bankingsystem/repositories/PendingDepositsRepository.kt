package com.dkb.bankingsystem.repositories

import com.dkb.bankingsystem.model.PendingDeposit
import org.springframework.data.jpa.repository.JpaRepository
import java.math.BigDecimal

interface PendingDepositsRepository : JpaRepository<PendingDeposit, Long> {


    fun findByIbanAndAmount(iban: String, amount: BigDecimal): List<PendingDeposit>

}