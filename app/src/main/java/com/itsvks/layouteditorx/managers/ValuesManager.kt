package com.itsvks.layouteditorx.managers

import com.itsvks.layouteditorx.parser.ValuesResourceParser
import java.io.FileInputStream
import java.io.FileNotFoundException

object ValuesManager {
  fun getValueFromResources(tag: String, value: String, path: String): String? {
    val resValueName = value.substring(value.indexOf("/") + 1)
    var result: String? = null
    try {
      ValuesResourceParser.instance.parseXml(FileInputStream(path), tag)

      for (item in ValuesResourceParser.instance.values) {
        if (item.name == resValueName) {
          result = item.value
        }
      }
    } catch (e: FileNotFoundException) {
      e.printStackTrace()
    }
    return result
  }
}