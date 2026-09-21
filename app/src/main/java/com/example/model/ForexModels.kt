package com.example.model

enum class OrderType(val label: String, val arrow: String) {
  BUY("BUY", "▲"),
  SELL("SELL", "▼")
}

data class CalculationResult(
  val ratio: String,
  val slPips: Double,
  val tpPips: Double,
  val slPrice: Double,
  val tpPrice: Double,
  val slText: String,
  val tpText: String
)

data class MarketPreset(
  val name: String,
  val defaultPrice: String,
  val pipValue: String
)
