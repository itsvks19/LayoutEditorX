package com.itsvks.layouteditorx.activities

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.blankj.utilcode.util.ClipboardUtils
import com.itsvks.layouteditorx.R
import com.itsvks.layouteditorx.ui.CodeEditor
import com.itsvks.layouteditorx.ui.ToastHost
import com.itsvks.layouteditorx.ui.rememberCodeEditorState
import com.itsvks.layouteditorx.ui.rememberToastHostState
import com.itsvks.layouteditorx.ui.theme.LayoutEditorXTheme
import io.github.rosemoe.sora.text.Content
import kotlinx.coroutines.launch

class ShowXMLActivity : ComponentActivity() {
  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val xml = intent.getStringExtra(EXTRA_XML) ?: ""

    setContent {
      var expandFab by remember { mutableStateOf(true) }

      val toastHostState = rememberToastHostState()
      val scope = rememberCoroutineScope()

      val backPressedOwner = LocalOnBackPressedDispatcherOwner.current

      val copyIcon = painterResource(R.drawable.content_copy)

      LayoutEditorXTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
              TopAppBar(
                title = { Text("XML Preview") },
                navigationIcon = {
                  IconButton(onClick = {
                    backPressedOwner?.onBackPressedDispatcher?.onBackPressed()
                  }) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                  }
                }
              )
            },
            floatingActionButton = {
              ExtendedFloatingActionButton(
                onClick = {
                  ClipboardUtils.copyText(xml)
                  scope.launch {
                    toastHostState.showToast(
                      message = "Copied to clipboard",
                      painter = copyIcon
                    )
                  }
                },
                expanded = expandFab,
                text = { Text(text = "Copy") },
                icon = {
                  Icon(
                    painter = copyIcon,
                    contentDescription = "Copy"
                  )
                }
              )
            }
          ) { innerPadding ->
            CodeViewer(
              modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
              text = xml,
              onScrollChange = { _, _, y, _, oldY ->
                when {
                  y > oldY + 20 && expandFab -> expandFab = false

                  y < oldY - 20 && !expandFab -> expandFab = true

                  y == 0 -> expandFab = true
                }
              }
            )
          }

          ToastHost(hostState = toastHostState)
        }
      }
    }
  }

  companion object {
    const val EXTRA_XML = "xml"
  }
}

@Composable
fun CodeViewer(
  modifier: Modifier = Modifier,
  text: String,
  onScrollChange: View.OnScrollChangeListener
) {
  val editorState = rememberCodeEditorState(initialContent = Content(text))
  editorState.editor?.setOnScrollChangeListener(onScrollChange)

  CodeEditor(
    modifier = modifier,
    state = editorState
  )
}