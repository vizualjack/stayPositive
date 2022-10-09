package dev.vizualjack.staypositive

import org.junit.Test

import org.junit.Assert.*
import java.time.LocalDate

class PaymentUtilTest {
    @Test
    fun calculatePast_ones_1() {
        val nextTime = LocalDate.now()
        val payments = arrayListOf(Payment("Test", 1f, nextTime, null, PaymentType.ONES))
        val newCash = PaymentUtil.calculateTillDate(LocalDate.now(), payments, 0f, false)
        assertEquals(0f, newCash)
    }

    @Test
    fun calculatePast_ones_2() {
        val nextTime = LocalDate.now().minusDays(1).minusMonths(1)
        val payments = arrayListOf(Payment("Test", 1f, nextTime, null, PaymentType.ONES))
        val newCash = PaymentUtil.calculateTillDate(LocalDate.now(), payments, 0f, false)
        assertEquals(1f, newCash)
    }

    @Test
    fun calculatePast_ones_3() {
        val nextTime = LocalDate.now().plusDays(1).plusMonths(1).minusYears(1)
        val payments = arrayListOf(Payment("Test", 1f, nextTime, null, PaymentType.ONES))
        val newCash = PaymentUtil.calculateTillDate(LocalDate.now(), payments, 0f, false)
        assertEquals(1f, newCash)
    }

    @Test
    fun calculatePast_monthly_1() {
        val nextTime = LocalDate.now()
        val payments = arrayListOf(Payment("Test", 1f, nextTime, null, PaymentType.MONTHLY))
        val newCash = PaymentUtil.calculateTillDate(LocalDate.now(), payments, 0f, false)
        assertEquals(0f, newCash)
    }

    @Test
    fun calculatePast_monthly_2() {
        val nextTime = LocalDate.now().minusDays(1)
        val payments = arrayListOf(Payment("Test", 1f, nextTime, null, PaymentType.MONTHLY))
        val newCash = PaymentUtil.calculateTillDate(LocalDate.now(), payments, 0f, false)
        assertEquals(1f, newCash)
    }

    @Test
    fun calculatePast_monthly_3() {
        val lastTime = LocalDate.now().minusDays(1).minusMonths(1)
        val nextTime = lastTime
        val payments = arrayListOf(Payment("Test", 1f, nextTime, lastTime, PaymentType.MONTHLY))
        val newCash = PaymentUtil.calculateTillDate(LocalDate.now(), payments, 0f, false)
        assertEquals(1f, newCash)
    }

    @Test
    fun calculatePast_monthly_4() {
        val lastTime = LocalDate.now().minusDays(1)
        val nextTime = lastTime.minusMonths(1)
        val payments = arrayListOf(Payment("Test", 1f, nextTime, lastTime, PaymentType.MONTHLY))
        val newCash = PaymentUtil.calculateTillDate(LocalDate.now(), payments, 0f, false)
        assertEquals(2f, newCash)
    }

    @Test
    fun calculatePast_monthly_5() {
        val lastTime = LocalDate.now().minusDays(1)
        val nextTime = lastTime.minusMonths(1).minusYears(1)
        val payments = arrayListOf(Payment("Test", 1f, nextTime, lastTime, PaymentType.MONTHLY))
        val newCash = PaymentUtil.calculateTillDate(LocalDate.now(), payments, 0f, false)
        assertEquals(14f, newCash)
    }

    @Test
    fun calculatePast_monthly_6() {
        val lastTime = LocalDate.now()
        val nextTime = lastTime.minusMonths(1).minusYears(1)
        val payments = arrayListOf(Payment("Test", 1f, nextTime, lastTime, PaymentType.MONTHLY))
        val newCash = PaymentUtil.calculateTillDate(LocalDate.now(), payments, 0f, false)
        assertEquals(13f, newCash)
    }
}