package com.dkb.bankingsystem.service

import com.dkb.bankingsystem.exceptions.*
import com.dkb.bankingsystem.model.Account
import com.dkb.bankingsystem.model.PendingDeposit
import com.dkb.bankingsystem.model.TransferHistory
import com.dkb.bankingsystem.model.enum.AccountType
import com.dkb.bankingsystem.model.enum.TransactionType
import com.dkb.bankingsystem.model.enum.TransferStatus
import com.dkb.bankingsystem.repositories.AccountRepository
import com.dkb.bankingsystem.repositories.PendingDepositsRepository
import com.dkb.bankingsystem.repositories.TransactionHistoryRepository
import com.dkb.bankingsystem.service.dto.DepositDTO
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import javax.transaction.Transactional
import kotlin.random.Random

@Service
class AccountService(
    val repository: AccountRepository,
    val historyRepository: TransactionHistoryRepository,
    val pendingTransactionsRepository: PendingDepositsRepository
) {

    fun filterAccountsByAccountType(
        customerId: String,
        accountType: List<AccountType>
    ): List<Account> {
        return repository.findByCustomerIdAndAccountTypeIsIn(
            customerId = customerId,
            accountTypes = accountType.map { it.name }
        )
    }

    fun createAccount(
        customerId: String,
        accountType: AccountType
    ): Account {
        if (customerId.isBlank()) {
            throw CustomerCannotOpenAccountException("CustomerId Can't be Blank")
        }
        val checkedAccount =
            repository.findByCustomerIdAndAccountType(customerId = customerId, accountType = accountType.name)
        return when (checkedAccount == null) {
            true -> {
                val accountNumber = generateAccountNumber()
                val accountTypeIndex = accountType.ordinal
                val iban = String.format("BE XX %s %d 00", accountNumber, accountTypeIndex)

                val account = Account(
                    customerId = customerId,
                    accountNumber = accountNumber,
                    accountType = accountType.name,
                    iban = iban
                )
                repository.save(account)
            }
            false -> throw CustomerCannotOpenAccountException(
                String.format(
                    "Customer Already has account of type %s",
                    accountType.name
                )
            )
        }
    }

    private fun generateAccountNumber(): String {
        var isUniqueAccountNumber = false
        var accountNumber: String
        do {
            accountNumber = String.format("%d%d", Random.nextInt(999999999), Random.nextInt(999))
            val account = repository.findByAccountNumber(accountNumber)
            if (account == null)
                isUniqueAccountNumber = true
        } while (!isUniqueAccountNumber)
        return accountNumber
    }

    fun lockAccount(iban: String): Account {
        if (iban.isBlank()) {
            throw AccountLockException("IBAN is blank, Please make sure to enter valid IBAN")
        }
        val account = repository.findByIbanIgnoreCase(iban = iban)
        return when {
            account == null -> throw AccountNotFoundException("Account You are trying to lock doesn't exists")
            account.locked -> throw AccountLockException("Account You are trying to lock already locked, Do you mean unlock?")
            else -> {
                val updatedAccount = account.copy(
                    locked = true,
                    lastModified = LocalDate.now()
                )
                repository.save(updatedAccount)
            }
        }
    }

    fun unlockAccount(iban: String): Account {
        if (iban.isBlank()) {
            throw AccountLockException("IBAN is blank, Please make sure to enter valid IBAN")
        }

        val account = repository.findByIbanIgnoreCase(iban = iban)
        return when {
            account == null -> throw AccountNotFoundException("Account You are trying to unlock doesn't exists")
            !account.locked -> throw AccountLockException("Account You are trying to unlock is not locked, Do you mean lock?")
            else -> {
                val updatedAccount = account.copy(
                    locked = false,
                    lastModified = LocalDate.now()
                )
                repository.save(updatedAccount)
            }
        }
    }

    fun getAccountBalanceByIbanOrAccountNumber(accountNumber: String? = null, iban: String? = null): BigDecimal {
        return when {
            accountNumber != null -> repository.findByAccountNumber(accountNumber)?.balance
                ?: throw AccountNotFoundException(String.format("No account with #%s exists", accountNumber))
            iban != null -> repository.findByIbanIgnoreCase(iban)?.balance
                ?: throw AccountNotFoundException(String.format("No IBAN with #%s exists", iban))
            else -> throw MissingParametersForAccountBalanceRetrievalException("Account Number or Iban must be present to perform the query")
        }
    }


    @Transactional
    fun deposit(iban: String, amount: BigDecimal): DepositDTO {
        val initiatedAt = LocalDate.now()
        if (amount <= BigDecimal.ZERO)
            throw EmptyDepositException(String.format("Can't Perform Empty or negative Deposit, Please make sure that correct amount is selected"))

        val account = repository.findByIbanIgnoreCase(iban)

        when {
            account == null -> throw AccountNotFoundException("Account You are trying to deposit into doesn't exists")
            account.locked -> throw DepositToLockedAccountException("Can't Deposit To Locked Account")
            else -> {

                if (!account.transactionPending) {
                    val blockingCopy = account.copy(
                        transactionPending = true,
                        lastModified = LocalDate.now()
                    )
                    val latestAccountData = repository.save(blockingCopy)
                    val updatedAccount = latestAccountData.copy(
                        balance = latestAccountData.balance.plus(amount),
                        lastModified = LocalDate.now(),
                        transactionPending = false
                    )
                    val transferHistory = TransferHistory(
                        toAccount = iban,
                        initiatedAt = initiatedAt,
                        finishedAt = updatedAccount.lastModified!!,
                        amount = amount,
                        status = TransferStatus.ACCEPTED.name,
                        transactionType = TransactionType.DEPOSIT.name,
                        transactionCode = generateTransactionCode()
                    )
                    val currentUpdatedAccount = repository.save(updatedAccount);
                    val history = historyRepository.save(transferHistory)
                    return DepositDTO(
                        message = String.format(
                            "Amount Is Successfully Deposited, Current Amount [%f]",
                            currentUpdatedAccount.balance
                        ),
                        transferReport = history
                    )
                } else {
                    pendingTransactionsRepository.save(
                        PendingDeposit(
                            iban = iban,
                            amount = amount
                        )
                    )
                    return DepositDTO(
                        message = "Current Deposit Process Is Pending Due to Processing on the account"
                    )
                }
            }
        }
    }
}