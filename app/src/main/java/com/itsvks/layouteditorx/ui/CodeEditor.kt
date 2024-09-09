package com.itsvks.layouteditorx.ui

import android.content.Context
import android.graphics.Typeface
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor
import org.eclipse.tm4e.core.registry.IThemeSource

@Composable
fun rememberCodeEditorState(
  initialContent: Content = Content()
) = remember {
  CodeEditorState(
    initialContent = initialContent
  )
}

@Composable
fun CodeEditor(
  modifier: Modifier = Modifier,
  state: CodeEditorState
) {
  val context = LocalContext.current
  val dark = isSystemInDarkTheme()

  val editor = remember {
    setCodeEditorFactory(
      isDarkMode = dark,
      context = context,
      state = state
    )
  }
  AndroidView(
    factory = { editor },
    modifier = modifier,
    onRelease = {
      it.release()
    }
  )
}

private fun setCodeEditorFactory(
  isDarkMode: Boolean = true,
  context: Context,
  state: CodeEditorState
): CodeEditor {
  val editor = CodeEditor(context)
  val typeface = Typeface.createFromAsset(context.assets, "JetBrainsMono-Regular.ttf")

  editor.apply {
    setText(state.content)
    typefaceText = typeface
    typefaceLineNumber = typeface
    isEditable = false

    FileProviderRegistry.getInstance().addFileProvider(AssetsFileResolver(context.assets))
    val themeRegistry = ThemeRegistry.getInstance()
    val path = "editor/textmate/darcula.json"
    themeRegistry.loadTheme(
      ThemeModel(
        IThemeSource.fromInputStream(
          FileProviderRegistry.getInstance().tryGetInputStream(path), path, null
        ), "darcula"
      )
    )

    themeRegistry.setTheme("darcula")

    ThemeRegistry.getInstance().setTheme("darcula")
    GrammarRegistry.getInstance().loadGrammars("editor/textmate/languages.json")

    if (colorScheme !is TextMateColorScheme) {
      colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
      colorScheme = colorScheme
    }

    val language = TextMateLanguage.create("text.xml", true)
    setEditorLanguage(language)
  }
  state.editor = editor
  return editor
}

data class CodeEditorState(
  var editor: CodeEditor? = null,
  val initialContent: Content = Content()
) {
  var content by mutableStateOf(initialContent)
}