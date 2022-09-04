package dev.vizualjack.staypositive

import java.time.LocalDate

data class PaymentTimelineEntry(var payment: Payment, var currentTime: LocalDate)
