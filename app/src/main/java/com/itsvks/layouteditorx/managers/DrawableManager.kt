package com.itsvks.layouteditorx.managers

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import com.itsvks.layouteditorx.extensions.getVectorDrawableAsync
import java.io.File

class DrawableManager private constructor() {
  companion object {
    @JvmStatic
    @get:Synchronized
    val instance by lazy { DrawableManager() }
  }

  private val items = mutableMapOf<String, String>()

  fun loadFromFiles(vararg files: File) {
    items.clear()

    files.forEach { items[it.nameWithoutExtension] = it.path }
  }

  fun contains(name: String) = items.containsKey(name)

  suspend fun getDrawable(context: Context, key: String): Drawable? {
    return if (items[key]?.endsWith(".xml") == true) {
      context.getVectorDrawableAsync(Uri.fromFile(items[key]?.let { File(it) }))
    } else Drawable.createFromPath(items[key])
  }

  fun keySet() = items.keys
  fun clear() = items.clear()
}