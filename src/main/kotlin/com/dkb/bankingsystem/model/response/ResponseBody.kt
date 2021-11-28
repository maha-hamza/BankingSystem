package com.dkb.bankingsystem.model.response

import java.lang.Exception

class ResponseBody(
    var data: Any?,
    var hasException: Boolean,
    var exceptionMessage: String?,
    var exception: Exception?
) {
    constructor() : this(data = null, hasException = false, exceptionMessage = null, exception = null) {}
}