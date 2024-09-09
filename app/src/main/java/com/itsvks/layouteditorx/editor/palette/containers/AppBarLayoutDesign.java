package com.itsvks.layouteditorx.editor.palette.containers;

import com.google.android.material.appbar.AppBarLayout;
import com.itsvks.layouteditorx.Constants;
import com.itsvks.layouteditorx.utils.Utils;

import android.content.Context;
import android.graphics.Canvas;

public class AppBarLayoutDesign extends AppBarLayout {

  private boolean drawStrokeEnabled;
  private boolean isBlueprint;

  public AppBarLayoutDesign(Context context) {
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
  
  @Override
  public void draw(Canvas canvas) {
    if (isBlueprint) Utils.drawDashPathStroke(this, canvas, Constants.BLUEPRINT_DASH_COLOR);
    else super.draw(canvas);
  }

  public void setBlueprint(boolean isBlueprint) {
    this.isBlueprint = isBlueprint;
    invalidate();
  }
}