package com.itsvks.layouteditorx.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ErrorMessage(message: String) {
  Text(
    text = message,
    color = MaterialTheme.colorScheme.error,
    style = MaterialTheme.typography.labelSmall,
    modifier = Modifier.padding(top = 4.dp)
  )
}