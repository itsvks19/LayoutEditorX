package com.itsvks.layouteditorx.viemodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.itsvks.layouteditorx.Constants
import com.itsvks.layouteditorx.models.Project
import java.io.File

data class ProjectState(var projects: List<Project>, var isLoading: Boolean)

class HomeViewModel : ViewModel() {
  private val _projectState = mutableStateOf(ProjectState(listOf(), isLoading = true))
  val projectState: State<ProjectState> get() = _projectState

  fun addProject(project: Project) {
    val updatedProjects = _projectState.value.projects.toMutableList()
    updatedProjects.add(project)

    _projectState.value = _projectState.value.copy(projects = updatedProjects)
  }

  fun updateProject(project: Project, newProject: Project) {
    val updatedProjects = _projectState.value.projects.map {
      if (it.path == project.path) newProject
      else it
    }

    _projectState.value = _projectState.value.copy(projects = updatedProjects)
  }

  fun removeProject(project: Project) {
    val updatedProjects = _projectState.value.projects.filter { project !== it }
    _projectState.value = _projectState.value.copy(projects = updatedProjects)
  }

  fun loadProjects() {
    _projectState.value = _projectState.value.copy(isLoading = true)

    val projects = mutableListOf<Project>()
    val projectsHome = File(Constants.PROJECTS_PATH)
    if (!projectsHome.exists()) projectsHome.mkdirs()

    projectsHome.listFiles()?.forEach {
      val config = it.resolve("config.json")
      if (!config.exists()) return@forEach

      val project: Project
      try {
        val json = config.readText()
        project = Gson().fromJson(json, Project::class.java)
      } catch (err: Exception) {
        err.printStackTrace()
        return@forEach
      }

      projects.add(project)
    }

    _projectState.value = ProjectState(projects = projects, isLoading = false)
  }
}