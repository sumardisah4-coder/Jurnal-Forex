package com.example.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppTheme
import com.example.model.CalculationResult
import com.example.model.MarketPreset
import com.example.model.OrderType

@Composable
fun ForexCalculatorScreen(
  viewModel: ForexCalculatorViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val focusManager = LocalFocusManager.current

  val theme = uiState.currentTheme
  var showCustomRatioDialog by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(theme.bgDark)
      .drawBehind {
        // Ambient trading radial glows tuned per theme
        val glowAlpha1 = if (theme.isDark) 0.30f else 0.12f
        val glowAlpha2 = if (theme.isDark) 0.18f else 0.08f
        val glowAlpha3 = if (theme.isDark) 0.14f else 0.06f

        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(theme.accent.copy(alpha = glowAlpha1), Color.Transparent),
            center = Offset(size.width * 0.15f, 0f),
            radius = size.width * 0.5f
          )
        )
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(theme.buyColor.copy(alpha = glowAlpha2), Color.Transparent),
            center = Offset(size.width * 0.95f, size.height * 0.22f),
            radius = size.width * 0.45f
          )
        )
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(theme.goldColor.copy(alpha = glowAlpha3), Color.Transparent),
            center = Offset(size.width * 0.5f, size.height),
            radius = size.width * 0.55f
          )
        )
      }
      .statusBarsPadding()
      .navigationBarsPadding(),
    contentAlignment = Alignment.TopCenter
  ) {
    // Background terminal grid lines
    Canvas(modifier = Modifier.fillMaxSize()) {
      val gridSize = 32.dp.toPx()
      val strokeColor = if (theme.isDark) Color(0x08FFFFFF) else Color(0x06000000)
      var x = 0f
      while (x < size.width) {
        drawLine(
          color = strokeColor,
          start = Offset(x, 0f),
          end = Offset(x, size.height),
          strokeWidth = 1f
        )
        x += gridSize
      }
      var y = 0f
      while (y < size.height) {
        drawLine(
          color = strokeColor,
          start = Offset(0f, y),
          end = Offset(size.width, y),
          strokeWidth = 1f
        )
        y += gridSize
      }
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 8.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Main Card container with max width 620.dp
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
        ) {
          // HEADER WITH BRAND & PRO BADGE
          HeaderSection(theme = theme)

          // THEME SELECTOR BAR (Cyber Dark, Light Modern, Gold Bullion, Emerald Mint)
          ThemeSelectorRow(
            currentTheme = theme,
            onThemeSelect = { selectedTheme ->
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              viewModel.setTheme(selectedTheme)
            }
          )

          // MAIN CONTENT
          Column(modifier = Modifier.padding(14.dp)) {

            // ==========================================
            // BAGIAN PALING ATAS: RISK RATIO
            // ==========================================
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              SectionTitle(title = "RISK RATIO", theme = theme)
              TextButton(
                onClick = { showCustomRatioDialog = true },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "Tambah Rasio",
                  tint = theme.accent,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "TAMBAH",
                  color = theme.accent,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            RatioGrid(
              availableRatios = uiState.availableRatios,
              selectedRatios = uiState.selectedRatios,
              theme = theme,
              onToggleRatio = { ratio ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.toggleRatio(ratio)
              },
              onRemoveCustom = viewModel::removeCustomRatio
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // BAGIAN 2: MARKET PRESETS & INPUT MARKET
            // ==========================================
            SectionTitle(title = "INPUT MARKET", theme = theme)

            MarketPresetsRow(
              presets = uiState.presets,
              activeOpen = uiState.openPrice,
              activePip = uiState.pipValue,
              theme = theme,
              onSelect = { preset ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.applyPreset(preset)
              }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              MarketInputField(
                label = "PRICE / OPEN",
                value = uiState.openPrice,
                onValueChange = viewModel::onOpenPriceChanged,
                theme = theme,
                modifier = Modifier
                  .weight(1f)
                  .testTag("open_price_input")
              )
              MarketInputField(
                label = "PIP / POINT VALUE",
                value = uiState.pipValue,
                onValueChange = viewModel::onPipValueChanged,
                theme = theme,
                modifier = Modifier
                  .weight(1f)
                  .testTag("pip_value_input")
              )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // BAGIAN 3: JENIS ORDER (BUY / SELL)
            // ==========================================
            SectionTitle(title = "JENIS ORDER", theme = theme)
            OrderTypeSelector(
              selectedOrder = uiState.orderType,
              theme = theme,
              onSelectOrder = { order ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.setOrderType(order)
              }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // BAGIAN 4: CALCULATE BUTTON
            // ==========================================
            CalculateButton(
              theme = theme,
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                focusManager.clearFocus()
                viewModel.calculateExplicitly()
              }
            )

            // Feedback banner
            AnimatedVisibility(
              visible = uiState.feedbackMessage != null,
              enter = fadeIn(),
              exit = fadeOut()
            ) {
              Text(
                text = uiState.feedbackMessage ?: "",
                color = theme.buyColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 8.dp)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // BAGIAN 5: RESULTS TABLE
            // ==========================================
            ResultsTable(
              results = uiState.results,
              copiedKey = uiState.copiedKey,
              theme = theme,
              onCopy = { key, value ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.copyToClipboard(context, key, value)
              }
            )
          }

          // FOOTER
          FooterSection(theme = theme)
        }
      }
      Spacer(modifier = Modifier.height(72.dp))
    }
  }

  // Dialog to add custom ratio
  if (showCustomRatioDialog) {
    CustomRatioDialog(
      theme = theme,
      onDismiss = { showCustomRatioDialog = false },
      onConfirm = { sl, tp ->
        val success = viewModel.addCustomRatio(sl, tp)
        if (success) {
          showCustomRatioDialog = false
        }
      }
    )
  }
}

@Composable
private fun HeaderSection(theme: AppTheme) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(
        brush = Brush.linearGradient(
          colors = if (theme.isDark) {
            listOf(theme.accent.copy(alpha = 0.22f), Color(0x04FFFFFF))
          } else {
            listOf(theme.accent.copy(alpha = 0.12f), Color(0x04000000))
          }
        )
      )
      .border(
        width = 1.dp,
        color = theme.cardBorder,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
      )
      .padding(horizontal = 14.dp, vertical = 13.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // ₿ Icon Box
      Box(
        modifier = Modifier
          .size(40.dp)
          .shadow(elevation = 10.dp, shape = RoundedCornerShape(12.dp), spotColor = theme.accent)
          .clip(RoundedCornerShape(12.dp))
          .background(
            brush = Brush.linearGradient(
              colors = listOf(theme.accent, theme.accentDark)
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "₿",
          color = Color.White,
          fontSize = 20.sp,
          fontWeight = FontWeight.Black
        )
      }

      Column {
        Text(
          text = "HAN FOREX",
          color = theme.textPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 0.8.sp
        )
        Text(
          text = "SMART SL / TP CALCULATOR",
          color = theme.textMuted,
          fontSize = 8.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      }
    }

    // PRO Badge
    Box(
      modifier = Modifier
        .clip(CircleShape)
        .border(width = 1.dp, color = theme.goldColor.copy(alpha = 0.45f), shape = CircleShape)
        .background(theme.goldColor.copy(alpha = 0.15f))
        .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
      Text(
        text = "PRO",
        color = theme.goldColor,
        fontSize = 8.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.8.sp
      )
    }
  }
}

@Composable
private fun ThemeSelectorRow(
  currentTheme: AppTheme,
  onThemeSelect: (AppTheme) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(
        if (currentTheme.isDark) Color(0x0DFFFFFF) else Color(0x06000000)
      )
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.padding(bottom = 6.dp)
    ) {
      Text(
        text = "TEMA TAMPILAN",
        color = currentTheme.textMuted,
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.8.sp
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      AppTheme.entries.forEach { themeOption ->
        val isSelected = currentTheme == themeOption
        val borderAnimColor by animateColorAsState(
          targetValue = if (isSelected) themeOption.accent else currentTheme.cardBorder,
          animationSpec = tween(200),
          label = "theme_border"
        )

        val pillBg = if (isSelected) {
          themeOption.accent.copy(alpha = if (currentTheme.isDark) 0.25f else 0.18f)
        } else {
          if (currentTheme.isDark) Color(0x0AFFFFFF) else Color(0x0D000000)
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.2.dp, borderAnimColor, RoundedCornerShape(20.dp))
            .background(pillBg)
            .clickable { onThemeSelect(themeOption) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Text(
              text = themeOption.iconEmoji,
              fontSize = 12.sp
            )
            Text(
              text = themeOption.label,
              color = if (isSelected) themeOption.accent else currentTheme.textMuted,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MarketPresetsRow(
  presets: List<MarketPreset>,
  activeOpen: String,
  activePip: String,
  theme: AppTheme,
  onSelect: (MarketPreset) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    presets.forEach { preset ->
      val isSelected = preset.defaultPrice == activeOpen && preset.pipValue == activePip
      val bgColor = if (isSelected) {
        theme.accent.copy(alpha = if (theme.isDark) 0.25f else 0.16f)
      } else {
        if (theme.isDark) Color(0x0DFFFFFF) else Color(0x06000000)
      }
      val borderColor = if (isSelected) theme.accent else theme.cardBorder
      val textColor = if (isSelected) theme.accent else theme.textMuted

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .border(1.dp, borderColor, RoundedCornerShape(8.dp))
          .background(bgColor)
          .clickable { onSelect(preset) }
          .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = preset.name,
          color = textColor,
          fontSize = 9.5.sp,
          fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
        )
      }
    }
  }
}

@Composable
private fun SectionTitle(title: String, theme: AppTheme) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(7.dp),
    modifier = Modifier.padding(bottom = 8.dp)
  ) {
    Box(
      modifier = Modifier
        .size(width = 4.dp, height = 12.dp)
        .clip(RoundedCornerShape(5.dp))
        .background(theme.accent)
    )
    Text(
      text = title,
      color = theme.textMuted,
      fontSize = 9.sp,
      fontWeight = FontWeight.Black,
      letterSpacing = 1.2.sp
    )
  }
}

@Composable
private fun MarketInputField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  theme: AppTheme,
  modifier: Modifier = Modifier
) {
  var isFocused by remember { mutableStateOf(false) }

  val borderColor by animateColorAsState(
    targetValue = if (isFocused) theme.accent else theme.cardBorder,
    animationSpec = tween(180),
    label = "field_border"
  )

  Column(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, borderColor, RoundedCornerShape(14.dp))
      .background(
        brush = Brush.linearGradient(
          colors = if (theme.isDark) {
            listOf(Color(0x0CFFFFFF), Color(0x05FFFFFF))
          } else {
            listOf(Color(0x0A000000), Color(0x03000000))
          }
        )
      )
      .padding(11.dp)
  ) {
    Text(
      text = label,
      color = theme.textMuted,
      fontSize = 8.sp,
      fontWeight = FontWeight.ExtraBold,
      letterSpacing = 0.7.sp,
      modifier = Modifier.padding(bottom = 4.dp)
    )

    BasicTextField(
      value = value,
      onValueChange = { input ->
        if (input.isEmpty() || input.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
          onValueChange(input)
        }
      },
      textStyle = TextStyle(
        color = theme.textPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Black
      ),
      cursorBrush = SolidColor(theme.accent),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Decimal,
        imeAction = ImeAction.Done
      ),
      singleLine = true,
      modifier = Modifier
        .fillMaxWidth()
        .height(25.dp)
        .onFocusChanged { isFocused = it.isFocused }
    )
  }
}

