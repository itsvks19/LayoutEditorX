package com.itsvks.layouteditorx.editor

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.DragEvent
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itsvks.layouteditorx.Constants
import com.itsvks.layouteditorx.editor.dialogs.BooleanDialog
import com.itsvks.layouteditorx.editor.dialogs.CancelButton
import com.itsvks.layouteditorx.editor.dialogs.ColorDialog
import com.itsvks.layouteditorx.editor.dialogs.DialogState
import com.itsvks.layouteditorx.editor.dialogs.DialogType
import com.itsvks.layouteditorx.editor.dialogs.DimensionDialog
import com.itsvks.layouteditorx.editor.dialogs.EnumDialog
import com.itsvks.layouteditorx.editor.dialogs.FlagDialog
import com.itsvks.layouteditorx.editor.dialogs.IdDialog
import com.itsvks.layouteditorx.editor.dialogs.NumberDialog
import com.itsvks.layouteditorx.editor.dialogs.SizeDialog
import com.itsvks.layouteditorx.editor.dialogs.StringDialog
import com.itsvks.layouteditorx.editor.dialogs.ViewDialog
import com.itsvks.layouteditorx.editor.initializer.AttributeInitializer
import com.itsvks.layouteditorx.editor.map.AttributeMap
import com.itsvks.layouteditorx.extensions.toFile
import com.itsvks.layouteditorx.managers.IdManager
import com.itsvks.layouteditorx.models.Project
import com.itsvks.layouteditorx.parser.XmlLayoutParser
import com.itsvks.layouteditorx.ui.ToastHostState
import com.itsvks.layouteditorx.utils.ArgumentUtil
import com.itsvks.layouteditorx.utils.InvokeUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

