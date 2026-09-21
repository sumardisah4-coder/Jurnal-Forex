package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppTheme
import com.example.model.ForexAppTab
import com.example.model.OrderType
import com.example.model.TradeEntry
import org.json.JSONArray
import org.json.JSONObject

class ForexPreferences(context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("jurnal_forex_storage_v1", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_ACTIVE_TAB = "pref_active_tab"
    private const val KEY_THEME = "pref_theme"

    // TP / SL
    private const val KEY_OPEN_PRICE = "pref_open_price"
    private const val KEY_PIP_VALUE = "pref_pip_value"
    private const val KEY_ORDER_TYPE = "pref_order_type"
    private const val KEY_SELECTED_RATIOS = "pref_selected_ratios"
    private const val KEY_AVAILABLE_RATIOS = "pref_available_ratios"

    // Lot Size
    private const val KEY_LOT_BALANCE = "pref_lot_balance"
    private const val KEY_LOT_RISK_PCT = "pref_lot_risk_pct"
    private const val KEY_LOT_SL_PIPS = "pref_lot_sl_pips"
    private const val KEY_LOT_INSTRUMENT = "pref_lot_instrument"

    // Compounding
    private const val KEY_COMP_CAPITAL = "pref_comp_capital"
    private const val KEY_COMP_DAILY_PCT = "pref_comp_daily_pct"
    private const val KEY_COMP_DAYS = "pref_comp_days"
    private const val KEY_COMP_WITHDRAW_PCT = "pref_comp_withdraw_pct"

    // Trade Journal
    private const val KEY_JOURNAL_CAPITAL = "trade_capital"
    private const val KEY_JOURNAL_TRADES = "trade_journal_v1"
  }

  // Active Tab
  fun saveActiveTab(tab: ForexAppTab) {
    prefs.edit().putString(KEY_ACTIVE_TAB, tab.name).apply()
  }

  fun getActiveTab(): ForexAppTab {
    val name = prefs.getString(KEY_ACTIVE_TAB, ForexAppTab.JOURNAL.name)
    return try {
      ForexAppTab.valueOf(name ?: ForexAppTab.JOURNAL.name)
    } catch (_: Exception) {
      ForexAppTab.JOURNAL
    }
  }

  // Theme
  fun saveTheme(theme: AppTheme) {
    prefs.edit().putString(KEY_THEME, theme.name).apply()
  }

  fun getTheme(): AppTheme {
    val name = prefs.getString(KEY_THEME, AppTheme.CYBER_DARK.name)
    return try {
      AppTheme.valueOf(name ?: AppTheme.CYBER_DARK.name)
    } catch (_: Exception) {
      AppTheme.CYBER_DARK
    }
  }

  // TP / SL
  fun saveTpSlState(
    openPrice: String,
    pipValue: String,
    orderType: OrderType,
    selectedRatios: Set<String>,
    availableRatios: List<String>
  ) {
    prefs.edit()
      .putString(KEY_OPEN_PRICE, openPrice)
      .putString(KEY_PIP_VALUE, pipValue)
      .putString(KEY_ORDER_TYPE, orderType.name)
      .putStringSet(KEY_SELECTED_RATIOS, selectedRatios)
      .putString(KEY_AVAILABLE_RATIOS, availableRatios.joinToString(","))
      .apply()
  }

  fun getOpenPrice(): String = prefs.getString(KEY_OPEN_PRICE, "4000") ?: "4000"
  fun getPipValue(): String = prefs.getString(KEY_PIP_VALUE, "0.1") ?: "0.1"
  fun getOrderType(): OrderType {
    val name = prefs.getString(KEY_ORDER_TYPE, OrderType.BUY.name)
    return try {
      OrderType.valueOf(name ?: OrderType.BUY.name)
    } catch (_: Exception) {
      OrderType.BUY
    }
  }
  fun getSelectedRatios(): Set<String> {
    return prefs.getStringSet(KEY_SELECTED_RATIOS, null) ?: setOf("100:200", "200:400")
  }
  fun getAvailableRatios(): List<String>? {
    val raw = prefs.getString(KEY_AVAILABLE_RATIOS, null) ?: return null
    val list = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    return if (list.isNotEmpty()) list else null
  }

  // Lot Size
  fun saveLotSizeState(
    balance: String,
    riskPct: Float,
    slPips: String,
    instrumentId: String
  ) {
    prefs.edit()
      .putString(KEY_LOT_BALANCE, balance)
      .putFloat(KEY_LOT_RISK_PCT, riskPct)
      .putString(KEY_LOT_SL_PIPS, slPips)
      .putString(KEY_LOT_INSTRUMENT, instrumentId)
      .apply()
  }

  fun getLotBalance(): String = prefs.getString(KEY_LOT_BALANCE, "1000") ?: "1000"
  fun getLotRiskPct(): Float = prefs.getFloat(KEY_LOT_RISK_PCT, 1.0f)
  fun getLotSlPips(): String = prefs.getString(KEY_LOT_SL_PIPS, "20") ?: "20"
  fun getLotInstrumentId(): String = prefs.getString(KEY_LOT_INSTRUMENT, "xauusd") ?: "xauusd"

  // Compounding
  fun saveCompoundingState(
    capital: String,
    dailyProfitPct: String,
    days: String,
    withdrawPct: Float
  ) {
    prefs.edit()
      .putString(KEY_COMP_CAPITAL, capital)
      .putString(KEY_COMP_DAILY_PCT, dailyProfitPct)
      .putString(KEY_COMP_DAYS, days)
      .putFloat(KEY_COMP_WITHDRAW_PCT, withdrawPct)
      .apply()
  }

  fun getCompoundingCapital(): String = prefs.getString(KEY_COMP_CAPITAL, "5000") ?: "5000"
  fun getCompoundingDailyProfitPct(): String = prefs.getString(KEY_COMP_DAILY_PCT, "5") ?: "5"
  fun getCompoundingDays(): String = prefs.getString(KEY_COMP_DAYS, "22") ?: "22"
  fun getCompoundingWithdrawPct(): Float = prefs.getFloat(KEY_COMP_WITHDRAW_PCT, 0f)

  // Trade Journal
  fun saveJournalCapital(capital: Double) {
    prefs.edit().putString(KEY_JOURNAL_CAPITAL, capital.toString()).apply()
  }

  fun getJournalCapital(): Double {
    val raw = prefs.getString(KEY_JOURNAL_CAPITAL, "5.0")
    return raw?.toDoubleOrNull() ?: 5.0
  }

  fun saveJournalTrades(trades: List<TradeEntry>) {
    val array = JSONArray()
    for (t in trades) {
      val obj = JSONObject()
      obj.put("id", t.id)
      obj.put("date", t.date)
      obj.put("result", t.result)
      obj.put("pnl", t.pnl)
      if (t.pips != null) obj.put("pips", t.pips)
      obj.put("rr", t.rr)
      obj.put("pair", t.pair)
      obj.put("session", t.session)
      obj.put("notes", t.notes)
      if (t.imageUri != null) obj.put("image", t.imageUri)
      array.put(obj)
    }
    prefs.edit().putString(KEY_JOURNAL_TRADES, array.toString()).apply()
  }

  fun getJournalTrades(): List<TradeEntry> {
    val raw = prefs.getString(KEY_JOURNAL_TRADES, null)
    if (raw == null) {
      return defaultSampleTrades()
    }
    return try {
      val array = JSONArray(raw)
      val list = ArrayList<TradeEntry>(array.length())
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val pips = if (obj.has("pips") && !obj.isNull("pips")) obj.optDouble("pips") else null
        val img = if (obj.has("image") && !obj.isNull("image")) obj.optString("image") else null
        list.add(
          TradeEntry(
            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
            date = obj.optString("date", ""),
            result = obj.optString("result", "win"),
            pnl = obj.optDouble("pnl", 0.0),
            pips = pips,
            rr = obj.optString("rr", ""),
            pair = obj.optString("pair", "EUR/USD"),
            session = obj.optString("session", "ASIA"),
            notes = obj.optString("notes", ""),
            imageUri = img
          )
        )
      }
      list
    } catch (_: Exception) {
      defaultSampleTrades()
    }
  }

  private fun defaultSampleTrades(): List<TradeEntry> {
    return listOf(
      TradeEntry(id = "t1", date = "2026-09-01", result = "win", pnl = 26.00, pips = 13.0, rr = "1:2", pair = "EUR/USD", session = "ASIA", notes = "Buy on London/Asia sweep"),
      TradeEntry(id = "t2", date = "2026-09-02", result = "win", pnl = 17.00, pips = 8.5, rr = "1:1.5", pair = "GBP/USD", session = "ASIA", notes = "Trend continuation pullback"),
      TradeEntry(id = "t3", date = "2026-09-03", result = "win", pnl = 35.00, pips = 17.5, rr = "1:2.5", pair = "XAU/USD", session = "ASIA", notes = "Gold bounce at support"),
      TradeEntry(id = "t4", date = "2026-09-04", result = "win", pnl = 7.00, pips = 3.5, rr = "1:1", pair = "USD/JPY", session = "ASIA", notes = "Scalp quick profit"),
      TradeEntry(id = "t5", date = "2026-09-07", result = "win", pnl = 11.00, pips = 5.5, rr = "1:1.5", pair = "EUR/USD", session = "ASIA", notes = "Break of structure retest"),
      TradeEntry(id = "t6", date = "2026-09-08", result = "win", pnl = 16.00, pips = 8.0, rr = "1:2", pair = "GBP/USD", session = "ASIA", notes = "Bullish flag breakout"),
      TradeEntry(id = "t7", date = "2026-09-09", result = "loss", pnl = -35.00, pips = -15.0, rr = "1:1", pair = "XAU/USD", session = "LONDON", notes = "Hit stop loss on sudden spike"),
      TradeEntry(id = "t8", date = "2026-09-10", result = "win", pnl = 12.00, pips = 6.0, rr = "1:1.5", pair = "USD/JPY", session = "ASIA", notes = "Bounce off EMA"),
      TradeEntry(id = "t9", date = "2026-09-11", result = "win", pnl = 48.00, pips = 24.0, rr = "1:3", pair = "XAU/USD", session = "ASIA", notes = "Gold rally after news"),
      TradeEntry(id = "t10", date = "2026-09-14", result = "win", pnl = 14.00, pips = 7.0, rr = "1:2", pair = "EUR/USD", session = "ASIA", notes = "Morning breakout"),
      TradeEntry(id = "t11", date = "2026-09-15", result = "win", pnl = 8.00, pips = 4.0, rr = "1:1", pair = "USD/CAD", session = "ASIA", notes = "Range trading"),
      TradeEntry(id = "t12", date = "2026-09-16", result = "win", pnl = 40.00, pips = 20.0, rr = "1:2.5", pair = "XAU/USD", session = "ASIA", notes = "Gold momentum push"),
      TradeEntry(id = "t13", date = "2026-09-17", result = "win", pnl = 12.00, pips = 6.0, rr = "1:1.5", pair = "GBP/USD", session = "ASIA", notes = "Double bottom confirmation"),
      TradeEntry(id = "t14", date = "2026-09-18", result = "win", pnl = 20.00, pips = 10.0, rr = "1:2", pair = "EUR/USD", session = "ASIA", notes = "Friday closing profit"),
      TradeEntry(id = "t15", date = "2026-09-21", result = "win", pnl = 85.00, pips = 42.5, rr = "1:4", pair = "XAU/USD", session = "ASIA", notes = "High RR expansion setup")
    )
  }
}
