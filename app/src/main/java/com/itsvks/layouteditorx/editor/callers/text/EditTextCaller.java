package com.itsvks.layouteditorx.editor.callers.text;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;

import com.itsvks.layouteditorx.Constants;
import com.itsvks.layouteditorx.managers.ProjectManager;
import com.itsvks.layouteditorx.managers.ValuesManager;
import com.itsvks.layouteditorx.models.Project;
import com.itsvks.layouteditorx.parser.ValuesResourceParser;

public class EditTextCaller {

  public static void setHint(View target, String value, Context context) {
    if (value.startsWith("@string/")) {
      Project project = ProjectManager.getInstance().getOpenedProject();

      value =
          ValuesManager.INSTANCE.getValueFromResources(
              ValuesResourceParser.TAG_STRING, value, project.getStringsPath());
    }
    if (target instanceof EditText) ((EditText) target).setHint(value);
  }

  public static void setHintTextColor(View target, String value, Context context) {
    if (target instanceof EditText) ((EditText) target).setHintTextColor(Color.parseColor(value));
  }

  public static void setSingleLine(View target, String value, Context context) {
    if (value.equals("true")) {
      if (target instanceof EditText) ((EditText) target).setSingleLine(true);
    } else {
      if (target instanceof EditText) ((EditText) target).setSingleLine(false);
    }
  }

  public static void setInputType(View target, String value, Context context) {
    String[] flags = value.split("\\|");
    int result = 0;

    for (String flag : flags) result |= Constants.inputTypes.get(flag);

    if (target instanceof EditText) ((EditText) target).setInputType(result);
  }
}
