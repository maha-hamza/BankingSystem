package com.dkb.bankingsystem.service

fun generateTransactionCode(): String {
    val characters = ('0'..'z').toList().toTypedArray()
    return (1..20).map { characters.random() }.joinToString("")
}