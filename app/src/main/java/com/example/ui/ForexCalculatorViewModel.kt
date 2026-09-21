package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ForexPreferences
import com.example.model.AppTheme
import com.example.model.CalendarDayCell
import com.example.model.CalculationResult
import com.example.model.CompoundingDayRow
import com.example.model.CompoundingSummary
import com.example.model.ForexAppTab
import com.example.model.InstrumentOption
import com.example.model.JournalMonthStats
import com.example.model.LotSizeResult
import com.example.model.MarketPreset
import com.example.model.OrderType
import com.example.model.QuickLotRef
import com.example.model.TradeEntry
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.ceil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForexUiState(
  val activeTab: ForexAppTab = ForexAppTab.JOURNAL,
  val currentTheme: AppTheme = AppTheme.CYBER_DARK,

  // --- TP / SL State ---
  val openPrice: String = "4000",
  val pipValue: String = "0.1",
  val orderType: OrderType = OrderType.BUY,
  val availableRatios: List<String> = listOf(
    "100:200",
    "200:200",
    "200:300",
    "200:400",
    "250:500",
    "300:400",
    "300:500"
  ),
  val selectedRatios: Set<String> = setOf("100:200", "200:400"),
  val results: List<CalculationResult> = emptyList(),
  val presets: List<MarketPreset> = listOf(
    MarketPreset("GOLD (XAU)", "4000", "0.1"),
    MarketPreset("EUR/USD", "1.0850", "0.0001"),
    MarketPreset("GBP/USD", "1.2950", "0.0001"),
    MarketPreset("NASDAQ", "20000", "1.0"),
    MarketPreset("BTC/USD", "65000", "1.0")
  ),
  val selectedPresetName: String = "GOLD (XAU)",

  // --- Lot Size Calculator State ---
  val selectedInstrument: InstrumentOption = InstrumentOption.ALL.first(),
  val lotBalance: String = "1000",
  val lotRiskPercent: Float = 1.0f,
  val lotStopLossPips: String = "20",
  val lotSizeResult: LotSizeResult = LotSizeResult(0.5, 10.0, 0.5, emptyList()),

  // --- Compounding Calculator State ---
  val compoundingCapital: String = "5000",
  val compoundingDailyProfitPct: String = "5",
  val compoundingDays: String = "22",
  val compoundingWithdrawPct: Float = 0f,
  val compoundingSummary: CompoundingSummary = CompoundingSummary(
    finalBalance = 14626.04,
    totalProfit = 9626.04,
    totalWithdrawn = 0.0,
    growthMultiple = 2.93,
    isHighRiskWarning = true
  ),
  val compoundingRows: List<CompoundingDayRow> = emptyList(),

  // --- Trade Journal State ---
  val journalCapital: Double = 5.0,
  val journalTrades: List<TradeEntry> = emptyList(),
  val journalSelectedYear: Int = 2026,
  val journalSelectedMonth: Int = 8, // 0-indexed: 8 = September
  val journalMonthName: String = "September 2026",
  val journalMonthStats: JournalMonthStats = JournalMonthStats(),
  val journalCalendarDays: List<CalendarDayCell> = emptyList(),
  val isCapitalModalOpen: Boolean = false,
  val capitalModalInput: String = "5.0",
  val isTradeModalOpen: Boolean = false,
  val isDayDetailModalOpen: Boolean = false,
  val selectedDayDetailKey: String? = null,
  val editingTradeId: String? = null,
  val tradeFormDate: String = "",
  val tradeFormResult: String = "win",
  val tradeFormPnl: String = "",
  val tradeFormPips: String = "",
  val tradeFormRr: String = "",
  val tradeFormPair: String = "XAUUSD",
  val tradeFormSession: String = "ASIA",
  val tradeFormNotes: String = "",
  val tradeFormImageUri: String? = null,

  // Shared UI feedback
  val copiedKey: String? = null,
  val feedbackMessage: String? = null
)

class ForexCalculatorViewModel(private val applicationContext: Context? = null) : ViewModel() {

  private val _uiState = MutableStateFlow(ForexUiState())
  val uiState: StateFlow<ForexUiState> = _uiState.asStateFlow()

