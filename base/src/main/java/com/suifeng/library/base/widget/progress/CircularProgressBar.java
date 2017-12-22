package com.suifeng.library.base.widget.progress;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.suifeng.library.base.R;

/**
 * Created by suifeng_e on 11/10/13.
 */
public class CircularProgressBar extends ProgressBar {

  public CircularProgressBar(Context context) {
    this(context, null);
  }

  public CircularProgressBar(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.cpbStyle);
  }

  public CircularProgressBar(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    if (isInEditMode()) {
      setIndeterminateDrawable(new CircularProgressDrawable.Builder(context, true).build());
      return;
    }

    Resources res = context.getResources();
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressBar, defStyle, 0);


    final int color = a.getColor(R.styleable.CircularProgressBar_cpb_color, res.getColor(R.color.c_33b5e5));
    final float strokeWidth = a.getDimension(R.styleable.CircularProgressBar_cpb_stroke_width, res.getDimension(R.dimen.dp_4));
    final float sweepSpeed = a.getFloat(R.styleable.CircularProgressBar_cpb_sweep_speed, Float.parseFloat("1"));
    final float rotationSpeed = a.getFloat(R.styleable.CircularProgressBar_cpb_rotation_speed, Float.parseFloat("1"));
    final int colorsId = a.getResourceId(R.styleable.CircularProgressBar_cpb_colors, 0);
    final int minSweepAngle = a.getInteger(R.styleable.CircularProgressBar_cpb_min_sweep_angle, 20);
    final int maxSweepAngle = a.getInteger(R.styleable.CircularProgressBar_cpb_max_sweep_angle, 300);
    a.recycle();

    int[] colors = null;
    //colors
    if (colorsId != 0) {
      colors = res.getIntArray(colorsId);
    }

    Drawable indeterminateDrawable;
    CircularProgressDrawable.Builder builder = new CircularProgressDrawable.Builder(context)
        .sweepSpeed(sweepSpeed)
        .rotationSpeed(rotationSpeed)
        .strokeWidth(strokeWidth)
        .minSweepAngle(minSweepAngle)
        .maxSweepAngle(maxSweepAngle);

    if (colors != null && colors.length > 0)
      builder.colors(colors);
    else
      builder.color(color);

    indeterminateDrawable = builder.build();
    setIndeterminateDrawable(indeterminateDrawable);
  }

  private CircularProgressDrawable checkIndeterminateDrawable() {
     Drawable ret = getIndeterminateDrawable();
     if (ret == null || !(ret instanceof CircularProgressDrawable))
        throw new RuntimeException("The drawable is not a CircularProgressDrawable");
     return (CircularProgressDrawable) ret;
  }

  public void progressiveStop() {
    checkIndeterminateDrawable().progressiveStop();
  }

  public void progressiveStop(CircularProgressDrawable.OnEndListener listener) {
    checkIndeterminateDrawable().progressiveStop(listener);
  }
}
