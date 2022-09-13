package dev.vizualjack.staypositive

import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class PaymentUtil {
    companion object {
        fun sortPayments(payments: List<Payment>): SortedSet<Payment> {
            return payments.toSortedSet(kotlin.Comparator { p1, p2 ->
                val nextTimeComp = p1.nextTime!!.compareTo(p2.nextTime)
                if (nextTimeComp != 0) return@Comparator nextTimeComp
                return@Comparator p1.name!!.compareTo(p2.name!!)
            })
        }

        fun createPaymentTimeline(sortedPayments: List<Payment>, startDate: LocalDate, neededNumberOfEntries: Int): ArrayList<PaymentTimelineEntry> {
            val timeline = ArrayList<PaymentTimelineEntry>()
            var currentLocalDate = startDate
            val lastPossibleDate = getLastPossibleDate(sortedPayments)
            while (neededNumberOfEntries > timeline.size) {
                if (currentLocalDate > lastPossibleDate) break
                timeline.addAll(getPaymentsForDay(sortedPayments, currentLocalDate))
                currentLocalDate = currentLocalDate.plusDays(1)
            }
            return timeline
        }

        private fun getLastPossibleDate(payments: List<Payment>): LocalDate {
            var lastPossibleDate = LocalDate.now()
            for (payment in payments) {
                var currentLastPossibleDate = payment.nextTime
                if (payment.type == PaymentType.MONTHLY)
                    currentLastPossibleDate = payment.nextTime!!.withYear(LocalDate.MAX.year)
                if (currentLastPossibleDate!! > lastPossibleDate)
                    lastPossibleDate = currentLastPossibleDate
            }
            return lastPossibleDate
        }

        private fun getPaymentsForDay(payments: List<Payment>, localDate: LocalDate): ArrayList<PaymentTimelineEntry> {
            val paymentsForDay = ArrayList<PaymentTimelineEntry>()
            for (payment in payments) {
                if (isPaymentForDay(payment, localDate))
                    paymentsForDay.add(PaymentTimelineEntry(payment, localDate))
            }
            return paymentsForDay
        }

        private fun isPaymentForDay(payment: Payment, localDate: LocalDate): Boolean {
            return when (payment.type) {
                PaymentType.ONES -> payment.nextTime!! == localDate
                PaymentType.MONTHLY -> {
                    if (payment.nextTime!! > localDate) false
                    payment.nextTime!!
                        .withYear(localDate.year)
                        .withMonth(localDate.monthValue)
                        .compareTo(localDate) == 0
                }
                else -> false
            }
        }

        fun calculatePast(payments: ArrayList<Payment>, todayCash: Float): Float {
            val now = LocalDate.now()
            val finishedPayments = ArrayList<Payment>()
            var newCash = todayCash
            for (payment in payments) {
                if (payment.nextTime!! >= now) continue
                when (payment.type) {
                    PaymentType.ONES -> {
                        newCash += payment.value!!
                        finishedPayments.add(payment)
                    }
                    PaymentType.MONTHLY -> {
                        var monthDifference = now.monthValue - payment.nextTime!!.monthValue
                        var newNextTime = payment.nextTime!!.withMonth(now.monthValue)
                        if (newNextTime < now) {
                            monthDifference += 1
                            newNextTime = newNextTime.plusMonths(1)
                        }
                        newCash += (payment.value!! * monthDifference)
                        payment.nextTime = newNextTime
                    }
                    else -> {}
                }
            }
            for (finishedPayment in finishedPayments)
                payments.remove(finishedPayment)
            return newCash
        }
    }
}