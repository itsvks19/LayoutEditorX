package com.itsvks.layouteditorx.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.itsvks.layouteditorx.managers.ProjectManager
import com.itsvks.layouteditorx.screens.EditorScreen
import com.itsvks.layouteditorx.ui.theme.LayoutEditorXTheme

class EditorActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val project = requireNotNull(ProjectManager.instance.openedProject) {
      "project = null"
    }

    setContent {
      LayoutEditorXTheme {
        EditorScreen(project)
      }
    }
  }

  companion object {
    const val EXTRA_PROJECT = "project"
  }
}