  private var preferences: ForexPreferences? = null
  private var copyResetJob: Job? = null
  private var feedbackResetJob: Job? = null

  private val monthNamesId = listOf(
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
  )

  init {
    if (applicationContext != null) {
      preferences = ForexPreferences(applicationContext)
      loadFromPreferences()
    } else {
      val now = Calendar.getInstance()
      val y = now.get(Calendar.YEAR)
      val m = now.get(Calendar.MONTH)
      _uiState.update {
        it.copy(
          journalSelectedYear = y,
          journalSelectedMonth = m,
          journalMonthName = "${monthNamesId.getOrElse(m) { "" }} $y"
        )
      }
      recalculate()
      recalculateLotSize()
      recalculateCompounding()
      recalculateJournal()
    }
  }

  fun attachContext(context: Context) {
    if (preferences == null) {
      preferences = ForexPreferences(context.applicationContext)
      loadFromPreferences()
    }
  }

  private fun loadFromPreferences() {
    val prefs = preferences ?: return
    val savedTab = prefs.getActiveTab()
    val savedTheme = prefs.getTheme()
    val savedPreset = prefs.getSelectedPreset()
    val open = prefs.getOpenPrice()
    val pip = prefs.getPipValue()
    val order = prefs.getOrderType()
    val selRatios = prefs.getSelectedRatios()
    val availRatios = prefs.getAvailableRatios() ?: _uiState.value.availableRatios

    val lotBal = prefs.getLotBalance()
    val lotRisk = prefs.getLotRiskPct()
    val lotSl = prefs.getLotSlPips()
    val lotInstId = prefs.getLotInstrumentId()
    val inst = InstrumentOption.ALL.find { it.id == lotInstId } ?: InstrumentOption.ALL.first()

    val compCap = prefs.getCompoundingCapital()
    val compPct = prefs.getCompoundingDailyProfitPct()
    val compDays = prefs.getCompoundingDays()
    val compWith = prefs.getCompoundingWithdrawPct()

    val journalCap = prefs.getJournalCapital()
    val journalTrades = prefs.getJournalTrades()

    val now = Calendar.getInstance()
    val y = now.get(Calendar.YEAR)
    val m = now.get(Calendar.MONTH)

    _uiState.update {
      it.copy(
        activeTab = savedTab,
        currentTheme = savedTheme,
        selectedPresetName = savedPreset,
        openPrice = open,
        pipValue = pip,
        orderType = order,
        selectedRatios = selRatios,
        availableRatios = availRatios,
        lotBalance = lotBal,
        lotRiskPercent = lotRisk,
        lotStopLossPips = lotSl,
        selectedInstrument = inst,
        compoundingCapital = compCap,
        compoundingDailyProfitPct = compPct,
        compoundingDays = compDays,
        compoundingWithdrawPct = compWith,
        journalCapital = journalCap,
        journalTrades = journalTrades,
        journalSelectedYear = y,
        journalSelectedMonth = m,
        journalMonthName = "${monthNamesId.getOrElse(m) { "" }} $y"
      )
    }

    recalculate()
    recalculateLotSize()
    recalculateCompounding()
    recalculateJournal()
  }

  private fun saveToPreferences() {
    val prefs = preferences ?: return
    val s = _uiState.value
    prefs.saveActiveTab(s.activeTab)
    prefs.saveTheme(s.currentTheme)
    prefs.saveTpSlState(
      s.openPrice,
      s.pipValue,
      s.orderType,
      s.selectedRatios,
      s.availableRatios,
      s.selectedPresetName
    )
    prefs.saveLotSizeState(
      s.lotBalance,
      s.lotRiskPercent,
      s.lotStopLossPips,
      s.selectedInstrument.id
    )
    prefs.saveCompoundingState(
      s.compoundingCapital,
      s.compoundingDailyProfitPct,
      s.compoundingDays,
      s.compoundingWithdrawPct
    )
    prefs.saveJournalCapital(s.journalCapital)
    prefs.saveJournalTrades(s.journalTrades)
  }

  fun setActiveTab(tab: ForexAppTab) {
    _uiState.update { it.copy(activeTab = tab) }
    preferences?.saveActiveTab(tab)
  }

