package com.itsvks.layouteditorx.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itsvks.layouteditorx.managers.ProjectManager
import com.itsvks.layouteditorx.screens.HomeScreen
import com.itsvks.layouteditorx.ui.theme.LayoutEditorXTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      LayoutEditorXTheme {
        var paletteLoaded by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        scope.launch {
          delay(2000)
          ProjectManager.instance.initPalette(this@HomeActivity)
          paletteLoaded = true
        }

        AnimatedVisibility(
          paletteLoaded,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          HomeScreen()
        }

        AnimatedVisibility(
          !paletteLoaded,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          Surface(
            modifier = Modifier.fillMaxSize()
          ) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Layout Editor",
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                fontStyle = FontStyle.Normal,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 100.dp)
              )
              CircularProgressIndicator()
            }
          }
        }
      }
    }
  }
}