class RootLayout @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DesignEditor(
  modifier: Modifier = Modifier,
  viewType: ViewType = ViewType.DESIGN,
  project: Project,
  scope: CoroutineScope,
  toastHostState: ToastHostState,
  update: (RootLayout?, Map<View, AttributeMap>) -> String
) {
  val shape = MaterialTheme.shapes.extraSmall
  val layouts = remember { mutableStateListOf<View>() }
  val ctx = LocalContext.current
  val haptic = LocalHapticFeedback.current

  var viewAttributeMap = remember { mutableStateMapOf<View, AttributeMap>() }
  var rootLayout by remember { mutableStateOf<RootLayout?>(null) }

  val attributes = convertJsonToJavaObject(Constants.ATTRIBUTES_FILE, ctx)
  val parentAttributes = convertJsonToJavaObject(Constants.PARENT_ATTRIBUTES_FILE, ctx)
  val initializer = remember {
    mutableStateOf(
      AttributeInitializer(
        ctx,
        attributes,
        parentAttributes,
        viewAttributeMap
      )
    )
  }

  var showShadow by remember { mutableStateOf(false) }
  var currentlyClickedView by remember { mutableStateOf<View?>(null) }

  rootLayout?.let { update(it, viewAttributeMap) }

  LaunchedEffect(rootLayout, viewAttributeMap) {
    update(rootLayout, viewAttributeMap)
  }

  val dragCallback = remember {
    object : DragAndDropTarget {
      override fun onEntered(event: DragAndDropEvent) {
        super.onEntered(event)
        showShadow = true
      }

      override fun onExited(event: DragAndDropEvent) {
        super.onExited(event)
        showShadow = false
      }

      override fun onDrop(event: DragAndDropEvent): Boolean {
        val draggedView = event.toAndroidDragEvent().localState as? View
        if (draggedView == null) {
          val newView = createNewView(event, ctx)
          layouts.add(newView)
        } else {
          layouts.add(draggedView)
        }
        return true
      }
    }
  }

  currentlyClickedView?.let {
    DefinedAttributesSheet(
      view = it,
      viewAttributeMap = viewAttributeMap,
      initializer = initializer.value,
      onDeleteClick = {
        IdManager.instance.removeId(it, it is ViewGroup)
        removeViewAttributes(it, viewAttributeMap)
        removeWidget(it)
      },
      onSaveAttribute = { v -> currentlyClickedView = v },
      onDeleteAttributeClick = { key ->
        val view = removeAttribute(
          hapticFeedback = haptic,
          target = it,
          viewAttributeMap = viewAttributeMap,
          initializer = initializer.value,
          attributeKey = key
        ) { v -> currentlyClickedView = v }
        currentlyClickedView = view
      }
    ) { currentlyClickedView = null }
  }

  Box(
    modifier = modifier
      .background(
        when (viewType) {
          ViewType.DESIGN -> MaterialTheme.colorScheme.surface
          ViewType.BLUEPRINT -> Color(Constants.BLUEPRINT_BACKGROUND_COLOR)
        },
        shape = shape
      )
      .dragAndDropTarget(
        shouldStartDragAndDrop = { true },
        target = dragCallback
      )
      .then(
        drawStroke(
          shape = shape,
          viewType = viewType
        )
      )
  ) {
    if (showShadow) {
      ShadowBox(viewType)
    }

    AndroidView(
      factory = {
        val shadow = ComposeView(it).apply {
          setContent {
            ShadowBox(viewType)
          }
        }

        RootLayout(it).apply {
          rootLayout = this

          fun getIndexForNewChildOfLinear(layout: LinearLayout, event: DragEvent): Int {
            val orientation = layout.orientation
            if (orientation == LinearLayout.HORIZONTAL) {
              var index = 0
              for (i in 0 until layout.childCount) {
                val child = layout.getChildAt(i)
                if (child === shadow) continue
                if (child.right < event.x) index++
              }
              return index
            }
            if (orientation == LinearLayout.VERTICAL) {
              var index = 0
              for (i in 0 until layout.childCount) {
                val child = layout.getChildAt(i)
                if (child === shadow) continue
                if (child.bottom < event.y) index++
              }
              return index
            }
            return -1
          }

          fun addWidget(view: View, newParent: ViewGroup, event: DragEvent) {
            removeWidget(view)
            if (newParent is LinearLayout) {
              val index = getIndexForNewChildOfLinear(newParent, event)
              newParent.addView(view, index)
            } else {
              try {
                newParent.addView(view, newParent.childCount)
              } catch (e: Exception) {
                e.printStackTrace()
              }
            }
          }

          fun ViewGroup.setDragListener(
            initializer: AttributeInitializer
          ) {
            setOnDragListener(
              View.OnDragListener { host, event ->
                var parent = host as ViewGroup
                val draggedView =
                  if (event.localState is View) event.localState as View else null

                when (event.action) {
                  DragEvent.ACTION_DRAG_STARTED -> {
                    if ((draggedView != null && !(draggedView is AdapterView<*> && parent is AdapterView<*>))
                    ) parent.removeView(draggedView)
                  }

                  DragEvent.ACTION_DRAG_EXITED -> {
                    removeWidget(shadow)
                  }

                  DragEvent.ACTION_DRAG_ENDED -> if (!event.result && draggedView != null) {
                    IdManager.instance.removeId(draggedView, draggedView is ViewGroup)
                    removeViewAttributes(draggedView, viewAttributeMap)
                    viewAttributeMap.remove(draggedView)
                  }

                  DragEvent.ACTION_DRAG_LOCATION, DragEvent.ACTION_DRAG_ENTERED -> if (shadow.parent == null) addWidget(
                    shadow,
                    parent,
                    event
                  )
                  else {
                    if (parent is LinearLayout) {
                      val index = parent.indexOfChild(shadow)
                      val newIndex = getIndexForNewChildOfLinear(parent, event)

                      if (index != newIndex) {
                        parent.removeView(shadow)
                        try {
                          parent.addView(shadow, newIndex)
                        } catch (_: IllegalStateException) {
                        }
                      }
                    } else {
                      if (shadow.parent !== parent) addWidget(shadow, parent, event)
                    }
                  }

                  DragEvent.ACTION_DROP -> {
                    removeWidget(shadow)
                    if (this@apply.childCount >= 1) {
                      if (this@apply.getChildAt(0) !is ViewGroup) {
                        scope.launch {
                          toastHostState.showToast(
                            message = "Can't add more than one widget in the editor.",
                            icon = Icons.Rounded.Close
                          )
                        }
                        return@OnDragListener true
                      } else {
                        if (parent is RootLayout) parent = this@apply.getChildAt(0) as ViewGroup
                      }
                    }
                    if (draggedView == null) {
                      @Suppress("UNCHECKED_CAST")
                      val data = event.localState as HashMap<String, Any>
                      val newView = InvokeUtil.createView(
                        data[Constants.KEY_CLASS_NAME].toString(), context
                      ) as View

                      newView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                      )

                      rearrangeListeners(haptic, newView) { v ->
                        currentlyClickedView = v
                      }

                      if (newView is ViewGroup) {
                        newView.setDragListener(initializer)
                        setTransition(newView)
                      }
                      newView.minimumWidth = SizeUtils.dp2px(20f)
                      newView.minimumHeight = SizeUtils.dp2px(20f)

                      if (newView is EditText) newView.isFocusable = false

                      val map = AttributeMap()
                      val id = getIdForNewView(
                        newView.javaClass.superclass.simpleName
                          .replace(" ".toRegex(), "_")
                          .lowercase()
                      )
                      IdManager.instance.addNewId(newView, id)
                      map["android:id"] = "@+id/$id"
                      map["android:layout_width"] = "wrap_content"
                      map["android:layout_height"] = "wrap_content"
                      viewAttributeMap[newView] = map

                      addWidget(newView, parent, event)

                      try {
                        val cls: Class<*> = newView.javaClass
                        val setStrokeEnabled =
                          cls.getMethod("setStrokeEnabled", Boolean::class.javaPrimitiveType)
                        val setBlueprint =
                          cls.getMethod("setBlueprint", Boolean::class.javaPrimitiveType)
                        setStrokeEnabled.invoke(newView, true)
                        setBlueprint.invoke(newView, viewType == ViewType.BLUEPRINT)
                      } catch (e: Exception) {
                        e.printStackTrace()
                      }

                      if (data.containsKey(Constants.KEY_DEFAULT_ATTRS)) {
                        @Suppress("UNCHECKED_CAST")
                        initializer.applyDefaultAttributes(
                          newView, data[Constants.KEY_DEFAULT_ATTRS] as MutableMap<String, String>
                        )
                      }
                    } else addWidget(draggedView, parent, event)
                  }
                }
                true
              })
          }

          fun clearAll() {
            removeAllViews()
            viewAttributeMap.clear()
          }

          fun setStrokeOnChildren() {
            try {
              for (view in viewAttributeMap.keys) {
                val cls = view.javaClass
                val method = cls.getMethod("setStrokeEnabled", Boolean::class.javaPrimitiveType)
                method.invoke(view, true)
              }
            } catch (e: Exception) {
              e.printStackTrace()
            }
          }

          fun setBlueprintOnChildren() {
            try {
              for (view in viewAttributeMap.keys) {
                val cls = view.javaClass
                val method = cls.getMethod("setBlueprint", Boolean::class.javaPrimitiveType)
                method.invoke(view, viewType == ViewType.BLUEPRINT)
              }
            } catch (e: Exception) {
              e.printStackTrace()
            }
          }

          fun loadLayoutFromParser(xml: String) {
            clearAll()

            if (xml.isEmpty()) return

            val parser = XmlLayoutParser(context)
            parser.parseFromXml(xml, context)

            addView(parser.root)
            viewAttributeMap.putAll(parser.viewAttributeMap)

            for (view in (viewAttributeMap as? Map<View, AttributeMap>)!!.keys) {
              rearrangeListeners(
                hapticFeedback = haptic,
                view = view
              ) { v -> currentlyClickedView = v }

              if (view is ViewGroup) {
                view.setDragListener(initializer.value)
                setTransition(view)
              }
              view.minimumWidth = SizeUtils.dp2px(20f)
              view.minimumHeight = SizeUtils.dp2px(20f)
            }

            setStrokeOnChildren()
            setBlueprintOnChildren()

            initializer.value = AttributeInitializer(
              context,
              attributes,
              parentAttributes,
              viewAttributeMap
            )
          }

          setDragListener(initializer.value)
          setTransition(this)

          layouts.forEach { view ->
            addView(view)
          }

          loadLayoutFromParser(project.mainLayoutPath.toFile().readText())
        }
      },
      modifier = Modifier
        .fillMaxSize()
        .clip(shape)
    ) {
      it.updateLayoutParams<ViewGroup.LayoutParams> {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
      }
    }
  }
}