  fun setTheme(theme: AppTheme) {
    _uiState.update { it.copy(currentTheme = theme) }
    preferences?.saveTheme(theme)
  }

  // ==========================================
  // TP / SL METHODS
  // ==========================================

  fun onOpenPriceChanged(value: String) {
    _uiState.update { it.copy(openPrice = value) }
    recalculate()
    saveToPreferences()
  }

  fun onPipValueChanged(value: String) {
    _uiState.update { it.copy(pipValue = value) }
    recalculate()
    saveToPreferences()
  }

  fun setOrderType(order: OrderType) {
    _uiState.update { it.copy(orderType = order) }
    recalculate()
    saveToPreferences()
  }

  fun toggleRatio(ratio: String) {
    _uiState.update { state ->
      val newSelected = state.selectedRatios.toMutableSet()
      if (newSelected.contains(ratio)) {
        newSelected.remove(ratio)
      } else {
        newSelected.add(ratio)
      }
      state.copy(selectedRatios = newSelected)
    }
    recalculate()
    saveToPreferences()
  }

  fun applyPreset(preset: MarketPreset) {
    _uiState.update {
      it.copy(
        selectedPresetName = preset.name,
        openPrice = preset.defaultPrice,
        pipValue = preset.pipValue
      )
    }
    recalculate()
    saveToPreferences()
  }

  fun addCustomRatio(slPips: String, tpPips: String): Boolean {
    val sl = slPips.trim().toDoubleOrNull() ?: return false
    val tp = tpPips.trim().toDoubleOrNull() ?: return false
    if (sl <= 0 || tp <= 0) return false

    val ratioKey = "${formatNumber(sl)}:${formatNumber(tp)}"
    _uiState.update { state ->
      val updatedList = if (state.availableRatios.contains(ratioKey)) {
        state.availableRatios
      } else {
        state.availableRatios + ratioKey
      }
      state.copy(
        availableRatios = updatedList,
        selectedRatios = state.selectedRatios + ratioKey
      )
    }
    recalculate()
    saveToPreferences()
    return true
  }

  fun removeCustomRatio(ratio: String) {
    val defaultList = setOf("100:200", "200:200", "200:300", "200:400", "300:400", "300:500")
    if (ratio in defaultList) return
    _uiState.update { state ->
      state.copy(
        availableRatios = state.availableRatios - ratio,
        selectedRatios = state.selectedRatios - ratio
      )
    }
    recalculate()
    saveToPreferences()
  }

  fun calculateExplicitly() {
    recalculate()
    showFeedback("SL & TP diperbarui!")
  }

  // ==========================================
  // LOT SIZE CALCULATOR METHODS
  // ==========================================

  fun onInstrumentSelected(instrument: InstrumentOption) {
    _uiState.update { it.copy(selectedInstrument = instrument) }
    recalculateLotSize()
    saveToPreferences()
  }

  fun onLotBalanceChanged(value: String) {
    _uiState.update { it.copy(lotBalance = value) }
    recalculateLotSize()
    saveToPreferences()
  }

  fun saveLotBalanceExplicitly() {
    saveToPreferences()
    showFeedback("Balance akun tersimpan!")
  }

  fun onLotRiskPercentChanged(percent: Float) {
    _uiState.update { it.copy(lotRiskPercent = percent) }
    recalculateLotSize()
    saveToPreferences()
  }

  fun onLotStopLossPipsChanged(value: String) {
    _uiState.update { it.copy(lotStopLossPips = value) }
    recalculateLotSize()
    saveToPreferences()
  }

  private fun recalculateLotSize() {
    _uiState.update { state ->
      val balance = state.lotBalance.toDoubleOrNull() ?: 0.0
      val riskPct = (state.lotRiskPercent.toDouble() / 100.0)
      val sl = state.lotStopLossPips.toDoubleOrNull() ?: 1.0
      val pipVal = state.selectedInstrument.pipValuePerLot

      val riskAmt = balance * riskPct
      val lotSize = if (sl > 0 && pipVal > 0) riskAmt / (sl * pipVal) else 0.0
      val actualPipVal = lotSize * pipVal

      val refs = listOf(0.01, 0.05, 0.10, 0.25, 0.50, 1.00)
      val pills = refs.map { refLot ->
        val pip = refLot * pipVal
        val riskForLot = pip * sl
        val pct = if (balance > 0) (riskForLot / balance) * 100.0 else 0.0
        val isClose = abs(refLot - lotSize) < 0.005
        QuickLotRef(
          lot = refLot,
          pipValue = pip,
          riskAmount = riskForLot,
          riskPercent = pct,
          isCloseToCalculated = isClose
        )
      }

      val result = LotSizeResult(
        lotSize = lotSize,
        riskAmount = riskAmt,
        actualPipValue = actualPipVal,
        referencePills = pills
      )
      state.copy(lotSizeResult = result)
    }
  }

