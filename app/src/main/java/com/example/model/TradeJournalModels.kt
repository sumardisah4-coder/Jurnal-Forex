package com.example.model

import java.util.UUID

data class TradeEntry(
  val id: String = UUID.randomUUID().toString(),
  val date: String, // "YYYY-MM-DD"
  val result: String, // "win" or "loss"
  val pnl: Double,
  val pips: Double? = null,
  val rr: String = "",
  val pair: String = "EUR/USD",
  val session: String = "ASIA", // "ASIA", "LONDON", "NYC"
  val notes: String = "",
  val imageUri: String? = null
)

data class JournalMonthStats(
  val totalTrades: Int = 0,
  val netPnl: Double = 0.0,
  val totalPips: Double = 0.0,
  val capital: Double = 5.0,
  val balance: Double = 5.0,
  val growthPercent: Double = 0.0,
  val winRate: Int = 0
)

data class CalendarDayCell(
  val dateKey: String,
  val dayNumber: Int,
  val isCurrentMonth: Boolean,
  val isToday: Boolean,
  val trades: List<TradeEntry>
)
