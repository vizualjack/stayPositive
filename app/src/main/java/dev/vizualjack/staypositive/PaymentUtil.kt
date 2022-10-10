package dev.vizualjack.staypositive

import java.time.LocalDate
import java.time.Period
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

        fun calculatePast(payments: ArrayList<Payment>, todayCash: Float): Float {
            val date = LocalDate.now()
            val finishedPayments = ArrayList<Payment>()
            var newCash = todayCash
            for (payment in payments) {
                if (payment.nextTime!! >= date) continue
                when (payment.type) {
                    PaymentType.ONES -> {
                        newCash += payment.value!!
                        finishedPayments.add(payment)
                    }
                    PaymentType.MONTHLY -> {
                        var monthDifference = 0
                        if (payment.lastTime != null && payment.lastTime!! < date) {
                            val lastTime = payment.lastTime!!
                            val period = Period.between(payment.nextTime!!, lastTime)
                            monthDifference = period.years * 12
                            monthDifference += period.months
                            monthDifference += 1
                            finishedPayments.add(payment)
                        }
                        else {
                            val period = Period.between(payment.nextTime!!, date)
                            monthDifference = period.years * 12
                            monthDifference += period.months
                            var newNextTime = payment.nextTime!!.withMonth(date.monthValue).withYear(date.year)
                            if (newNextTime < date) {
                                monthDifference += 1
                                newNextTime = newNextTime.plusMonths(1)
                            }
                            payment.nextTime = newNextTime
                        }
                        newCash += (payment.value!! * monthDifference)
                    }
                    else -> {}
                }
            }
            for (finishedPayment in finishedPayments)
                payments.remove(finishedPayment)
            return newCash
        }

        fun testPayment(testingPayment: Payment, currentPayments: ArrayList<Payment>, startCash: Float): Boolean {
            val testingPayments = arrayListOf<Payment>(testingPayment)
            testingPayments.addAll(currentPayments)
            return checkForStayingPositive(testingPayments, startCash)
        }

        private fun getLastPossibleDate(payments: List<Payment>): LocalDate {
            var lastPossibleDate = LocalDate.now()
            for (payment in payments) {
                var currentLastPossibleDate = payment.nextTime
                if (payment.type == PaymentType.MONTHLY) {
                    currentLastPossibleDate = payment.nextTime!!.withYear(LocalDate.MAX.year)
                    if (payment.lastTime != null)
                        currentLastPossibleDate = payment.lastTime!!
                }
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
                    if (payment.nextTime!! <= localDate && (payment.lastTime == null || localDate <= payment.lastTime)) {
                        payment.nextTime!!
                            .withYear(localDate.year)
                            .withMonth(localDate.monthValue)
                            .compareTo(localDate) == 0
                    } else false
                }
                else -> false
            }
        }

//        private fun calculateMonthlyCash(payments: ArrayList<Payment>): Float {
//            var monthlyCash = 0f
//            for (payment in payments) {
//                if (payment.type == PaymentType.ONES) continue
//                if (payment.lastTime != null) continue
//                monthlyCash += payment.value!!
//            }
//            return monthlyCash
//        }

        private fun getLastChangePaymentDate(payments: ArrayList<Payment>): LocalDate {
            var lastPossibleDate = LocalDate.now()
            for (payment in payments) {
                var currentLastPossibleDate: LocalDate? = null
                if (payment.type == PaymentType.MONTHLY && payment.lastTime != null)
                    currentLastPossibleDate = payment.lastTime!!
                else if (payment.type == PaymentType.ONES)
                    currentLastPossibleDate = payment.nextTime!!
                if (currentLastPossibleDate == null) continue
                if (currentLastPossibleDate > lastPossibleDate)
                    lastPossibleDate = currentLastPossibleDate
            }
            return lastPossibleDate
        }

        private fun checkForStayingPositive(testingPayments: ArrayList<Payment>, startCash: Float): Boolean {
            val endDate = getLastChangePaymentDate(testingPayments)!!.plusMonths(1).plusDays(1)
            val newCash = simulateCashFlow(endDate, testingPayments, startCash)
            return newCash >= 0f
        }

        private fun simulateCashFlow(endDate: LocalDate, payments: ArrayList<Payment>, startCash: Float): Float {
            var date = LocalDate.now()
            var cash = startCash
            while (date < endDate) {
                for (timelineEntry in getPaymentsForDay(payments, date)) {
                    cash += timelineEntry.payment.value!!
                }
                if (cash < 0f) break
                date = date.plusDays(1)
            }
            return cash
        }

    }
}