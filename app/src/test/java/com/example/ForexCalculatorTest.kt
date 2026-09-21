package com.example

import com.example.model.OrderType
import com.example.ui.ForexCalculatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForexCalculatorTest {

  @Test
  fun testBuyOrderCalculation() {
    val viewModel = ForexCalculatorViewModel()
    // Default open: 4000, pip: 0.1, selected: 100:200, 200:400
    // BUY:
    // 100:200 -> SL = 4000 - 100*0.1 = 3990
    //            TP = 4000 + 200*0.1 = 4020
    // 200:400 -> SL = 4000 - 200*0.1 = 3980
    //            TP = 4000 + 400*0.1 = 4040

    val state = viewModel.uiState.value
    assertEquals(2, state.results.size)

    val res1 = state.results.first { it.ratio == "100:200" }
    assertEquals("3990", res1.slText)
    assertEquals("4020", res1.tpText)

    val res2 = state.results.first { it.ratio == "200:400" }
    assertEquals("3980", res2.slText)
    assertEquals("4040", res2.tpText)
  }

  @Test
  fun testSellOrderCalculation() {
    val viewModel = ForexCalculatorViewModel()
    viewModel.setOrderType(OrderType.SELL)

    // SELL:
    // 100:200 -> SL = 4000 + 100*0.1 = 4010
    //            TP = 4000 - 200*0.1 = 3980
    // 200:400 -> SL = 4000 + 200*0.1 = 4020
    //            TP = 4000 - 400*0.1 = 3960

    val state = viewModel.uiState.value
    val res1 = state.results.first { it.ratio == "100:200" }
    assertEquals("4010", res1.slText)
    assertEquals("3980", res1.tpText)

    val res2 = state.results.first { it.ratio == "200:400" }
    assertEquals("4020", res2.slText)
    assertEquals("3960", res2.tpText)
  }

  @Test
  fun testToggleRatio() {
    val viewModel = ForexCalculatorViewModel()
    // Unselect 100:200
    viewModel.toggleRatio("100:200")
    var state = viewModel.uiState.value
    assertEquals(1, state.results.size)
    assertEquals("200:400", state.results[0].ratio)

    // Select 300:500
    viewModel.toggleRatio("300:500")
    state = viewModel.uiState.value
    assertEquals(2, state.results.size)
    assertTrue(state.results.any { it.ratio == "300:500" })
  }

  @Test
  fun testAddCustomRatio() {
    val viewModel = ForexCalculatorViewModel()
    val added = viewModel.addCustomRatio("150", "350")
    assertTrue(added)

    val state = viewModel.uiState.value
    assertTrue(state.availableRatios.contains("150:350"))
    assertTrue(state.results.any { it.ratio == "150:350" })
  }

  @Test
  fun testThemeSelection() {
    val viewModel = ForexCalculatorViewModel()
    assertEquals(com.example.model.AppTheme.CYBER_DARK, viewModel.uiState.value.currentTheme)

    viewModel.setTheme(com.example.model.AppTheme.LIGHT_PRO)
    assertEquals(com.example.model.AppTheme.LIGHT_PRO, viewModel.uiState.value.currentTheme)

    viewModel.setTheme(com.example.model.AppTheme.GOLD_TRADER)
    assertEquals(com.example.model.AppTheme.GOLD_TRADER, viewModel.uiState.value.currentTheme)

    viewModel.setTheme(com.example.model.AppTheme.EMERALD_MINT)
    assertEquals(com.example.model.AppTheme.EMERALD_MINT, viewModel.uiState.value.currentTheme)
  }

  @Test
  fun testRatio250to500AvailableAndCalculated() {
    val viewModel = ForexCalculatorViewModel()
    assertTrue(viewModel.uiState.value.availableRatios.contains("250:500"))

    // Select 250:500
    viewModel.toggleRatio("250:500")
    val state = viewModel.uiState.value
    assertTrue(state.selectedRatios.contains("250:500"))

    // BUY: open 4000, pip 0.1
    // SL = 4000 - 250*0.1 = 3975
    // TP = 4000 + 500*0.1 = 4050
    val result = state.results.firstOrNull { it.ratio == "250:500" }
    org.junit.Assert.assertNotNull(result)
    assertEquals("3975", result?.slText)
    assertEquals("4050", result?.tpText)
  }

  @Test
  fun testNasdaqPresetApplication() {
    val viewModel = ForexCalculatorViewModel()
    val nasdaqPreset = viewModel.uiState.value.presets.firstOrNull { it.name == "NASDAQ" }
    org.junit.Assert.assertNotNull(nasdaqPreset)

    viewModel.applyPreset(nasdaqPreset!!)
    val state = viewModel.uiState.value
    assertEquals("20000", state.openPrice)
    assertEquals("1.0", state.pipValue)
  }

  @Test
  fun testLotSizeCalculation() {
    val viewModel = ForexCalculatorViewModel()
    // Default: balance = 1000, risk = 1.0% ($10 risk), sl = 20 pips, gold (pipVal = 1.0)
    // lotSize = 10 / (20 * 1.0) = 0.5
    val lotRes = viewModel.uiState.value.lotSizeResult
    assertEquals(0.5, lotRes.lotSize, 0.001)
    assertEquals(10.0, lotRes.riskAmount, 0.001)
    assertEquals(0.5, lotRes.actualPipValue, 0.001)

    // Check with EURUSD (pipVal = 10.0)
    val eurusd = com.example.model.InstrumentOption.ALL.first { it.id == "eurusd" }
    viewModel.onInstrumentSelected(eurusd)
    // lotSize = 10 / (20 * 10.0) = 0.05
    val eurRes = viewModel.uiState.value.lotSizeResult
    assertEquals(0.05, eurRes.lotSize, 0.001)
    assertEquals(10.0, eurRes.riskAmount, 0.001)
    assertEquals(0.5, eurRes.actualPipValue, 0.001)
  }

  @Test
  fun testCompoundingCalculation() {
    val viewModel = ForexCalculatorViewModel()
    // Default: capital = 5000, daily pct = 5%, days = 22, withdraw = 0%
    // 5000 * (1.05)^22 = 14626.30
    val compSummary = viewModel.uiState.value.compoundingSummary
    assertEquals(22, viewModel.uiState.value.compoundingRows.size)
    assertEquals(14626.30, compSummary.finalBalance, 0.5)
    assertEquals(9626.30, compSummary.totalProfit, 0.5)
    assertEquals(0.0, compSummary.totalWithdrawn, 0.001)
    assertEquals(2.93, compSummary.growthMultiple, 0.02)
    assertTrue(compSummary.isHighRiskWarning)

    // Test conservative preset (1%/day)
    viewModel.setCompoundingDailyProfitPreset(1.0)
    val updatedSummary = viewModel.uiState.value.compoundingSummary
    org.junit.Assert.assertFalse(updatedSummary.isHighRiskWarning)
  }

  @Test
  fun testTabSwitching() {
    val viewModel = ForexCalculatorViewModel()
    assertEquals(com.example.model.ForexAppTab.JOURNAL, viewModel.uiState.value.activeTab)

    viewModel.setActiveTab(com.example.model.ForexAppTab.TP_SL)
    assertEquals(com.example.model.ForexAppTab.TP_SL, viewModel.uiState.value.activeTab)

    viewModel.setActiveTab(com.example.model.ForexAppTab.LOT_SIZE)
    assertEquals(com.example.model.ForexAppTab.LOT_SIZE, viewModel.uiState.value.activeTab)

    viewModel.setActiveTab(com.example.model.ForexAppTab.COMPOUNDING)
    assertEquals(com.example.model.ForexAppTab.COMPOUNDING, viewModel.uiState.value.activeTab)
  }

  @Test
  fun testTradeJournalAddTradeAndStats() {
    val viewModel = ForexCalculatorViewModel()
    val y = viewModel.uiState.value.journalSelectedYear
    val m = viewModel.uiState.value.journalSelectedMonth
    val dateKey = String.format(java.util.Locale.US, "%04d-%02d-15", y, m + 1)

    // Open trade modal for day 15
    viewModel.openTradeModal(dateKey)
    viewModel.setTradeFormResult("win")
    viewModel.onTradeFormPnlChanged("25.50")
    viewModel.onTradeFormPipsChanged("12.0")
    viewModel.onTradeFormPairChanged("XAU/USD")
    viewModel.setTradeFormSession("LONDON")
    viewModel.saveTrade()

    val state = viewModel.uiState.value
    assertEquals(1, state.journalTrades.size)
    assertEquals(1, state.journalMonthStats.totalTrades)
    assertEquals(25.50, state.journalMonthStats.netPnl, 0.001)
    assertEquals(12.0, state.journalMonthStats.totalPips, 0.001)
    assertEquals(100, state.journalMonthStats.winRate)
    assertEquals(30.50, state.journalMonthStats.balance, 0.001) // 5.0 capital + 25.50 profit
  }

  @Test
  fun testFiveDayTradingCalendarGridAndDayDetail() {
    val viewModel = ForexCalculatorViewModel()
    val state = viewModel.uiState.value

    // Calendar grid should be multiple of 5 (Monday to Friday only)
    assertTrue(state.journalCalendarDays.isNotEmpty())
    assertEquals(0, state.journalCalendarDays.size % 5)

    // Verify day detail open and close
    viewModel.openDayDetail("2026-09-07")
    assertTrue(viewModel.uiState.value.isDayDetailModalOpen)
    assertEquals("2026-09-07", viewModel.uiState.value.selectedDayDetailKey)

    // Add another trade on this date
    viewModel.openTradeModal("2026-09-07", null)
    assertTrue(viewModel.uiState.value.isTradeModalOpen)
    assertEquals("2026-09-07", viewModel.uiState.value.tradeFormDate)

    viewModel.closeDayDetail()
    org.junit.Assert.assertFalse(viewModel.uiState.value.isDayDetailModalOpen)
  }
}
