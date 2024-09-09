package com.itsvks.layouteditorx

import android.app.Application

class LayoutEditorX : Application() {
  companion object {
    @JvmStatic
    @get:Synchronized
    val instance by lazy { LayoutEditorX() }
  }
}