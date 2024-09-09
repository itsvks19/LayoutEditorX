package com.itsvks.layouteditorx.tools

import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.SearchView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.itsvks.layouteditorx.editor.RootLayout
import com.itsvks.layouteditorx.editor.map.AttributeMap

class XmlLayoutGenerator {
  val builder: StringBuilder = StringBuilder()
  var TAB: String = "\t"

  var useSuperclasses: Boolean = false

  fun generate(
    editor: RootLayout,
    viewAttributeMap: Map<View, AttributeMap>,
    useSuperclasses: Boolean
  ): String {
    this.useSuperclasses = useSuperclasses

    if (editor.childCount == 0) {
      return ""
    }
    builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
    builder.append(
      """
        <!--
        ${'\t'}Welcome to LayoutEditor!

        ${'\t'}We are proud to present our innovative layout generator app that
        ${'\t'}allows users to create and customize stunning layouts in no time.
        ${'\t'}With LayoutEditor, you can easily create beautiful and custom
        ${'\t'}layouts that are tailored to fit your unique needs.

        ${'\t'}Thank you for using LayoutEditor and we hope you enjoy our app!
        -->
        
        
        """.trimIndent()
    )

    return "${peek(editor.getChildAt(0), viewAttributeMap, 0)}\n"
  }

  private fun peek(view: View?, attributeMap: Map<View, AttributeMap>, depth: Int): String {
    if (view == null) return ""

    val indent = getIndent(depth)
    var nextDepth = depth

    val className = getClassName(view, indent)

    if (depth == 0) {
      builder.append(TAB).append("xmlns:android=\"http://schemas.android.com/apk/res/android\"\n")
      builder.append(TAB).append("xmlns:app=\"http://schemas.android.com/apk/res-auto\"\n")
    }

    val keys = if ((attributeMap[view] != null)) attributeMap[view]!!.keySet() else ArrayList()
    val values = if ((attributeMap[view] != null)) attributeMap[view]!!.values() else ArrayList()
    for (key in keys) {
      // If the value contains special characters it will be converted
      builder.append(TAB).append(indent).append(key).append("=\"").append(
        attributeMap[view]?.get(key)
      ).append("\"\n")
    }

    builder.deleteCharAt(builder.length - 1)

    if (view is ViewGroup) {
      if (
        view !is CalendarView &&
        view !is SearchView &&
        view !is NavigationView &&
        view !is BottomNavigationView &&
        view !is TabLayout
      ) {
        nextDepth++

        if (view.childCount > 0) {
          builder.append(">\n\n")

          for (i in 0 until view.childCount) {
            peek(view.getChildAt(i), attributeMap, nextDepth)
          }

          builder.append(indent).append("</").append(className).append(">\n\n")
        } else {
          builder.append(" />\n\n")
        }
      } else {
        builder.append(" />\n\n")
      }
    } else {
      builder.append(" />\n\n")
    }

    return builder.toString().trim { it <= ' ' }
  }

  private fun getClassName(view: View, indent: String): String {
    var className = if (useSuperclasses) view.javaClass.superclass.name else view.javaClass.name

    if (useSuperclasses) {
      if (className.startsWith("android.widget.")) {
        className = className.replace("android.widget.", "")
      }
    }

    builder.append(indent).append("<").append(className).append("\n")
    return className
  }

  private fun getIndent(depth: Int): String {
    return TAB.repeat(depth)
  }
}