@Composable
private fun RatioGrid(
  availableRatios: List<String>,
  selectedRatios: Set<String>,
  theme: AppTheme,
  onToggleRatio: (String) -> Unit,
  onRemoveCustom: (String) -> Unit
) {
  val defaultRatios = setOf("100:200", "200:200", "200:300", "200:400", "250:500", "300:400", "300:500")

  // Render in 2 columns
  val chunked = availableRatios.chunked(2)

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    chunked.forEach { rowRatios ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        rowRatios.forEach { ratio ->
          val isSelected = selectedRatios.contains(ratio)
          val isCustom = ratio !in defaultRatios

          RatioItem(
            ratio = ratio,
            isSelected = isSelected,
            isCustom = isCustom,
            theme = theme,
            onClick = { onToggleRatio(ratio) },
            onRemove = { onRemoveCustom(ratio) },
            modifier = Modifier
              .weight(1f)
              .testTag("ratio_chip_${ratio.replace(':', '_')}")
          )
        }
        if (rowRatios.size == 1) {
          Spacer(modifier = Modifier.weight(1f))
        }
      }
    }
  }
}

@Composable
private fun RatioItem(
  ratio: String,
  isSelected: Boolean,
  isCustom: Boolean,
  theme: AppTheme,
  onClick: () -> Unit,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier
) {
  val borderColor = if (isSelected) theme.accent else theme.cardBorder
  val bgBrush = if (isSelected) {
    Brush.linearGradient(
      colors = listOf(
        theme.accent.copy(alpha = if (theme.isDark) 0.28f else 0.16f),
        theme.accent.copy(alpha = if (theme.isDark) 0.08f else 0.05f)
      )
    )
  } else {
    SolidColor(
      if (theme.isDark) Color(0x06FFFFFF) else Color(0x05000000)
    )
  }

  Row(
    modifier = modifier
      .heightIn(min = 50.dp)
      .clip(RoundedCornerShape(13.dp))
      .border(1.dp, borderColor, RoundedCornerShape(13.dp))
      .background(bgBrush)
      .clickable(onClick = onClick)
      .padding(horizontal = 9.dp, vertical = 9.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(9.dp)
  ) {
    // Custom Checkbox Square
    val checkboxBg = if (isSelected) theme.accent else if (theme.isDark) Color(0xFF091223) else Color(0xFFE2E8F0)
    val checkboxBorder = if (isSelected) theme.accent else theme.textMuted.copy(alpha = 0.5f)

    Box(
      modifier = Modifier
        .size(22.dp)
        .clip(RoundedCornerShape(7.dp))
        .border(
          width = 1.5.dp,
          color = checkboxBorder,
          shape = RoundedCornerShape(7.dp)
        )
        .background(checkboxBg),
      contentAlignment = Alignment.Center
    ) {
      if (isSelected) {
        Text(
          text = "✓",
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.Black
        )
      }
    }

    Text(
      text = ratio,
      color = theme.textPrimary,
      fontSize = 13.sp,
      fontWeight = FontWeight.Black,
      modifier = Modifier.weight(1f)
    )

    if (isCustom) {
      IconButton(
        onClick = onRemove,
        modifier = Modifier.size(20.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Hapus Rasio $ratio",
          tint = theme.textMuted,
          modifier = Modifier.size(12.dp)
        )
      }
    }
  }
}

@Composable
private fun OrderTypeSelector(
  selectedOrder: OrderType,
  theme: AppTheme,
  onSelectOrder: (OrderType) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // BUY BUTTON
    val isBuy = selectedOrder == OrderType.BUY
    val buyBorder = if (isBuy) theme.buyColor.copy(alpha = 0.7f) else theme.cardBorder
    val buyBrush = if (isBuy) {
      Brush.linearGradient(
        colors = listOf(
          theme.buyColor.copy(alpha = if (theme.isDark) 0.28f else 0.18f),
          theme.buyColor.copy(alpha = if (theme.isDark) 0.10f else 0.06f)
        )
      )
    } else {
      SolidColor(if (theme.isDark) Color(0x06FFFFFF) else Color(0x05000000))
    }
    val buyTextColor = if (isBuy) theme.buyColor else theme.textMuted

    Box(
      modifier = Modifier
        .weight(1f)
        .height(50.dp)
        .clip(RoundedCornerShape(13.dp))
        .border(1.dp, buyBorder, RoundedCornerShape(13.dp))
        .background(buyBrush)
        .clickable { onSelectOrder(OrderType.BUY) }
        .testTag("order_buy_button"),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "▲ BUY",
        color = buyTextColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.Black
      )
    }

    // SELL BUTTON
    val isSell = selectedOrder == OrderType.SELL
    val sellBorder = if (isSell) theme.sellColor.copy(alpha = 0.7f) else theme.cardBorder
    val sellBrush = if (isSell) {
      Brush.linearGradient(
        colors = listOf(
          theme.sellColor.copy(alpha = if (theme.isDark) 0.28f else 0.18f),
          theme.sellColor.copy(alpha = if (theme.isDark) 0.10f else 0.06f)
        )
      )
    } else {
      SolidColor(if (theme.isDark) Color(0x06FFFFFF) else Color(0x05000000))
    }
    val sellTextColor = if (isSell) theme.sellColor else theme.textMuted

    Box(
      modifier = Modifier
        .weight(1f)
        .height(50.dp)
        .clip(RoundedCornerShape(13.dp))
        .border(1.dp, sellBorder, RoundedCornerShape(13.dp))
        .background(sellBrush)
        .clickable { onSelectOrder(OrderType.SELL) }
        .testTag("order_sell_button"),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "▼ SELL",
        color = sellTextColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.Black
      )
    }
  }
}

