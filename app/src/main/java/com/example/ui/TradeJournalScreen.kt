package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.model.AppTheme
import com.example.model.CalendarDayCell
import com.example.model.TradeEntry
import java.util.Locale

// Custom Journal Color Palette
private val JournalBg = Color(0xFF09090D)
private val JournalPanel = Color(0xFF121218)
private val JournalLine = Color(0xFF282832)
private val JournalPink = Color(0xFFDF6B9D)
private val JournalGreen = Color(0xFF79E0A3)
private val JournalRed = Color(0xFFE7839E)
private val WinCardBg = Color(0xFF183A2A)
private val WinCardBorder = Color(0xFF315F48)
private val LossCardBg = Color(0xFF341B27)
private val LossCardBorder = Color(0xFF65334B)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TradeJournalScreen(
  viewModel: ForexCalculatorViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val theme = uiState.currentTheme
  val haptic = LocalHapticFeedback.current
  var themeDropdownExpanded by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(if (theme.isDark) JournalBg else theme.bgDark),
    contentAlignment = Alignment.TopCenter
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 6.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Brand & Header Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 750.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "PRIVATE · JOURNAL ✎",
            color = JournalPink,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Trade Journal",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            color = if (theme.isDark) Color(0xFFF2F0F5) else theme.textPrimary
          )
        }

        // Theme Switcher
        Box {
          IconButton(
            onClick = { themeDropdownExpanded = true },
            modifier = Modifier
              .clip(CircleShape)
              .background(if (theme.isDark) Color(0xFF14141A) else Color(0x15000000))
              .border(1.dp, JournalLine, CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.Palette,
              contentDescription = "Ganti Tema",
              tint = JournalPink
            )
          }

          DropdownMenu(
            expanded = themeDropdownExpanded,
            onDismissRequest = { themeDropdownExpanded = false },
            modifier = Modifier.background(JournalPanel)
          ) {
            AppTheme.entries.forEach { appTheme ->
              DropdownMenuItem(
                text = {
                  Text(
                    text = "${appTheme.iconEmoji} ${appTheme.label}",
                    color = if (appTheme == theme) JournalPink else Color(0xFFF2F0F5),
                    fontWeight = if (appTheme == theme) FontWeight.Bold else FontWeight.Normal
                  )
                },
                onClick = {
                  viewModel.setTheme(appTheme)
                  themeDropdownExpanded = false
                }
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Stats Summary Section
      val stats = uiState.journalMonthStats
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 750.dp)
      ) {
        // Row 1: Primary Stat Cards (Scrollable on small screens)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatCard(
            label = "TRADE BULAN INI",
            value = "${stats.totalTrades}",
            valueColor = if (theme.isDark) Color(0xFFF2F0F5) else theme.textPrimary
          )

          val pnlText = if (stats.netPnl >= 0) "+$${String.format(Locale.US, "%.2f", stats.netPnl)}"
          else "-$${String.format(Locale.US, "%.2f", Math.abs(stats.netPnl))}"
          StatCard(
            label = "NET P/L",
            value = pnlText,
            valueColor = if (stats.netPnl >= 0) JournalGreen else JournalRed
          )

          val pipsText = if (stats.totalPips >= 0) "+${String.format(Locale.US, "%.1f", stats.totalPips)}"
          else String.format(Locale.US, "%.1f", stats.totalPips)
          StatCard(
            label = "PIPS",
            value = pipsText,
            valueColor = if (stats.totalPips >= 0) JournalGreen else JournalRed
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Secondary Quick Pills
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Modal Pill (Clickable to change capital)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(999.dp))
              .background(Color(0xFF17171E))
              .border(1.dp, Color(0xFF30303A), RoundedCornerShape(999.dp))
              .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.openCapitalModal()
              }
              .padding(horizontal = 14.dp, vertical = 8.dp)
              .testTag("journal_modal_pill")
          ) {
            Text(
              text = "MODAL $${String.format(Locale.US, "%.2f", stats.capital)} ✎",
              color = JournalPink,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // Saldo Pill
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(999.dp))
              .background(Color(0xFF17171E))
              .border(1.dp, Color(0xFF30303A), RoundedCornerShape(999.dp))
              .padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Text(
              text = "SALDO $${String.format(Locale.US, "%.2f", stats.balance)}",
              color = Color(0xFFD9D7DD),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // Growth Pill
          val growthText = if (stats.growthPercent >= 0) "+${String.format(Locale.US, "%.1f", stats.growthPercent)}%"
          else "${String.format(Locale.US, "%.1f", stats.growthPercent)}%"
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(999.dp))
              .background(Color(0xFF17171E))
              .border(1.dp, Color(0xFF30303A), RoundedCornerShape(999.dp))
              .padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Text(
              text = "GROWTH $growthText",
              color = if (stats.growthPercent >= 0) JournalGreen else JournalRed,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // Win Rate Pill
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(999.dp))
              .background(Color(0xFF17171E))
              .border(1.dp, Color(0xFF30303A), RoundedCornerShape(999.dp))
              .padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Text(
              text = "WR ${stats.winRate}%",
              color = Color(0xFFD9D7DD),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Month Navigation Toolbar (Minimalist & Centered)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.navigateMonth(-1)
          },
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0xFF14141A))
            .border(1.dp, JournalLine, CircleShape)
            .testTag("journal_prev_month")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Bulan Sebelumnya",
            tint = Color(0xFFDCD8E2),
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Text(
          text = uiState.journalMonthName,
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Serif,
          color = if (theme.isDark) Color(0xFFF2F0F5) else theme.textPrimary
        )

        Spacer(modifier = Modifier.width(18.dp))

        IconButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.navigateMonth(1)
          },
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0xFF14141A))
            .border(1.dp, JournalLine, CircleShape)
            .testTag("journal_next_month")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Bulan Berikutnya",
            tint = Color(0xFFDCD8E2),
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Calendar Container (Minimalist, Wide)
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(
            elevation = 16.dp,
            shape = RoundedCornerShape(16.dp),
            ambientColor = Color.Black,
            spotColor = Color(0x66000000)
          ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D12)),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(JournalLine))
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Weekday Header (SEN, SEL, RAB, KAM, JUM - Pasar Forex Senin s/d Jumat)
          val weekDays = listOf("SEN", "SEL", "RAB", "KAM", "JUM")
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF121217))
              .border(
                width = 1.dp,
                color = JournalLine,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
              )
          ) {
            weekDays.forEach { dayName ->
              Box(
                modifier = Modifier
                  .weight(1f)
                  .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = dayName,
                  color = Color(0xFFA29EAC),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 0.8.sp
                )
              }
            }
          }

          // Calendar Grid (5 columns for business trading days)
          val cells = uiState.journalCalendarDays
          val chunkedRows = cells.chunked(5)

          Column(modifier = Modifier.fillMaxWidth()) {
            chunkedRows.forEach { rowCells ->
              Row(modifier = Modifier.fillMaxWidth()) {
                rowCells.forEach { cell ->
                  Box(modifier = Modifier.weight(1f)) {
                    CalendarCell(
                      cell = cell,
                      onCellClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.openDayDetail(dateKey = cell.dateKey)
                      },
                      onTradeClick = { trade ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.openDayDetail(dateKey = cell.dateKey)
                      }
                    )
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(96.dp))
    }
  }

  // --- Modal Atur Modal Awal ---
  if (uiState.isCapitalModalOpen) {
    Dialog(
      onDismissRequest = { viewModel.closeCapitalModal() },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0xBA000000))
          .padding(16.dp),
        contentAlignment = Alignment.Center
      ) {
        Card(
          modifier = Modifier
            .widthIn(max = 430.dp)
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(24.dp)),
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = JournalPanel),
          border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFF30303A)))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Atur Modal Awal",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color(0xFFF2F0F5)
              )
              IconButton(onClick = { viewModel.closeCapitalModal() }) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Tutup",
                  tint = Color(0xFFDDDDDD)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = "Modal awal (USD)",
              color = Color(0xFFAAA7B0),
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
              value = uiState.capitalModalInput,
              onValueChange = viewModel::onCapitalModalInputChanged,
              textStyle = TextStyle(
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              ),
              leadingIcon = {
                Text(
                  text = "$",
                  color = JournalPink,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold
                )
              },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JournalPink,
                unfocusedBorderColor = Color(0xFF303039),
                focusedContainerColor = Color(0xFF0C0C10),
                unfocusedContainerColor = Color(0xFF0C0C10)
              ),
              shape = RoundedCornerShape(14.dp),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Modal ini digunakan sebagai dasar perhitungan saldo dan Growth.",
              fontSize = 12.sp,
              color = Color(0xFF77747E)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Button(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.saveCapital()
              },
              colors = ButtonDefaults.buttonColors(containerColor = JournalPink),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
            ) {
              Text(
                text = "Simpan Modal",
                color = Color(0xFF160C11),
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
              )
            }
          }
        }
      }
    }
  }

  // --- Modal Trade (Add / Edit) ---
  if (uiState.isTradeModalOpen) {
    TradeFormDialog(
      uiState = uiState,
      viewModel = viewModel,
      onDismiss = { viewModel.closeTradeModal() }
    )
  }

  // --- Modal Detail Trade Harian (Sesuai Gambar 8.jpeg) ---
  val dayDetailKey = uiState.selectedDayDetailKey
  if (uiState.isDayDetailModalOpen && dayDetailKey != null) {
    DayDetailDialog(
      dateKey = dayDetailKey,
      uiState = uiState,
      viewModel = viewModel,
      onDismiss = { viewModel.closeDayDetail() }
    )
  }
}

