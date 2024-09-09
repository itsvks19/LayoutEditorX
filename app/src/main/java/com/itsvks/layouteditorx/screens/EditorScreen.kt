package com.itsvks.layouteditorx.screens

import android.content.ClipData
import android.content.Intent
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.itsvks.layouteditorx.Constants
import com.itsvks.layouteditorx.R
import com.itsvks.layouteditorx.activities.ShowXMLActivity
import com.itsvks.layouteditorx.editor.DesignEditor
import com.itsvks.layouteditorx.editor.RootLayout
import com.itsvks.layouteditorx.editor.ViewType
import com.itsvks.layouteditorx.editor.map.AttributeMap
import com.itsvks.layouteditorx.extensions.getEmptyActivityBundle
import com.itsvks.layouteditorx.extensions.toFile
import com.itsvks.layouteditorx.managers.ProjectManager
import com.itsvks.layouteditorx.models.Project
import com.itsvks.layouteditorx.tools.XmlLayoutGenerator
import com.itsvks.layouteditorx.ui.ToastHost
import com.itsvks.layouteditorx.ui.ToastHostState
import com.itsvks.layouteditorx.ui.rememberToastHostState
import com.itsvks.layouteditorx.utils.InvokeUtil
import com.itsvks.layouteditorx.viemodels.EditorViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun EditorScreen(project: Project, viewModel: EditorViewModel = viewModel()) {
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  val toastHostState = rememberToastHostState()

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        modifier = Modifier
          .fillMaxWidth()
          .systemBarsPadding()
          .padding(end = 100.dp),
        drawerShape = MaterialTheme.shapes.small
      ) {
        EditorDrawerContent(scope, drawerState)
      }
    },
    gesturesEnabled = true
  ) {
    MainEditorContent(project, viewModel, scope, toastHostState, drawerState)
    ToastHost(hostState = toastHostState)
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditorDrawerContent(
  scope: CoroutineScope,
  drawerState: DrawerState
) {
  var selectedPalette by remember { mutableIntStateOf(0) }
  val nestedScrollConnection = rememberNestedScrollInteropConnection()
  val context = LocalContext.current

  val paletteNames = listOf(
    Constants.TAB_TITLE_COMMON,
    Constants.TAB_TITLE_TEXT,
    Constants.TAB_TITLE_BUTTONS,
    Constants.TAB_TITLE_WIDGETS,
    Constants.TAB_TITLE_LAYOUTS,
    Constants.TAB_TITLE_CONTAINERS,
    Constants.TAB_TITLE_LEGACY
  )
  val paletteIcons = listOf(
    painterResource(R.drawable.android),
    painterResource(R.drawable.ic_palette_text_view),
    painterResource(R.drawable.ic_palette_button),
    painterResource(R.drawable.ic_palette_view),
    painterResource(R.drawable.ic_palette_relative_layout),
    painterResource(R.drawable.ic_palette_view_pager),
    painterResource(R.drawable.ic_palette_grid_layout)
  )

  val haptic = LocalHapticFeedback.current

  Row {
    NavigationRail {
      paletteNames.forEachIndexed { index, name ->
        NavigationRailItem(
          icon = { Icon(paletteIcons[index], contentDescription = name) },
          label = { Text(name) },
          selected = selectedPalette == index,
          onClick = { selectedPalette = index },
          alwaysShowLabel = true
        )
      }
    }

    LazyColumn(
      modifier = Modifier
        .padding(3.dp)
        .nestedScroll(nestedScrollConnection),
      contentPadding = PaddingValues(2.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      val palettes = ProjectManager.instance.getPalette(selectedPalette, context)

      items(palettes) { widgetItem ->
        var iconId: Int
        try {
          val drawableCls = R.drawable::class.java
          val field = drawableCls.getField(widgetItem["iconName"].toString())
          iconId = field.getInt(drawableCls)
        } catch (err: Exception) {
          err.printStackTrace()
          iconId = R.drawable.android
        }
        val name = widgetItem["name"].toString()
        val className = InvokeUtil.getSuperClassName(widgetItem["className"].toString())
        EditorPaletteItem(
          modifier = Modifier
            .animateItem()
            .dragAndDropSource {
              detectTapGestures(onLongPress = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch { drawerState.close() }

                startTransfer(
                  DragAndDropTransferData(
                    ClipData.newPlainText("name", name),
                    localState = widgetItem
                  )
                )
              })
            },
          name = name,
          className = className.toString(),
          icon = painterResource(iconId)
        )
      }
    }
  }
}

@Composable
fun EditorPaletteItem(
  modifier: Modifier = Modifier,
  name: String,
  className: String,
  icon: Painter
) {
  ElevatedCard(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    shape = MaterialTheme.shapes.small
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.padding(4.dp)
    ) {
      Icon(
        painter = icon,
        contentDescription = name,
        modifier = Modifier
          .padding(7.dp)
      )

      Column(
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = name,
          fontSize = 13.sp,
          maxLines = 1,
          softWrap = false,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.basicMarquee(
            animationMode = MarqueeAnimationMode.Immediately,
            repeatDelayMillis = 4000
          )
        )

        Text(
          text = className,
          fontSize = 9.sp,
          maxLines = 1,
          softWrap = false,
          style = LocalTextStyle.current.copy(
            textMotion = TextMotion.Animated
          ),
          color = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.horizontalScroll(state = rememberScrollState())
        )
      }
    }
  }
}

