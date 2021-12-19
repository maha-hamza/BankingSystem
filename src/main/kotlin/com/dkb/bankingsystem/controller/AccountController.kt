package com.dkb.bankingsystem.controller

import com.dkb.bankingsystem.controller.utils.prepareErrorResponseBody
import com.dkb.bankingsystem.model.request.*
import com.dkb.bankingsystem.model.response.ResponseBody
import com.dkb.bankingsystem.service.AccountService
import org.springframework.web.bind.annotation.*
import java.lang.Exception

@RestController
@RequestMapping("/account")
class AccountController(
    val accountService: AccountService
) {

    @PatchMapping("/lock")
    fun lockAccount(@RequestBody requestBody: IbanRequestRequestBody): ResponseBody {
        val responseBody = ResponseBody()
        try {
            val result = accountService.lockAccount(requestBody.iban)
            responseBody.data = result
        } catch (e: Exception) {
            prepareErrorResponseBody(e, responseBody)
        }
        return responseBody
    }

    @PatchMapping("/unlock")
    fun unlockAccount(@RequestBody requestBody: IbanRequestRequestBody): ResponseBody {
        val responseBody = ResponseBody()
        try {
            val result = accountService.unlockAccount(requestBody.iban)
            responseBody.data = result
        } catch (e: Exception) {
            prepareErrorResponseBody(e, responseBody)
        }
        return responseBody
    }

    @PostMapping("/create")
    fun createAccount(@RequestBody requestBody: AccountCreationRequestBody): ResponseBody {
        val responseBody = ResponseBody()
        try {
            val result = accountService.createAccount(requestBody.customerId, requestBody.accountType)
            responseBody.data = result
        } catch (e: Exception) {
            prepareErrorResponseBody(e, responseBody)
        }

        return responseBody
    }

    @GetMapping("/accountType/filter")
    fun filterAccountsByAccountType(@RequestBody requestBody: AccountRequestByAccountTypeRequestBody): ResponseBody {
        val responseBody = ResponseBody()
        try {
            val result = accountService.filterAccountsByAccountType(requestBody.customerId, requestBody.accountsType)
            responseBody.data = result
        } catch (e: Exception) {
            prepareErrorResponseBody(e, responseBody)
        }

        return responseBody
    }

    @GetMapping("/balance")
    fun getAccountBalance(@RequestBody requestBody: AccountBalanceRequestBody): ResponseBody {
        val responseBody = ResponseBody()
        try {
            val result =
                accountService.getAccountBalanceByIbanOrAccountNumber(
                    accountNumber = requestBody.accountNumber,
                    iban = requestBody.iban
                )
            responseBody.data = result
        } catch (e: Exception) {
            prepareErrorResponseBody(e, responseBody)
        }

        return responseBody
    }

    @PostMapping("/deposit")
    fun deposit(@RequestBody requestBody: AccountDepositRequestBody): ResponseBody {
        val responseBody = ResponseBody()
        try {
            val result =
                accountService.deposit(
                    iban = requestBody.iban,
                    amount = requestBody.amount
                )
            responseBody.data = result
        } catch (e: Exception) {
            prepareErrorResponseBody(e, responseBody)
        }

        return responseBody
    }

}