@Composable
private fun StatCard(
  label: String,
  value: String,
  valueColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .widthIn(min = 140.dp)
      .shadow(12.dp, RoundedCornerShape(17.dp)),
    shape = RoundedCornerShape(17.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF121218)),
    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(JournalLine))
  ) {
    Column(
      modifier = Modifier
        .background(
          brush = Brush.linearGradient(
            colors = listOf(Color(0xFF15151B), Color(0xFF111117))
          )
        )
        .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
      Text(
        text = label,
        color = Color(0xFF9B98A2),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = valueColor,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

@Composable
private fun CalendarCell(
  cell: CalendarDayCell,
  onCellClick: () -> Unit,
  onTradeClick: (TradeEntry) -> Unit,
  modifier: Modifier = Modifier.fillMaxWidth()
) {
  val isOpacityDimmed = !cell.isCurrentMonth
  val bgColor = if (cell.isCurrentMonth) Color(0xFF0F0F14) else Color(0xFF08080C)

  Box(
    modifier = modifier
      .heightIn(min = 90.dp)
      .background(bgColor)
      .border(0.5.dp, Color(0xFF1F1F27))
      .clickable { onCellClick() }
      .padding(horizontal = 3.dp, vertical = 4.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.Top
    ) {
      // Top row: Day Number
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (cell.isToday) {
          Box(
            modifier = Modifier
              .size(23.dp)
              .clip(CircleShape)
              .background(Color(0xFFE887A9)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${cell.dayNumber}",
              color = Color(0xFF1E1018),
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Black
            )
          }
        } else {
          Text(
            text = "${cell.dayNumber}",
            color = if (isOpacityDimmed) Color(0xFF40404C) else Color(0xFFEDEBF2),
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 2.dp, top = 1.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Middle/Bottom: Total Day Summary Card (Total WIN/LOSS) OR "+ trade" prompt
      if (cell.trades.isEmpty()) {
        if (cell.isCurrentMonth) {
          Text(
            text = "+ trade",
            color = Color(0xFF4C4A56),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 2.dp, top = 4.dp)
          )
        }
      } else {
        // Compute Daily Net Total P/L
        val totalPnl = cell.trades.sumOf { it.pnl }
        val isWin = totalPnl >= 0
        val cardBg = if (isWin) Color(0xFF173827) else Color(0xFF381B27)
        val cardBorder = if (isWin) Color(0xFF27583D) else Color(0xFF5E2B3E)
        val resultColor = if (isWin) Color(0xFF56E396) else Color(0xFFF87171)

        val sessionName = cell.trades.lastOrNull()?.session?.uppercase(Locale.US) ?: "ASIA"
        val sessionBg = when (sessionName) {
          "LONDON" -> Color(0xFFA594F9)
          "NYC" -> Color(0xFF67B7F7)
          else -> Color(0xFFE887A9)
        }
        val sessionTextColor = Color(0xFF1D0E1A)

        val sign = if (totalPnl >= 0) "+" else "-"
        val absPnl = Math.abs(totalPnl)
        val pnlFormatted = "$sign$${String.format(Locale.US, "%.2f", absPnl)}"

        val pnlFontSize = when {
          pnlFormatted.length > 9 -> 9.5.sp
          pnlFormatted.length > 7 -> 10.5.sp
          else -> 11.5.sp
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
            .clickable { onCellClick() }
            .padding(horizontal = 2.dp, vertical = 4.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // WIN / LOSS label
            Text(
              text = if (isWin) "WIN" else "LOSS",
              fontSize = 10.sp,
              fontWeight = FontWeight.Black,
              color = resultColor,
              letterSpacing = 0.5.sp,
              maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Total Net PnL Amount (tidak ada yang terpotong)
            Text(
              text = pnlFormatted,
              fontSize = pnlFontSize,
              fontWeight = FontWeight.Black,
              color = Color.White,
              fontFamily = FontFamily.Monospace,
              maxLines = 1,
              softWrap = false
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Session badge pill
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(sessionBg)
                .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
              Text(
                text = sessionName,
                color = sessionTextColor,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DayDetailDialog(
  dateKey: String,
  uiState: ForexUiState,
  viewModel: ForexCalculatorViewModel,
  onDismiss: () -> Unit
) {
  val haptic = LocalHapticFeedback.current
  val dayTrades = uiState.journalTrades.filter { it.date == dateKey }
  val netPnl = dayTrades.sumOf { it.pnl }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xBA000000))
        .clickable { onDismiss() }
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      Card(
        modifier = Modifier
          .widthIn(max = 520.dp)
          .fillMaxWidth()
          .clickable(enabled = false) {}
          .shadow(30.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A)),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFF262634)))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          // Top Row: Net P/L, Date Title, Close Button (Matching Screenshot 8.jpeg)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            Column(modifier = Modifier.weight(1f)) {
              val netSign = if (netPnl > 0) "+" else if (netPnl < 0) "-" else ""
              val netColor = if (netPnl > 0) Color(0xFF56E396) else if (netPnl < 0) Color(0xFFF87171) else Color(0xFF888494)
              Text(
                text = "$netSign$${String.format(Locale.US, "%.2f", Math.abs(netPnl))} NET",
                color = netColor,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
              )

              Spacer(modifier = Modifier.height(3.dp))

              Text(
                text = viewModel.formatDisplayDate(dateKey),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color(0xFFF3F1F7)
              )
            }

            IconButton(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onDismiss()
              },
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF22222D))
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Tutup",
                tint = Color(0xFFDCD8E2),
                modifier = Modifier.size(16.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // List of Trade Cards for this day
          if (dayTrades.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Belum ada trade yang dicatat pada hari ini.",
                color = Color(0xFF726F7C),
                fontSize = 13.sp
              )
            }
          } else {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              dayTrades.forEach { trade ->
                val isWin = trade.result.lowercase(Locale.US) == "win"
                val cardBg = if (isWin) Color(0xFF173827) else Color(0xFF381B27)
                val cardBorder = if (isWin) Color(0xFF27583D) else Color(0xFF5E2B3E)
                val resultColor = if (isWin) Color(0xFF56E396) else Color(0xFFF87171)
                val sessionBg = when (trade.session.uppercase(Locale.US)) {
                  "LONDON" -> Color(0xFFA594F9)
                  "NYC" -> Color(0xFF67B7F7)
                  else -> Color(0xFFE887A9)
                }

                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                    .clickable {
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      viewModel.openTradeModal(dateKey, trade.id)
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                  Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                          text = if (isWin) "WIN" else "LOSS",
                          fontSize = 15.sp,
                          fontWeight = FontWeight.Black,
                          color = resultColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                          text = "$${String.format(Locale.US, "%.2f", Math.abs(trade.pnl))}",
                          fontSize = 15.5.sp,
                          fontWeight = FontWeight.ExtraBold,
                          color = Color.White,
                          fontFamily = FontFamily.Monospace
                        )
                      }

                      Row(verticalAlignment = Alignment.CenterVertically) {
                        // Session badge
                        Box(
                          modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(sessionBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                          Text(
                            text = trade.session.uppercase(Locale.US),
                            color = Color(0xFF1B0F18),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                          )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Small X to delete trade
                        IconButton(
                          onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deleteTrade(trade.id)
                          },
                          modifier = Modifier.size(24.dp)
                        ) {
                          Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hapus Trade",
                            tint = Color(0xFFA5A2AF),
                            modifier = Modifier.size(15.dp)
                          )
                        }
                      }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                      text = trade.pair,
                      fontSize = 13.5.sp,
                      fontWeight = FontWeight.Medium,
                      color = Color(0xFFD4D1DC)
                    )

                    if (trade.notes.isNotBlank() || trade.pips != null || trade.rr.isNotBlank()) {
                      Spacer(modifier = Modifier.height(4.dp))
                      val details = mutableListOf<String>()
                      trade.pips?.let { details.add("${if (it >= 0) "+" else ""}${it} pips") }
                      if (trade.rr.isNotBlank()) details.add("RR ${trade.rr}")
                      if (trade.notes.isNotBlank()) details.add(trade.notes)

                      Text(
                        text = details.joinToString(" • "),
                        fontSize = 11.5.sp,
                        color = Color(0xFFA8A5B2),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                      )
                    }
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // "+ Tambah trade lain" Button (Exactly as in Screenshot 8.jpeg)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF171722))
              .border(1.dp, Color(0xFF2C2C3C), RoundedCornerShape(12.dp))
              .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.openTradeModal(dateKey = dateKey, tradeId = null)
              }
              .padding(vertical = 13.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "+ Tambah trade lain",
              color = Color(0xFFDCD8E4),
              fontSize = 14.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TradeFormDialog(
  uiState: ForexUiState,
  viewModel: ForexCalculatorViewModel,
  onDismiss: () -> Unit
) {
  val haptic = LocalHapticFeedback.current
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      viewModel.setTradeFormImageUri(uri.toString())
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xBA000000))
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      Card(
        modifier = Modifier
          .widthIn(max = 600.dp)
          .fillMaxWidth()
          .heightIn(max = 750.dp)
          .shadow(30.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = JournalPanel),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFF30303A)))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
        ) {
          // Modal Head
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = viewModel.formatDisplayDate(uiState.tradeFormDate),
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Serif,
              color = Color(0xFFF2F0F5),
              modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Tutup",
                tint = Color(0xFFDDDDDD)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Result Toggle: Win / Loss
          Text(
            text = "Hasil Trade",
            color = Color(0xFFAAA7B0),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            val isWin = uiState.tradeFormResult == "win"
            val isLoss = uiState.tradeFormResult == "loss"

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(13.dp))
                .background(if (isWin) WinCardBg else Color(0xFF0D0D12))
                .border(
                  width = 1.dp,
                  color = if (isWin) Color(0xFF55BA80) else Color(0xFF303039),
                  shape = RoundedCornerShape(13.dp)
                )
                .clickable {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  viewModel.setTradeFormResult("win")
                }
                .padding(vertical = 12.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Win",
                fontWeight = FontWeight.ExtraBold,
                color = if (isWin) JournalGreen else Color(0xFFAAAAAA),
                fontSize = 15.sp
              )
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(13.dp))
                .background(if (isLoss) LossCardBg else Color(0xFF0D0D12))
                .border(
                  width = 1.dp,
                  color = if (isLoss) Color(0xFFA94C6C) else Color(0xFF303039),
                  shape = RoundedCornerShape(13.dp)
                )
                .clickable {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  viewModel.setTradeFormResult("loss")
                }
                .padding(vertical = 12.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Loss",
                fontWeight = FontWeight.ExtraBold,
                color = if (isLoss) JournalRed else Color(0xFFAAAAAA),
                fontSize = 15.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // PnL & Pips Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "P/L (USD)",
                color = Color(0xFFAAA7B0),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = uiState.tradeFormPnl,
                onValueChange = viewModel::onTradeFormPnlChanged,
                placeholder = { Text("$ 0.00", color = Color(0xFF55535D)) },
                textStyle = TextStyle(
                  color = Color.White,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = JournalPink,
                  unfocusedBorderColor = Color(0xFF303039),
                  focusedContainerColor = Color(0xFF0C0C10),
                  unfocusedContainerColor = Color(0xFF0C0C10)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Pips (opsional)",
                color = Color(0xFFAAA7B0),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = uiState.tradeFormPips,
                onValueChange = viewModel::onTradeFormPipsChanged,
                placeholder = { Text("0.0", color = Color(0xFF55535D)) },
                textStyle = TextStyle(
                  color = Color.White,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = JournalPink,
                  unfocusedBorderColor = Color(0xFF303039),
                  focusedContainerColor = Color(0xFF0C0C10),
                  unfocusedContainerColor = Color(0xFF0C0C10)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // RR & Pair Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "RR (opsional)",
                color = Color(0xFFAAA7B0),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = uiState.tradeFormRr,
                onValueChange = viewModel::onTradeFormRrChanged,
                placeholder = { Text("cth. 2", color = Color(0xFF55535D)) },
                textStyle = TextStyle(
                  color = Color.White,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold
                ),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = JournalPink,
                  unfocusedBorderColor = Color(0xFF303039),
                  focusedContainerColor = Color(0xFF0C0C10),
                  unfocusedContainerColor = Color(0xFF0C0C10)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Pair",
                color = Color(0xFFAAA7B0),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = uiState.tradeFormPair,
                onValueChange = viewModel::onTradeFormPairChanged,
                textStyle = TextStyle(
                  color = Color.White,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold
                ),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = JournalPink,
                  unfocusedBorderColor = Color(0xFF303039),
                  focusedContainerColor = Color(0xFF0C0C10),
                  unfocusedContainerColor = Color(0xFF0C0C10)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Session (ASIA, LONDON, NYC)
          Text(
            text = "Session",
            color = Color(0xFFAAA7B0),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val sessions = listOf("ASIA", "LONDON", "NYC")
            sessions.forEach { sessionName ->
              val isSelected = uiState.tradeFormSession == sessionName
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (isSelected) Color(0xFF2C1C26) else Color(0xFF0D0D12))
                  .border(
                    width = 1.dp,
                    color = if (isSelected) JournalPink else Color(0xFF303039),
                    shape = RoundedCornerShape(12.dp)
                  )
                  .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.setTradeFormSession(sessionName)
                  }
                  .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = sessionName,
                  color = if (isSelected) JournalPink else Color(0xFFAAAAAA),
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Notes / Catatan
          Text(
            text = "Catatan (opsional)",
            color = Color(0xFFAAA7B0),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = uiState.tradeFormNotes,
            onValueChange = viewModel::onTradeFormNotesChanged,
            placeholder = { Text("Setup, kesalahan, pelajaran...", color = Color(0xFF55535D)) },
            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = JournalPink,
              unfocusedBorderColor = Color(0xFF303039),
              focusedContainerColor = Color(0xFF0C0C10),
              unfocusedContainerColor = Color(0xFF0C0C10)
            ),
            shape = RoundedCornerShape(12.dp),
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Screenshot Chart (opsional)
          Text(
            text = "Screenshot chart (opsional)",
            color = Color(0xFFAAA7B0),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))

          if (uiState.tradeFormImageUri != null) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF303039), RoundedCornerShape(12.dp))
            ) {
              AsyncImage(
                model = uiState.tradeFormImageUri,
                contentDescription = "Chart Screenshot",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )

              IconButton(
                onClick = { viewModel.setTradeFormImageUri(null) },
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(4.dp)
                  .background(Color(0xAA000000), CircleShape)
                  .size(32.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Hapus Gambar",
                  tint = JournalRed,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          } else {
            OutlinedButton(
              onClick = {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = JournalPink),
              border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(Color(0xFF303039))),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(imageVector = Icons.Default.Image, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Pilih Gambar Screenshot Chart", fontSize = 13.sp)
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Save Button
          Button(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              viewModel.saveTrade()
            },
            colors = ButtonDefaults.buttonColors(containerColor = JournalPink),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
          ) {
            Text(
              text = "Simpan Trade",
              color = Color(0xFF160C11),
              fontWeight = FontWeight.Black,
              fontSize = 15.sp
            )
          }

          // Delete Button (if editing existing trade)
          if (uiState.editingTradeId != null) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.deleteCurrentEditingTrade()
              },
              colors = ButtonDefaults.outlinedButtonColors(contentColor = JournalRed),
              border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(Color(0xFF5D293D))),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
            ) {
              Text(
                text = "Hapus Trade",
                color = JournalRed,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Data tersimpan otomatis di perangkat ini (tersimpan permanen).",
            fontSize = 11.sp,
            color = Color(0xFF77747E),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }
}