  // ==========================================
  // COMPOUNDING METHODS
  // ==========================================

  fun onCompoundingCapitalChanged(value: String) {
    _uiState.update { it.copy(compoundingCapital = value) }
    recalculateCompounding()
    saveToPreferences()
  }

  fun saveCompoundingCapitalExplicitly() {
    saveToPreferences()
    showFeedback("Modal awal compounding tersimpan!")
  }

  fun onCompoundingDailyProfitChanged(value: String) {
    _uiState.update { it.copy(compoundingDailyProfitPct = value) }
    recalculateCompounding()
    saveToPreferences()
  }

  fun setCompoundingDailyProfitPreset(pct: Double) {
    val formatted = if (pct == pct.toLong().toDouble()) pct.toLong().toString() else pct.toString()
    _uiState.update { it.copy(compoundingDailyProfitPct = formatted) }
    recalculateCompounding()
    saveToPreferences()
  }

  fun onCompoundingDaysChanged(value: String) {
    _uiState.update { it.copy(compoundingDays = value) }
    recalculateCompounding()
    saveToPreferences()
  }

  fun onCompoundingWithdrawPctChanged(pct: Float) {
    _uiState.update { it.copy(compoundingWithdrawPct = pct) }
    recalculateCompounding()
    saveToPreferences()
  }

  private fun recalculateCompounding() {
    _uiState.update { state ->
      val capital = state.compoundingCapital.toDoubleOrNull() ?: 0.0
      val pct = state.compoundingDailyProfitPct.toDoubleOrNull() ?: 0.0
      val daysRaw = state.compoundingDays.toIntOrNull() ?: 22
      val days = daysRaw.coerceIn(1, 365)
      val withdrawPct = state.compoundingWithdrawPct.toDouble().coerceIn(0.0, 100.0)

      var balance = capital
      var totalWithdrawn = 0.0
      var totalProfit = 0.0
      val rows = ArrayList<CompoundingDayRow>(days)

      for (d in 1..days) {
        val start = balance
        val profit = start * (pct / 100.0)
        val withdrawn = profit * (withdrawPct / 100.0)
        val reinvested = profit - withdrawn
        val end = start + reinvested
        totalWithdrawn += withdrawn
        totalProfit += profit
        rows.add(
          CompoundingDayRow(
            day = d,
            startBalance = start,
            profit = profit,
            withdrawn = withdrawn,
            endBalance = end
          )
        )
        balance = end
      }

      val multiple = if (capital > 0) balance / capital else 0.0
      val isHighRisk = pct >= 4.0

      val summary = CompoundingSummary(
        finalBalance = balance,
        totalProfit = totalProfit,
        totalWithdrawn = totalWithdrawn,
        growthMultiple = multiple,
        isHighRiskWarning = isHighRisk
      )

      state.copy(
        compoundingSummary = summary,
        compoundingRows = rows
      )
    }
  }

  // ==========================================
  // TRADE JOURNAL METHODS
  // ==========================================

  fun navigateMonth(delta: Int) {
    _uiState.update { state ->
      var y = state.journalSelectedYear
      var m = state.journalSelectedMonth + delta
      if (m < 0) {
        m = 11
        y -= 1
      } else if (m > 11) {
        m = 0
        y += 1
      }
      val mName = "${monthNamesId.getOrElse(m) { "" }} $y"
      state.copy(
        journalSelectedYear = y,
        journalSelectedMonth = m,
        journalMonthName = mName
      )
    }
    recalculateJournal()
  }

  fun openCapitalModal() {
    _uiState.update {
      it.copy(
        isCapitalModalOpen = true,
        capitalModalInput = String.format(Locale.US, "%.2f", it.journalCapital)
      )
    }
  }

