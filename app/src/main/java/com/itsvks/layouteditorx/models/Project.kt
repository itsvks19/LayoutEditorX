package com.itsvks.layouteditorx.models

import com.blankj.utilcode.util.FileUtils
import java.io.File
import java.io.Serializable

data class Project(var path: String, var date: String) : Serializable {
  val name: String
    get() = path.substringAfterLast("/")

  val drawablePath: String
    get() = "$path/drawable"

  val fontPath: String
    get() = "$path/font"

  val layoutPath: String
    get() = "$path/layout"

  val valuesPath: String
    get() = "$path/values"

  val colorsPath: String
    get() = "$valuesPath/colors.xml"

  val stringsPath: String
    get() = "$valuesPath/strings.xml"

  val mainLayoutPath: String
    get() = "$layoutPath/layout_main.xml"

  val drawables: List<File>
    get() {
      val file = File(drawablePath)
      FileUtils.createOrExistsDir(file)
      return file.listFiles()?.toList() ?: emptyList()
    }

  val fonts: List<File>
    get() {
      val file = File(fontPath)
      FileUtils.createOrExistsDir(file)
      return file.listFiles()?.toList() ?: emptyList()
    }
}
