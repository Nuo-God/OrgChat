package dream.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlideView extends LinearLayout {
  private int height_limit = 200;
  private int speed;
  private float progress;
  protected int lastY;
  private OnProgress on_progress;
  private ArrayList<Assembly> assemblys;
  private OnErrorPrint onErrorPrint =
      (String err) -> {
        android.widget.Toast.makeText(getContext(), "SlideView[" + err + "]", 0).show();
      };
  private String title_style;
  private int cscrolly = 0;
  private boolean pressDown = false;
  protected int target = -1;

  public SlideView(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a =
        context
            .getTheme()
            .obtainStyledAttributes(attrs, com.dream.orgchat.R.styleable.SlideView, 0, 0);
    try {
      height_limit = a.getInt(com.dream.orgchat.R.styleable.SlideView_heightLimit, 200);
      title_style = a.getString(com.dream.orgchat.R.styleable.SlideView_titleStyle);
    } finally {
      a.recycle();
    }
    init();
  }

  public SlideView(Context context) {
    super(context);
    init();
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    setTitleStyle(title_style);
  }

  private void init() {
    assemblys = new ArrayList<>();
  }

  public void setProgress(OnProgress method) {
    on_progress = method;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {

    if (target != -1) return true;

    int y = (int) event.getY();

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        lastY = y;
        pressDown = true;
        break;
      case MotionEvent.ACTION_MOVE:
        int dy = lastY - y;
        scrollBy(0, dy);
        lastY = y;
        break;
      case MotionEvent.ACTION_UP:
        pressDown = false;
        invalidate();
        break;
    }
    return true;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {

    if (target != -1) return false;

    int y = (int) event.getY();

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        lastY = y;
        pressDown = true;
        break;
      case MotionEvent.ACTION_MOVE:
        int dy = lastY - y;
        if (Math.abs(dy) > ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
          return true; // 拦截事件，开始滑动
        }
        break;
      case MotionEvent.ACTION_UP:
        pressDown = false;
        invalidate();
        break;
    }
    return false;
  }

  @Override
  public void scrollBy(int x, int y) {
    int current_scrolly = Math.min(cscrolly, height_limit);
    int scrolly = current_scrolly + y;
    progress = current_scrolly * (1f / height_limit);
    if (scrolly < 0) {
      speed = 0;
      cscrolly = 0;
      current_scrolly = Math.min(cscrolly, height_limit);
      progress = current_scrolly * (1f / height_limit);
      for (Assembly value : assemblys) {
        value.update(progress);
      }
      on_progress.call(progress);
      return;
    } else if (getChildCount() > 1) {
      ScrollView view = (ScrollView) getChildAt(1);
      View v = view.getChildAt(0);
      if (v.getMeasuredHeight() - view.getHeight() <= view.getScrollY()) {
        speed = 0;
        cscrolly = view.getScrollY() - 1 + height_limit;
        view.scrollBy(0, -1);
        for (Assembly value : assemblys) {
          value.update(progress);
        }
        on_progress.call(progress);
        return;
      }
    }
    if (target != -1) speed = y;
    cscrolly += y;
    if (getChildCount() > 1) {
      View child = getChildAt(1);
      if (cscrolly > height_limit) {
        child.scrollBy(x, y);
      } else {
        child.scrollBy(x, -child.getScrollY());
      }
    }
    for (Assembly value : assemblys) {
      value.update(progress);
    }
    on_progress.call(progress);
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (target != -1) {
      if (target > 0) {
        int p = (int) (height_limit * (1 - progress) / 5);
        scrollBy(0, p < 1 ? 1 : p);
      } else {
        int p = (int) (height_limit * progress / 5);
        scrollBy(0, -(p < 1 ? 1 : p));
      }
      target = target == progress ? -1 : target;
      return;
    }
    if (!pressDown && progress != 0 && progress != 1) {
      if (progress + progress / 5 > 0.5) {
        int p = (int) (height_limit * (1 - progress) / 5);
        scrollBy(0, p < 1 ? 1 : p);
      } else {
        int p = (int) (height_limit * progress / 5);
        scrollBy(0, -(p < 1 ? 1 : p));
      }
      speed = 0;
      return;
    }
    if (target != -1 && speed != 0) {
      scrollBy(0, (int) (speed / 1.1f));
    }
  }

  public boolean setUnfoldingState(boolean state) {
    if (target != -1) return false;
    if (target != progress) target = state ? 1 : 0;
    scrollBy(0, target > 0 ? 1 : -1);
    return true;
  }

  public void setOnErrorPrint(OnErrorPrint method) {
    onErrorPrint = method;
  }

  public SlideView setLimit(int height) {
    height_limit = height;
    return this;
  }

  public SlideView setTitleStyle(String styleJson) {
    Gson gson = new Gson();
    try {
      StyleGsonContext[] styleArray = gson.fromJson(styleJson, StyleGsonContext[].class);
      if (styleArray == null || styleArray.length == 0 || getChildCount() == 0) {
        return this;
      }
      int layer = 0;
      ViewGroup view = (ViewGroup) getChildAt(0);
      loadStyle(view, layer, styleArray[0]);
    } catch (Exception err) {
      err.printStackTrace();
      onErrorPrint.call(err.getMessage());
    }
    return this;
  }

  // 第一层加载需要
  private void loadStyle(ViewGroup child, int layer, StyleGsonContext style) {
    ViewGroup.MarginLayoutParams layoutParams =
        (ViewGroup.MarginLayoutParams) child.getLayoutParams();
    if (style == null) {
      return;
    }
    boolean margins = !isNull(style.left, style.top, style.right, style.bottom);
    boolean range = !isNull(style.width, style.height);
    boolean alpha = !isNull(style.alpha);
    Assembly assembly = new Assembly(child, margins, range, alpha);
    if (margins) {
      Rect marginRect =
          new Rect(
              loadMargins(style.left, layoutParams.leftMargin),
              loadMargins(style.top, layoutParams.topMargin),
              loadMargins(style.right, layoutParams.rightMargin),
              loadMargins(style.bottom, layoutParams.bottomMargin));
      assembly.setMargins(
          marginRect,
          new Rect(
              layoutParams.leftMargin,
              layoutParams.topMargin,
              layoutParams.rightMargin,
              layoutParams.bottomMargin));
    }
    if (range) {
      int newWidth = loadRange(style.width, child.getWidth());
      int newHeight = loadRange(style.height, child.getHeight());
      assembly.setRange(newWidth, newHeight, child.getWidth(), child.getHeight());
    }
    if (alpha) {
      assembly.setAlpha(loadAlpha(style.alpha, child.getAlpha()), child.getAlpha());
    }
    assemblys.add(assembly);
    if (child instanceof ViewGroup && style.context != null) {
      loadStyle((ViewGroup) child, layer + 1, style.context);
    }
  }

  // 后续层次加载需要
  private void loadStyle(ViewGroup view, int layer, List<StyleGsonContext> styleArray) {
    for (int i = 0; i < Math.min(view.getChildCount(), styleArray.size()); i++) {
      StyleGsonContext style = styleArray.get(i);
      View child = view.getChildAt(i);
      ViewGroup.MarginLayoutParams layoutParams =
          (ViewGroup.MarginLayoutParams) child.getLayoutParams();
      if (style == null) {
        break;
      }
      boolean margins = !isNull(style.left, style.top, style.right, style.bottom);
      boolean range = !isNull(style.width, style.height);
      boolean alpha = !isNull(style.alpha);
      Assembly assembly = new Assembly(child, margins, range, alpha);
      if (margins) {
        Rect marginRect =
            new Rect(
                loadMargins(style.left, layoutParams.leftMargin),
                loadMargins(style.top, layoutParams.topMargin),
                loadMargins(style.right, layoutParams.rightMargin),
                loadMargins(style.bottom, layoutParams.bottomMargin));
        assembly.setMargins(
            marginRect,
            new Rect(
                layoutParams.leftMargin,
                layoutParams.topMargin,
                layoutParams.rightMargin,
                layoutParams.bottomMargin));
      }
      if (range) {
        int newWidth = loadRange(style.width, child.getWidth());
        int newHeight = loadRange(style.height, child.getHeight());
        assembly.setRange(newWidth, newHeight, child.getWidth(), child.getHeight());
      }
      if (alpha) {
        assembly.setAlpha(loadAlpha(style.alpha, child.getAlpha()), child.getAlpha());
      }
      assemblys.add(assembly);
      if (child instanceof ViewGroup && style.context != null) {
        loadStyle((ViewGroup) child, layer + 1, style.context);
      }
    }
  }

  // 判断是否为Null
  private boolean isNull(Object... value) {
    boolean state = false;
    for (Object v : value) {
      state = v == null ? true : state;
    }
    return state;
  }

  // 加载透明度
  private float loadAlpha(String newAlpha, float originalAlpha) {
    if (newAlpha == null) {
      return originalAlpha;
    }
    try {
      return Float.parseFloat(newAlpha);
    } catch (Exception err) {
      err.printStackTrace();
      return originalAlpha;
    }
  }

  // 加载高宽
  private int loadRange(String newValue, int originalValue) {
    if (newValue == null) {
      return originalValue;
    }
    try {
      return Integer.parseInt(newValue);
    } catch (Exception err) {
      err.printStackTrace();
      // 处理特定字符串情况
      if (newValue.equals("match_parent")) {
        return originalValue; // 如果是 match_parent，返回原值
      } else if (newValue.equals("wrap_content")) {
        return originalValue; // 如果是 wrap_content，返回原值
      }
      return (int) xp(newValue);
    }
  }

  // 加载边距
  private int loadMargins(String newMargins, int originalMargins) {
    if (newMargins == null) {
      return originalMargins;
    }
    try {
      return Integer.parseInt(newMargins);
    } catch (Exception err) {
      err.printStackTrace();
      return (int) xp(newMargins);
    }
  }

  // dp 转 xp
  private HashMap<String, Integer> types = new HashMap<>();

  public float xp(String sdp) {
    // 初始化单位类型映射
    types.put("px", TypedValue.COMPLEX_UNIT_PX);
    types.put("dp", TypedValue.COMPLEX_UNIT_DIP);
    types.put("sp", TypedValue.COMPLEX_UNIT_SP);
    types.put("pt", TypedValue.COMPLEX_UNIT_PT);
    types.put("in", TypedValue.COMPLEX_UNIT_IN);
    types.put("mm", TypedValue.COMPLEX_UNIT_MM);
    DisplayMetrics dm = this.getResources().getDisplayMetrics();
    // 使用正则表达式匹配数字和单位
    Pattern pattern = Pattern.compile("^(\\-?[\\.\\d]+)(\\w{2})$");
    Matcher matcher = pattern.matcher(sdp);
    if (matcher.find()) {
      // 获取匹配的数字和单位
      String nStr = matcher.group(1);
      String ty = matcher.group(2);
      // 将字符串转换为浮点数
      float n = Float.parseFloat(nStr);
      // 获取类型索引
      Integer typeIndex = types.get(ty);
      if (typeIndex != null) {
        // 应用尺寸转换并返回结果
        return TypedValue.applyDimension(typeIndex, n, dm);
      }
    }
    return 0; // 如果格式不正确，返回0或抛出异常
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

  public static interface OnProgress {
    void call(float value);
  }

  public static interface OnErrorPrint {
    void call(String err);
  }

  /* 支持动画样式 */
  private class StyleGsonContext {
    @SerializedName("width")
    String width;

    @SerializedName("height")
    String height;

    @SerializedName("alpha")
    String alpha;

    @SerializedName("background")
    String background;

    @SerializedName("left")
    String left;

    @SerializedName("top")
    String top;

    @SerializedName("right")
    String right;

    @SerializedName("bottom")
    String bottom;

    @SerializedName("context")
    List<StyleGsonContext> context;
  }

  public class Assembly {
    private boolean margins;
    private boolean range;
    private boolean alpha;
    private Rect oMargins;
    private Rect tMargins;
    private int oWidth, oHeight;
    private int tWidth, tHeight;
    private float oAlpha;
    private float tAlpha;
    private View view;

    public Assembly(View view, boolean margins, boolean range, boolean alpha) {
      this.view = view;
      this.margins = margins;
      this.range = range;
      this.alpha = alpha;
    }

    public void setMargins(Rect margins, Rect original) {
      tMargins = margins;
      oMargins = original;
    }

    public void setRange(int w, int h, int original_w, int original_h) {
      tWidth = w;
      oWidth = original_w;
      tHeight = h;
      oHeight = original_h;
    }

    public void setAlpha(float a, float original) {
      tAlpha = a;
      oAlpha = original;
    }

    public void update(float p /* 0~1 */) {
      // 根据以上内容和progress来修改view的状态
      /* (1-progress)*(旧)value+progress*(新)value */
      float progress = p < 0 ? 0 : p > 1 ? 1 : p;

      // 根据 progress 计算当前边距值
      if (margins) {
        int left = (int) ((1 - progress) * oMargins.left + progress * tMargins.left);
        int top = (int) ((1 - progress) * oMargins.top + progress * tMargins.top);
        int right = (int) ((1 - progress) * oMargins.right + progress * tMargins.right);
        int bottom = (int) ((1 - progress) * oMargins.bottom + progress * tMargins.bottom);

        ViewGroup.MarginLayoutParams layoutParams =
            (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(left, top, right, bottom);
        view.setLayoutParams(layoutParams); // 更新布局
      }

      // 根据 progress 计算当前宽高值
      if (range) {
        int currentWidth = (int) ((1 - progress) * oWidth + progress * tWidth);
        int currentHeight = (int) ((1 - progress) * oHeight + progress * tHeight);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = currentWidth;
        layoutParams.height = currentHeight;
        view.setLayoutParams(layoutParams); // 更新布局
      }

      // 根据 progress 计算当前透明度值
      if (alpha) {
        float currentAlpha = (1 - progress) * oAlpha + progress * tAlpha;
        view.setAlpha(currentAlpha); // 更新透明度
      }
    }
  }
}
