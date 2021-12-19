package com.dkb.bankingsystem.service

import com.dkb.bankingsystem.exceptions.*
import com.dkb.bankingsystem.model.Account
import com.dkb.bankingsystem.model.enum.AccountType
import com.dkb.bankingsystem.model.enum.TransactionType
import com.dkb.bankingsystem.model.enum.TransferStatus
import com.dkb.bankingsystem.repositories.AccountRepository
import com.dkb.bankingsystem.repositories.PendingDepositsRepository
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountServiceTest(
    @Autowired
    val accountRepository: AccountRepository,
    @Autowired
    val accountService: AccountService,
    @Autowired
    val pendingDepositsRepository: PendingDepositsRepository
) {

    @AfterEach
    fun clean() {
        accountRepository.deleteAll()
    }

    @Test
    fun shouldReturnCustomerAllMatchingAccountsFiltersByAccountsType() {
        accountRepository.saveAll(
            mutableListOf(
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789101",
                    accountType = AccountType.SAVINGS_ACCOUNT.name,
                    iban = "BE XX 123456789101 1 00"
                ),
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789102",
                    accountType = AccountType.CHECKING_ACCOUNT.name,
                    iban = "BE XX 123456789101 0 00"
                ),
                Account(
                    customerId = "customer-1",
                    accountNumber = "123456789103",
                    accountType = AccountType.PRIVATE_LOAN_ACCOUNT.name,
                    iban = "BE XX 123456789101 2 00"
                )

            )
        )

        val accounts = accountService.filterAccountsByAccountType(
            customerId = "customer-1",
            accountType = listOf(AccountType.PRIVATE_LOAN_ACCOUNT, AccountType.SAVINGS_ACCOUNT)
        )

        assertThat(accounts.size).isEqualTo(2)
    }

    @Test
    fun shouldReturnEmptyListIfNoMatchingAccountTypeFound() {
        accountRepository.save(
            Account(
                customerId = "customer-2",
                accountNumber = "123456789104",
                accountType = AccountType.PRIVATE_LOAN_ACCOUNT.name,
                iban = "BE XX 123456789104 2 00"
            )
        )
        val accounts = accountService.filterAccountsByAccountType(
            customerId = "customer-2",
            accountType = listOf(AccountType.SAVINGS_ACCOUNT)
        )

        assertThat(accounts).isEmpty()
    }

    @Test
    fun shouldReturnEmptyListIfNoMatchingCustomerIdFound() {
        val accounts = accountService.filterAccountsByAccountType(
            customerId = "customer",
            accountType = listOf(AccountType.SAVINGS_ACCOUNT)
        )

        assertThat(accounts).isEmpty()
    }

    @Test
    fun shouldCreateNewCustomerAccountSuccessfully() {
        val account = accountService.createAccount(
            customerId = "customer-id",
            accountType = AccountType.CHECKING_ACCOUNT
        )

        assertThat(account.iban).isEqualTo(String.format("BE XX %s 0 00", account.accountNumber))
        assertThat(account.accountType).isEqualTo(AccountType.CHECKING_ACCOUNT.name)
        assertThat(account.locked).isFalse
        assertThat(account.balance).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun shouldFailCreatingCustomerAccountIfCustomerIdIsBlank() {
        val exception = assertThrows(CustomerCannotOpenAccountException::class.java) {
            accountService.createAccount(
                customerId = "",
                accountType = AccountType.CHECKING_ACCOUNT
            )
        }

        assertThat(exception.message).isEqualTo("CustomerId Can't be Blank")
    }

    @Test
    fun shouldFailCreatingCustomerAccountIFTheSameAccountTypeForTheCustomerExists() {
        accountRepository.save(
            Account(
                customerId = "customer-1",
                accountNumber = "123456789102",
                accountType = AccountType.CHECKING_ACCOUNT.name,
                iban = "BE XX 123456789101 0 00"
            )
        )
        val exception = assertThrows(CustomerCannotOpenAccountException::class.java) {
            accountService.createAccount(
                customerId = "customer-1",
                accountType = AccountType.CHECKING_ACCOUNT
            )
        }
        assertThat(exception.message).isEqualTo("Customer Already has account of type CHECKING_ACCOUNT")
    }

    @Test
    fun shouldLockAccountSuccessfully() {
        accountRepository.save(
            Account(
                customerId = "customer-1",
                accountNumber = "123456789101",
                accountType = AccountType.SAVINGS_ACCOUNT.name,
                iban = "BE XX 123456789101 1 00"
            )
        )
        val account = accountService.lockAccount(iban = "BE XX 123456789101 1 00")
        assertTrue(account.locked)
    }

    @Test
    fun shouldFailLockingIfAccountNotFound() {
        val exception = assertThrows(AccountNotFoundException::class.java) {
            accountService.lockAccount(iban = "BE XX 123456789101 1 10")
        }

        assertThat(exception.message).isEqualTo("Account You are trying to lock doesn't exists")
    }

    @Test
    fun shouldFailLockingIfAccountIsLocked() {
        accountRepository.save(
            Account(
                customerId = "customer-1",
                accountNumber = "123456789101",
                accountType = AccountType.PRIVATE_LOAN_ACCOUNT.name,
                iban = "BE XX 123456789105 2 00",
                locked = true
            )
        )
        val exception = assertThrows(AccountLockException::class.java) {
            accountService.lockAccount(iban = "BE XX 123456789105 2 00")
        }

        assertThat(exception.message).isEqualTo("Account You are trying to lock already locked, Do you mean unlock?")
    }

    @Test
    fun shouldFailLockingAccountIfIBANIsBlank() {
        val exception = assertThrows(AccountLockException::class.java) {
            accountService.lockAccount(iban = "")
        }

        assertThat(exception.message).isEqualTo("IBAN is blank, Please make sure to enter valid IBAN")
    }

    @Test
    fun shouldUnLockAccountSuccessfully() {
        accountRepository.save(
            Account(
                customerId = "customer-3",
                accountNumber = "123456789106",
                accountType = AccountType.PRIVATE_LOAN_ACCOUNT.name,
                iban = "BE XX 123456789106 2 00",
                locked = true
            )
        )
        val account = accountService.unlockAccount(iban = "BE XX 123456789106 2 00")
        assertFalse(account.locked)
    }

    @Test
    fun shouldFailUnLockingAccountIfIBANIsBlank() {
        val exception = assertThrows(AccountLockException::class.java) {
            accountService.unlockAccount(iban = "")
        }

        assertThat(exception.message).isEqualTo("IBAN is blank, Please make sure to enter valid IBAN")
    }

    @Test
    fun shouldFailUnLockingIfAccountIsNotLocked() {
        accountRepository.save(
            Account(
                customerId = "customer-1",
                accountNumber = "123456789101",
                accountType = AccountType.SAVINGS_ACCOUNT.name,
                iban = "BE XX 123456789101 0 00"
            )
        )
        val exception = assertThrows(AccountLockException::class.java) {
            accountService.unlockAccount(iban = "BE XX 123456789101 0 00")
        }

        assertThat(exception.message).isEqualTo("Account You are trying to unlock is not locked, Do you mean lock?")
    }

    @Test
    fun shouldFailUnLockingIfAccountNotFound() {
        val exception = assertThrows(AccountNotFoundException::class.java) {
            accountService.unlockAccount(iban = "BE XX 123456789101 1 10")
        }

        assertThat(exception.message).isEqualTo("Account You are trying to unlock doesn't exists")
    }

    @Test
    fun shouldRetrieveCustomerBalanceByIBan() {
        val account = accountRepository.save(
            Account(
                customerId = "customer-1",
                accountNumber = "123456789101",
                accountType = AccountType.SAVINGS_ACCOUNT.name,
                iban = "BE XX 123456789101 1 00",
                balance = BigDecimal(20)
            )
        )

        val result = accountService.getAccountBalanceByIbanOrAccountNumber(
            iban = account.iban
        )

        assertThat(result.toDouble()).isEqualTo(20.00)
    }

    @Test
    fun shouldRetrieveCustomerBalanceByAccountNumber() {
        val account = accountRepository.save(
            Account(
                customerId = "customer-1",
                accountNumber = "123456789102",
                accountType = AccountType.CHECKING_ACCOUNT.name,
                iban = "BE XX 123456789101 0 00",
                balance = BigDecimal(20)
            )
        )

        val result = accountService.getAccountBalanceByIbanOrAccountNumber(
            accountNumber = account.accountNumber
        )

        assertThat(result.toDouble()).isEqualTo(20.00)
    }

    @Test
    fun shouldThrowExceptionIfNoAccountNumberOrIBANGiven() {

        val exception = assertThrows(MissingParametersForAccountBalanceRetrievalException::class.java) {
            accountService.getAccountBalanceByIbanOrAccountNumber()
        }

        assertThat(exception.message).isEqualTo("Account Number or Iban must be present to perform the query")
    }

    @Test
    fun shouldThrowExceptionIfNoAccountNumberFoundForBalanceRetrieval() {

        val exception = assertThrows(AccountNotFoundException::class.java) {
            accountService.getAccountBalanceByIbanOrAccountNumber(accountNumber = "AnyNumber")
        }

        assertThat(exception.message).isEqualTo("No account with #AnyNumber exists")
    }

    @Test
    fun shouldThrowExceptionIfNoIBANFoundForBalanceRetrieval() {

        val exception = assertThrows(AccountNotFoundException::class.java) {
            accountService.getAccountBalanceByIbanOrAccountNumber(iban = "AnyIBAN")
        }

        assertThat(exception.message).isEqualTo("No IBAN with #AnyIBAN exists")
    }

    @Test
    fun shouldNotDepositToAccountIfAmountIsZero() {
        accountRepository.save(
            Account(
                customerId = "customer-1",
                accountNumber = "123456789101",
                accountType = AccountType.SAVINGS_ACCOUNT.name,
                iban = "BE XX 123456789101 1 00"
            )
        )
        val exception = assertThrows(EmptyDepositException::class.java) {
            accountService.deposit("BE XX 123456789101 1 00", BigDecimal.ZERO)
        }
        assertThat(exception.message).isEqualTo("Can't Perform Empty or negative Deposit, Please make sure that correct amount is selected")
    }

    @Test
    fun shouldNotDepositToAccountIfAmountIsNegative() {
        accountRepository.save(
            Account(
                customerId = "customer-1",
                accountNumber = "123456789101",
                accountType = AccountType.SAVINGS_ACCOUNT.name,
                iban = "BE XX 123456789101 1 00"
            )
        )
        val exception = assertThrows(EmptyDepositException::class.java) {
            accountService.deposit("BE XX 123456789101 1 00", BigDecimal(-20))
        }
        assertThat(exception.message).isEqualTo("Can't Perform Empty or negative Deposit, Please make sure that correct amount is selected")
    }

    @Test
    fun shouldNotDepositToAccountIfAccountNotFound() {
        val exception = assertThrows(AccountNotFoundException::class.java) {
            accountService.deposit("BE XX 123456789101 1 01", BigDecimal(20))
        }
        assertThat(exception.message).isEqualTo("Account You are trying to deposit into doesn't exists")
    }

    @Test
    fun shouldNotDepositToAccountIfAccountIsLocked() {
        accountRepository.save(
            Account(
                customerId = "customer-4",
                accountNumber = "123456789107",
                accountType = AccountType.CHECKING_ACCOUNT.name,
                iban = "BE XX 123456789107 0 00",
                locked = true
            )
        )
        val exception = assertThrows(DepositToLockedAccountException::class.java) {
            accountService.deposit("BE XX 123456789107 0 00", BigDecimal(20))
        }
        assertThat(exception.message).isEqualTo("Can't Deposit To Locked Account")
    }

    @Test
    fun shouldDepositToAccountSuccessfully() {
        accountRepository.save(
            Account(
                customerId = "customer-4",
                accountNumber = "123456789107",
                accountType = AccountType.CHECKING_ACCOUNT.name,
                iban = "BE XX 123456789107 0 00",
                locked = false,
                balance = BigDecimal(20)
            )
        )

        val result = accountService.deposit(iban = "BE XX 123456789107 0 00", amount = BigDecimal(10))
        assertThat(result.message).isEqualTo("Amount Is Successfully Deposited, Current Amount [30,000000]")
        assertThat(result.transferReport!!.fromAccount).isNull()
        assertThat(result.transferReport!!.status).isEqualTo(TransferStatus.ACCEPTED.name)
        assertThat(result.transferReport!!.transactionType).isEqualTo(TransactionType.DEPOSIT.name)
        assertThat(result.transferReport!!.amount.toDouble()).isEqualTo(10.0)
    }

    @Test
    fun shouldPutDepositIntoPendingIfAccountIsTransferPending() {
        accountRepository.save(
            Account(
                customerId = "customer-4",
                accountNumber = "123456789107",
                accountType = AccountType.CHECKING_ACCOUNT.name,
                iban = "BE XX 123456789107 0 00",
                transactionPending = true,
                balance = BigDecimal(20)
            )
        )

        val result = accountService.deposit(iban = "BE XX 123456789107 0 00", amount = BigDecimal(10))
        assertThat(result.message).isEqualTo("Current Deposit Process Is Pending Due to Processing on the account")

        val pending = pendingDepositsRepository.findByIbanAndAmount("BE XX 123456789107 0 00", BigDecimal(10)).last()
        assertThat(pending).isNotNull
    }

}