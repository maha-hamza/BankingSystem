package com.dkb.bankingsystem.cron

import com.dkb.bankingsystem.model.TransferHistory
import com.dkb.bankingsystem.model.enum.TransactionType
import com.dkb.bankingsystem.model.enum.TransferStatus
import com.dkb.bankingsystem.repositories.AccountRepository
import com.dkb.bankingsystem.repositories.PendingTransactionsRepository
import com.dkb.bankingsystem.repositories.TransactionHistoryRepository
import com.dkb.bankingsystem.service.generateTransactionCode
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate


@Component
class PendingDepositsCronJob(
    val accountRepository: AccountRepository,
    val historyRepository: TransactionHistoryRepository,
    val pendingTransactionsRepository: PendingTransactionsRepository
) {

    @Scheduled(fixedRate = 5000)
    fun replayPendingTransactions() {
        val pendingTransaction = pendingTransactionsRepository.findAll()
        pendingTransaction.forEach { transaction ->
            run {

                val account = accountRepository.findByIbanIgnoreCase(transaction.iban)
                if (!account!!.transactionPending) {
                    val blockingCopy = account.copy(
                        transactionPending = true,
                        lastModified = LocalDate.now()
                    )
                    val latestAccountData = accountRepository.save(blockingCopy)
                    val updatedAccount = latestAccountData.copy(
                        balance = latestAccountData.balance.plus(transaction.amount),
                        lastModified = LocalDate.now(),
                        transactionPending = false
                    )
                    val transferHistory = TransferHistory(
                        toAccount = transaction.iban,
                        initiatedAt = LocalDate.now(),
                        finishedAt = updatedAccount.lastModified!!,
                        amount = transaction.amount,
                        status = TransferStatus.ACCEPTED.name,
                        transactionType = TransactionType.DEPOSIT.name,
                        transactionCode = generateTransactionCode()
                    )
                    accountRepository.save(updatedAccount);
                    historyRepository.save(transferHistory)
                    pendingTransactionsRepository.delete(transaction)
                }
            }
        }
    }
}