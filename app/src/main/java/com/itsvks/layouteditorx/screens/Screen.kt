package com.itsvks.layouteditorx.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
  data object Projects : Screen("projects", "Projects", Icons.Filled.Home)
  data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
  data object About : Screen("about", "About", Icons.Filled.Info)
}