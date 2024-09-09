package com.itsvks.layouteditorx.editor.map

data class Attribute(val key: String, var value: String)

class AttributeMap {
  private val attributes = mutableListOf<Attribute>()

  operator fun set(key: String, value: String) {
    if (contains(key)) attributes[indexOf(key)].value = value
    else attributes.add(Attribute(key, value))
  }

  operator fun get(key: String) = attributes[indexOf(key)].value

  fun remove(key: String) = attributes.removeAt(indexOf(key))

  fun keySet() = attributes.map { it.key }

  fun values() = attributes.map { it.value }

  fun contains(key: String) = attributes.any { it.key == key }

  fun indexOf(key: String) = attributes.indexOfFirst { it.key == key }

  fun filterValues(predicate: (String) -> Boolean): List<String> {
    return attributes.filter { predicate(it.value) }.map { it.key }
  }
}