package dev.vizualjack.staypositive

import java.text.DecimalFormat
import java.time.LocalDate

class Util {
    companion object {
        val decimalFormat = DecimalFormat("##.00")
        fun roundToCashLikeValue(value: Float): Float {
            var cash = decimalFormat.format(value).toFloat()
            if (cash > 9999999f) cash = 9999999f
            else if (cash < -9999999f) cash = -9999999f
            return cash
        }

        fun toNiceString(value: Float, withSpaces: Boolean): String {
            var asString = value.toString().replace("-", "").replace("+", "")
            var pointIndex = asString.indexOf('.')
            if (pointIndex == -1) pointIndex = asString.indexOf(',')
            // following 0
            val numberOfZeros = (asString.count()-1) - pointIndex
            if (numberOfZeros < 2) asString += "0"
            if (!withSpaces) return asString
            // spaces
            var curIndex = pointIndex
            while (curIndex > 0) {
                curIndex -= 3
                if (curIndex < 0) break
                val s1 = asString.substring(0, curIndex)
                val s2 = asString.substring(curIndex)
                asString = "$s1 $s2"
            }
            return asString
        }

        fun toNiceString(value: LocalDate): String {
            return "${double(value.dayOfMonth)}.${double(value.monthValue)}.${value.year}"
        }

        private fun double(value: Int): String {
            return double(value.toString())
        }

        private fun double(value: String): String {
            if (value.length < 2) return "0$value"
            return value
        }
    }
}