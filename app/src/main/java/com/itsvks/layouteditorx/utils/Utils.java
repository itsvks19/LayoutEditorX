package com.itsvks.layouteditorx.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SizeUtils;

public class Utils {
  public static void drawDashPathStroke(@NonNull View view, @NonNull Canvas canvas, @NonNull Paint paint) {
    paint.setAntiAlias(true);
    paint.setStyle(Paint.Style.STROKE);
    paint.setPathEffect(new DashPathEffect(new float[]{10, 7}, 0));
    canvas.drawRect(0, 0, view.getWidth(), view.getHeight(), paint);
  }

  public static void drawDashPathStroke(View view, Canvas canvas) {
    drawDashPathStroke(view, canvas, getDefaultPaint(view.getContext()));
  }

  public static void drawDashPathStroke(@NonNull View view, Canvas canvas, int paintColor) {
    Paint paint = getDefaultPaint(view.getContext());
    paint.setColor(paintColor);
    drawDashPathStroke(view, canvas, paint);
  }

  @NonNull
  private static Paint getDefaultPaint(Context context) {
    Paint paint = new Paint();
    paint.setColor(Color.WHITE);
    paint.setStrokeWidth(SizeUtils.dp2px(2f));
    return paint;
  }
}
