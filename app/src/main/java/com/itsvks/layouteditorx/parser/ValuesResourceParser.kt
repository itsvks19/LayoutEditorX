package com.itsvks.layouteditorx.parser

import com.itsvks.layouteditorx.models.ValuesItem
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

class ValuesResourceParser private constructor() {
  companion object {
    const val TAG_STRING = "string"
    const val TAG_COLOR = "color"

    @JvmStatic
    @get:Synchronized
    val instance by lazy { ValuesResourceParser() }
  }

  var values = mutableListOf<ValuesItem>()

  fun parseXml(inputStream: InputStream, tag: String) {
    var name = ""
    var value = ""

    try {
      val factory = XmlPullParserFactory.newInstance().apply {
        isNamespaceAware = true
      }
      val parser = factory.newPullParser().apply {
        setInput(inputStream, null)
      }

      var eventType = parser.eventType
      while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
          XmlPullParser.START_TAG -> {
            val tagName = parser.name
            if (tagName.lowercase() == tag.lowercase()) {
              name = parser.getAttributeValue(null, "name")
            }
          }

          XmlPullParser.TEXT -> {
            value = parser.text
          }

          XmlPullParser.END_TAG -> {
            val tagName = parser.name
            if (tagName.lowercase() == tag.lowercase()) {
              values.add(ValuesItem(name, value))
            }
          }
        }
        eventType = parser.next()
      }
    } catch (err: Exception) {
      err.printStackTrace()
    }
  }
}