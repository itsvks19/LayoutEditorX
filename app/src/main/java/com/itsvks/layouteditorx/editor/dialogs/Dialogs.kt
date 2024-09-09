package com.itsvks.layouteditorx.editor.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.itsvks.layouteditorx.Constants
import com.itsvks.layouteditorx.extensions.isValidHex
import com.itsvks.layouteditorx.extensions.toHexColor
import com.itsvks.layouteditorx.managers.IdManager
import com.itsvks.layouteditorx.managers.ProjectManager
import com.itsvks.layouteditorx.managers.ValuesManager
import com.itsvks.layouteditorx.parser.ValuesResourceParser
import com.itsvks.layouteditorx.ui.ErrorMessage
import com.itsvks.layouteditorx.utils.DimensionUtil
import java.util.regex.Pattern
import com.itsvks.layouteditorx.managers.DrawableManager.Companion.instance as drawableManagerInstance

@Composable
fun NumberDialog(
  modifier: Modifier = Modifier,
  type: String = "float",
  title: String,
  savedValue: String,
  onSave: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var value by remember { mutableStateOf(savedValue.ifEmpty { "0" }) }
  var error by remember { mutableStateOf<String?>(null) }

  val keyboardOptions = if (type == "float") {
    KeyboardOptions(keyboardType = KeyboardType.Decimal)
  } else {
    KeyboardOptions(keyboardType = KeyboardType.Number)
  }

  fun checkError() {
    error = if (value.isEmpty()) {
      "Field cannot be empty"
    } else null
  }

  LaunchedEffect(value) { checkError() }

  AlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    title = { Text(text = title) },
    text = {
      Column {
        OutlinedTextField(
          value = value,
          onValueChange = {
            value = it
            checkError()
          },
          isError = error != null,
          keyboardOptions = keyboardOptions,
          modifier = Modifier.fillMaxWidth()
        )
        error?.let { ErrorMessage(it) }
      }
    },
    confirmButton = {
      SaveButton(error == null) {
        if (error == null) {
          onSave(value)
          onDismiss()
        }
      }
    },
    dismissButton = {
      CancelButton { onDismiss() }
    }
  )
}

@Composable
fun BooleanDialog(
  modifier: Modifier = Modifier,
  title: String,
  savedValue: String,
  onSave: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedValue by remember { mutableStateOf(savedValue.ifEmpty { "true" }) }

  AlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    title = { Text(text = title) },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedValue = "true" },
          verticalAlignment = Alignment.CenterVertically
        ) {
          RadioButton(
            selected = selectedValue == "true",
            onClick = { selectedValue = "true" }
          )
          Text("True")
        }
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedValue = "false" },
          verticalAlignment = Alignment.CenterVertically
        ) {
          RadioButton(
            selected = selectedValue == "false",
            onClick = { selectedValue = "false" }
          )
          Text("False")
        }
      }
    },
    confirmButton = {
      SaveButton {
        onSave(selectedValue)
        onDismiss()
      }
    },
    dismissButton = {
      CancelButton { onDismiss() }
    }
  )
}

@Composable
fun DimensionDialog(
  modifier: Modifier = Modifier,
  unit: String = "dp",
  title: String,
  savedValue: String,
  onSave: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var value by remember { mutableStateOf(DimensionUtil.getDimenWithoutSuffix(savedValue.ifEmpty { "0dp" })) }
  var error by remember { mutableStateOf<String?>(null) }

  fun checkError() {
    error = if (value.isEmpty()) {
      "Field cannot be empty"
    } else null
  }

  LaunchedEffect(value) { checkError() }

  AlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    title = { Text(text = title) },
    text = {
      Column {
        OutlinedTextField(
          value = value,
          onValueChange = {
            value = it
            checkError()
          },
          label = { Text("Enter dimension value") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          trailingIcon = { Text(unit) },
          isError = error != null,
          modifier = Modifier.fillMaxWidth()
        )
        error?.let { ErrorMessage(it) }
      }
    },
    confirmButton = {
      SaveButton(error == null) {
        onSave(value + unit)
        onDismiss()
      }
    },
    dismissButton = {
      CancelButton { onDismiss() }
    }
  )
}

@Composable
fun EnumDialog(
  modifier: Modifier = Modifier,
  arguments: List<String>,
  title: String,
  savedValue: String,
  onSave: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedOption by remember { mutableStateOf(savedValue) }

  AlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    title = { Text(text = title) },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth()
      ) {
        items(arguments) {
          EnumRowItem(
            text = it,
            selected = it == selectedOption
          ) { selectedOption = it }
        }
      }
    },
    confirmButton = {
      SaveButton(selectedOption.isNotEmpty()) {
        onSave(selectedOption)
        onDismiss()
      }
    },
    dismissButton = {
      CancelButton { onDismiss() }
    }
  )
}

