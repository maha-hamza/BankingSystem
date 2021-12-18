package com.dkb.bankingsystem.service.dto

import com.dkb.bankingsystem.model.TransferHistory

class DepositDTO(val message: String, val transferReport: TransferHistory? = null)