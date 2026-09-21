package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppTheme
import com.example.model.CompoundingDayRow
import java.util.Locale

@Composable
fun CompoundingScreen(
  viewModel: ForexCalculatorViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val theme = uiState.currentTheme
  val haptic = LocalHapticFeedback.current

  val summary = uiState.compoundingSummary
  val rows = uiState.compoundingRows

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(theme.bgDark),
    contentAlignment = Alignment.TopCenter
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 12.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 640.dp)
          .shadow(
            elevation = if (theme.isDark) 16.dp else 8.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = Color.Black,
            spotColor = if (theme.isDark) Color(0x66000000) else Color(0x1F000000)
          ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(theme.cardBorder))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              brush = Brush.verticalGradient(
                colors = listOf(theme.cardBgTop, theme.cardBgBottom)
              )
            )
            .padding(18.dp)
        ) {
          // Eyebrow & Title
          Text(
            text = "TRADING CAPITAL / LEDGER",
            fontFamily = FontFamily.Monospace,
            color = theme.goldColor,
            fontSize = 10.sp,
            letterSpacing = 1.8.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Sistem Compounding",
            fontFamily = FontFamily.Monospace,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Simulasi pertumbuhan modal berbasis target profit harian — lengkap dengan opsi withdraw dan batas risiko.",
            fontSize = 12.sp,
            color = theme.textMuted,
            lineHeight = 17.sp
          )

          Spacer(modifier = Modifier.height(16.dp))

          // PARAMETER PANEL
          Text(
            text = "PARAMETER",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = theme.textMuted
          )
          Spacer(modifier = Modifier.height(8.dp))

          // 2x2 Input Grid
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Modal Awal (USD)
            Column(modifier = Modifier.weight(1f)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Modal awal ($)",
                  fontSize = 11.sp,
                  color = theme.textMuted
                )
                Text(
                  text = "💾 Auto-save",
                  fontSize = 9.sp,
                  color = theme.accent
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = uiState.compoundingCapital,
                onValueChange = viewModel::onCompoundingCapitalChanged,
                textStyle = TextStyle(
                  color = theme.textPrimary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                ),
                trailingIcon = {
                  IconButton(
                    onClick = {
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      viewModel.saveCompoundingCapitalExplicitly()
                    }
                  ) {
                    Icon(
                      imageVector = Icons.Default.Save,
                      contentDescription = "Simpan Modal Awal",
                      tint = theme.accent,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = theme.accent,
                  unfocusedBorderColor = theme.cardBorder,
                  focusedContainerColor = if (theme.isDark) Color(0x1FFFFFFF) else Color(0x08000000),
                  unfocusedContainerColor = if (theme.isDark) Color(0x14FFFFFF) else Color(0x05000000)
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("comp_capital_input")
              )
            }

            // Target profit / hari (%)
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Target profit/hari (%)",
                fontSize = 11.sp,
                color = theme.textMuted
              )
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = uiState.compoundingDailyProfitPct,
                onValueChange = viewModel::onCompoundingDailyProfitChanged,
                textStyle = TextStyle(
                  color = theme.textPrimary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = theme.accent,
                  unfocusedBorderColor = theme.cardBorder,
                  focusedContainerColor = if (theme.isDark) Color(0x1FFFFFFF) else Color(0x08000000),
                  unfocusedContainerColor = if (theme.isDark) Color(0x14FFFFFF) else Color(0x05000000)
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("comp_pct_input")
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Jumlah hari trading
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Hari trading (1-365)",
                fontSize = 11.sp,
                color = theme.textMuted
              )
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = uiState.compoundingDays,
                onValueChange = viewModel::onCompoundingDaysChanged,
                textStyle = TextStyle(
                  color = theme.textPrimary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = theme.accent,
                  unfocusedBorderColor = theme.cardBorder,
                  focusedContainerColor = if (theme.isDark) Color(0x1FFFFFFF) else Color(0x08000000),
                  unfocusedContainerColor = if (theme.isDark) Color(0x14FFFFFF) else Color(0x05000000)
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("comp_days_input")
              )
            }

            // Withdraw profit / hari (%)
            Column(modifier = Modifier.weight(1f)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Withdraw / hari (%)",
                  fontSize = 11.sp,
                  color = theme.textMuted
                )
                Text(
                  text = "${uiState.compoundingWithdrawPct.toInt()}%",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = theme.accent,
                  fontFamily = FontFamily.Monospace
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Slider(
                value = uiState.compoundingWithdrawPct,
                onValueChange = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  viewModel.onCompoundingWithdrawPctChanged(it)
                },
                valueRange = 0f..100f,
                steps = 19, // step of 5%
                colors = SliderDefaults.colors(
                  thumbColor = theme.accent,
                  activeTrackColor = theme.accent,
                  inactiveTrackColor = theme.cardBorder
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Presets Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val presets = listOf(
              1.0 to "1% (konservatif)",
              2.0 to "2%",
              3.0 to "3%",
              5.0 to "5% (agresif)"
            )
            val currentPctVal = uiState.compoundingDailyProfitPct.toDoubleOrNull() ?: -1.0

            presets.forEach { (pct, label) ->
              val isSelected = (currentPctVal == pct)
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .background(
                    if (isSelected) theme.goldColor.copy(alpha = 0.15f)
                    else if (theme.isDark) Color(0x14FFFFFF)
                    else Color(0x0A000000)
                  )
                  .border(
                    width = 1.dp,
                    color = if (isSelected) theme.goldColor else theme.cardBorder,
                    shape = RoundedCornerShape(16.dp)
                  )
                  .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.setCompoundingDailyProfitPreset(pct)
                  }
                  .padding(horizontal = 8.dp, vertical = 5.dp)
              ) {
                Text(
                  text = label,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace,
                  color = if (isSelected) theme.goldColor else theme.textMuted,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              }
            }
          }

          // High Risk Warning Box
          AnimatedVisibility(visible = summary.isHighRiskWarning) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(theme.sellColor.copy(alpha = 0.10f))
                .border(1.dp, theme.sellColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .padding(10.dp)
            ) {
              Text(
                text = "Peringatan: target ${uiState.compoundingDailyProfitPct}%/hari secara konsisten sangat agresif dan sulit dipertahankan jangka panjang di pasar riil. Proyeksi di bawah adalah simulasi matematis.",
                color = theme.sellColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 15.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // RINGKASAN PANEL
          Text(
            text = "RINGKASAN",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = theme.textMuted
          )
          Spacer(modifier = Modifier.height(8.dp))

          // 2x2 Summary Cards Grid
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Modal Akhir
            SummaryCard(
              title = "MODAL AKHIR",
              value = formatCurrency(summary.finalBalance),
              valueColor = theme.goldColor,
              theme = theme,
              modifier = Modifier.weight(1f)
            )
            // Total Profit
            SummaryCard(
              title = "TOTAL PROFIT",
              value = "+${formatCurrency(summary.totalProfit)}",
              valueColor = theme.buyColor,
              theme = theme,
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Total Withdraw
            SummaryCard(
              title = "TOTAL WITHDRAW",
              value = formatCurrency(summary.totalWithdrawn),
              valueColor = theme.textPrimary,
              theme = theme,
              modifier = Modifier.weight(1f)
            )
            // Growth Multiple
            SummaryCard(
              title = "GROWTH MULTIPLE",
              value = String.format(Locale.US, "%.2fx", summary.growthMultiple),
              valueColor = theme.goldColor,
              theme = theme,
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          // KURVA PERTUMBUHAN MODAL (Canvas chart)
          Text(
            text = "KURVA PERTUMBUHAN MODAL",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = theme.textMuted
          )
          Spacer(modifier = Modifier.height(8.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(if (theme.isDark) Color(0x1FFFFFFF) else Color(0x06000000))
              .border(1.dp, theme.cardBorder, RoundedCornerShape(12.dp))
              .padding(8.dp)
          ) {
            CompoundingChart(
              rows = rows,
              capital = uiState.compoundingCapital.toDoubleOrNull() ?: 0.0,
              theme = theme
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          // LEDGER HARIAN (Table)
          Text(
            text = "LEDGER HARIAN",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = theme.textMuted
          )
          Spacer(modifier = Modifier.height(8.dp))

          // Table Header
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
              .background(if (theme.isDark) Color(0x22FFFFFF) else Color(0x0E000000))
              .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "Hari", fontSize = 10.sp, color = theme.textMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
            Text(text = "Awal", fontSize = 10.sp, color = theme.textMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
            Text(text = "Profit", fontSize = 10.sp, color = theme.textMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
            Text(text = "W/D", fontSize = 10.sp, color = theme.textMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text(text = "Akhir", fontSize = 10.sp, color = theme.textMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.3f))
          }

          // Scrollable table body
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 260.dp)
              .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
              .background(if (theme.isDark) Color(0x0EFFFFFF) else Color(0x04000000))
              .border(0.5.dp, theme.cardBorder, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
            ) {
              rows.forEach { row ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .border(0.2.dp, theme.cardBorder.copy(alpha = 0.3f))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "D${row.day}",
                    fontSize = 11.sp,
                    color = theme.textMuted,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(0.7f)
                  )
                  Text(
                    text = formatCurrencyCompact(row.startBalance),
                    fontSize = 11.sp,
                    color = theme.textPrimary,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.2f)
                  )
                  Text(
                    text = "+${formatCurrencyCompact(row.profit)}",
                    fontSize = 11.sp,
                    color = theme.buyColor,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.2f)
                  )
                  Text(
                    text = if (row.withdrawn > 0) formatCurrencyCompact(row.withdrawn) else "-",
                    fontSize = 11.sp,
                    color = if (row.withdrawn > 0) theme.sellColor else theme.textMuted,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                  )
                  Text(
                    text = formatCurrencyCompact(row.endBalance),
                    fontSize = 11.sp,
                    color = theme.goldColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.3f)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Footnote
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(if (theme.isDark) Color(0x0AFFFFFF) else Color(0x05000000))
              .padding(10.dp)
          ) {
            Text(
              text = "Catatan risiko: Target profit harian yang tinggi biasanya menuntut risiko per transaksi yang besar juga. Simulasi ini murni matematis (asumsi target tercapai setiap hari tanpa loss) — kondisi pasar nyata jarang sekonsisten ini. Gunakan sebagai referensi target, bukan jaminan hasil.",
              fontSize = 10.sp,
              color = theme.textMuted,
              lineHeight = 15.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(72.dp)) // Space for bottom bar
    }
  }
}

@Composable
private fun SummaryCard(
  title: String,
  value: String,
  valueColor: Color,
  theme: AppTheme,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(if (theme.isDark) Color(0x18FFFFFF) else Color(0x0A000000))
      .border(1.dp, theme.cardBorder, RoundedCornerShape(10.dp))
      .padding(horizontal = 10.dp, vertical = 8.dp)
  ) {
    Column {
      Text(
        text = title,
        fontSize = 9.sp,
        color = theme.textMuted,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        fontSize = 15.sp,
        color = valueColor,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

@Composable
private fun CompoundingChart(
  rows: List<CompoundingDayRow>,
  capital: Double,
  theme: AppTheme
) {
  if (rows.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(text = "Masukkan parameter untuk melihat kurva", color = theme.textMuted, fontSize = 11.sp)
    }
    return
  }

  val lineColor = theme.goldColor
  val gridColor = if (theme.isDark) Color(0x22FFFFFF) else Color(0x1A000000)

  Canvas(modifier = Modifier.fillMaxSize()) {
    val padL = 48.dp.toPx()
    val padR = 12.dp.toPx()
    val padT = 16.dp.toPx()
    val padB = 24.dp.toPx()

    val chartW = size.width - padL - padR
    val chartH = size.height - padT - padB

    if (chartW <= 0 || chartH <= 0) return@Canvas

    val maxVal = maxOf(rows.maxOfOrNull { it.endBalance } ?: capital, capital)
    val minVal = minOf(rows.minOfOrNull { it.startBalance } ?: capital, capital)
    val range = if (maxVal - minVal > 0) maxVal - minVal else 1.0

    val n = rows.size
    fun xPos(i: Int): Float = padL + (i.toFloat() / (n - 1).coerceAtLeast(1)) * chartW
    fun yPos(v: Double): Float = padT + (1f - ((v - minVal) / range).toFloat()) * chartH

    // 4 Horizontal Grid lines & values
    val textPaint = android.graphics.Paint().apply {
      color = android.graphics.Color.GRAY
      textSize = 9.dp.toPx()
      isAntiAlias = true
      textAlign = android.graphics.Paint.Align.RIGHT
    }

    for (g in 0..3) {
      val gVal = minVal + (range * g / 3.0)
      val gy = yPos(gVal)
      drawLine(
        color = gridColor,
        start = Offset(padL, gy),
        end = Offset(size.width - padR, gy),
        strokeWidth = 1.dp.toPx()
      )
      drawContext.canvas.nativeCanvas.drawText(
        formatCurrencyCompact(gVal),
        padL - 4.dp.toPx(),
        gy + 3.dp.toPx(),
        textPaint
      )
    }

    // Curve Path
    val path = Path()
    val areaPath = Path()

    path.moveTo(xPos(0), yPos(rows[0].startBalance))
    areaPath.moveTo(xPos(0), size.height - padB)
    areaPath.lineTo(xPos(0), yPos(rows[0].startBalance))

    rows.forEachIndexed { i, row ->
      val x = xPos(i)
      val y = yPos(row.endBalance)
      path.lineTo(x, y)
      areaPath.lineTo(x, y)
    }

    areaPath.lineTo(xPos(n - 1), size.height - padB)
    areaPath.close()

    // Draw gradient area
    drawPath(
      path = areaPath,
      brush = Brush.verticalGradient(
        colors = listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0f)),
        startY = padT,
        endY = size.height - padB
      )
    )

    // Draw Line
    drawPath(
      path = path,
      color = lineColor,
      style = Stroke(width = 2.5.dp.toPx())
    )

    // X-axis Day labels
    val step = (n / 5).coerceAtLeast(1)
    val dayPaint = android.graphics.Paint().apply {
      color = android.graphics.Color.GRAY
      textSize = 9.dp.toPx()
      isAntiAlias = true
      textAlign = android.graphics.Paint.Align.CENTER
    }

    rows.forEachIndexed { i, row ->
      if (i % step == 0 || i == n - 1) {
        val x = xPos(i)
        drawContext.canvas.nativeCanvas.drawText(
          "D${row.day}",
          x,
          size.height - 4.dp.toPx(),
          dayPaint
        )
      }
    }
  }
}

private fun formatCurrency(n: Double): String {
  return "$" + String.format(Locale.US, "%,.2f", n)
}

private fun formatCurrencyCompact(n: Double): String {
  return when {
    n >= 1_000_000 -> String.format(Locale.US, "$%.1fM", n / 1_000_000)
    n >= 1_000 -> String.format(Locale.US, "$%.1fk", n / 1_000)
    else -> String.format(Locale.US, "$%.0f", n)
  }
}