@Composable
fun EnumRowItem(
  text: String,
  selected: Boolean,
  onSelect: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(2.dp),
    onClick = onSelect,
    colors = CardDefaults.cardColors().copy(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      RadioButton(
        selected = selected,
        onClick = onSelect
      )
      Text(
        text = text,
        modifier = Modifier.padding(start = 0.dp),
        style = MaterialTheme.typography.bodyLarge
      )
    }
  }
}

@Composable
fun FlagDialog(
  modifier: Modifier = Modifier,
  arguments: List<String>,
  title: String,
  savedValue: String,
  onSave: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedFlags by remember {
    val initialFlags = savedValue.split("|").toSet()
    mutableStateOf(initialFlags)
  }

  AlertDialog(
    modifier = modifier
      .fillMaxSize()
      .padding(vertical = 30.dp),
    onDismissRequest = onDismiss,
    title = { Text(text = title) },
    text = {
      LazyColumn {
        items(arguments) {
          FlagRowItem(
            text = it,
            selected = selectedFlags.contains(it)
          ) { isChecked ->
            val currentFlags = selectedFlags.toMutableSet()
            if (isChecked) {
              currentFlags.add(it)
            } else {
              currentFlags.remove(it)
            }
            selectedFlags = currentFlags
          }
        }
      }
    },
    confirmButton = {
      SaveButton(selectedFlags.joinToString(separator = "|").removePrefix("|").isNotEmpty()) {
        onSave(selectedFlags.joinToString(separator = "|").removePrefix("|"))
        onDismiss()
      }
    },
    dismissButton = {
      CancelButton { onDismiss() }
    }
  )
}

@Composable
fun FlagRowItem(
  text: String,
  selected: Boolean,
  onToggle: (Boolean) -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(2.dp)
      .toggleable(
        value = selected,
        onValueChange = onToggle,
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple()
      ),
    colors = CardDefaults.cardColors().copy(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Checkbox(
        checked = selected,
        onCheckedChange = onToggle
      )
      Spacer(modifier = Modifier.width(3.dp))
      Text(text = text)
    }
  }
}

@Composable
fun SizeDialog(
  modifier: Modifier = Modifier,
  title: String,
  savedValue: String,
  onSave: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedOption by remember {
    mutableStateOf(
      when (savedValue) {
        "match_parent" -> "match_parent"
        "wrap_content" -> "wrap_content"
        else -> "Fixed value"
      }
    )
  }
  var fixedValue by remember {
    mutableStateOf(
      if (selectedOption == "Fixed value") DimensionUtil.getDimenWithoutSuffix(
        savedValue
      ) else ""
    )
  }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    title = { Text(text = title) },
    text = {
      Column {
        SizeRadioGroup(
          selectedOption = selectedOption
        ) {
          selectedOption = it
          errorMessage = null
        }

        AnimatedVisibility(
          visible = selectedOption == "Fixed value"
        ) {
          Column {
            OutlinedTextField(
              value = fixedValue,
              onValueChange = {
                fixedValue = it
                errorMessage = if (it.isEmpty()) "Field cannot be empty" else null
              },
              label = { Text("Enter dimension value") },
              keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
              isError = errorMessage != null,
              suffix = { Text("dp") },
              modifier = Modifier.fillMaxWidth()
            )
            errorMessage?.let { ErrorMessage(it) }
          }
        }
      }
    },
    confirmButton = {
      SaveButton(
        enabled = selectedOption != "Fixed value" || fixedValue.isNotEmpty()
      ) {
        if (selectedOption == "Fixed value" && fixedValue.isEmpty()) {
          errorMessage = "Field cannot be empty"
        } else {
          val result = when (selectedOption) {
            "match_parent" -> "match_parent"
            "wrap_content" -> "wrap_content"
            else -> "${fixedValue}dp"
          }
          onSave(result)
          onDismiss()
        }
      }
    },
    dismissButton = {
      CancelButton { onDismiss() }
    }
  )
}