@Composable
private fun CalculateButton(
  theme: AppTheme,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(52.dp)
      .shadow(elevation = 12.dp, shape = RoundedCornerShape(14.dp), spotColor = theme.accent)
      .clip(RoundedCornerShape(14.dp))
      .background(
        brush = Brush.linearGradient(
          colors = listOf(theme.accent, theme.accentDark)
        )
      )
      .clickable(onClick = onClick)
      .testTag("calculate_button"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "⚡   HITUNG SL & TP",
      color = Color.White,
      fontSize = 13.sp,
      fontWeight = FontWeight.Black,
      letterSpacing = 0.7.sp
    )
  }
}

@Composable
private fun ResultsTable(
  results: List<CalculationResult>,
  copiedKey: String?,
  theme: AppTheme,
  onCopy: (String, String) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
      .background(if (theme.isDark) Color(0x05FFFFFF) else Color(0x05000000))
      .testTag("results_container")
  ) {
    // Result Table Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(if (theme.isDark) Color(0x09FFFFFF) else Color(0x08000000))
        .padding(horizontal = 8.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "RATIO",
        color = theme.textMuted,
        fontSize = 8.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.8.sp,
        modifier = Modifier.weight(0.9f)
      )
      Text(
        text = "SL",
        color = theme.textMuted,
        fontSize = 8.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.8.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(1f)
      )
      Spacer(modifier = Modifier.width(55.dp))
      Text(
        text = "TP",
        color = theme.textMuted,
        fontSize = 8.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.8.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(1f)
      )
      Spacer(modifier = Modifier.width(55.dp))
    }

    if (results.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 25.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Centang minimal satu Risk Ratio.",
          color = theme.textMuted,
          fontSize = 11.sp,
          textAlign = TextAlign.Center
        )
      }
    } else {
      results.forEach { result ->
        ResultRow(
          result = result,
          copiedKey = copiedKey,
          theme = theme,
          onCopy = onCopy
        )
      }
    }
  }
}