@Composable
private fun drawStroke(shape: Shape, viewType: ViewType): Modifier {
  return Modifier.drawBehind {
    val strokeWidth = 2.dp.toPx()
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f), phase = 0f)

    drawIntoCanvas {
      drawOutline(
        outline = shape.createOutline(size, layoutDirection, this),
        color = when (viewType) {
          ViewType.BLUEPRINT -> Color(Constants.BLUEPRINT_DASH_COLOR)
          ViewType.DESIGN -> Color(Constants.DESIGN_DASH_COLOR)
        },
        style = Stroke(
          width = strokeWidth,
          pathEffect = pathEffect
        )
      )
    }
  }
}

@Composable
fun ShadowBox(viewType: ViewType) {
  Box(
    modifier = Modifier
      .widthIn(50.dp)
      .heightIn(35.dp)
      .background(
        when (viewType) {
          ViewType.BLUEPRINT -> Color.White.copy(alpha = 0.5f)
          ViewType.DESIGN -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        }
      )
      .clip(shape = MaterialTheme.shapes.medium)
  )
}

fun removeViewAttributes(view: View, viewAttributeMap: MutableMap<View, AttributeMap>) {
  viewAttributeMap.remove(view)
  if (view is ViewGroup) {
    for (i in 0 until view.childCount) {
      removeViewAttributes(view.getChildAt(i), viewAttributeMap)
    }
  }
}

