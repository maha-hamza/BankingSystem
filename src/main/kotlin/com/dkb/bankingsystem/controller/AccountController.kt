package com.dkb.bankingsystem.controller

import com.dkb.bankingsystem.controller.utils.prepareErrorResponseBody
import com.dkb.bankingsystem.model.request.*
import com.dkb.bankingsystem.model.response.ResponseBody
import com.dkb.bankingsystem.service.AccountService
import org.springframework.web.bind.annotation.*
import java.lang.Exception

@RestController
class AccountController(
    val accountService: AccountService
) {

    @PatchMapping("/account/lock")
    fun lockAccount(@RequestBody requestBody: IbanRequestRequestBody): ResponseBody {
        val responseBody = ResponseBody()
        try {
            accountService.lockAccount(requestBody.iban)
            responseBody.data = "Account_Locked"
        } catch (e: Exception) {
            prepareErrorResponseBody(e, responseBody)
        }
        return responseBody
    }

    @PatchMapping("/account/unlock")
    fun unlockAccount(@RequestBody requestBody: IbanRequestRequestBody): ResponseBody {
        val responseBody = ResponseBody()
        try {
            accountService.unlockAccount(requestBody.iban)
            responseBody.data = "Account_Unlocked"
        } catch (e: Exception) {
            prepareErrorResponseBody(e, responseBody)
        }
        return responseBody
    }

    @PostMapping("/account/create")
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

    @PostMapping("/account/accountType/filter")
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

    @PostMapping("/account/balance")
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

    @PostMapping("/account/deposit")
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