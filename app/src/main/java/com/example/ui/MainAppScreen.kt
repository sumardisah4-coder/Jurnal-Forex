package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppTheme
import com.example.model.ForexAppTab

@Composable
fun MainAppScreen(
  viewModel: ForexCalculatorViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  LaunchedEffect(Unit) {
    viewModel.attachContext(context)
  }

  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val theme = uiState.currentTheme
  val haptic = LocalHapticFeedback.current

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(theme.bgDark)
  ) {
    // Content Screen with Crossfade
    AnimatedContent(
      targetState = uiState.activeTab,
      transitionSpec = {
        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
      },
      label = "tab_content_transition",
      modifier = Modifier.fillMaxSize()
    ) { tab ->
      when (tab) {
        ForexAppTab.JOURNAL -> {
          TradeJournalScreen(viewModel = viewModel)
        }
        ForexAppTab.TP_SL -> {
          ForexCalculatorScreen(viewModel = viewModel)
        }
        ForexAppTab.LOT_SIZE -> {
          LotSizeScreen(viewModel = viewModel)
        }
        ForexAppTab.COMPOUNDING -> {
          CompoundingScreen(viewModel = viewModel)
        }
      }
    }

    // Feedback Toast (Pill at top)
    AnimatedVisibility(
      visible = uiState.feedbackMessage != null,
      enter = fadeIn() + slideInVertically { -it },
      exit = fadeOut() + slideOutVertically { -it },
      modifier = Modifier
        .align(Alignment.TopCenter)
        .statusBarsPadding()
        .padding(top = 16.dp)
    ) {
      uiState.feedbackMessage?.let { msg ->
        Box(
          modifier = Modifier
            .shadow(12.dp, RoundedCornerShape(999.dp))
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFF1E1E28))
            .border(1.dp, theme.accent, RoundedCornerShape(999.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
          Text(
            text = msg,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // Bottom Navigation Bar - Docked at the very bottom
    // Order: Jurnal, TP/SL, Lot Size, Compounding
    Surface(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth(),
      color = if (theme.isDark) Color(0xFF0C0C12) else theme.cardBgTop,
      border = BorderStroke(
        width = 1.dp,
        color = if (theme.isDark) Color(0xFF1E1E28) else theme.cardBorder
      ),
      tonalElevation = 8.dp
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding(),
        contentAlignment = Alignment.Center
      ) {
        Row(
          modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 5.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          BottomNavButton(
            tab = ForexAppTab.JOURNAL,
            icon = Icons.Default.MenuBook,
            label = "Jurnal",
            isSelected = uiState.activeTab == ForexAppTab.JOURNAL,
            theme = theme,
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              viewModel.setActiveTab(ForexAppTab.JOURNAL)
            },
            testTag = "tab_button_journal",
            modifier = Modifier.weight(1f)
          )

          BottomNavButton(
            tab = ForexAppTab.TP_SL,
            icon = Icons.Default.SwapVert,
            label = "TP / SL",
            isSelected = uiState.activeTab == ForexAppTab.TP_SL,
            theme = theme,
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              viewModel.setActiveTab(ForexAppTab.TP_SL)
            },
            testTag = "tab_button_tpsl",
            modifier = Modifier.weight(1f)
          )

          BottomNavButton(
            tab = ForexAppTab.LOT_SIZE,
            icon = Icons.Default.Calculate,
            label = "Lot Size",
            isSelected = uiState.activeTab == ForexAppTab.LOT_SIZE,
            theme = theme,
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              viewModel.setActiveTab(ForexAppTab.LOT_SIZE)
            },
            testTag = "tab_button_lotsize",
            modifier = Modifier.weight(1f)
          )

          BottomNavButton(
            tab = ForexAppTab.COMPOUNDING,
            icon = Icons.Default.AutoGraph,
            label = "Compounding",
            isSelected = uiState.activeTab == ForexAppTab.COMPOUNDING,
            theme = theme,
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              viewModel.setActiveTab(ForexAppTab.COMPOUNDING)
            },
            testTag = "tab_button_compounding",
            modifier = Modifier.weight(1f)
          )
        }
      }
    }
  }
}

@Composable
private fun BottomNavButton(
  tab: ForexAppTab,
  icon: ImageVector,
  label: String,
  isSelected: Boolean,
  theme: AppTheme,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier
) {
  val activeBg = if (isSelected) {
    if (theme.isDark) theme.accent.copy(alpha = 0.16f) else theme.accent.copy(alpha = 0.14f)
  } else {
    Color.Transparent
  }

  val activeBorder = if (isSelected) {
    theme.accent.copy(alpha = 0.45f)
  } else {
    Color.Transparent
  }

  val contentColor = if (isSelected) {
    theme.accent
  } else {
    theme.textMuted
  }

  Box(
    modifier = modifier
      .padding(horizontal = 2.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(activeBg)
      .border(1.dp, activeBorder, RoundedCornerShape(16.dp))
      .clickable { onClick() }
      .padding(vertical = 8.dp, horizontal = 4.dp)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = contentColor,
        modifier = Modifier.size(20.dp)
      )

      Spacer(modifier = Modifier.height(3.dp))

      Text(
        text = label,
        color = contentColor,
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        maxLines = 1
      )
    }
  }
}
