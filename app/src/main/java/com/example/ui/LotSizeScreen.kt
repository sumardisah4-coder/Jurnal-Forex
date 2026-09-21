package com.example.ui

import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.model.InstrumentOption
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LotSizeScreen(
  viewModel: ForexCalculatorViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val theme = uiState.currentTheme
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current

  val result = uiState.lotSizeResult
  var instrumentDropdownExpanded by remember { mutableStateOf(false) }

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
      // Main Card Container
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 620.dp)
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
          // Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "RISK MANAGEMENT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = theme.goldColor,
                fontFamily = FontFamily.Monospace
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Kalkulator Lot Size",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
              )
            }

            // Pip Value Badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(theme.accent.copy(alpha = 0.12f))
                .border(1.dp, theme.accent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "1.0 Lot = $${String.format(Locale.US, "%.2f", uiState.selectedInstrument.pipValuePerLot)}",
                color = theme.accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Instrument Selector & Pip Value Display
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Dropdown button for instrument
            Box(
              modifier = Modifier
                .weight(1.3f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (theme.isDark) Color(0x22FFFFFF) else Color(0x0A000000))
                .border(1.dp, theme.cardBorder, RoundedCornerShape(12.dp))
                .clickable { instrumentDropdownExpanded = true }
                .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
              Column {
                Text(
                  text = "INSTRUMEN",
                  fontSize = 10.sp,
                  color = theme.textMuted,
                  fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = uiState.selectedInstrument.name,
                    color = theme.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Pilih Instrumen",
                    tint = theme.accent
                  )
                }
              }

              DropdownMenu(
                expanded = instrumentDropdownExpanded,
                onDismissRequest = { instrumentDropdownExpanded = false },
                modifier = Modifier.background(theme.cardBgTop)
              ) {
                InstrumentOption.ALL.forEach { instrument ->
                  DropdownMenuItem(
                    text = {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                        Text(
                          text = instrument.name,
                          color = theme.textPrimary,
                          fontWeight = if (instrument.id == uiState.selectedInstrument.id) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                          text = "$${String.format(Locale.US, "%.2f", instrument.pipValuePerLot)}/lot",
                          color = theme.textMuted,
                          fontSize = 12.sp,
                          fontFamily = FontFamily.Monospace
                        )
                      }
                    },
                    onClick = {
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      viewModel.onInstrumentSelected(instrument)
                      instrumentDropdownExpanded = false
                    }
                  )
                }
              }
            }

            // Pip Value Per 1.0 Lot Card
            Box(
              modifier = Modifier
                .weight(0.9f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (theme.isDark) Color(0x22FFFFFF) else Color(0x0A000000))
                .border(1.dp, theme.cardBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
              Column {
                Text(
                  text = "PIP VALUE (1.0 LOT)",
                  fontSize = 9.sp,
                  color = theme.textMuted,
                  fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "$${String.format(Locale.US, "%.2f", uiState.selectedInstrument.pipValuePerLot)}",
                  color = theme.goldColor,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Balance Input
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Balance Akun (USD)",
                color = theme.textMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
              Text(
                text = "💾 Auto-save aktif",
                color = theme.accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedTextField(
                value = uiState.lotBalance,
                onValueChange = viewModel::onLotBalanceChanged,
                textStyle = TextStyle(
                  color = theme.textPrimary,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                ),
                leadingIcon = {
                  Text(
                    text = "$",
                    color = theme.textMuted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                  )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = theme.accent,
                  unfocusedBorderColor = theme.cardBorder,
                  focusedContainerColor = if (theme.isDark) Color(0x1FFFFFFF) else Color(0x08000000),
                  unfocusedContainerColor = if (theme.isDark) Color(0x14FFFFFF) else Color(0x05000000)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                  .weight(1f)
                  .testTag("lot_balance_input")
              )

              Button(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  viewModel.saveLotBalanceExplicitly()
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.accent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .height(52.dp)
                  .testTag("lot_balance_save_btn")
              ) {
                Text(
                  text = "Simpan",
                  color = theme.bgDark,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Risk Slider + Stop Loss Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Risk Card with Slider
            Box(
              modifier = Modifier
                .weight(1.2f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (theme.isDark) Color(0x1FFFFFFF) else Color(0x08000000))
                .border(1.dp, theme.cardBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Risk Toleransi",
                    color = theme.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                  )
                  Text(
                    text = String.format(Locale.US, "%.2f%%", uiState.lotRiskPercent),
                    color = theme.accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                }

                Slider(
                  value = uiState.lotRiskPercent,
                  onValueChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onLotRiskPercentChanged(it)
                  },
                  valueRange = 0.1f..5.0f,
                  colors = SliderDefaults.colors(
                    thumbColor = theme.accent,
                    activeTrackColor = theme.accent,
                    inactiveTrackColor = theme.cardBorder
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lot_risk_slider")
                )

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = "0.1%", fontSize = 9.sp, color = theme.textMuted)
                  Text(text = "2.5%", fontSize = 9.sp, color = theme.textMuted)
                  Text(text = "5.0%", fontSize = 9.sp, color = theme.textMuted)
                }
              }
            }

            // Stop Loss Input
            Column(modifier = Modifier.weight(0.9f)) {
              Text(
                text = "Stop Loss (Pips)",
                color = theme.textMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = uiState.lotStopLossPips,
                onValueChange = viewModel::onLotStopLossPipsChanged,
                textStyle = TextStyle(
                  color = theme.textPrimary,
                  fontSize = 16.sp,
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
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("lot_sl_pips_input")
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // ==========================================
          // RESULTS GRID (Lot Size, Risk $, Pip Value)
          // ==========================================
          Text(
            text = "HASIL KALKULASI",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = theme.textMuted
          )
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Main Accent Card: LOT SIZE
            val lotText = String.format(Locale.US, "%.2f", result.lotSize)
            val isLotCopied = uiState.copiedKey == "lot_size"

            Card(
              modifier = Modifier
                .weight(1.2f)
                .testTag("result_lot_size_card")
                .clickable {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  viewModel.copyToClipboard(context, "lot_size", lotText)
                },
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = theme.accent.copy(alpha = if (theme.isDark) 0.22f else 0.12f)
              ),
              border = CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(theme.accent.copy(alpha = 0.6f))
              )
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Text(
                    text = "Lot Size",
                    fontSize = 11.sp,
                    color = theme.accent,
                    fontWeight = FontWeight.SemiBold
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                    imageVector = if (isLotCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = "Copy Lot Size",
                    tint = theme.accent,
                    modifier = Modifier.size(12.dp)
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = lotText,
                  fontSize = 24.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = theme.accent,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = if (isLotCopied) "Tersalin!" else "Tap untuk salin",
                  fontSize = 9.sp,
                  color = theme.accent.copy(alpha = 0.8f)
                )
              }
            }

            // Card 2: Risk ($)
            Card(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (theme.isDark) Color(0x1FFFFFFF) else Color(0x0A000000)
              ),
              border = CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(theme.cardBorder)
              )
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "Risk ($)",
                  fontSize = 11.sp,
                  color = theme.textMuted,
                  fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "$${String.format(Locale.US, "%.2f", result.riskAmount)}",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = theme.sellColor,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = "Batas rugi",
                  fontSize = 9.sp,
                  color = theme.textMuted
                )
              }
            }

            // Card 3: Pip Value Aktual
            Card(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (theme.isDark) Color(0x1FFFFFFF) else Color(0x0A000000)
              ),
              border = CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(theme.cardBorder)
              )
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "Pip Aktual",
                  fontSize = 11.sp,
                  color = theme.textMuted,
                  fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "$${String.format(Locale.US, "%.2f", result.actualPipValue)}",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = theme.goldColor,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = "/pip",
                  fontSize = 9.sp,
                  color = theme.textMuted
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // ==========================================
          // QUICK REFERENCE LOT SIZE PILLS
          // ==========================================
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "REFERENSI CEPAT LOT SIZE",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = theme.textMuted
            )
            Text(
              text = "vs Risk Akun",
              fontSize = 10.sp,
              color = theme.textMuted
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            result.referencePills.forEach { pill ->
              val isHighlight = pill.isCloseToCalculated
              val pipColor = if (theme.isDark) Color(0xFFFBBF24) else Color(0xFFB45309) // Sharp Amber/Gold for pip
              val riskColor = if (theme.isDark) Color(0xFFFF7A7A) else Color(0xFFDC2626) // Sharp Coral Red for risk
              val arrowColor = if (theme.isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(
                    if (isHighlight) theme.accent.copy(alpha = if (theme.isDark) 0.25f else 0.16f)
                    else if (theme.isDark) Color(0x14FFFFFF)
                    else Color(0x08000000)
                  )
                  .border(
                    width = if (isHighlight) 1.5.dp else 1.dp,
                    color = if (isHighlight) theme.accent else theme.cardBorder,
                    shape = RoundedCornerShape(10.dp)
                  )
                  .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.copyToClipboard(
                      context,
                      "ref_${pill.lot}",
                      String.format(Locale.US, "%.2f", pill.lot)
                    )
                  }
                  .padding(horizontal = 12.dp, vertical = 9.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Lot size (Tebal & tajam)
                  Text(
                    text = "${String.format(Locale.US, "%.2f", pill.lot)} lot",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = if (isHighlight) theme.accent else theme.textPrimary,
                    fontFamily = FontFamily.Monospace
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "→",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = arrowColor
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  // Pip Value (Warna Amber tajam)
                  Text(
                    text = "$${String.format(Locale.US, "%.2f", pill.pipValue)}/pip",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = pipColor,
                    fontFamily = FontFamily.Monospace
                  )
                  Text(
                    text = " · ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = arrowColor
                  )
                  // Risk Percent (Warna Merah/Coral tajam)
                  Text(
                    text = "${String.format(Locale.US, "%.1f", pill.riskPercent)}% risk",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = riskColor,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Information Note
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(if (theme.isDark) Color(0x0EFFFFFF) else Color(0x06000000))
              .padding(12.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Pip value USDJPY & GBPJPY bersifat estimasi (~$9.09/lot).\nSesuaikan dengan rate real-time broker kamu.",
              fontSize = 11.sp,
              color = theme.textMuted,
              textAlign = TextAlign.Center,
              lineHeight = 16.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(72.dp)) // Space for bottom bar
    }
  }
}
