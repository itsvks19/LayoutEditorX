package com.itsvks.layouteditorx.editor.initializer

import android.content.Context
import android.view.View
import com.itsvks.layouteditorx.Constants
import com.itsvks.layouteditorx.utils.InvokeUtil
import com.itsvks.layouteditorx.editor.RootLayout
import com.itsvks.layouteditorx.editor.map.AttributeMap

class AttributeInitializer(
  private val context: Context,
  private val attributes: Map<String, List<HashMap<String, Any>>>,
  private val parentAttributes: Map<String, List<HashMap<String, Any>>>
) {
  private lateinit var viewAttributeMap: Map<View, AttributeMap>

  constructor(
    context: Context,
    attributes: Map<String, List<HashMap<String, Any>>>,
    parentAttributes: Map<String, List<HashMap<String, Any>>>,
    viewAttributeMap: Map<View, AttributeMap>
  ) : this(context, attributes, parentAttributes) {
    this.viewAttributeMap = viewAttributeMap
  }

  fun applyDefaultAttributes(view: View, defaultAttrs: Map<String, String?>) {
    val allAttrs = getAllAttributesForView(view)

    defaultAttrs.forEach { (key, value) ->
      allAttrs.find { it[Constants.KEY_ATTRIBUTE_NAME]?.toString() == key }?.let { map ->
        value?.let { applyAttribute(view, it, map) }
      }
    }
  }

  fun applyAttribute(
    view: View,
    value: String,
    attribute: HashMap<String, Any>
  ) {
    val methodName = attribute[Constants.KEY_METHOD_NAME]?.toString()
    val className = attribute[Constants.KEY_CLASS_NAME]?.toString()
    val attributeName = attribute[Constants.KEY_ATTRIBUTE_NAME]?.toString()

    val targetAttrs = viewAttributeMap[view]!!

    if (value.startsWith("@+id/") && targetAttrs.contains("android:id")) {
      val targetId = targetAttrs["android:id"].replace("+", "")
      viewAttributeMap.forEach { (_, map) ->
        map.filterValues { it.startsWith("@id/") && it == targetId }.forEach { key ->
          map[key] = value.replace("+", "")
        }
      }
    }

    attributeName?.let { targetAttrs[it] = value }
    methodName?.let { InvokeUtil.invokeMethod(it, className ?: return, view, value, context) }
  }

  fun getAvailableAttributesForView(view: View): List<HashMap<String, Any>> {
    val keys = viewAttributeMap[view]?.keySet() ?: emptyList()
    val allAttrs = getAllAttributesForView(view)

    allAttrs.removeAll { it[Constants.KEY_ATTRIBUTE_NAME]?.toString() in keys }
    return allAttrs
  }

  @Suppress("UNCHECKED_CAST")
  fun getAllAttributesForView(view: View): MutableList<HashMap<String, Any>> {
    val allAttrs = mutableListOf<HashMap<String, Any>>()

    var cls = view.javaClass
    val viewParentCls = View::class.java.superclass

    while (cls != viewParentCls) {
      attributes[cls.name]?.let { allAttrs.addAll(it) }
      cls = cls.superclass as Class<View>
    }

    view.parent?.let { parent ->
      if (parent.javaClass != RootLayout::class.java) {
        cls = parent.javaClass as Class<View>
        while (cls != viewParentCls) {
          parentAttributes[cls.name]?.let { allAttrs.addAll(it) }
          cls = cls.superclass as Class<View>
        }
      }
    }

    return allAttrs
  }

  fun getAttributeFromKey(key: String, list: List<HashMap<String, Any>>): HashMap<String, Any>? {
    return list.find { it[Constants.KEY_ATTRIBUTE_NAME] == key }
  }
}