package dev.vizualjack.staypositive

import java.time.LocalDate

class LocalDateTest {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            var janDate = LocalDate.of(2022, 1, 29)
            println(janDate)
            janDate = janDate.withMonth(1)
            var febDate = janDate.withMonth(2)
            println(janDate)
            println(febDate)
        }
    }
}