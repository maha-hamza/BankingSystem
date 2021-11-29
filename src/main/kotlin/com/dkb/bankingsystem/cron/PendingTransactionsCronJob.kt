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
class PendingTransactionsCronJob(
    val accountRepository: AccountRepository,
    val historyRepository: TransactionHistoryRepository,
    val pendingTransactionsRepository: PendingTransactionsRepository
) {

    @Scheduled(fixedRate = 6000)
    fun replayPendingTransactions() {
        val pendingTransactions = pendingTransactionsRepository.findAll()
        pendingTransactions.forEach { transaction ->
            run {

                val senderAccount = accountRepository.findByIbanIgnoreCase(transaction.sender)
                val receiverAccount = accountRepository.findByIbanIgnoreCase(transaction.receiver)

                if (!senderAccount?.transactionPending!! && !receiverAccount?.transactionPending!!) {
                    val initialTransaction = historyRepository.save(
                        TransferHistory(
                            fromAccount = transaction.sender,
                            toAccount = transaction.receiver,
                            initiatedAt = LocalDate.now(),
                            amount = transaction.amount,
                            status = TransferStatus.INITIATED.name,
                            transactionType = TransactionType.TRANSFER.name,
                            transactionCode = generateTransactionCode()
                        )
                    )

                    val latestSenderAccount = accountRepository.save(
                        senderAccount.copy(
                            transactionPending = true,
                            lastModified = LocalDate.now()
                        )
                    )
                    val latestReceiverAccount = accountRepository.save(
                        receiverAccount.copy(
                            transactionPending = true,
                            lastModified = LocalDate.now()
                        )
                    )

                    val senderCopyForTransfer = latestSenderAccount.copy(
                        balance = latestSenderAccount.balance.minus(transaction.amount),
                        lastModified = LocalDate.now(),
                        transactionPending = false
                    )
                    val receiverCopyForTransfer = latestReceiverAccount.copy(
                        balance = latestReceiverAccount.balance.plus(transaction.amount),
                        lastModified = LocalDate.now(),
                        transactionPending = false
                    )
                    accountRepository.save(senderCopyForTransfer)
                    accountRepository.save(receiverCopyForTransfer)

                    historyRepository.save(
                        initialTransaction.copy(
                            finishedAt = LocalDate.now(),
                            status = TransferStatus.ACCEPTED.name
                        )
                    )

                    pendingTransactionsRepository.delete(transaction)

                }
            }
        }
    }
}
