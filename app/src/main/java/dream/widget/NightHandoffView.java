package dream.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.VectorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Checkable;
import androidx.appcompat.app.AppCompatDelegate;

public class NightHandoffView extends Button implements Checkable {
  private float proportion = 0.7f;
  private float degrees;
  private Paint paint;
  protected float progress;
  protected boolean nighttimeState;
  private int usedWindow[] = new int[] {0, 0};
  private ValueAnimator anim;
  private VectorDrawable sunVector;
  private VectorDrawable nightVector;
  public int nightColor;
  private float radius;
  private OnStatusUpdate onStatusUpdate = (boolean state) -> {};

  private OnClickListener mListener;

  public NightHandoffView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public NightHandoffView(Context context) {
    super(context);
    init();
  }

  // 判断是否处于夜间模式
  protected boolean isNightMode() {
    int nightModeFlags =
        getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
  }

  private void init() {
    paint = new Paint();
    paint.setAntiAlias(true);
    sunVector =
        (VectorDrawable)
            getResources().getDrawable(com.dream.orgchat.R.drawable.ic_brightness_4, null);
    nightVector =
        (VectorDrawable)
            getResources().getDrawable(com.dream.orgchat.R.drawable.ic_brightness_7, null);
    nighttimeState = !isNightMode();
    progress = nighttimeState ? 1 : 0;
    setBackgroundResource(com.dream.orgchat.R.drawable.ripple_background);
    RippleDrawable rippleDrawable =
        (RippleDrawable) getResources().getDrawable(com.dream.orgchat.R.drawable.ripple_background);
    rippleDrawable.setRadius(50);
    setBackground(rippleDrawable);
    mListener =
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            // 响应点击事件
            toggle();
          }
        };
    setOnClickListener(mListener);
    anim = ObjectAnimator.ofFloat(new float[] {0, 1});
    anim.addUpdateListener(
        new ValueAnimator.AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator self) {
            float p = (float) self.getAnimatedValue();
            progress = p;
            if (p == 0) {
              // 切换成功
              onStatusUpdate.call(nighttimeState);
            }
            invalidate();
          }
        });
    anim.setDuration(300);
    anim.setRepeatMode(Animation.RESTART);
    anim.setInterpolator(new LinearInterpolator());
  }

  public void setOnStatusUpdate(OnStatusUpdate method) {
    onStatusUpdate = method;
  }

  public void setProportion(float proportion) {
    this.proportion = proportion;
  }

  @Override
  public boolean isChecked() {
    return nighttimeState;
  }

  @Override
  public void setChecked(boolean checked) {
    nighttimeState = checked;
    anim.start();
  }

  @Override
  public void toggle() {
    setChecked(!nighttimeState);
  }

  private float[] aTP(float x, float y, float a, float r) {
    double angle = Math.toRadians(a);
    float[] p = new float[2];
    p[0] = x + (float) (r * Math.cos(angle));
    p[1] = y + (float) (r * Math.sin(angle));
    return p;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    int width = getWidth();
    int height = getHeight();
    float min_edge = Math.min(width, height) * proportion;
    int x = width / 2, y = height / 2;
    int mp = (int) (min_edge * proportion);
    float m2 = (mp) / 2;
    float p = progress;
    if (nighttimeState) p = 1 - p;
    if (width != usedWindow[0] || height != usedWindow[1]) {
      usedWindow[0] = width;
      usedWindow[1] = height;
      RippleDrawable rippleDrawable =
          (RippleDrawable)
              getResources().getDrawable(com.dream.orgchat.R.drawable.ripple_background);
      rippleDrawable.setRadius((int) min_edge);
      sunVector.setBounds(0, 0, mp, mp);
      nightVector.setBounds(0, 0, mp, mp);
    }
    canvas.translate(m2, m2);
    sunVector.setAlpha((int) p * 255);
    sunVector.draw(canvas);
    nightVector.setAlpha(255 - (int) p * 255);
    nightVector.draw(canvas);
  }

  public interface OnStatusUpdate {
    void call(boolean state);
  }

  // 自定义print提示
  private static StringBuilder conte = new StringBuilder();
  private static Float record;
  private static android.widget.Toast toast;
  private static boolean request_state = false;

  public void print(Object... a) {
    if (record == null) {
      record = (float) System.nanoTime();
      toast = android.widget.Toast.makeText(getContext(), conte, 1);
    }
    if (System.nanoTime() - record > 2000000000f) {
      record = (float) System.nanoTime();
      conte = new StringBuilder();
      toast = android.widget.Toast.makeText(getContext(), conte, 1);
      request_state = false;
    }
    if (request_state) {
      conte.append("\n");
    } else {
      request_state = true;
    }
    int quantity = 0;
    for (Object v : a) {
      if (quantity > 0) conte.append("  ");
      quantity++;
      if (v == null) {
        conte.append("null");
        continue;
      }
      if (v instanceof float[]) {
        conte.append("[");
        int length = ((float[]) v).length;
        for (int i = 0; i < length; i++) {
          if (i != 0 && i < length) conte.append(", ");
          conte.append(((float[]) v)[i]);
        }
        conte.append("]");
        continue;
      } else if (v instanceof int[]) {
        conte.append("[");
        int length = ((int[]) v).length;
        for (int i = 0; i < length; i++) {
          if (i != 0 && i < length) conte.append(", ");
          conte.append(((int[]) v)[i]);
        }
        conte.append("]");
        continue;
      }
      conte.append(v.toString());
    }
    toast.setText(conte);
    toast.show();
  }
}