fun removeWidget(view: View) {
  (view.parent as ViewGroup?)?.removeView(view)
}

private fun rearrangeListeners(
  hapticFeedback: HapticFeedback,
  view: View,
  onClick: (View) -> Unit
) {
  val gestureDetector =
    GestureDetector(
      view.context,
      object : GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(event: MotionEvent) {
          hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
          view.startDragAndDrop(null, View.DragShadowBuilder(view), view, 0)
        }
      })

  view.setOnTouchListener(
    object : View.OnTouchListener {
      var bClick: Boolean = true
      var startX: Float = 0f
      var startY: Float = 0f
      var endX: Float = 0f
      var endY: Float = 0f
      var diffX: Float = 0f
      var diffY: Float = 0f

      @SuppressLint("ClickableViewAccessibility")
      override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
          MotionEvent.ACTION_DOWN -> {
            startX = event.x
            startY = event.y
            bClick = true
          }

          MotionEvent.ACTION_UP -> {
            endX = event.x
            endY = event.y
            diffX = abs((startX - endX).toDouble()).toFloat()
            diffY = abs((startY - endY).toDouble()).toFloat()

            if ((diffX <= 5) && (diffY <= 5) && bClick) onClick(v)

            bClick = false
          }
        }
        gestureDetector.onTouchEvent(event)
        return true
      }
    })
}

fun getIdForNewView(name: String): String {
  var id = name
  var n = 0
  var firstTime = true
  while (IdManager.instance.containsId(id)) {
    n++
    id = if (firstTime) "$name$n" else id.replace(
      id.elementAt(id.lastIndex).toString().toRegex(), n.toString()
    )
    firstTime = false
  }
  return id
}

@Suppress("UNCHECKED_CAST")
fun createNewView(event: DragAndDropEvent, context: Context): View {
  val data = event.toAndroidDragEvent().localState as HashMap<String, Any>
  return InvokeUtil.createView(data[Constants.KEY_CLASS_NAME].toString(), context) as View
}

private fun convertJsonToJavaObject(
  filePath: String,
  context: Context
): HashMap<String, ArrayList<HashMap<String, Any>>> {
  return Gson()
    .fromJson(
      context.assets.open(filePath).bufferedReader().use { it.readText() },
      object : TypeToken<HashMap<String, ArrayList<HashMap<String, Any>>>>() {}.type
    )
}

private fun setTransition(group: ViewGroup) {
  if (group is RecyclerView) return
  LayoutTransition().apply {
    disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
    enableTransitionType(LayoutTransition.CHANGING)
    setDuration(150)
  }.also { group.layoutTransition = it }
}

