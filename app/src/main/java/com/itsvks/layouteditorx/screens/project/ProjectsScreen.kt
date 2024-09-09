package com.itsvks.layouteditorx.screens.project

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.FileUtils
import com.itsvks.layouteditorx.activities.EditorActivity
import com.itsvks.layouteditorx.editor.dialogs.CancelButton
import com.itsvks.layouteditorx.extensions.open
import com.itsvks.layouteditorx.managers.ProjectManager
import com.itsvks.layouteditorx.models.Project
import com.itsvks.layouteditorx.ui.ErrorMessage
import com.itsvks.layouteditorx.viemodels.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun ProjectsScreen(
  viewModel: HomeViewModel,
  projectListState: LazyListState,
  modifier: Modifier = Modifier
) {
  val projectState by viewModel.projectState
  val scope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    scope.launch { viewModel.loadProjects() }
  }

  if (projectState.isLoading) {
    LoadingIndicator()
  } else if (projectState.projects.isEmpty()) {
    NoProjectsScreen()
  } else {
    ProjectsList(
      state = projectListState,
      modifier = modifier,
      projects = projectState.projects,
      viewModel = viewModel
    )
  }
}

@Composable
fun NoProjectsScreen() {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Text("No projects found.")
  }
}

@Composable
fun LoadingIndicator() {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    CircularProgressIndicator()
  }
}

@Composable
fun ProjectsList(
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel,
  state: LazyListState = rememberLazyListState(),
  projects: List<Project>
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    state = state,
    contentPadding = PaddingValues(5.dp),
    verticalArrangement = Arrangement.spacedBy(5.dp)
  ) {
    items(projects) { project ->
      ProjectItem(project, viewModel = viewModel)
    }
  }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProjectItem(project: Project, viewModel: HomeViewModel) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current

  var showProjectActionSheet by remember { mutableStateOf(false) }

  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .padding(2.dp)
      .combinedClickable(
        onLongClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          showProjectActionSheet = true
        }
      ) {
        ProjectManager.instance.openProject(project)
        context.open(EditorActivity::class.java)
      },
    shape = MaterialTheme.shapes.small
  ) {
    ListItem(
      headlineContent = { Text(project.name) },
      supportingContent = { Text(project.date) },
      colors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        headlineColor = MaterialTheme.colorScheme.primary,
        supportingColor = MaterialTheme.colorScheme.tertiary
      )
    )
  }

  if (showProjectActionSheet) {
    ProjectActionSheet(
      project = project,
      viewModel = viewModel
    ) { showProjectActionSheet = false }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectActionSheet(
  project: Project,
  viewModel: HomeViewModel,
  sheetState: SheetState = rememberModalBottomSheetState(),
  onDismiss: () -> Unit
) {
  var showRenameDialog by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }

  val projectState by viewModel.projectState

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState
  ) {
    LazyColumn {
      item {
        ProjectActionItem(
          text = "Rename",
          icon = Icons.Outlined.Edit
        ) { showRenameDialog = true }
      }
      item {
        ProjectActionItem(
          text = "Delete",
          icon = Icons.Outlined.Delete
        ) { showDeleteDialog = true }
      }
    }
  }

  if (showRenameDialog) {
    RenameDialog(
      project = project,
      projects = projectState.projects,
      onRename = {
        viewModel.updateProject(project, it)
        onDismiss()
      }
    ) { showRenameDialog = false }
  }

  if (showDeleteDialog) {
    DeleteProjectDialog(onConfirm = {
      showDeleteDialog = false
      viewModel.removeProject(project)
      FileUtils.delete(project.path)
      onDismiss()
    }) { showDeleteDialog = false }
  }
}

@Composable
fun ProjectActionItem(
  text: String,
  icon: ImageVector,
  onClick: () -> Unit
) {
  ElevatedCard(
    modifier = Modifier
      .padding(3.dp),
    onClick = onClick
  ) {
    ListItem(
      headlineContent = { Text(text) },
      leadingContent = { Icon(icon, contentDescription = null) },
      colors = ListItemDefaults.colors(
        headlineColor = MaterialTheme.colorScheme.secondary,
        leadingIconColor = MaterialTheme.colorScheme.tertiary
      )
    )
  }
}

@Composable
fun RenameDialog(
  project: Project,
  projects: List<Project>,
  onRename: (Project) -> Unit,
  onDismiss: () -> Unit
) {
  val oldName = project.name

  var newName by remember { mutableStateOf(project.name) }
  var error by remember { mutableStateOf<String?>(null) }

  fun checkError(name: String) {
    error = if (name.isEmpty()) {
      "Name cannot be empty"
    } else {
      val nameExists = projects.any { it.name == name && it.name != oldName }
      if (nameExists) "Current name is unavailable" else null
    }
  }

  LaunchedEffect(newName) { checkError(newName) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Rename Project")
    },
    text = {
      Column {
        OutlinedTextField(
          value = newName,
          onValueChange = {
            newName = it
            checkError(it)
          },
          label = { Text("New Name") },
          isError = error != null
        )
        error?.let { ErrorMessage(it) }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val path = project.path
          val newPath = "${path.substringBeforeLast("/")}/$newName"
          FileUtils.rename(path, newName)
          val newProject = Project(newPath, project.date)
          onRename(newProject)
          onDismiss()
        },
        enabled = error == null
      ) { Text("Rename") }
    },
    dismissButton = { CancelButton { onDismiss() } }
  )
}

@Composable
fun DeleteProjectDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Delete Project") },
    text = { Text("Do you want to delete this project?") },
    confirmButton = {
      Button(onClick = onConfirm) { Text("Delete") }
    },
    dismissButton = { CancelButton { onDismiss() } }
  )
}
