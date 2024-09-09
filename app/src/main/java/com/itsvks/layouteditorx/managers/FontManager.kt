package com.itsvks.layouteditorx.managers

import android.graphics.Typeface
import java.io.File

class FontManager private constructor() {
  companion object {
    @JvmStatic
    @get:Synchronized
    val instance by lazy { FontManager() }
  }

  private val fonts = mutableMapOf<String, String>()

  fun loadFromFiles(vararg files: File) {
    fonts.clear()

    files.forEach { fonts[it.nameWithoutExtension] = it.path }
  }

  fun contains(name: String) = fonts.containsKey(name)

  fun getFont(name: String) = Typeface.createFromFile(fonts[name])

  fun keySet() = fonts.keys

  fun clear() = fonts.clear()
}