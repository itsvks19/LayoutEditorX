package com.itsvks.layouteditorx.editor.callers.text;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.AutoCompleteTextView;

import com.itsvks.layouteditorx.managers.ProjectManager;
import com.itsvks.layouteditorx.managers.ValuesManager;
import com.itsvks.layouteditorx.models.Project;
import com.itsvks.layouteditorx.parser.ValuesResourceParser;
import com.itsvks.layouteditorx.utils.DimensionUtil;

public class AutoCompleteTextViewCaller extends EditTextCaller {
  public static void setCompletionHint(View target, String value, Context context) {
    if (value.startsWith("@string/")) {
      Project project = ProjectManager.getInstance().getOpenedProject();
      value = ValuesManager.INSTANCE.getValueFromResources(ValuesResourceParser.TAG_STRING, value, project.getStringsPath());
    }
    ((AutoCompleteTextView) target).setCompletionHint(value);
  }

  public static void setThreshold(View target, String value, Context context) {
    ((AutoCompleteTextView) target).setThreshold((int) DimensionUtil.parse(value, context));
  }

  public static void setDropDownHeight(View target, String value, Context context) {
    ((AutoCompleteTextView) target).setDropDownHeight((int) DimensionUtil.parse(value, context));
  }

  public static void setDropDownHorizontalOffset(View target, String value, Context context) {
    ((AutoCompleteTextView) target)
        .setDropDownHorizontalOffset((int) DimensionUtil.parse(value, context));
  }

  public static void setDropDownVerticalOffset(View target, String value, Context context) {
    ((AutoCompleteTextView) target)
        .setDropDownVerticalOffset((int) DimensionUtil.parse(value, context));
  }

  public static void setDropDownWidth(View target, String value, Context context) {
    ((AutoCompleteTextView) target).setDropDownWidth((int) DimensionUtil.parse(value, context));
  }

  @SuppressLint("ResourceType")
  public static void setDropDownBackgroundResource(View target, String value, Context context) {
    ((AutoCompleteTextView) target).setDropDownBackgroundResource(Color.parseColor(value));
  }
}
