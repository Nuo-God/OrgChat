package com.dream.orgchat;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.core.app.ActivityOptionsCompat;
import com.dream.orgchat.pages.Introduce;
import com.dream.orgchat.service.NotificationService;
import dream.activity.Default;
import dream.activity.Notification;
import dream.animator.CustomAnimator;
import dream.widget.DreamLogo;
import java.util.HashMap;

public class Main extends Default {
  private IdContext id;
  private DreamLogo logo;
  private ImageView image;

  @Override
  protected void onCreate(Bundle bindle) {
    super.onCreate(bindle);
    // setStatusBarVisibility(true);
    setStatusBarColor(getColor(com.dream.orgchat.R.color.background_color));
    
    // 预设置动画效果
    LayoutTransition transition = new LayoutTransition();
    transition.enableTransitionType(LayoutTransition.CHANGING);
    transition.setDuration(LayoutTransition.CHANGE_APPEARING, 400);
    transition.setDuration(LayoutTransition.CHANGE_DISAPPEARING, 400);

    id = new IdContext();
    setContentView(loadlayout(R.layout.main_layout, id));
    // 绑定过滤动画
    ((FrameLayout) id.g("logo_backg")).setLayoutTransition(transition);
    image = ((ImageView) id.g("main_logo_image"));
    logo = ((DreamLogo) id.g("main_logo"));
    logo.start();
    logo.setOnAnimationEnds(
        (DreamLogo view) -> {
          view.setVisibility(View.GONE);
          image.setVisibility(View.VISIBLE);
          jumpToIntroduce();
        });
    
  }

  public void jumpToIntroduce() {
    task(
        500,
        () -> {
          newActivity(Introduce.class, R.anim.load_in, R.anim.load_out);
          finish();
          return true;
        });
  }
}
