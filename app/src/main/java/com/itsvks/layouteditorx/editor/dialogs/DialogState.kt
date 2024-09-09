package com.itsvks.layouteditorx.editor.dialogs

data class DialogState(
  val dialogType: DialogType,
  val title: String?,
  val savedValue: String,
  val options: List<String>? = null,
  val dimensionUnit: String? = null,
  val argType: String? = null,
  val constant: String? = null,
  val onSave: (String) -> Unit,
  val onDismiss: () -> Unit
)