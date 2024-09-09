package com.itsvks.layouteditorx.managers

import android.content.Context
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itsvks.layouteditorx.Constants
import com.itsvks.layouteditorx.LayoutEditorX
import com.itsvks.layouteditorx.models.Project
import java.io.File
import java.lang.reflect.Type
import java.util.Locale
import com.itsvks.layouteditorx.managers.DrawableManager.Companion.instance as drawableManager
import com.itsvks.layouteditorx.managers.FontManager.Companion.instance as fontManagerInstance

class ProjectManager private constructor() {
  private val paletteList = mutableListOf<List<HashMap<String, Any>>>()

  var openedProject: Project? = null
    private set

  fun openProject(project: Project) {
    openedProject = project
    drawableManager.loadFromFiles(*openedProject!!.drawables.toTypedArray())
    fontManagerInstance.loadFromFiles(*openedProject!!.fonts.toTypedArray())
  }

  fun closeProject() {
    openedProject = null
    drawableManager.clear()
    fontManagerInstance.clear()
  }

  val colorsXml: String
    get() = File(openedProject!!.colorsPath).readText()

  val stringsXml: String
    get() = File(openedProject!!.stringsPath).readText()

  val formattedProjectName: String
    get() {
      var projectName = openedProject!!.name.lowercase(Locale.getDefault()).trim { it <= ' ' }
      if (projectName.contains(" ")) {
        projectName = projectName.replace(" ".toRegex(), "_")
      }
      if (!projectName.endsWith(".xml")) {
        projectName = "$projectName.xml"
      }
      return projectName
    }

  fun getPalette(position: Int, context: Context): List<HashMap<String, Any>> {
    return if (paletteList.isNotEmpty()) paletteList[position]
    else {
      initPalette(context)
      paletteList[position]
    }
  }

  fun initPalette(context: Context) {
    val gson = Gson()
    val type = object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
    paletteList.clear()
    paletteList.add(convertJsonToJavaObject(context, gson, type, Constants.PALETTE_COMMON))
    paletteList.add(convertJsonToJavaObject(context, gson, type, Constants.PALETTE_TEXT))
    paletteList.add(convertJsonToJavaObject(context, gson, type, Constants.PALETTE_BUTTONS))
    paletteList.add(convertJsonToJavaObject(context, gson, type, Constants.PALETTE_WIDGETS))
    paletteList.add(convertJsonToJavaObject(context, gson, type, Constants.PALETTE_LAYOUTS))
    paletteList.add(convertJsonToJavaObject(context, gson, type, Constants.PALETTE_CONTAINERS))
    // paletteList.add(convertJsonToJavaObject(context, gson, type, Constants.PALETTE_GOOGLE));
    paletteList.add(convertJsonToJavaObject(context, gson, type, Constants.PALETTE_LEGACY))
  }

  private fun convertJsonToJavaObject(
    context: Context,
    gson: Gson,
    type: Type,
    filePath: String
  ): ArrayList<HashMap<String, Any>> {
    return gson.fromJson(
      context.assets.open(filePath).bufferedReader().use { it.readText() },
      type
    )
  }

  companion object {
    @JvmStatic
    val instance: ProjectManager by lazy { ProjectManager() }
  }
}