package dev.vizualjack.staypositive

import java.time.LocalDate
import java.util.*

data class Entry(var name: String?, var value: Float?, var startTime: LocalDate?, var type: EntryType?)