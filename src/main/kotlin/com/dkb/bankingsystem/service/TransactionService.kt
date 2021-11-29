package com.dkb.bankingsystem.service

import com.dkb.bankingsystem.exceptions.*
import com.dkb.bankingsystem.model.Account
import com.dkb.bankingsystem.model.PendingTransaction
import com.dkb.bankingsystem.model.TransferHistory
import com.dkb.bankingsystem.model.enum.AccountType
import com.dkb.bankingsystem.model.enum.TransactionType
import com.dkb.bankingsystem.model.enum.TransferStatus
import com.dkb.bankingsystem.repositories.AccountRepository
import com.dkb.bankingsystem.repositories.PendingTransactionsRepository
import com.dkb.bankingsystem.repositories.TransactionHistoryRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Service
class TransactionService(
    val accountRepository: AccountRepository,
    val transactionHistoryRepository: TransactionHistoryRepository,
    val pendingTransactionsRepository: PendingTransactionsRepository
) {

    fun makeTransfer(
        sender: String,
        receiver: String,
        amount: BigDecimal
    ): TransferHistory? {

        val initialTransaction = transactionHistoryRepository.save(
            TransferHistory(
                fromAccount = sender,
                toAccount = receiver,
                initiatedAt = LocalDate.now(),
                amount = amount,
                status = TransferStatus.INITIATED.name,
                transactionType = TransactionType.TRANSFER.name,
                transactionCode = generateTransactionCode()
            )
        )

        val senderAccount = accountRepository.findByIbanIgnoreCase(sender)
        val receiverAccount = accountRepository.findByIbanIgnoreCase(receiver)

        try {
            validateSenderAndReceiverAccounts(
                sender = senderAccount,
                receiver = receiverAccount,
                amount = amount
            )
        } catch (e: Exception) {
            prepareFailingTransferHistoryLog(initialTransaction, e)
        }

        if (senderAccount?.transactionPending!! || receiverAccount?.transactionPending!!) {
            pendingTransactionsRepository.save(
                PendingTransaction(
                    sender = sender,
                    receiver = receiver,
                    amount = amount
                )
            )
            return null
        }
        val transaction = transactionHistoryRepository.findById(initialTransaction.id)

        if (transaction.get().status == TransferStatus.INITIATED.name) {
            // block any future transaction for now
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

            // prepare transaction data
            val senderCopyForTransfer = latestSenderAccount.copy(
                balance = latestSenderAccount.balance.minus(amount),
                lastModified = LocalDate.now(),
                transactionPending = false
            )
            val receiverCopyForTransfer = latestReceiverAccount.copy(
                balance = latestReceiverAccount.balance.plus(amount),
                lastModified = LocalDate.now(),
                transactionPending = false
            )
            accountRepository.save(senderCopyForTransfer)
            accountRepository.save(receiverCopyForTransfer)

            prepareSuccessfulTransferHistoryLog(initialTransaction)

        }
        return transactionHistoryRepository.findById(initialTransaction.id).get()
    }

    private fun validateSenderAndReceiverAccounts(
        sender: Account?,
        receiver: Account?,
        amount: BigDecimal
    ) {
        validateSenderAccount(sender, amount)
        validateReceiverAccount(receiver)
        if (sender!!.accountType == AccountType.SAVINGS_ACCOUNT.name && receiver!!.accountType != AccountType.CHECKING_ACCOUNT.name) {
            throw AccountTransferException("Only transferring money from the savings account to the reference account (checking account) is possible")
        }
    }

    private fun validateSenderAccount(account: Account?, amount: BigDecimal) {
        when {
            account == null -> throw AccountNotFoundException("Sender Account doesn't exists")
            account.locked -> throw AccountLockException("Sender Account is Locked, Can't Make a transfer from locked accounts")
            account.dateClosed?.isBefore(LocalDate.now()) == true -> throw ClosedAccountException("Sender Account is Closed, Can't Make a transfer from locked accounts")
            account.balance < amount -> throw InsufficientBalanceException("Sender Account has Insufficient Balance")
            account.accountType == AccountType.PRIVATE_LOAN_ACCOUNT.name -> throw AccountTransferException("Sender Account is PRIVATE_LOAN_ACCOUNT, Can't transfer from Private Loan Account")
            amount <= BigDecimal.ZERO -> throw EmptyDepositException("Can't Transfer Negative/Zero amount")
        }
    }

    private fun validateReceiverAccount(account: Account?) {
        when {
            account == null -> throw AccountNotFoundException("Receiver Account doesn't exists")
            account.locked -> throw AccountLockException("Receiver Account is Locked, Can't Make a transfer from locked accounts")
            account.dateClosed?.isBefore(LocalDate.now()) == true -> throw ClosedAccountException("Receiver Account is Closed, Can't Make a transfer from locked accounts")
        }
    }

    fun prepareFailingTransferHistoryLog(transfer: TransferHistory, e: Exception) {
        transactionHistoryRepository.save(
            transfer.copy(
                finishedAt = LocalDate.now(),
                status = TransferStatus.REJECTED.name,
                comment = e.message
            )
        )
    }

    fun prepareSuccessfulTransferHistoryLog(transfer: TransferHistory) {
        transactionHistoryRepository.save(
            transfer.copy(
                finishedAt = LocalDate.now(),
                status = TransferStatus.ACCEPTED.name
            )
        )
    }

}