@Composable
fun MainEditorContent(
  project: Project,
  viewModel: EditorViewModel,
  scope: CoroutineScope,
  toastHostState: ToastHostState,
  drawerState: DrawerState
) {
  var layout by remember { mutableStateOf<RootLayout?>(null) }
  var viewAttributeMap by remember { mutableStateOf(mapOf<View, AttributeMap>()) }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      EditorTopBar(
        project = project,
        scope = scope,
        layout = layout,
        viewAttributeMap = viewAttributeMap,
        drawerState = drawerState
      ) {
        scope.launch {
          toastHostState.showToast(
            message = "Saved",
            icon = Icons.Rounded.Done
          )
        }
      }
    }
  ) { innerPadding ->
    DesignEditor(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(16.dp),
      viewType = ViewType.DESIGN,
      project = project,
      scope = scope,
      toastHostState = toastHostState
    ) { l, vm ->
      layout = l
      viewAttributeMap = vm
      File(project.mainLayoutPath).readText()
    }
  }
}

fun saveXml(project: Project, layout: RootLayout, viewAttributeMap: Map<View, AttributeMap>) {
  if (layout.childCount == 0) {
    project.mainLayoutPath.toFile().writeText("")
  } else {
    val result = XmlLayoutGenerator().generate(layout, viewAttributeMap, false)
    project.mainLayoutPath.toFile().writeText(result)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTopBar(
  project: Project,
  layout: RootLayout?,
  viewAttributeMap: Map<View, AttributeMap>,
  scope: CoroutineScope,
  drawerState: DrawerState,
  onSave: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  TopAppBar(
    title = {
      Column {
        Text(
          text = "Layout Editor",
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.SansSerif
        )
        Text(
          text = project.name,
          style = MaterialTheme.typography.bodyMedium
        )
      }
    },
    actions = {
      TextButton(onClick = {
        if (layout != null) {
          saveXml(
            project = project,
            layout = layout,
            viewAttributeMap = viewAttributeMap
          )
          onSave()
        }
      }) {
        Text("Save")
      }

      IconButton(onClick = { showMenu = true }) {
        Icon(
          imageVector = Icons.Default.MoreVert,
          contentDescription = null
        )
      }
      EditorMenu(
        expanded = showMenu,
        layout = layout,
        viewAttributeMap = viewAttributeMap
      ) { showMenu = false }
    },
    navigationIcon = {
      IconButton(onClick = {
        scope.launch {
          drawerState.apply {
            if (isClosed) open() else close()
          }
        }
      }) {
        Icon(
          imageVector = Icons.Default.Menu,
          contentDescription = "Menu"
        )
      }
    }
  )
}

@Composable
fun EditorMenu(
  expanded: Boolean,
  layout: RootLayout?,
  viewAttributeMap: Map<View, AttributeMap>,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
    DropdownMenuItem(
      text = { Text("Show XML") },
      leadingIcon = { Icon(painter = painterResource(R.drawable.xml), contentDescription = null) },
      onClick = {
        onDismiss()
        val result = layout?.let { XmlLayoutGenerator().generate(it, viewAttributeMap, true) } ?: ""
        context.startActivity(
          Intent(context, ShowXMLActivity::class.java).putExtra(ShowXMLActivity.EXTRA_XML, result),
          context.getEmptyActivityBundle()
        )
      }
    )
  }
}