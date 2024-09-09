package com.itsvks.layouteditorx.managers

import android.view.View
import android.view.ViewGroup

class IdManager private constructor() {
  companion object {
    @JvmStatic
    @get:Synchronized
    val instance by lazy { IdManager() }
  }

  val idMap = HashMap<View, String>()

  fun addNewId(view: View, id: String) {
    if (!idMap.containsKey(view)) {
      view.id = View.generateViewId()
    }
    idMap[view] = id.replace("@+id/", "")
  }

  fun addId(view: View, idName: String, id: Int) {
    view.id = id
    idMap[view] = idName.replace("@+id/", "")
  }

  fun removeId(view: View, removeChilds: Boolean) {
    idMap.remove(view)
    if (removeChilds && view is ViewGroup) {
      for (i in 0 until view.childCount) {
        removeId(view.getChildAt(i), true)
      }
    }
  }

  fun containsId(name: String): Boolean {
    val mName = name.replace("@id/", "")
    for (view in idMap.keys) {
      if (idMap[view] == mName) {
        return true
      }
    }
    return false
  }

  fun clear() = idMap.clear()

  fun getViewId(name: String): Int {
    val mName = name.replace("@id/", "")
    for (view in idMap.keys) {
      if (idMap[view] == mName) {
        return view.id
      }
    }
    return -1
  }

  fun getIds() = idMap.values.toTypedArray()
}
