package com.dkb.bankingsystem.controller.utils

import com.dkb.bankingsystem.model.response.ResponseBody
import java.lang.Exception

fun prepareErrorResponseBody(
    e: Exception,
    responseBody: ResponseBody
) {
    responseBody.hasException = true
    responseBody.exception = e
    responseBody.exceptionMessage = e.message
}
