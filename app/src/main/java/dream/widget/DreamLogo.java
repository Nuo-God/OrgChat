package dream.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.res.ResourcesCompat;
import dream.animator.CustomAnimator;

public class DreamLogo extends View {
  protected Paint icon_paint;
  protected Paint line_paint;
  protected Paint text_paint;
  protected Rect range;
  protected float icon_multiple = 1f;
  protected float icon_width;
  protected float icon_height;
  private CustomAnimator animator;
  private static final float radius = 0.08148f;
  private static final float[] lines =
      new float[] {
        /* Point1 x, y, r, b */
        0.332222f, 0.357037f, 66.1f, 0.312595f,
        /* Point2 x, y, r, b */
        0.490740f, 0.473333f, 66.1f, 0.185185f,
        /* Point3 x, y, r, b */
        0.593333f, 0.473333f, 66.1f, 0.185185f,
      };
  private static final float[] logo_text_points =
      new float[] {
        /* Range1 x, y, end_x, end_y */
        0.407407f, 0.740740f, 0.5f, 0.851851f,
        /* Range2 x, y, end_x, end_y */
        0.5f, 0.740740f, 0.592592f, 0.851851f,
      };
  private float used_edge = 0;
  protected float rad = 0;
  protected float[] pot = new float[12];
  protected Bitmap[] text_map = new Bitmap[2];
  protected float[][] texts_pot = new float[2][2];
  private float[] gradient_post = new float[4];
  protected int time = 1000;
  private boolean display_title = true;

  private OnAnimationEnds onAnimationEnds = (DreamLogo view) -> {};

  private void init() {
    icon_paint = new Paint();
    line_paint = new Paint();
    text_paint = new Paint();
    range = new Rect();
    LinearGradient linearGradient =
        new LinearGradient(
            0,
            0,
            600,
            600,
            new int[] {0xFF5F7EF3, 0xFF7DA5EA, 0xFFAADFDE},
            null, // 色标
            Shader.TileMode.CLAMP); // 填充模式
    icon_paint.setShader(linearGradient);
    icon_paint.setStyle(Paint.Style.STROKE);
    icon_paint.setStrokeWidth(radius);
    icon_paint.setAntiAlias(true);
    icon_paint.setStrokeCap(Paint.Cap.ROUND);
    line_paint.setStyle(Paint.Style.STROKE);
    line_paint.setStrokeWidth(10);
    text_paint.setTypeface(
        ResourcesCompat.getFont(getContext(), com.dream.orgchat.R.font.bold_font));
    reset();
  }

  public DreamLogo(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a =
        context
            .getTheme()
            .obtainStyledAttributes(attrs, com.dream.orgchat.R.styleable.DreamLogo, 0, 0);
    try {
      icon_multiple = a.getFloat(com.dream.orgchat.R.styleable.DreamLogo_size, 1);
      time = a.getInt(com.dream.orgchat.R.styleable.DreamLogo_time, 1000);
      display_title = a.getBoolean(com.dream.orgchat.R.styleable.DreamLogo_time, true);
    } finally {
      a.recycle();
    }
    init();
  }

  public DreamLogo(Context context) {
    super(context);
    init();
  }

  private Bitmap limit(Bitmap map, int width, int height) {
    return Bitmap.createScaledBitmap(map, width, height, true);
  }

