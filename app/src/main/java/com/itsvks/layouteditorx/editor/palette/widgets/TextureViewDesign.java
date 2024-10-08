package com.itsvks.layouteditorx.editor.palette.widgets;

import android.view.TextureView;
import android.content.Context;
import android.graphics.Canvas;

import com.itsvks.layouteditorx.Constants;
import com.itsvks.layouteditorx.utils.Utils;

public class TextureViewDesign extends TextureView {
  
  private boolean drawStrokeEnabled;
  private boolean isBlueprint;

  public TextureViewDesign(Context context) {
    super(context);
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);

    if (drawStrokeEnabled)
      Utils.drawDashPathStroke(
          this, canvas, isBlueprint ? Constants.BLUEPRINT_DASH_COLOR : Constants.DESIGN_DASH_COLOR);
  }

  public void setStrokeEnabled(boolean enabled) {
    drawStrokeEnabled = enabled;
    invalidate();
  }

  public void setBlueprint(boolean isBlueprint) {
    this.isBlueprint = isBlueprint;
    invalidate();
  }
}