@Composable
private fun ResultRow(
  result: CalculationResult,
  copiedKey: String?,
  theme: AppTheme,
  onCopy: (String, String) -> Unit
) {
  val slKey = "SL_${result.ratio}"
  val tpKey = "TP_${result.ratio}"

  val isSlCopied = copiedKey == slKey
  val isTpCopied = copiedKey == tpKey

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = 57.dp)
      .border(width = 0.5.dp, color = theme.cardBorder)
      .background(
        brush = Brush.horizontalGradient(
          colors = listOf(
            theme.accent.copy(alpha = if (theme.isDark) 0.08f else 0.04f),
            Color.Transparent
          )
        )
      )
      .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Ratio name
    Text(
      text = result.ratio,
      color = theme.textPrimary,
      fontSize = 12.sp,
      fontWeight = FontWeight.Black,
      modifier = Modifier.weight(0.9f)
    )

    // SL Price
    Text(
      text = result.slText,
      color = theme.textPrimary,
      fontSize = 13.sp,
      fontWeight = FontWeight.Black,
      textAlign = TextAlign.Center,
      modifier = Modifier.weight(1f)
    )

    // SL Copy Button
    CopyButton(
      isCopied = isSlCopied,
      theme = theme,
      onClick = { onCopy(slKey, result.slText) },
      modifier = Modifier.testTag("copy_sl_${result.ratio.replace(':', '_')}")
    )

    // TP Price
    Text(
      text = result.tpText,
      color = theme.textPrimary,
      fontSize = 13.sp,
      fontWeight = FontWeight.Black,
      textAlign = TextAlign.Center,
      modifier = Modifier.weight(1f)
    )

    // TP Copy Button
    CopyButton(
      isCopied = isTpCopied,
      theme = theme,
      onClick = { onCopy(tpKey, result.tpText) },
      modifier = Modifier.testTag("copy_tp_${result.ratio.replace(':', '_')}")
    )
  }
}