  private Bitmap intercept(Bitmap map, float[] pot) {
    if (pot[0] > pot[2] || pot[1] > pot[3] || pot[2] > map.getWidth() || pot[3] > map.getHeight()) {
      return null;
    }
    Bitmap bitmap =
        Bitmap.createBitmap(
            (int) (pot[2] - pot[0]), (int) (pot[3] - pot[1]), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    canvas.drawBitmap(
        map,
        new Rect((int) pot[0], (int) pot[1], (int) pot[2], (int) pot[3]),
        new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
        null);
    return bitmap;
  }

  private Bitmap intercept(Bitmap map, float[] pot, int initial) {
    int x = initial;
    int y = initial + 1;
    int end_x = initial + 2;
    int end_y = initial + 3;
    if (pot[x] < 0
        || pot[y] < 0
        || pot[end_x] > map.getWidth()
        || pot[end_y] > map.getHeight()
        || pot[x] > pot[end_x]
        || pot[y] > pot[end_y]) {
      return null;
    }
    Bitmap bitmap =
        Bitmap.createBitmap(
            (int) (pot[end_x] - pot[x]), (int) (pot[end_y] - pot[y]), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    canvas.drawBitmap(
        map,
        new Rect((int) pot[x], (int) pot[y], (int) pot[end_x], (int) pot[end_y]),
        new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
        null);
    return bitmap;
  }

  private float[] aTP(float x, float y, float a, float r) {
    double angle = Math.toRadians(a);
    float[] p = new float[2];
    p[0] = x + (float) (r * Math.cos(angle));
    p[1] = y + (float) (r * Math.sin(angle));
    return p;
  }

  private float distance(float x, float y, float e_x, float e_y) {
    return (float) Math.sqrt((x - e_x) * (x - e_x) + (y - e_y) * (y - e_y));
  }

  private float calculateAngle(float x1, float y1, float x2, float y2) {
    // 计算差值
    float deltaX = x2 - x1;
    float deltaY = y2 - y1;

    // 计算角度（以弧度为单位），然后转换为度
    float angleInRadians = (float) Math.atan2(deltaY, deltaX);
    float angleInDegrees = (float) Math.toDegrees(angleInRadians);

    return angleInDegrees; // 返回角度值
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int width = getWidth();
    int height = getHeight();
    float min_edge = Math.min(width, height) * icon_multiple;
    float min_edge_2 = min_edge / 2;
    range.right = (int) min_edge;
    range.bottom = (int) min_edge;
    canvas.save();
    canvas.translate(width / 2, height / 2);
    canvas.scale(icon_multiple, icon_multiple);
    canvas.translate(-min_edge_2, -min_edge_2);
    /* 绘制 logo */
    if ((int) used_edge != (int) min_edge) {
      used_edge = min_edge;
      refreshLines();
    }
    for (int k = 0; k < pot.length; k += 4) {
      canvas.drawLine(pot[k], pot[k + 1], pot[k + 2], pot[k + 3], icon_paint);
    }
    if (false)
      /* 绘制 Text */
      for (int k = 0; k < texts_pot.length; k++) {
        canvas.drawBitmap(text_map[k], texts_pot[k][0], texts_pot[k][1], text_paint);
      }
    canvas.restore();
  }

  protected void refreshLines() {
    icon_paint.setStrokeWidth(radius * used_edge);
    for (int k = 0; k < lines.length; k += 4) {
      float a, r;
      pot[k] = lines[k] * used_edge;
      pot[k + 1] = lines[k + 1] * used_edge;
      a = lines[k + 2];
      r = lines[k + 3] * used_edge;
      float[] pos = aTP(pot[k], pot[k + 1], a, r);
      pot[k + 2] = pos[0];
      pot[k + 3] = pos[1];
    }
    setLinearGradient(0.0001f);
    /* 加载logo图片 */
    Bitmap logo_map =
        limit(
            BitmapFactory.decodeResource(
                getContext().getResources(),
                com.dream.orgchat.R.drawable.illusory_world_of_the_mortals),
            (int) used_edge,
            (int) used_edge);
    float[] local_pot = new float[4];
    for (int i = 0; i < logo_text_points.length; i += 4) {
      for (int k = 0; k < 4; k++) local_pot[k] = logo_text_points[i + k] * logo_map.getWidth();
      text_map[i / 4] = intercept(logo_map, local_pot);
      texts_pot[i / 4][0] = local_pot[0];
      texts_pot[i / 4][1] = local_pot[1];
    }
  }

  private void setLinearGradient(float value) {
    float rad = radius * used_edge;
    float rads = (1 - value) * rad;
    float ang = calculateAngle(pot[0], pot[1], pot[10], pot[11]);
    float dis = distance(pot[0], pot[1], pot[10], pot[11]);
    float min_dis = dis / 3;
    dis = min_dis * 4 + rad + rads;
    float[] p = aTP(pot[0] - rads, pot[1] - rads, ang, value * dis);
    gradient_post = new float[] {pot[0] - rads, pot[1] - rads, p[0], p[1]};
    LinearGradient linearGradient =
        new LinearGradient(
            gradient_post[0],
            gradient_post[1],
            gradient_post[2],
            gradient_post[3],
            new int[] {
              0xFF5F7EF3,
              0xFF7DA5EA,
              0xFFAADFDE,
              getContext().getColor(com.dream.orgchat.R.color.background_color)
            },
            null,
            Shader.TileMode.CLAMP);
    icon_paint.setShader(linearGradient);
  }

  public void start() {
    animator.start();
  }

  public void stop() {
    animator.stop();
  }

  public void reset() {
    animator = CustomAnimator.create(0, 1, time);
    animator.setOnUpdate(
        (float v) -> {
          setLinearGradient(v);
          invalidate();
          if (v == 1) {
            onAnimationEnds.call(this);
          }
        });
    invalidate();
  }

  public void setOnAnimationEnds(OnAnimationEnds method) {
    onAnimationEnds = method;
  }

  public void setTime(int time) {
    this.time = time;
    reset();
  }

  public void setTitleVisibility(boolean visibility) {
    display_title = visibility;
  }

  public interface OnAnimationEnds {
    void call(DreamLogo view);
  }
}
