package dev.vizualjack.staypositive

import java.time.LocalDate

data class Payment(var name: String?, var value: Float?, var nextTime: LocalDate?, var type: PaymentType?)