private fun removeAttribute(
  hapticFeedback: HapticFeedback,
  target: View,
  attributeKey: String,
  initializer: AttributeInitializer,
  viewAttributeMap: MutableMap<View, AttributeMap>,
  onClick: (View) -> Unit
): View {
  @Suppress("NAME_SHADOWING")
  var target = target
  val allAttrs = initializer.getAllAttributesForView(target)
  val currentAttr =
    initializer.getAttributeFromKey(attributeKey, allAttrs)

  val attributeMap = viewAttributeMap[target]

  if (currentAttr != null) {
    if (currentAttr.containsKey(Constants.KEY_CAN_DELETE)) return target
  }

  val name =
    if (attributeMap!!.contains("android:id")) attributeMap["android:id"] else null
  val id = if (name != null) IdManager.instance.getViewId(name.replace("@+id/", "")) else -1
  attributeMap.remove(attributeKey)

  if ((attributeKey == "android:id")) {
    IdManager.instance.removeId(target, false)
    target.id = -1
    target.requestLayout()

    // delete all id attributes for views
    for (view: View in viewAttributeMap.keys) {
      val map = viewAttributeMap[view]

      for (key: String in map!!.keySet()) {
        val value = map[key]

        if (value.startsWith("@id/") && (value == name!!.replace("+", ""))) map.remove(key)
      }
    }
//    updateStructure()
    return target
  }

  viewAttributeMap.remove(target)

  val parent = target.parent as ViewGroup
  val indexOfView = parent.indexOfChild(target)

  parent.removeView(target)

  val childs: MutableList<View> = ArrayList()

  if (target is ViewGroup) {
    val group = target

    if (group.childCount > 0) {
      for (i in 0 until group.childCount) {
        childs.add(group.getChildAt(i))
      }
    }

    group.removeAllViews()
  }

  if (name != null) IdManager.instance.removeId(target, false)

  target = InvokeUtil.createView(target.javaClass.name, target.context) as View
  rearrangeListeners(hapticFeedback, target, onClick = onClick)

  if (target is ViewGroup) {
    target.setMinimumWidth(SizeUtils.dp2px(20f))
    target.setMinimumHeight(SizeUtils.dp2px(20f))
    val group = target
    if (childs.size > 0) {
      for (i in childs.indices) {
        group.addView(childs[i])
      }
    }
    setTransition(group)
  }

  parent.addView(target, indexOfView)
  viewAttributeMap[target] = attributeMap

  if (name != null) {
    IdManager.instance.addId(target, name, id)
    target.requestLayout()
  }

  val keys = attributeMap.keySet()
  val values = attributeMap.values()
  val attrs: MutableList<HashMap<String, Any>> = ArrayList()

  for (key: String in keys) {
    for (map: HashMap<String, Any> in allAttrs) {
      if ((map[Constants.KEY_ATTRIBUTE_NAME].toString() == key)) {
        attrs.add(map)
        break
      }
    }
  }

  for (i in keys.indices) {
    val key = keys[i]
    if ((key == "android:id")) continue
    initializer.applyAttribute(target, values[i], attrs[i])
  }

  try {
    val cls: Class<*> = target.javaClass
    val method = cls.getMethod("setStrokeEnabled", Boolean::class.javaPrimitiveType)
    method.invoke(target, true)
  } catch (e: Exception) {
    e.printStackTrace()
  }
//  updateStructure()
//  updateUndoRedoHistory()
  return target
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefinedAttributesSheet(
  view: View,
  viewAttributeMap: MutableMap<View, AttributeMap>,
  initializer: AttributeInitializer,
  sheetState: SheetState = rememberModalBottomSheetState(),
  onSaveAttribute: (View) -> Unit,
  onDeleteClick: () -> Unit,
  onDeleteAttributeClick: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var showDeleteViewDialog by remember { mutableStateOf(false) }
  var showAvailableAttrsDialog by remember { mutableStateOf(false) }

  var selectedAttr by remember { mutableStateOf<Map<String, Any>?>(null) }

  val keys = viewAttributeMap[view]!!.keySet()
  val values = viewAttributeMap[view]!!.values()

  val attrs = mutableListOf<Map<String, Any>>()
  val allAttrs = initializer.getAllAttributesForView(view)

  keys.forEach { key ->
    allAttrs.find { it[Constants.KEY_ATTRIBUTE_NAME]?.toString() == key }?.let { attrs.add(it) }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = view.javaClass.superclass.simpleName,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = view.javaClass.superclass.name,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = {
          showAvailableAttrsDialog = true
        }) {
          Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Add",
            tint = MaterialTheme.colorScheme.tertiary
          )
        }

        IconButton(onClick = {
          showDeleteViewDialog = true
        }) {
          Icon(
            imageVector = Icons.Rounded.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.tertiary
          )
        }
      }

      HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.secondary
      )

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
        contentPadding = PaddingValues(5.dp)
      ) {
        items(attrs) { attr ->
          AppliedAttributesItem(
            canDelete = !attr.containsKey(Constants.KEY_CAN_DELETE),
            attrName = attr["name"].toString(),
            attrValue = values[attrs.indexOf(attr)],
            onDeleteClick = {
              onDismiss()
              onDeleteAttributeClick(keys[attrs.indexOf(attr)])
            }
          ) { selectedAttr = attr }
        }
      }
    }
  }

  if (showDeleteViewDialog) {
    DeleteViewDialog(onDismiss = { showDeleteViewDialog = false }) {
      showDeleteViewDialog = false
      onDismiss()
      onDeleteClick()
    }
  }

  if (showAvailableAttrsDialog) {
    AvailableAttributeDialog(
      view = view,
      initializer = initializer,
      viewAttributeMap = viewAttributeMap,
      onSaveAttribute = onSaveAttribute,
      onDismiss = { showAvailableAttrsDialog = false }
    )
  }

  selectedAttr?.let { attr ->
    AttributeEditDialog(
      view = view,
      attributeKey = attr[Constants.KEY_ATTRIBUTE_NAME].toString(),
      initializer = initializer,
      viewAttributeMap = viewAttributeMap,
      onSave = { onSaveAttribute(it) },
      onDismiss = { selectedAttr = null }
    )
  }
}