  fun closeCapitalModal() {
    _uiState.update { it.copy(isCapitalModalOpen = false) }
  }

  fun onCapitalModalInputChanged(value: String) {
    _uiState.update { it.copy(capitalModalInput = value) }
  }

  fun saveCapital() {
    val input = _uiState.value.capitalModalInput.toDoubleOrNull()
    if (input != null && input >= 0) {
      _uiState.update {
        it.copy(
          journalCapital = input,
          isCapitalModalOpen = false
        )
      }
      recalculateJournal()
      saveToPreferences()
      showFeedback("Modal awal trading tersimpan: $${String.format(Locale.US, "%.2f", input)}")
    } else {
      showFeedback("Masukkan modal awal yang valid")
    }
  }

  fun openTradeModal(dateKey: String, tradeId: String? = null) {
    val existing = if (tradeId != null) {
      _uiState.value.journalTrades.find { it.id == tradeId }
    } else null

    _uiState.update {
      it.copy(
        isTradeModalOpen = true,
        editingTradeId = tradeId,
        tradeFormDate = dateKey,
        tradeFormResult = existing?.result ?: "win",
        tradeFormPnl = existing?.let { t -> String.format(Locale.US, "%.2f", t.pnl) } ?: "",
        tradeFormPips = existing?.pips?.let { p -> String.format(Locale.US, "%.1f", p) } ?: "",
        tradeFormRr = existing?.rr ?: "",
        tradeFormPair = existing?.pair ?: "XAUUSD",
        tradeFormSession = existing?.session ?: "ASIA",
        tradeFormNotes = existing?.notes ?: "",
        tradeFormImageUri = existing?.imageUri
      )
    }
  }

  fun closeTradeModal() {
    _uiState.update { it.copy(isTradeModalOpen = false, editingTradeId = null) }
  }

  fun openDayDetail(dateKey: String) {
    _uiState.update {
      it.copy(
        isDayDetailModalOpen = true,
        selectedDayDetailKey = dateKey
      )
    }
  }

  fun closeDayDetail() {
    _uiState.update {
      it.copy(
        isDayDetailModalOpen = false,
        selectedDayDetailKey = null
      )
    }
  }

  fun setTradeFormResult(result: String) {
    _uiState.update { it.copy(tradeFormResult = result) }
  }

  fun onTradeFormPnlChanged(value: String) {
    _uiState.update { it.copy(tradeFormPnl = value) }
  }

  fun onTradeFormPipsChanged(value: String) {
    _uiState.update { it.copy(tradeFormPips = value) }
  }

  fun onTradeFormRrChanged(value: String) {
    _uiState.update { it.copy(tradeFormRr = value) }
  }

  fun onTradeFormPairChanged(value: String) {
    _uiState.update { it.copy(tradeFormPair = value) }
  }

  fun setTradeFormSession(session: String) {
    _uiState.update { it.copy(tradeFormSession = session) }
  }

  fun onTradeFormNotesChanged(value: String) {
    _uiState.update { it.copy(tradeFormNotes = value) }
  }

  fun setTradeFormImageUri(uri: String?) {
    _uiState.update { it.copy(tradeFormImageUri = uri) }
  }

  fun saveTrade() {
    val s = _uiState.value
    val rawPnl = s.tradeFormPnl.toDoubleOrNull() ?: 0.0
    // If result is loss and user typed positive number, adjust sign
    val pnl = if (s.tradeFormResult == "loss" && rawPnl > 0) -rawPnl else rawPnl
    val pips = s.tradeFormPips.toDoubleOrNull()

    val trade = TradeEntry(
      id = s.editingTradeId ?: UUID.randomUUID().toString(),
      date = s.tradeFormDate,
      result = s.tradeFormResult,
      pnl = pnl,
      pips = pips,
      rr = s.tradeFormRr.trim(),
      pair = s.tradeFormPair.trim().ifEmpty { "EUR/USD" },
      session = s.tradeFormSession,
      notes = s.tradeFormNotes.trim(),
      imageUri = s.tradeFormImageUri
    )

    _uiState.update { state ->
      val updatedList = if (state.editingTradeId != null) {
        state.journalTrades.map { if (it.id == state.editingTradeId) trade else it }
      } else {
        state.journalTrades + trade
      }
      state.copy(
        journalTrades = updatedList,
        isTradeModalOpen = false,
        editingTradeId = null
      )
    }

    recalculateJournal()
    saveToPreferences()
    showFeedback("Trade berhasil disimpan!")
  }

