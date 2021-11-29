package com.dkb.bankingsystem.controller

import com.dkb.bankingsystem.controller.utils.prepareErrorResponseBody
import com.dkb.bankingsystem.model.request.IbanRequestRequestBody
import com.dkb.bankingsystem.model.request.TransferRequestBody
import com.dkb.bankingsystem.model.response.ResponseBody
import com.dkb.bankingsystem.service.TransactionHistoryService
import com.dkb.bankingsystem.service.TransactionService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TransactionController(
    val transactionHistoryService: TransactionHistoryService,
    val transactionService: TransactionService
) {

    @PostMapping("/transaction/history")
    fun getTransactionHistory(@RequestBody requestBody: IbanRequestRequestBody): ResponseBody {
        val responseBody = ResponseBody()
        try {
            val result = transactionHistoryService.getAllTransactionsHistoryForAccount(requestBody.iban)
            responseBody.data = result
        } catch (e: Exception) {
            prepareErrorResponseBody(e, responseBody)
        }
        return responseBody
    }

    @PostMapping("/transaction/transfer")
    fun makeTransfer(@RequestBody requestBody: TransferRequestBody): ResponseBody {
        val responseBody = ResponseBody()
        val result = transactionService.makeTransfer(
            sender = requestBody.senderIban,
            receiver = requestBody.receiverIban,
            amount = requestBody.amount
        )
        when {
            result == null -> responseBody.data = "Current Transfer Process Is Pending Due to Processing on one of the accounts"
            result.comment == null -> responseBody.data = result
            else -> {
                responseBody.hasException = true
                responseBody.exceptionMessage = result.comment
            }
        }

        return responseBody
    }

}