@Composable
fun DeleteViewDialog(
  onDismiss: () -> Unit,
  onConfirm: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = "Delete view") },
    text = { Text(text = "Do you want to remove the view?") },
    confirmButton = {
      Button(onClick = onConfirm) { Text("Delete") }
    },
    dismissButton = {
      CancelButton { onDismiss() }
    }
  )
}

@Composable
fun AppliedAttributesItem(
  modifier: Modifier = Modifier,
  canDelete: Boolean = true,
  attrName: String,
  attrValue: String,
  onDeleteClick: () -> Unit,
  onClick: () -> Unit
) {
  ElevatedCard(
    onClick = onClick,
    modifier = modifier.padding(3.dp)
  ) {
    ListItem(
      headlineContent = {
        Text(
          text = attrName,
          color = MaterialTheme.colorScheme.primary
        )
      },
      supportingContent = {
        Text(
          text = attrValue,
          color = MaterialTheme.colorScheme.tertiary
        )
      },
      trailingContent = {
        if (canDelete) {
          IconButton(onClick = onDeleteClick) {
            Icon(
              imageVector = Icons.Outlined.Delete,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.tertiary
            )
          }
        }
      }
    )
  }
}

@Composable
fun AvailableAttributeDialog(
  view: View,
  initializer: AttributeInitializer,
  viewAttributeMap: MutableMap<View, AttributeMap>,
  onSaveAttribute: (View) -> Unit,
  onDismiss: () -> Unit
) {
  val availableAttrs by remember { mutableStateOf(initializer.getAvailableAttributesForView(view)) }
  val names = mutableListOf<String>()

  var selectedAttr by remember { mutableStateOf<Map<String, Any>?>(null) }

  availableAttrs.forEach { names.add(it["name"].toString()) }

  AlertDialog(
    modifier = Modifier
      .fillMaxSize()
      .padding(vertical = 50.dp, horizontal = 2.dp),
    onDismissRequest = onDismiss,
    title = { Text("Available attributes") },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
          vertical = 3.dp,
          horizontal = 1.dp
        )
      ) {
        items(names) { name ->
          Card(
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.padding(vertical = 2.dp),
            onClick = { selectedAttr = availableAttrs[names.indexOf(name)] }
          ) {
            ListItem(
              headlineContent = { Text(name, color = MaterialTheme.colorScheme.tertiary) }
            )
          }
        }
      }
    },
    confirmButton = { CancelButton { onDismiss() } }
  )

  selectedAttr?.let { attr ->
    AttributeEditDialog(
      view = view,
      attributeKey = attr[Constants.KEY_ATTRIBUTE_NAME].toString(),
      initializer = initializer,
      viewAttributeMap = viewAttributeMap,
      onSave = {
        onSaveAttribute(it)
        onDismiss()
      },
      onDismiss = { selectedAttr = null }
    )
  }
}

