package com.itsvks.layouteditorx.editor.callers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.itsvks.layouteditorx.managers.DrawableManager;

import java.util.HashMap;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.Dispatchers;

public class ImageViewCaller {

  private static final HashMap<String, ImageView.ScaleType> scaleTypes = new HashMap<>();

  static {
    scaleTypes.put("fitXY", ImageView.ScaleType.FIT_XY);
    scaleTypes.put("fitStart", ImageView.ScaleType.FIT_START);
    scaleTypes.put("fitCenter", ImageView.ScaleType.FIT_CENTER);
    scaleTypes.put("fitEnd", ImageView.ScaleType.FIT_END);
    scaleTypes.put("center", ImageView.ScaleType.CENTER);
    scaleTypes.put("centerCrop", ImageView.ScaleType.CENTER_CROP);
    scaleTypes.put("centerInside", ImageView.ScaleType.CENTER_INSIDE);
  }

  public static void setImage(View target, String value, Context context) {
    String name = value.replace("@drawable/", "");
    ((ImageView) target).setImageDrawable((android.graphics.drawable.Drawable) DrawableManager.getInstance().getDrawable(context, value, new Continuation<>() {
      @NonNull
      @Override
      public CoroutineContext getContext() {
        return Dispatchers.getDefault();
      }

      @Override
      public void resumeWith(@NonNull Object o) {

      }
    }));
  }

  public static void setScaleType(View target, String value, Context context) {
    ((ImageView) target).setScaleType(scaleTypes.get(value));
  }

  public static void setTint(View target, String value, Context context) {
    ((ImageView) target).setColorFilter(Color.parseColor(value));
  }
}
