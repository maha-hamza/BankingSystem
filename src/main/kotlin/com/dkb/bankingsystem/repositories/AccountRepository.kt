package com.dkb.bankingsystem.repositories

import com.dkb.bankingsystem.model.Account
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<Account, Long> {

    fun findByIbanIgnoreCase(iban: String): Account?

    fun findByCustomerIdAndAccountType(customerId: String, accountType: String): Account?

    fun findByAccountNumber(accountNumber: String): Account?
    
    fun findByCustomerIdAndAccountTypeIsIn(customerId: String, accountTypes: List<String>): List<Account>

}