package com.dkb.bankingsystem.service

import com.dkb.bankingsystem.model.TransferHistory
import com.dkb.bankingsystem.model.enum.TransactionType
import com.dkb.bankingsystem.model.enum.TransferStatus
import com.dkb.bankingsystem.repositories.TransactionHistoryRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionHistoryServiceTest(
    @Autowired
    val service: TransactionHistoryService,
    @Autowired
    val repository: TransactionHistoryRepository
) {

    @AfterEach
    fun clean() {
        repository.deleteAll()
    }

    @Test
    fun shouldReturnEmptyListIfNoHistoryFoundOrIBANDoesntExists() {

        val result = service.getAllTransactionsHistoryForAccount("AnyIBAN")
        Assertions.assertThat(result).isEmpty()
    }

    @Test
    fun shouldReturnListOfHistory() {

        val histories = repository.saveAll(
            mutableListOf(
                TransferHistory(
                    fromAccount = "BE XX 123456789101 0 00",
                    toAccount = "BE XX 123456789102 0 00",
                    initiatedAt = LocalDate.now().minusDays(1),
                    finishedAt = LocalDate.now(),
                    amount = BigDecimal(10),
                    status = TransferStatus.ACCEPTED.name,
                    transactionType = TransactionType.TRANSFER.name,
                    transactionCode = generateTransactionCode()
                ),
                TransferHistory(
                    toAccount = "BE XX 123456789101 0 00",
                    initiatedAt = LocalDate.now().minusDays(2),
                    finishedAt = LocalDate.now().minusDays(1),
                    amount = BigDecimal(20),
                    status = TransferStatus.ACCEPTED.name,
                    transactionType = TransactionType.DEPOSIT.name,
                    transactionCode = generateTransactionCode()
                ),
                TransferHistory(
                    toAccount = "BE XX 123456789103 0 00",
                    initiatedAt = LocalDate.now().minusDays(1),
                    finishedAt = LocalDate.now(),
                    amount = BigDecimal(10),
                    status = TransferStatus.ACCEPTED.name,
                    transactionType = TransactionType.DEPOSIT.name,
                    transactionCode = generateTransactionCode()
                )
            )
        )
        val result = service.getAllTransactionsHistoryForAccount("BE XX 123456789101 0 00")
        Assertions.assertThat(result).hasSize(2)
    }

}