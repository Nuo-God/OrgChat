package dream.animator;

import android.animation.ValueAnimator;

public class CustomAnimator {
  private ValueAnimator animator;
  private OnUpdate onUpdate = (float value) -> {};
  private float startValue;
  private float endValue;

  private CustomAnimator(float start, float end, long time) {
    this.startValue = start;
    this.endValue = end;
    animator = ValueAnimator.ofFloat(startValue, endValue);
    animator.setDuration(time);
    animator.addUpdateListener(
        new ValueAnimator.AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator animation) {
            float value = (float) animation.getAnimatedValue();
            onUpdate.call(value);
          }
        });
  }

  public static CustomAnimator create(float start, float end) {
    return new CustomAnimator(start, end, 1000);
  }

  public static CustomAnimator create(float start, float end, long time) {
    return new CustomAnimator(start, end, time);
  }

  public CustomAnimator start() {
    animator.start();
    return this;
  }

  public CustomAnimator pause() {
    animator.pause();
    return this;
  }

  public CustomAnimator resume() {
    animator.resume();
    return this;
  }

  public CustomAnimator stop() {
    animator.end();
    return this;
  }

  public CustomAnimator setValue(float target) {
    // 如果动画正在运行，则获取当前的动画值
    if (animator.isRunning()) {
      float current = (float) animator.getAnimatedValue();
      // 创建新的动画目标值
      animator.setFloatValues(current, target);
    } else {
      // 如果动画没有在运行，直接设置新的目标值
      animator.setFloatValues(startValue, target);
    }
    start(); // 启动动画
    return this;
  }

  public CustomAnimator setOnUpdate(OnUpdate method) {
    onUpdate = method;
    return this;
  }

  public interface OnUpdate {
    void call(float value);
  }
}
