package com.dkb.bankingsystem.controller

import com.dkb.bankingsystem.controller.utils.prepareErrorResponseBody
import com.dkb.bankingsystem.model.request.IbanRequestRequestBody
import com.dkb.bankingsystem.model.response.ResponseBody
import com.dkb.bankingsystem.service.TransactionHistoryService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TransactionController(
    val transactionHistoryService: TransactionHistoryService
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


}
