package com.dkb.bankingsystem.service

import com.dkb.bankingsystem.exceptions.*
import com.dkb.bankingsystem.model.Account
import com.dkb.bankingsystem.model.enum.AccountType
import com.dkb.bankingsystem.model.enum.TransferStatus
import com.dkb.bankingsystem.repositories.AccountRepository
import com.dkb.bankingsystem.repositories.TransactionHistoryRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionServiceTest(
    @Autowired
    val accountRepository: AccountRepository,
    @Autowired
    val transactionHistoryRepository: TransactionHistoryRepository,
    @Autowired
    val transactionService: TransactionService
) {

    @AfterEach
    fun clean() {
        accountRepository.deleteAll()
        transactionHistoryRepository.deleteAll()
    }

    @Test
    fun shouldTransferMoneyFromCheckingAccountToSavingAccountSuccessfully() {
        accountRepository.saveAll(
            mutableListOf(
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789101",
                    accountType = AccountType.CHECKING_ACCOUNT.name,
                    iban = "BE XX 123456789101 0 00",
                    balance = BigDecimal(30)
                ),
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789102",
                    accountType = AccountType.SAVINGS_ACCOUNT.name,
                    iban = "BE XX 123456789101 1 00"
                ),
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789103",
                    accountType = AccountType.PRIVATE_LOAN_ACCOUNT.name,
                    iban = "BE XX 123456789101 2 00"
                )
            )
        )

        val result =
            transactionService.makeTransfer("BE XX 123456789101 0 00", "BE XX 123456789101 1 00", BigDecimal(10))

        Assertions.assertThat(result).isNotNull
        Assertions.assertThat(result!!.status).isEqualTo(TransferStatus.ACCEPTED.name)
    }

    @Test
    fun shouldTransferMoneyFromCheckingAccountToPVTLoanAccountSuccessfully() {
        accountRepository.saveAll(
            mutableListOf(
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789101",
                    accountType = AccountType.CHECKING_ACCOUNT.name,
                    iban = "BE XX 123456789101 0 00",
                    balance = BigDecimal(30)
                ),
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789102",
                    accountType = AccountType.SAVINGS_ACCOUNT.name,
                    iban = "BE XX 123456789101 1 00"
                ),
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789103",
                    accountType = AccountType.PRIVATE_LOAN_ACCOUNT.name,
                    iban = "BE XX 123456789101 2 00"
                )
            )
        )

        val result =
            transactionService.makeTransfer("BE XX 123456789101 0 00", "BE XX 123456789101 2 00", BigDecimal(10))

        Assertions.assertThat(result).isNotNull
        Assertions.assertThat(result!!.status).isEqualTo(TransferStatus.ACCEPTED.name)
    }

    @Test
    fun shouldTransferMoneyFromSavingAccountToCheckingAccountSuccessfully() {
        accountRepository.saveAll(
            mutableListOf(
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789101",
                    accountType = AccountType.CHECKING_ACCOUNT.name,
                    iban = "BE XX 123456789101 0 00"
                ),
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789102",
                    accountType = AccountType.SAVINGS_ACCOUNT.name,
                    iban = "BE XX 123456789101 1 00",
                    balance = BigDecimal(30)
                ),
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789103",
                    accountType = AccountType.PRIVATE_LOAN_ACCOUNT.name,
                    iban = "BE XX 123456789101 2 00"
                )
            )
        )

        val result =
            transactionService.makeTransfer("BE XX 123456789101 1 00", "BE XX 123456789101 0 00", BigDecimal(10))

        Assertions.assertThat(result).isNotNull
        Assertions.assertThat(result!!.status).isEqualTo(TransferStatus.ACCEPTED.name)

        val account = accountRepository.findByIbanIgnoreCase("BE XX 123456789101 0 00")
        Assertions.assertThat(account!!.balance.toDouble()).isEqualTo(10.00)
    }

    @Test
    fun shouldFailTransferMoneyIfSenderAccountNotFound() {
        val exception = assertThrows(AccountNotFoundException::class.java) {
            transactionService.makeTransfer(sender = "anyIBAN", receiver = "AnyOtherIBAN", amount = BigDecimal.TEN)
        }
        Assertions.assertThat(exception.message).isEqualTo("Sender Account doesn't exists")
    }

    @Test
    fun shouldFailTransferMoneyIfSenderAccountIsLocked() {
        accountRepository.save(
            Account(
                customerId = "customer-1",
                accountNumber = "123456789101",
                accountType = AccountType.CHECKING_ACCOUNT.name,
                iban = "BE XX 123456789101 0 00",
                locked = true
            )
        )

        val exception = assertThrows(AccountLockException::class.java) {
            transactionService.makeTransfer(
                sender = "BE XX 123456789101 0 00",
                receiver = "AnyOtherIBAN",
                amount = BigDecimal.TEN
            )
        }
        Assertions.assertThat(exception.message)
            .isEqualTo("Sender Account is Locked, Can't Make a transfer from locked accounts")
    }

    @Test
    fun shouldFailTransferMoneyIfSenderBalanceIsLessThanTheAmount() {
        accountRepository.save(
            Account(
                customerId = "customer-1",
                accountNumber = "123456789101",
                accountType = AccountType.CHECKING_ACCOUNT.name,
                iban = "BE XX 123456789101 0 00",
                balance = BigDecimal.ONE
            )
        )

        val exception = assertThrows(InsufficientBalanceException::class.java) {
            transactionService.makeTransfer(
                sender = "BE XX 123456789101 0 00",
                receiver = "AnyOtherIBAN",
                amount = BigDecimal.TEN
            )
        }
        Assertions.assertThat(exception.message)
            .isEqualTo("Sender Account has Insufficient Balance")
    }

    @Test
    fun shouldFailTransferMoneyIfSenderAccountIsClosed() {
        accountRepository.save(
            Account(
                customerId = "customer-1",
                accountNumber = "123456789101",
                accountType = AccountType.CHECKING_ACCOUNT.name,
                iban = "BE XX 123456789101 0 00",
                dateClosed = LocalDate.now().minusDays(1)
            )
        )

        val exception = assertThrows(ClosedAccountException::class.java) {
            transactionService.makeTransfer(
                sender = "BE XX 123456789101 0 00",
                receiver = "AnyOtherIBAN",
                amount = BigDecimal.TEN
            )
        }
        Assertions.assertThat(exception.message)
            .isEqualTo("Sender Account is Closed, Can't Make a transfer from Closed accounts")
    }

    @Test
    fun shouldFailTransferMoneyIfSenderAccountIsPvtLoanAccount() {
        accountRepository.save(
            Account(
                customerId = "customer-1",
                accountNumber = "123456789101",
                accountType = AccountType.PRIVATE_LOAN_ACCOUNT.name,
                iban = "BE XX 123456789101 0 00",
                balance = BigDecimal.TEN
            )
        )

        val exception = assertThrows(AccountTransferException::class.java) {
            transactionService.makeTransfer(
                sender = "BE XX 123456789101 0 00",
                receiver = "AnyOtherIBAN",
                amount = BigDecimal.TEN
            )
        }
        Assertions.assertThat(exception.message)
            .isEqualTo("Sender Account is PRIVATE_LOAN_ACCOUNT, Can't transfer from Private Loan Account")
    }

    @Test
    fun shouldFailTransferMoneyIfAmountIsZERO() {
        accountRepository.save(
            Account(
                customerId = "customer-1",
                accountNumber = "123456789101",
                accountType = AccountType.CHECKING_ACCOUNT.name,
                iban = "BE XX 123456789101 0 00",
                balance = BigDecimal.TEN
            )
        )

        val exception = assertThrows(EmptyTransferAmountException::class.java) {
            transactionService.makeTransfer(
                sender = "BE XX 123456789101 0 00",
                receiver = "AnyOtherIBAN",
                amount = BigDecimal.ZERO
            )
        }
        Assertions.assertThat(exception.message)
            .isEqualTo("Can't Transfer Negative/Zero amount")
    }

    @Test
    fun shouldFailTransferMoneyIfAmountIsNegativeValue() {
        accountRepository.save(
            Account(
                customerId = "customer-1",
                accountNumber = "123456789101",
                accountType = AccountType.CHECKING_ACCOUNT.name,
                iban = "BE XX 123456789101 0 00",
                balance = BigDecimal.TEN
            )
        )

        val exception = assertThrows(EmptyTransferAmountException::class.java) {
            transactionService.makeTransfer(
                sender = "BE XX 123456789101 0 00",
                receiver = "AnyOtherIBAN",
                amount = BigDecimal.valueOf(-10)
            )
        }
        Assertions.assertThat(exception.message)
            .isEqualTo("Can't Transfer Negative/Zero amount")
    }

    @Test
    fun shouldFailTransferMoneyIfReceiverAccountNotFound() {
        accountRepository.save(
            Account(
                customerId = "customer-1",
                accountNumber = "123456789101",
                accountType = AccountType.CHECKING_ACCOUNT.name,
                iban = "BE XX 123456789101 0 00",
                balance = BigDecimal.TEN
            )
        )
        val exception = assertThrows(AccountNotFoundException::class.java) {
            transactionService.makeTransfer(
                sender = "BE XX 123456789101 0 00",
                receiver = "AnyOtherIBAN",
                amount = BigDecimal.TEN
            )
        }
        Assertions.assertThat(exception.message).isEqualTo("Receiver Account doesn't exists")
    }

    @Test
    fun shouldFailTransferMoneyIfReceiverAccountIsLocked() {
        accountRepository.saveAll(
            mutableListOf(
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789101",
                    accountType = AccountType.CHECKING_ACCOUNT.name,
                    iban = "BE XX 123456789101 0 00",
                    balance = BigDecimal.TEN
                ),
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789102",
                    accountType = AccountType.SAVINGS_ACCOUNT.name,
                    iban = "BE XX 123456789101 1 00",
                    locked = true
                )
            )
        )

        val exception = assertThrows(AccountLockException::class.java) {
            transactionService.makeTransfer(
                sender = "BE XX 123456789101 0 00",
                receiver = "BE XX 123456789101 1 00",
                amount = BigDecimal.TEN
            )
        }
        Assertions.assertThat(exception.message)
            .isEqualTo("Receiver Account is Locked, Can't Make a transfer from locked accounts")
    }

    @Test
    fun shouldFailTransferMoneyIfReceiverAccountIsClosed() {
        accountRepository.saveAll(
            mutableListOf(
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789101",
                    accountType = AccountType.CHECKING_ACCOUNT.name,
                    iban = "BE XX 123456789101 0 00",
                    balance = BigDecimal.TEN
                ),
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789102",
                    accountType = AccountType.SAVINGS_ACCOUNT.name,
                    iban = "BE XX 123456789101 1 00",
                    dateClosed = LocalDate.now().minusDays(1)
                )
            )
        )

        val exception = assertThrows(ClosedAccountException::class.java) {
            transactionService.makeTransfer(
                sender = "BE XX 123456789101 0 00",
                receiver = "BE XX 123456789101 1 00",
                amount = BigDecimal.TEN
            )
        }
        Assertions.assertThat(exception.message)
            .isEqualTo("Receiver Account is Closed, Can't Make a transfer from Closed accounts")
    }

    @Test
    fun shouldFailTransferMoneyIfSenderAccountIsSavingAccountAndReceiverIsNotChecking() {
        accountRepository.saveAll(
            mutableListOf(
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789101",
                    accountType = AccountType.SAVINGS_ACCOUNT.name,
                    iban = "BE XX 123456789101 0 00",
                    balance = BigDecimal.TEN
                ),
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789102",
                    accountType = AccountType.PRIVATE_LOAN_ACCOUNT.name,
                    iban = "BE XX 123456789101 1 00"
                )
            )
        )

        val exception = assertThrows(AccountTransferException::class.java) {
            transactionService.makeTransfer(
                sender = "BE XX 123456789101 0 00",
                receiver = "BE XX 123456789101 1 00",
                amount = BigDecimal.TEN
            )
        }
        Assertions.assertThat(exception.message)
            .isEqualTo("Only transferring money from the savings account to the reference account (checking account) is possible")
    }


}