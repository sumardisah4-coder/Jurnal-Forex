package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.ForexCalculatorViewModel
import com.example.ui.MainAppScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: ForexCalculatorViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    viewModel.attachContext(this)
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          MainAppScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme {
    MainAppScreen(viewModel = ForexCalculatorViewModel())
  }
}