@Composable
fun SizeRadioGroup(
  selectedOption: String,
  onOptionSelected: (String) -> Unit
) {
  var option by remember { mutableStateOf(selectedOption) }

  Column {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      RadioButton(
        selected = option == "match_parent",
        onClick = {
          option = "match_parent"
          onOptionSelected(option)
        }
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(text = "Match Parent", modifier = Modifier.clickable {
        option = "match_parent"
        onOptionSelected(option)
      })
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      RadioButton(
        selected = option == "wrap_content",
        onClick = {
          option = "wrap_content"
          onOptionSelected(option)
        }
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(text = "Wrap Content", modifier = Modifier.clickable {
        option = "wrap_content"
        onOptionSelected(option)
      })
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      RadioButton(
        selected = option == "Fixed value",
        onClick = {
          option = "Fixed value"
          onOptionSelected(option)
        }
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(text = "Fixed value", modifier = Modifier.clickable {
        option = "Fixed value"
        onOptionSelected(option)
      })
    }
  }
}

@Composable
fun StringDialog(
  modifier: Modifier = Modifier,
  title: String,
  savedValue: String,
  argumentType: String,
  onSave: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var text by remember { mutableStateOf(savedValue) }
  var error by remember { mutableStateOf<String?>(null) }

  fun checkErrors() {
    when {
      text.isEmpty() -> {
        error = "Field cannot be empty"
      }

      !Pattern.matches(
        "[a-z_][a-z0-9_]*",
        text
      ) && argumentType != Constants.ARGUMENT_TYPE_TEXT -> {
        error = "Only small letters(a-z) and numbers"
      }

      argumentType == Constants.ARGUMENT_TYPE_DRAWABLE && !drawableManagerInstance.contains(text) -> {
        error = "No Drawable found"
      }

      argumentType == Constants.ARGUMENT_TYPE_STRING && ValuesManager.getValueFromResources(
        ValuesResourceParser.TAG_STRING,
        if (text.startsWith("@string/")) text else "@string/$text",
        ProjectManager.instance.openedProject!!.stringsPath
      ) == null -> {
        error = "No string found"
      }

      else -> error = null
    }
  }

  LaunchedEffect(text) { checkErrors() }

  AlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    title = { Text(text = title) },
    text = {
      Column {
        OutlinedTextField(
          value = text,
          onValueChange = {
            text = it
            checkErrors()
          },
          label = {
            Text(
              text = when (argumentType) {
                Constants.ARGUMENT_TYPE_DRAWABLE -> "Enter drawable name"
                Constants.ARGUMENT_TYPE_STRING -> "Enter string name"
                Constants.ARGUMENT_TYPE_TEXT -> "Enter text value"
                else -> "Enter text value"
              }
            )
          },
          isError = error != null,
          prefix = {
            when (argumentType) {
              Constants.ARGUMENT_TYPE_DRAWABLE -> Text("@drawable/")
              Constants.ARGUMENT_TYPE_STRING -> Text("@string/")
              Constants.ARGUMENT_TYPE_TEXT -> {}
            }
          }
        )
        error?.let { ErrorMessage(it) }
      }
    },
    confirmButton = {
      SaveButton(error == null) {
        if (error == null) {
          when (argumentType) {
            Constants.ARGUMENT_TYPE_DRAWABLE -> onSave("@drawable/$text")
            Constants.ARGUMENT_TYPE_STRING -> onSave("@string/$text")
            Constants.ARGUMENT_TYPE_TEXT -> onSave(text)
          }
        }
        onDismiss()
      }
    },
    dismissButton = {
      CancelButton { onDismiss() }
    }
  )
}

@Composable
fun IdDialog(
  modifier: Modifier = Modifier,
  title: String,
  savedValue: String,
  onSave: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val ids = IdManager.instance.getIds()
  val id = savedValue.replace("@+id/", "")

  var idText by remember { mutableStateOf(savedValue.replace("@+id/", "")) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val pattern = "[a-z][A-Za-z0-9_\\s]*".toRegex()

  fun checkError(value: String) {
    errorMessage = when {
      value.isEmpty() -> "Field cannot be empty"
      !pattern.matches(value) -> "The name must not contain symbols."
      (id != value) && ids.contains(value) -> "Current ID is unavailable"
      else -> null
    }
  }

  LaunchedEffect(idText) { checkError(idText) }

  AlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    title = { Text(text = title) },
    text = {
      Column {
        OutlinedTextField(
          value = idText,
          onValueChange = {
            idText = it
            checkError(it)
          },
          label = { Text("Enter new ID") },
          keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
          isError = errorMessage != null,
          prefix = { Text("@+id/") },
          modifier = Modifier.fillMaxWidth()
        )
        errorMessage?.let { ErrorMessage(it) }
      }
    },
    confirmButton = {
      SaveButton(errorMessage == null) {
        if (errorMessage == null) {
          onSave("@+id/${idText.lowercase().replace(" ", "_")}")
          onDismiss()
        }
      }
    },
    dismissButton = {
      CancelButton { onDismiss() }
    }
  )
}

@Composable
fun ViewDialog(
  modifier: Modifier = Modifier,
  title: String,
  savedValue: String,
  constant: String?,
  onSave: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedId by remember { mutableStateOf(savedValue.replace("@id/", "")) }
  val ids = remember { mutableStateListOf<String>() }

  LaunchedEffect(Unit) {
    ids.clear()
    ids.addAll(IdManager.instance.getIds())
    constant?.let { ids.add(0, it) }
  }

  AlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    title = { Text(text = title) },
    text = {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp)
      ) {
        items(ids) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedId = it }
              .padding(16.dp)
          ) {
            RadioButton(
              selected = selectedId == it,
              onClick = { selectedId = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = it)
          }
        }
      }
    },
    confirmButton = {
      SaveButton {
        if (selectedId.isNotEmpty()) {
          onSave(if (constant != null && ids.indexOf(selectedId) == 0) constant else "@id/$selectedId")
        } else {
          onSave("-1")
        }
        onDismiss()
      }
    },
    dismissButton = {
      CancelButton { onDismiss() }
    }
  )
}

