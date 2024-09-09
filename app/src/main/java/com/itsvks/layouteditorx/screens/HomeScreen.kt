package com.itsvks.layouteditorx.screens

import android.content.res.AssetManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.blankj.utilcode.util.FileUtils
import com.google.gson.Gson
import com.itsvks.layouteditorx.Constants
import com.itsvks.layouteditorx.activities.EditorActivity
import com.itsvks.layouteditorx.editor.dialogs.CancelButton
import com.itsvks.layouteditorx.extensions.open
import com.itsvks.layouteditorx.managers.ProjectManager
import com.itsvks.layouteditorx.models.Project
import com.itsvks.layouteditorx.screens.about.AboutScreen
import com.itsvks.layouteditorx.screens.project.ProjectsScreen
import com.itsvks.layouteditorx.screens.setting.SettingsScreen
import com.itsvks.layouteditorx.ui.ErrorMessage
import com.itsvks.layouteditorx.viemodels.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  val projectListState = rememberLazyListState()
  val navController = rememberNavController()
  val scope = rememberCoroutineScope()

  val context = LocalContext.current

  val expandedFab by remember {
    derivedStateOf {
      projectListState.firstVisibleItemIndex == 0
    }
  }

  var showNewProjectDialog by remember { mutableStateOf(false) }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = { HomeTopBar(scrollBehavior) },
    floatingActionButton = {
      AnimatedVisibility(
        navController.currentBackStackEntryAsState().value?.destination?.route == Screen.Projects.route,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        NewProjectButton(expandedFab) { showNewProjectDialog = true }
      }
    },
    floatingActionButtonPosition = FabPosition.End,
    bottomBar = { BottomNavigationBar(navController = navController) }
  ) { innerPadding ->
    NavHost(
      navController = navController,
      startDestination = Screen.Projects.route,
      modifier = Modifier.padding(innerPadding)
    ) {
      composable(Screen.Projects.route) { ProjectsScreen(viewModel, projectListState) }
      composable(Screen.Settings.route) { SettingsScreen() }
      composable(Screen.About.route) { AboutScreen() }
    }
  }

  if (showNewProjectDialog) {
    NewProjectDialog(
      scope = scope,
      onCreate = {
        viewModel.addProject(it)
        ProjectManager.instance.openProject(it)
        context.open(EditorActivity::class.java)
      }
    ) { showNewProjectDialog = false }
  }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
  val items = listOf(
    Screen.Projects,
    Screen.Settings,
    Screen.About
  )

  BottomAppBar {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    items.forEach { screen ->
      NavigationBarItem(
        icon = { Icon(screen.icon, contentDescription = screen.title) },
        label = { Text(screen.title) },
        selected = currentRoute == screen.route,
        onClick = {
          navController.navigate(screen.route) {
            popUpTo(navController.graph.startDestinationId) {
              saveState = true
            }
            launchSingleTop = true
            restoreState = true
          }
        },
        alwaysShowLabel = true
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
  scrollBehavior: TopAppBarScrollBehavior
) {
  LargeTopAppBar(
    title = {
      Text(
        text = "Layout Editor",
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.SansSerif,
        fontStyle = FontStyle.Normal
      )
    },
    scrollBehavior = scrollBehavior
  )
}

@Composable
fun NewProjectButton(
  expanded: Boolean,
  onClick: () -> Unit
) {
  ExtendedFloatingActionButton(
    onClick = onClick,
    text = { Text("New Project") },
    icon = { Icon(Icons.Filled.Add, contentDescription = "New Project") },
    expanded = expanded
  )
}

@Composable
fun NewProjectDialog(
  scope: CoroutineScope,
  onCreate: (Project) -> Unit,
  onDismiss: () -> Unit
) {
  var projectName by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val context = LocalContext.current

  fun checkError() {
    errorMessage = when {
      projectName.isEmpty() -> "Project name cannot be empty"
      else -> null
    }
  }

  LaunchedEffect(projectName) {
    checkError()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("New Project")
    },
    text = {
      Column {
        OutlinedTextField(
          value = projectName,
          onValueChange = { projectName = it },
          label = { Text("Project Name") },
          isError = errorMessage != null
        )
        errorMessage?.let { ErrorMessage(it) }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          scope.launch {
            val project = createProject(projectName, context.assets)
            onDismiss()
            onCreate(project)
          }
        },
        enabled = errorMessage == null
      ) { Text("Create") }
    },
    dismissButton = { CancelButton { onDismiss() } }
  )
}

private suspend fun createProject(name: String, assets: AssetManager): Project {
  val path = "${Constants.PROJECTS_PATH}/$name"
  val date = Calendar.getInstance().time.toString()

  val project = Project(path, date)

  withContext(Dispatchers.IO) {
    FileUtils.createOrExistsDir(path)

    val drawable = File("${project.drawablePath}/default_image.jpg")
    val strings = File(project.stringsPath)
    val colors = File(project.colorsPath)
    val layout = File(project.mainLayoutPath)
    val font = File("${project.fontPath}/default_font.ttf")
    FileUtils.createOrExistsFile(drawable)
    FileUtils.createOrExistsFile(strings)
    FileUtils.createOrExistsFile(colors)
    FileUtils.createOrExistsFile(layout)
    FileUtils.createOrExistsFile(font)

    assets.open("project/default_image.jpg").use {
      drawable.writeBytes(it.readBytes())
    }
    assets.open("project/default_font.ttf").use {
      font.writeBytes(it.readBytes())
    }
    assets.open("project/strings.xml").bufferedReader().use {
      strings.writeText(it.readText())
    }
    assets.open("project/colors.xml").bufferedReader().use {
      colors.writeText(it.readText())
    }

    val config = File("${project.path}/config.json")
    FileUtils.createOrExistsFile(config)
    config.writeText(Gson().toJson(project))
  }
  return project
}
