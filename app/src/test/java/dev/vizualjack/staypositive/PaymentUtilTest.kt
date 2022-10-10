package dev.vizualjack.staypositive

import org.junit.Test

import org.junit.Assert.*
import java.time.LocalDate
import java.util.ArrayList

class PaymentUtilTest {
    @Test
    fun calculatePast_ones_1() {
        val nextTime = LocalDate.now()
        val payments = arrayListOf(Payment("Test", 1f, nextTime, null, PaymentType.ONES))
        val newCash = PaymentUtil.calculatePast( payments, 0f)
        assertEquals(0f, newCash)
    }

    @Test
    fun calculatePast_ones_2() {
        val nextTime = LocalDate.now().minusDays(1).minusMonths(1)
        val payments = arrayListOf(Payment("Test", 1f, nextTime, null, PaymentType.ONES))
        val newCash = PaymentUtil.calculatePast( payments, 0f)
        assertEquals(1f, newCash)
    }

    @Test
    fun calculatePast_ones_3() {
        val nextTime = LocalDate.now().plusDays(1).plusMonths(1).minusYears(1)
        val payments = arrayListOf(Payment("Test", 1f, nextTime, null, PaymentType.ONES))
        val newCash = PaymentUtil.calculatePast( payments, 0f)
        assertEquals(1f, newCash)
    }

    @Test
    fun calculatePast_monthly_1() {
        val nextTime = LocalDate.now()
        val payments = arrayListOf(Payment("Test", 1f, nextTime, null, PaymentType.MONTHLY))
        val newCash = PaymentUtil.calculatePast( payments, 0f)
        assertEquals(0f, newCash)
    }

    @Test
    fun calculatePast_monthly_2() {
        val nextTime = LocalDate.now().minusDays(1)
        val payments = arrayListOf(Payment("Test", 1f, nextTime, null, PaymentType.MONTHLY))
        val newCash = PaymentUtil.calculatePast( payments, 0f)
        assertEquals(1f, newCash)
    }

    @Test
    fun calculatePast_monthly_3() {
        val lastTime = LocalDate.now().minusDays(1).minusMonths(1)
        val nextTime = lastTime
        val payments = arrayListOf(Payment("Test", 1f, nextTime, lastTime, PaymentType.MONTHLY))
        val newCash = PaymentUtil.calculatePast( payments, 0f)
        assertEquals(1f, newCash)
    }

    @Test
    fun calculatePast_monthly_4() {
        val lastTime = LocalDate.now().minusDays(1)
        val nextTime = lastTime.minusMonths(1)
        val payments = arrayListOf(Payment("Test", 1f, nextTime, lastTime, PaymentType.MONTHLY))
        val newCash = PaymentUtil.calculatePast( payments, 0f)
        assertEquals(2f, newCash)
    }

    @Test
    fun calculatePast_monthly_5() {
        val lastTime = LocalDate.now().minusDays(1)
        val nextTime = lastTime.minusMonths(1).minusYears(1)
        val payments = arrayListOf(Payment("Test", 1f, nextTime, lastTime, PaymentType.MONTHLY))
        val newCash = PaymentUtil.calculatePast( payments, 0f)
        assertEquals(14f, newCash)
    }

    @Test
    fun calculatePast_monthly_6() {
        val lastTime = LocalDate.now()
        val nextTime = lastTime.minusMonths(1).minusYears(1)
        val payments = arrayListOf(Payment("Test", 1f, nextTime, lastTime, PaymentType.MONTHLY))
        val newCash = PaymentUtil.calculatePast( payments, 0f)
        assertEquals(13f, newCash)
    }

    @Test
    fun testPayment_1() {
        val today = LocalDate.now()
        val payments = ArrayList<Payment>()
        payments.add(Payment("", 1000f, today.withDayOfMonth(1).plusMonths(1), null, PaymentType.MONTHLY))
        payments.add(Payment("", -500f, today.withDayOfMonth(5).plusMonths(1), null, PaymentType.MONTHLY))
        val testPayment = Payment("", -500f, today.withDayOfMonth(3).plusMonths(1), null, PaymentType.MONTHLY)
        assertTrue(PaymentUtil.testPayment(testPayment, payments, 0f))
    }

