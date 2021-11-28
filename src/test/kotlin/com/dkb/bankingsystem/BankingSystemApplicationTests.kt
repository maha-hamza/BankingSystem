package com.dkb.bankingsystem

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.lang.String
import kotlin.math.floor
import kotlin.math.pow
import kotlin.random.Random

@SpringBootTest
class BankingSystemApplicationTests {

    @Test
    fun contextLoads() {
        println(
            String.valueOf(Random.nextInt(999999999)).plus(Random.nextInt(999999))
        )
    }

}