@Composable
fun ColorDialog(
  modifier: Modifier = Modifier,
  title: String,
  savedValue: String,
  onSave: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var color by remember { mutableStateOf(Color(android.graphics.Color.parseColor(savedValue.ifEmpty { "#FFFFFF" }))) }
  var hexValue by remember { mutableStateOf(savedValue.replace("#", "")) }
  var alpha by remember { mutableStateOf(android.graphics.Color.alpha(color.toArgb())) }
  var red by remember { mutableStateOf(android.graphics.Color.red(color.toArgb())) }
  var green by remember { mutableStateOf(android.graphics.Color.green(color.toArgb())) }
  var blue by remember { mutableStateOf(android.graphics.Color.blue(color.toArgb())) }

  fun updateColorComponents(
    color: Color,
    onAlphaChanged: (Int) -> Unit,
    onRedChanged: (Int) -> Unit,
    onGreenChanged: (Int) -> Unit,
    onBlueChanged: (Int) -> Unit
  ) {
    onAlphaChanged(android.graphics.Color.alpha(color.toArgb()))
    onRedChanged(android.graphics.Color.red(color.toArgb()))
    onGreenChanged(android.graphics.Color.green(color.toArgb()))
    onBlueChanged(android.graphics.Color.blue(color.toArgb()))
  }

  AlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    title = { Text(text = title) },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(50.dp)
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.height(8.dp))
        HsvColorPicker(
          modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
          controller = rememberColorPickerController(),
          initialColor = color,
          onColorChanged = {
            val newColor = it.color

            color = newColor
            hexValue = newColor.toArgb().toHexColor()
            alpha = newColor.alpha.toInt()
            red = newColor.red.toInt()
            green = newColor.green.toInt()
            blue = newColor.blue.toInt()
          }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = "#$hexValue",
          onValueChange = { newValue ->
            hexValue = newValue.replace("#", "")
            if (hexValue.isValidHex()) {
              color = Color(android.graphics.Color.parseColor("#$hexValue"))
              updateColorComponents(
                color,
                onAlphaChanged = { alpha = it },
                onRedChanged = { red = it },
                onGreenChanged = { green = it },
                onBlueChanged = { blue = it }
              )
            }
          },
          label = { Text("Enter hex value") },
          singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          ARGBInput("A", alpha) { alpha = it; color = color.copy(alpha = it / 255f) }
          ARGBInput("R", red) { red = it; color = color.copy(red = it / 255f) }
          ARGBInput("G", green) { green = it; color = color.copy(green = it / 255f) }
          ARGBInput("B", blue) { blue = it; color = color.copy(blue = it / 255f) }
        }
      }
    },
    confirmButton = {
      SaveButton {
        onSave("#$hexValue")
        onDismiss()
      }
    },
    dismissButton = {
      CancelButton { onDismiss() }
    }
  )
}

@Composable
fun ARGBInput(
  label: String,
  value: Int,
  onValueChange: (Int) -> Unit
) {
  TextField(
    value = value.toString(),
    onValueChange = {
      val newValue = it.toIntOrNull()
      if (newValue != null && newValue in 0..255) {
        onValueChange(newValue)
      }
    },
    label = { Text(label) },
    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
    singleLine = true,
    modifier = Modifier.width(50.dp),
    shape = MaterialTheme.shapes.medium
  )
}

@Composable
fun SaveButton(enabled: Boolean = true, onClick: () -> Unit) {
  Button(
    onClick = onClick,
    enabled = enabled
  ) {
    Text("Save")
  }
}

@Composable
fun CancelButton(onClick: () -> Unit) {
  OutlinedButton(onClick = onClick) {
    Text("Cancel")
  }
}
