package com.dkb.bankingsystem.cron

import com.dkb.bankingsystem.model.TransferHistory
import com.dkb.bankingsystem.model.enum.TransactionType
import com.dkb.bankingsystem.model.enum.TransferStatus
import com.dkb.bankingsystem.repositories.AccountRepository
import com.dkb.bankingsystem.repositories.PendingDepositsRepository
import com.dkb.bankingsystem.repositories.TransactionHistoryRepository
import com.dkb.bankingsystem.service.generateTransactionCode
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate


@Component
class PendingDepositsCronJob(
    val accountRepository: AccountRepository,
    val historyRepository: TransactionHistoryRepository,
    val pendingDepositsRepository: PendingDepositsRepository
) {

    @Scheduled(fixedRate = 5000)
    fun replayPendingDeposits() {
        val pendingDeposits = pendingDepositsRepository.findAll()
        pendingDeposits.forEach { deposit ->
            run {

                val account = accountRepository.findByIbanIgnoreCase(deposit.iban)
                if (!account!!.transactionPending) {
                    val blockingCopy = account.copy(
                        transactionPending = true,
                        lastModified = LocalDate.now()
                    )
                    val latestAccountData = accountRepository.save(blockingCopy)
                    val updatedAccount = latestAccountData.copy(
                        balance = latestAccountData.balance.plus(deposit.amount),
                        lastModified = LocalDate.now(),
                        transactionPending = false
                    )
                    val transferHistory = TransferHistory(
                        toAccount = deposit.iban,
                        initiatedAt = LocalDate.now(),
                        finishedAt = updatedAccount.lastModified!!,
                        amount = deposit.amount,
                        status = TransferStatus.ACCEPTED.name,
                        transactionType = TransactionType.DEPOSIT.name,
                        transactionCode = generateTransactionCode()
                    )
                    accountRepository.save(updatedAccount);
                    historyRepository.save(transferHistory)
                    pendingDepositsRepository.delete(deposit)
                }
            }
        }
    }
}