@Composable
private fun CopyButton(
  isCopied: Boolean,
  theme: AppTheme,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val border = if (isCopied) theme.buyColor.copy(alpha = 0.7f) else theme.cardBorder
  val bg = if (isCopied) {
    theme.buyColor.copy(alpha = if (theme.isDark) 0.22f else 0.16f)
  } else {
    if (theme.isDark) Color(0xFF101A2C) else Color(0xFFE2E8F0)
  }
  val textColor = if (isCopied) theme.buyColor else theme.textMuted
  val text = if (isCopied) "COPIED" else "COPY"

  Box(
    modifier = modifier
      .width(52.dp)
      .height(30.dp)
      .clip(RoundedCornerShape(8.dp))
      .border(1.dp, border, RoundedCornerShape(8.dp))
      .background(bg)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      color = textColor,
      fontSize = 8.sp,
      fontWeight = FontWeight.Black
    )
  }
}

@Composable
private fun FooterSection(theme: AppTheme) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 16.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "✓ Hanya rasio yang dicentang yang akan dihitung",
      color = theme.textMuted,
      fontSize = 8.sp,
      letterSpacing = 0.4.sp,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun CustomRatioDialog(
  theme: AppTheme,
  onDismiss: () -> Unit,
  onConfirm: (slPips: String, tpPips: String) -> Unit
) {
  var slInput by remember { mutableStateOf("") }
  var tpInput by remember { mutableStateOf("") }
  var error by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = theme.cardBgTop,
    title = {
      Text(
        text = "Tambah Risk Ratio Kustom",
        color = theme.textPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Masukkan nilai SL Pips dan TP Pips (misal SL: 150, TP: 300)",
          color = theme.textMuted,
          fontSize = 12.sp
        )

        OutlinedTextField(
          value = slInput,
          onValueChange = {
            slInput = it
            error = false
          },
          label = { Text("SL Pips", color = theme.textMuted, fontSize = 11.sp) },
          textStyle = TextStyle(color = theme.textPrimary, fontWeight = FontWeight.Bold),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = theme.accent,
            unfocusedBorderColor = theme.cardBorder,
            focusedTextColor = theme.textPrimary,
            unfocusedTextColor = theme.textPrimary
          ),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = tpInput,
          onValueChange = {
            tpInput = it
            error = false
          },
          label = { Text("TP Pips", color = theme.textMuted, fontSize = 11.sp) },
          textStyle = TextStyle(color = theme.textPrimary, fontWeight = FontWeight.Bold),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = theme.accent,
            unfocusedBorderColor = theme.cardBorder,
            focusedTextColor = theme.textPrimary,
            unfocusedTextColor = theme.textPrimary
          ),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        if (error) {
          Text(
            text = "Harap masukkan angka pip yang valid dan lebih dari 0",
            color = theme.sellColor,
            fontSize = 10.sp
          )
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          val slVal = slInput.toDoubleOrNull()
          val tpVal = tpInput.toDoubleOrNull()
          if (slVal != null && tpVal != null && slVal > 0 && tpVal > 0) {
            onConfirm(slInput, tpInput)
          } else {
            error = true
          }
        }
      ) {
        Text("SIMPAN", color = theme.accent, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("BATAL", color = theme.textMuted)
      }
    }
  )
}