  fun deleteCurrentEditingTrade() {
    val id = _uiState.value.editingTradeId ?: return
    deleteTrade(id)
    _uiState.update { it.copy(isTradeModalOpen = false, editingTradeId = null) }
  }

  fun deleteTrade(tradeId: String) {
    _uiState.update { state ->
      state.copy(journalTrades = state.journalTrades.filter { it.id != tradeId })
    }
    recalculateJournal()
    saveToPreferences()
    showFeedback("Trade telah dihapus")
  }

  private fun recalculateJournal() {
    _uiState.update { state ->
      val y = state.journalSelectedYear
      val m = state.journalSelectedMonth // 0-indexed

      val monthTrades = state.journalTrades.filter { t ->
        val parts = t.date.split("-")
        if (parts.size == 3) {
          val tradeY = parts[0].toIntOrNull() ?: 0
          val tradeM = (parts[1].toIntOrNull() ?: 1) - 1
          tradeY == y && tradeM == m
        } else false
      }

      val totalTrades = monthTrades.size
      val netPnl = monthTrades.sumOf { it.pnl }
      val totalPips = monthTrades.sumOf { it.pips ?: 0.0 }
      val wins = monthTrades.count { it.result == "win" }
      val winRate = if (totalTrades > 0) ((wins.toDouble() / totalTrades) * 100).toInt() else 0
      val capital = state.journalCapital
      val balance = capital + netPnl
      val growth = if (capital > 0.0) (netPnl / capital) * 100.0 else 0.0

      val stats = JournalMonthStats(
        totalTrades = totalTrades,
        netPnl = netPnl,
        totalPips = totalPips,
        capital = capital,
        balance = balance,
        growthPercent = growth,
        winRate = winRate
      )

      // Build Calendar Grid for Forex Trading Days: Monday - Friday (Senin - Jumat) only
      val todayCal = Calendar.getInstance()
      val todayY = todayCal.get(Calendar.YEAR)
      val todayM = todayCal.get(Calendar.MONTH)
      val todayD = todayCal.get(Calendar.DAY_OF_MONTH)

      val startCal = Calendar.getInstance()
      startCal.set(Calendar.YEAR, y)
      startCal.set(Calendar.MONTH, m)
      startCal.set(Calendar.DAY_OF_MONTH, 1)

      // Align startCal to the Monday of the starting week
      when (startCal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> {}
        Calendar.TUESDAY -> startCal.add(Calendar.DAY_OF_MONTH, -1)
        Calendar.WEDNESDAY -> startCal.add(Calendar.DAY_OF_MONTH, -2)
        Calendar.THURSDAY -> startCal.add(Calendar.DAY_OF_MONTH, -3)
        Calendar.FRIDAY -> startCal.add(Calendar.DAY_OF_MONTH, -4)
        Calendar.SATURDAY -> startCal.add(Calendar.DAY_OF_MONTH, 2)
        Calendar.SUNDAY -> startCal.add(Calendar.DAY_OF_MONTH, 1)
      }

      val cells = ArrayList<CalendarDayCell>()
      val currCal = startCal.clone() as Calendar

      while (true) {
        val weekCells = ArrayList<CalendarDayCell>(5)
        for (dayIndex in 0 until 5) {
          val cY = currCal.get(Calendar.YEAR)
          val cM = currCal.get(Calendar.MONTH)
          val cD = currCal.get(Calendar.DAY_OF_MONTH)
          val isCurrentMonth = (cY == y && cM == m)
          val dateKey = String.format(Locale.US, "%04d-%02d-%02d", cY, cM + 1, cD)

          val isToday = (cY == todayY && cM == todayM && cD == todayD)
          val dayTrades = state.journalTrades.filter { it.date == dateKey }

          weekCells.add(
            CalendarDayCell(
              dateKey = dateKey,
              dayNumber = cD,
              isCurrentMonth = isCurrentMonth,
              isToday = isToday,
              trades = dayTrades
            )
          )
          if (dayIndex < 4) {
            currCal.add(Calendar.DAY_OF_MONTH, 1)
          }
        }

        cells.addAll(weekCells)

        // Check if currCal (which is on Friday of this week) is past the month
        val isPastMonth = (currCal.get(Calendar.YEAR) > y ||
          (currCal.get(Calendar.YEAR) == y && currCal.get(Calendar.MONTH) > m))
        if (isPastMonth) {
          break
        }

        // Advance to next Monday: Friday + 3 days -> Monday
        currCal.add(Calendar.DAY_OF_MONTH, 3)
      }

      state.copy(
        journalMonthStats = stats,
        journalCalendarDays = cells
      )
    }
  }

