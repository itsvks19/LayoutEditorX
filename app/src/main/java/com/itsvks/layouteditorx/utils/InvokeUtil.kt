package com.itsvks.layouteditorx.utils

import android.content.Context
import android.view.View

object InvokeUtil {
  @JvmStatic
  fun invokeMethod(
    methodName: String,
    className: String,
    target: View,
    value: String,
    context: Context
  ) {
    try {
      val clazz = Class.forName("com.itsvks.layouteditorx.editor.callers.$className")
      val method =
        clazz.getMethod(methodName, View::class.java, String::class.java, Context::class.java)
      method.invoke(clazz, target, value, context)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  @JvmStatic
  fun createView(className: String, context: Context): Any? {
    try {
      val clazz = Class.forName(className)
      val constructor = clazz.getConstructor(Context::class.java)
      return constructor.newInstance(context)
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return null
  }

  @JvmStatic
  fun getSuperClassName(className: String): String? {
    return try {
      Class.forName(className).superclass.name
    } catch (err: Exception) {
      err.printStackTrace()
      null
    }
  }
}