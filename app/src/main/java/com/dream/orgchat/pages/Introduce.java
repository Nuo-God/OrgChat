package com.dream.orgchat.pages;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.transition.TransitionInflater;
import com.dream.orgchat.R;
import dream.activity.Default;
import dream.widget.SlideView;

public class Introduce extends Default {
  private IdContext id;
  private SlideView backg;

  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    // setStatusBarVisibility(true);
    setStatusBarColor(getColor(com.dream.orgchat.R.color.background_color));

    id = new IdContext();
    setContentView(loadlayout(com.dream.orgchat.R.layout.introduce_layout, id));

    backg = ((SlideView) id.g("introduce_slide_backg"));

    backg.getChildAt(0).setEnabled(false);
    backg.setProgress(
        (float value) -> {
          // print(value);
          if (value != 1) backg.getChildAt(0).setEnabled(true);
          else backg.getChildAt(0).setEnabled(false);
        });

    backg.setLimit(getHeight() / 2);

    backg.getChildAt(1).setVerticalFadingEdgeEnabled(true);

    String titleStyle = getFileContext(R.raw.introduce_title_style);
    backg.post(
        () -> {
          backg.setTitleStyle(titleStyle);
          backg.setUnfoldingState(true);
        });

  }
}
