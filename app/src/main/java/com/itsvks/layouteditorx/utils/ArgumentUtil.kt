package com.itsvks.layouteditorx.utils

import java.util.regex.Pattern

object ArgumentUtil {
  const val COLOR = "color"
  const val DRAWABLE = "drawable"
  const val STRING = "string"

  private val patterns = HashMap<String, String>()

  init {
    patterns[COLOR] = "#[a-fA-F0-9]{6,8}"
    patterns[DRAWABLE] = "@drawable/.*"
    patterns[STRING] = "@string/.*"
  }

  @JvmStatic
  fun parseType(value: String, variants: Array<String>): String {
    for (variant in variants) {
      if (patterns.containsKey(variant)) if (patterns[variant]?.let {
          Pattern.matches(it, value)
        } == true
      ) return variant
    }
    return "text"
  }
}