@Suppress("UNCHECKED_CAST")
@Composable
fun AttributeEditDialog(
  view: View,
  attributeKey: String,
  initializer: AttributeInitializer,
  viewAttributeMap: MutableMap<View, AttributeMap>,
  onSave: (View) -> Unit,
  onDismiss: () -> Unit
) {
  val allAttrs = initializer.getAllAttributesForView(view)
  val currentAttr = initializer.getAttributeFromKey(attributeKey, allAttrs)
  val attributeMap = viewAttributeMap[view]!!

  val argumentTypes =
    currentAttr?.get(Constants.KEY_ARGUMENT_TYPE)?.toString()?.split("\\|".toRegex())
      ?.dropLastWhile { it.isEmpty() }
      ?.toTypedArray()

  var selectedArgumentType by remember { mutableStateOf<String?>(null) }
  var dialogState by remember { mutableStateOf<DialogState?>(null) }

  if (argumentTypes != null) {
    if (argumentTypes.size > 1 && attributeMap.contains(attributeKey)) {
      selectedArgumentType = ArgumentUtil.parseType(attributeMap[attributeKey], argumentTypes)
    } else if (argumentTypes.size == 1) {
      selectedArgumentType = argumentTypes[0]
    }
  }

  val hapticFeedback = LocalHapticFeedback.current

  fun handleSave(newValue: String, defaultValue: String?) {
    onDismiss()
    if (defaultValue != null && (defaultValue == newValue)) {
      if (viewAttributeMap[view]?.contains(attributeKey) == true) {
        removeAttribute(
          hapticFeedback = hapticFeedback,
          target = view,
          attributeKey = attributeKey,
          initializer = initializer,
          viewAttributeMap = viewAttributeMap
        ) { onSave(it) }
      }
    } else {
      if (currentAttr != null) {
        initializer.applyAttribute(view, newValue, currentAttr)
        onSave(view)
      }
    }
  }

  selectedArgumentType?.let { argType ->
    val savedValue = if (attributeMap.contains(attributeKey)) attributeMap[attributeKey] else ""
    val defaultValue = currentAttr?.get(Constants.KEY_DEFAULT_VALUE) as? String
    val constant = currentAttr?.get(Constants.KEY_CONSTANT) as? String
    val title = currentAttr?.get("name") as? String

    dialogState = when (argType) {
      Constants.ARGUMENT_TYPE_SIZE -> DialogState(
        dialogType = DialogType.SIZE,
        title = title,
        savedValue = savedValue,
        onSave = { handleSave(it, defaultValue) },
        onDismiss = { dialogState = null; onDismiss() }
      )

      Constants.ARGUMENT_TYPE_DIMENSION -> DialogState(
        dialogType = DialogType.DIMENSION,
        title = title,
        savedValue = savedValue,
        dimensionUnit = currentAttr?.get("dimensionUnit")?.toString(),
        onSave = { handleSave(it, defaultValue) },
        onDismiss = { dialogState = null; onDismiss() }
      )

      Constants.ARGUMENT_TYPE_ID -> DialogState(
        dialogType = DialogType.ID,
        title = title,
        savedValue = savedValue,
        onSave = { handleSave(it, defaultValue) },
        onDismiss = { dialogState = null; onDismiss() }
      )

      Constants.ARGUMENT_TYPE_VIEW -> DialogState(
        dialogType = DialogType.VIEW,
        title = title,
        savedValue = savedValue,
        constant = constant,
        onSave = { handleSave(it, defaultValue) },
        onDismiss = { dialogState = null; onDismiss() }
      )

      Constants.ARGUMENT_TYPE_BOOLEAN -> DialogState(
        dialogType = DialogType.BOOLEAN,
        title = title,
        savedValue = savedValue,
        onSave = { handleSave(it, defaultValue) },
        onDismiss = { dialogState = null; onDismiss() }
      )

      Constants.ARGUMENT_TYPE_DRAWABLE -> DialogState(
        dialogType = DialogType.DRAWABLE,
        title = title,
        savedValue = if (savedValue.startsWith("@drawable/")) savedValue.replace(
          "@drawable/",
          ""
        ) else savedValue,
        argType = Constants.ARGUMENT_TYPE_DRAWABLE,
        onSave = { handleSave(it, defaultValue) },
        onDismiss = { dialogState = null; onDismiss() }
      )

      Constants.ARGUMENT_TYPE_STRING -> DialogState(
        dialogType = DialogType.STRING,
        title = title,
        savedValue = if (savedValue.startsWith("@string/")) savedValue.replace(
          "@string/",
          ""
        ) else savedValue,
        argType = Constants.ARGUMENT_TYPE_STRING,
        onSave = { handleSave(it, defaultValue) },
        onDismiss = { dialogState = null; onDismiss() }
      )

      Constants.ARGUMENT_TYPE_TEXT -> DialogState(
        dialogType = DialogType.TEXT,
        title = title,
        savedValue = savedValue,
        argType = Constants.ARGUMENT_TYPE_TEXT,
        onSave = { handleSave(it, defaultValue) },
        onDismiss = { dialogState = null; onDismiss() }
      )

      Constants.ARGUMENT_TYPE_INT -> DialogState(
        dialogType = DialogType.INT,
        title = title,
        savedValue = savedValue,
        argType = Constants.ARGUMENT_TYPE_INT,
        onSave = { handleSave(it, defaultValue) },
        onDismiss = { dialogState = null; onDismiss() }
      )

      Constants.ARGUMENT_TYPE_FLOAT -> DialogState(
        dialogType = DialogType.FLOAT,
        title = title,
        savedValue = savedValue,
        argType = Constants.ARGUMENT_TYPE_FLOAT,
        onSave = { handleSave(it, defaultValue) },
        onDismiss = { dialogState = null; onDismiss() }
      )

      Constants.ARGUMENT_TYPE_FLAG -> DialogState(
        dialogType = DialogType.FLAG,
        title = title,
        savedValue = savedValue,
        options = currentAttr?.get("arguments") as? ArrayList<String>,
        onSave = { handleSave(it, defaultValue) },
        onDismiss = { dialogState = null; onDismiss() }
      )

      Constants.ARGUMENT_TYPE_ENUM -> DialogState(
        dialogType = DialogType.ENUM,
        title = title,
        savedValue = savedValue,
        options = currentAttr?.get("arguments") as? ArrayList<String>,
        onSave = { handleSave(it, defaultValue) },
        onDismiss = { dialogState = null; onDismiss() }
      )

      Constants.ARGUMENT_TYPE_COLOR -> DialogState(
        dialogType = DialogType.COLOR,
        title = title,
        savedValue = savedValue,
        onSave = { handleSave(it, defaultValue) },
        onDismiss = { dialogState = null; onDismiss() }
      )

      else -> {
        ToastUtils.showShort("Something went wrong.. $argType")
        null
      }
    }
  }

  dialogState?.let { state -> AttributeDialog(state) }

  if (selectedArgumentType == null && argumentTypes != null) {
    ArgumentTypeDropdown(argumentTypes) {
      selectedArgumentType = argumentTypes[it]
    }
  }
}