  fun formatDisplayDate(dateKey: String): String {
    return try {
      val parts = dateKey.split("-")
      if (parts.size == 3) {
        val y = parts[0].toInt()
        val m = parts[1].toInt() - 1
        val d = parts[2].toInt()
        val cal = Calendar.getInstance()
        cal.set(y, m, d)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val days = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
        val dayName = days[dayOfWeek - 1]
        val mName = monthNamesId.getOrElse(m) { "" }
        "$dayName, $d $mName $y"
      } else dateKey
    } catch (_: Exception) {
      dateKey
    }
  }

  // ==========================================
  // CLIPBOARD & FORMATTING
  // ==========================================

  fun copyToClipboard(context: Context, key: String, value: String) {
    try {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      val clip = ClipData.newPlainText("Jurnal Forex $key", value)
      clipboard.setPrimaryClip(clip)

      copyResetJob?.cancel()
      _uiState.update { it.copy(copiedKey = key) }

      copyResetJob = viewModelScope.launch {
        delay(1200)
        _uiState.update { it.copy(copiedKey = null) }
      }
    } catch (_: Exception) {
      // Graceful fallback
    }
  }

  private fun showFeedback(msg: String) {
    feedbackResetJob?.cancel()
    _uiState.update { it.copy(feedbackMessage = msg) }
    feedbackResetJob = viewModelScope.launch {
      delay(2000)
      _uiState.update { it.copy(feedbackMessage = null) }
    }
  }

  private fun recalculate() {
    _uiState.update { state ->
      val open = state.openPrice.toDoubleOrNull()
      val pip = state.pipValue.toDoubleOrNull()

      if (open == null || pip == null || state.selectedRatios.isEmpty()) {
        state.copy(results = emptyList())
      } else {
        val orderedRatios = state.availableRatios.filter { it in state.selectedRatios }
        val calculated = orderedRatios.mapNotNull { ratio ->
          val parts = ratio.split(":")
          if (parts.size != 2) return@mapNotNull null
          val slPips = parts[0].trim().toDoubleOrNull() ?: return@mapNotNull null
          val tpPips = parts[1].trim().toDoubleOrNull() ?: return@mapNotNull null

          val slPrice = when (state.orderType) {
            OrderType.BUY -> open - (slPips * pip)
            OrderType.SELL -> open + (slPips * pip)
          }

          val tpPrice = when (state.orderType) {
            OrderType.BUY -> open + (tpPips * pip)
            OrderType.SELL -> open - (tpPips * pip)
          }

          CalculationResult(
            ratio = ratio,
            slPips = slPips,
            tpPips = tpPips,
            slPrice = slPrice,
            tpPrice = tpPrice,
            slText = formatPrice(slPrice, pip),
            tpText = formatPrice(tpPrice, pip)
          )
        }
        state.copy(results = calculated)
      }
    }
  }

  private fun formatPrice(price: Double, pip: Double): String {
    if (price.isNaN() || price.isInfinite()) return "—"
    val decimals = if (pip < 0.01) 4 else 2
    val formatted = String.format(Locale.US, "%.${decimals}f", price)
    val trimmed = formatted.trimEnd('0').trimEnd('.')
    return if (trimmed.isEmpty()) "0" else trimmed
  }

  private fun formatNumber(n: Double): String {
    return if (n == n.toLong().toDouble()) {
      n.toLong().toString()
    } else {
      n.toString()
    }
  }
}