    @Test
    fun testPayment_2() {
        val today = LocalDate.now()
        val payments = ArrayList<Payment>()
        payments.add(Payment("", 1000f, today.withDayOfMonth(1).plusMonths(1), null, PaymentType.MONTHLY))
        payments.add(Payment("", -500f, today.withDayOfMonth(5).plusMonths(1), null, PaymentType.MONTHLY))
        val testPayment = Payment("", -600f, today.withDayOfMonth(3).plusMonths(1), null, PaymentType.MONTHLY)
        assertFalse(PaymentUtil.testPayment(testPayment, payments, 0f))
    }

    @Test
    fun testPayment_3() {
        val today = LocalDate.now()
        val payments = ArrayList<Payment>()
        payments.add(Payment("", 1000f, today.withDayOfMonth(1).plusMonths(1), null, PaymentType.MONTHLY))
        val testPayment = Payment("", -12000f, today.withDayOfMonth(3).plusMonths(13), null, PaymentType.ONES)
        assertTrue(PaymentUtil.testPayment(testPayment, payments, 0f))
    }

    @Test
    fun testPayment_4() {
        val today = LocalDate.now()
        val payments = ArrayList<Payment>()
        payments.add(Payment("", 1000f, today.withDayOfMonth(1).plusMonths(1), null, PaymentType.MONTHLY))
        val testPayment = Payment("", -12001f, today.withDayOfMonth(3).plusMonths(12), null, PaymentType.ONES)
        assertFalse(PaymentUtil.testPayment(testPayment, payments, 0f))
    }

    @Test
    fun testPayment_5() {
        val today = LocalDate.now()
        val payments = ArrayList<Payment>()
        payments.add(Payment("", 1000f, today.withDayOfMonth(1).plusMonths(1), null, PaymentType.MONTHLY))
        payments.add(Payment("", -11008f, today.withDayOfMonth(3).plusMonths(12), null, PaymentType.ONES))
        val testPayment = Payment("", -500f, today.withDayOfMonth(5).plusMonths(6), null, PaymentType.ONES)
        assertTrue(PaymentUtil.testPayment(testPayment, payments, 0f))
    }

    @Test
    fun testPayment_6() {
        val today = LocalDate.now()
        val payments = ArrayList<Payment>()
        payments.add(Payment("", 1000f, today.withDayOfMonth(1).plusMonths(1), null, PaymentType.MONTHLY))
        payments.add(Payment("", -11008f, today.withDayOfMonth(3).plusMonths(12), null, PaymentType.ONES))
        val testPayment = Payment("", -1000f, today.withDayOfMonth(5).plusMonths(6), null, PaymentType.ONES)
        assertFalse(PaymentUtil.testPayment(testPayment, payments, 0f))
    }

    @Test
    fun testPayment_7() {
        val today = LocalDate.now()
        val payments = ArrayList<Payment>()
        payments.add(Payment("", 1000f, today.withDayOfMonth(1).plusMonths(1), null, PaymentType.MONTHLY))
        payments.add(Payment("", -11008f, today.withDayOfMonth(3).plusMonths(12), null, PaymentType.ONES))
        val testPayment = Payment("", -250f, today.withDayOfMonth(5).plusMonths(6), today.withDayOfMonth(5).plusMonths(10), PaymentType.MONTHLY)
        assertFalse(PaymentUtil.testPayment(testPayment, payments, 0f))
    }

    @Test
    fun testPayment_8() {
        val today = LocalDate.now()
        val payments = ArrayList<Payment>()
        payments.add(Payment("", 1000f, today.withDayOfMonth(1).plusMonths(1), null, PaymentType.MONTHLY))
        payments.add(Payment("", -11000f, today.withDayOfMonth(3).plusMonths(12), null, PaymentType.ONES))
        val testPayment = Payment("", -250f, today.withDayOfMonth(5).plusMonths(7), today.withDayOfMonth(5).plusMonths(10), PaymentType.MONTHLY)
        assertTrue(PaymentUtil.testPayment(testPayment, payments, 0f))
    }

