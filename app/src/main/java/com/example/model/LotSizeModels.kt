package com.example.model

enum class ForexAppTab(val label: String, val subtitle: String) {
  JOURNAL("Jurnal", "Catatan Trading"),
  TP_SL("TP / SL", "SL & TP Targets"),
  LOT_SIZE("Lot Size", "Risk & Margin"),
  COMPOUNDING("Compounding", "Capital Growth")
}

data class InstrumentOption(
  val id: String,
  val name: String,
  val pipValuePerLot: Double
) {
  companion object {
    val ALL = listOf(
      InstrumentOption("xauusd", "XAUUSD (Gold)", 1.0),
      InstrumentOption("eurusd", "EURUSD", 10.0),
      InstrumentOption("gbpusd", "GBPUSD", 10.0),
      InstrumentOption("usdjpy", "USDJPY", 9.09),
      InstrumentOption("audusd", "AUDUSD", 10.0),
      InstrumentOption("gbpjpy", "GBPJPY", 9.09)
    )
  }
}

data class LotSizeResult(
  val lotSize: Double,
  val riskAmount: Double,
  val actualPipValue: Double,
  val referencePills: List<QuickLotRef>
)

data class QuickLotRef(
  val lot: Double,
  val pipValue: Double,
  val riskAmount: Double,
  val riskPercent: Double,
  val isCloseToCalculated: Boolean
)
