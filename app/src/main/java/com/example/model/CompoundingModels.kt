package com.example.model

data class CompoundingDayRow(
  val day: Int,
  val startBalance: Double,
  val profit: Double,
  val withdrawn: Double,
  val endBalance: Double
)

data class CompoundingSummary(
  val finalBalance: Double,
  val totalProfit: Double,
  val totalWithdrawn: Double,
  val growthMultiple: Double,
  val isHighRiskWarning: Boolean
)