    @Test
    fun testPayment_9() {
        val today = LocalDate.now()
        val payments = ArrayList<Payment>()
        payments.add(Payment("", 1000f, today.withDayOfMonth(1).plusMonths(1), null, PaymentType.MONTHLY))
        payments.add(Payment("", -1500f, today.withDayOfMonth(3).plusMonths(12), today.withDayOfMonth(3).plusMonths(15), PaymentType.MONTHLY))
        val testPayment = Payment("", -6000f, today.withDayOfMonth(5).plusMonths(6), null, PaymentType.ONES)
        assertTrue(PaymentUtil.testPayment(testPayment, payments, 0f))
    }

    @Test
    fun testPayment_10() {
        val today = LocalDate.now()
        val payments = ArrayList<Payment>()
        payments.add(Payment("", 1000f, today.withDayOfMonth(1).plusMonths(1), null, PaymentType.MONTHLY))
        payments.add(Payment("", -1500f, today.withDayOfMonth(3).plusMonths(12), today.withDayOfMonth(3).plusMonths(15), PaymentType.MONTHLY))
        val testPayment = Payment("", -2000f, today.withDayOfMonth(5).plusMonths(6), today.withDayOfMonth(5).plusMonths(8), PaymentType.MONTHLY)
        assertTrue(PaymentUtil.testPayment(testPayment, payments, 0f))
    }

    @Test
    fun testPayment_11() {
        val today = LocalDate.now()
        val payments = ArrayList<Payment>()
        payments.add(Payment("", 1000f, today.withDayOfMonth(1).plusMonths(1), null, PaymentType.MONTHLY))
        payments.add(Payment("", -500f, today.withDayOfMonth(5).plusMonths(1), null, PaymentType.MONTHLY))
        val testPayment = Payment("", -6000f, today.withDayOfMonth(3).plusMonths(12), null, PaymentType.ONES)
        assertTrue(PaymentUtil.testPayment(testPayment, payments, 0f))
    }

    @Test
    fun testPayment_12() {
        val today = LocalDate.now()
        val payments = ArrayList<Payment>()
        payments.add(Payment("", 1000f, today.withDayOfMonth(1).plusMonths(1), null, PaymentType.MONTHLY))
        payments.add(Payment("", -500f, today.withDayOfMonth(5).plusMonths(1), null, PaymentType.MONTHLY))
        val testPayment = Payment("", -2000f, today.withDayOfMonth(3).plusMonths(10), today.withDayOfMonth(3).plusMonths(12), PaymentType.MONTHLY)
        assertTrue(PaymentUtil.testPayment(testPayment, payments, 0f))
    }

    @Test
    fun testPayment_13() {
        val today = LocalDate.now()
        val payments = ArrayList<Payment>()
        payments.add(Payment("", 1000f, today.withDayOfMonth(1).plusMonths(1), null, PaymentType.MONTHLY))
        payments.add(Payment("", -500f, today.withDayOfMonth(5).plusMonths(1), null, PaymentType.MONTHLY))
        val testPayment = Payment("", -6001f, today.withDayOfMonth(3).plusMonths(12), null, PaymentType.ONES)
        assertFalse(PaymentUtil.testPayment(testPayment, payments, 0f))
    }

    @Test
    fun testPayment_14() {
        val today = LocalDate.now()
        val payments = ArrayList<Payment>()
        payments.add(Payment("", 1000f, today.withDayOfMonth(1).plusMonths(1), null, PaymentType.MONTHLY))
        payments.add(Payment("", -500f, today.withDayOfMonth(5).plusMonths(1), null, PaymentType.MONTHLY))
        val testPayment = Payment("", -1500.25f, today.withDayOfMonth(3).plusMonths(9), today.withDayOfMonth(3).plusMonths(12), PaymentType.MONTHLY)
        assertFalse(PaymentUtil.testPayment(testPayment, payments, 0f))
    }
}