@Composable
fun AttributeDialog(dialogState: DialogState) {
  when (dialogState.dialogType) {
    DialogType.SIZE -> {
      SizeDialog(
        title = dialogState.title.toString(),
        savedValue = dialogState.savedValue,
        onSave = dialogState.onSave,
        onDismiss = dialogState.onDismiss
      )
    }

    DialogType.DIMENSION -> {
      DimensionDialog(
        title = dialogState.title.toString(),
        savedValue = dialogState.savedValue,
        unit = dialogState.dimensionUnit.toString(),
        onSave = dialogState.onSave,
        onDismiss = dialogState.onDismiss
      )
    }

    DialogType.ID -> {
      IdDialog(
        title = dialogState.title.toString(),
        savedValue = dialogState.savedValue,
        onSave = dialogState.onSave,
        onDismiss = dialogState.onDismiss
      )
    }

    DialogType.VIEW -> {
      ViewDialog(
        title = dialogState.title.toString(),
        savedValue = dialogState.savedValue,
        constant = dialogState.constant,
        onSave = dialogState.onSave,
        onDismiss = dialogState.onDismiss
      )
    }

    DialogType.BOOLEAN -> {
      BooleanDialog(
        title = dialogState.title.toString(),
        savedValue = dialogState.savedValue,
        onSave = dialogState.onSave,
        onDismiss = dialogState.onDismiss
      )
    }

    DialogType.DRAWABLE, DialogType.STRING, DialogType.TEXT -> {
      StringDialog(
        title = dialogState.title.toString(),
        savedValue = dialogState.savedValue,
        argumentType = dialogState.argType.toString(),
        onSave = dialogState.onSave,
        onDismiss = dialogState.onDismiss
      )
    }

    DialogType.INT, DialogType.FLOAT -> {
      NumberDialog(
        title = dialogState.title.toString(),
        savedValue = dialogState.savedValue,
        type = dialogState.argType.toString(),
        onSave = dialogState.onSave,
        onDismiss = dialogState.onDismiss
      )
    }

    DialogType.FLAG -> {
      FlagDialog(
        title = dialogState.title.toString(),
        savedValue = dialogState.savedValue,
        arguments = dialogState.options.orEmpty(),
        onSave = dialogState.onSave,
        onDismiss = dialogState.onDismiss
      )
    }

    DialogType.ENUM -> {
      EnumDialog(
        title = dialogState.title.toString(),
        savedValue = dialogState.savedValue,
        arguments = dialogState.options.orEmpty(),
        onSave = dialogState.onSave,
        onDismiss = dialogState.onDismiss
      )
    }

    DialogType.COLOR -> {
      ColorDialog(
        title = dialogState.title.toString(),
        savedValue = dialogState.savedValue,
        onSave = dialogState.onSave,
        onDismiss = dialogState.onDismiss
      )
    }
  }
}

@Composable
fun ArgumentTypeDropdown(
  options: Array<String>,
  onDismiss: (Int) -> Unit
) {
  AlertDialog(
    onDismissRequest = { onDismiss(0) },
    title = { Text("Select argument type") },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth()
      ) {
        items(options) {
          Card(
            modifier = Modifier.padding(2.dp),
            onClick = { onDismiss(options.indexOf(it)) }
          ) {
            ListItem(
              headlineContent = { Text(it) },
            )
          }
        }
      }
    },
    confirmButton = { }
  )
}
