package com.dkb.bankingsystem

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class BankingSystemApplication

fun main(args: Array<String>) {
	runApplication<BankingSystemApplication>(*args)
}
