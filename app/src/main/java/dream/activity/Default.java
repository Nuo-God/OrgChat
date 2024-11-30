package dream.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.drawable.IconCompat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Default extends Activity {
  protected boolean IS_NIGHT_MODE;
  protected int width = 0;
  protected int height = 0;

  // 判断是否处于夜间模式
  protected static boolean isNightMode(Context context) {
    int nightModeFlags =
        context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
  }

  // 开启夜间模式
  protected void openNightMode() {
    if (!IS_NIGHT_MODE) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
  }

  // 关闭夜间模式
  protected void closeNightMode() {
    if (IS_NIGHT_MODE) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
  }

  // 设置状态栏可见性
  protected void setStatusBarVisibility(boolean visibility) {
    if (visibility) getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    else getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
  }

  // 设置状态栏颜色
  protected void setStatusBarColor(int color) {
    getWindow().setStatusBarColor(color);
    getWindow().setNavigationBarColor(color);
    getWindow().setSoftInputMode(0x10);
  }

  protected int getWidth() {
    return width;
  }

  protected int getHeight() {
    return height;
  }

  // 检测是否有调试器连接
  private boolean isDebugged() {
    return android.os.Debug.isDebuggerConnected();
  }

  // 服务是否在运行中
  protected boolean isServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningServiceInfo> services =
        manager.getRunningServices(Integer.MAX_VALUE);
    for (ActivityManager.RunningServiceInfo serviceInfo : services) {
      if (serviceInfo.service.getClassName().equals(serviceClass.getName())) {
        return true;
      }
    }
    return false;
  }

  // 判断应用是否处于电池优化状态
  protected boolean isBatteryOptimized() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
      String packageName = getPackageName();
      return powerManager.isIgnoringBatteryOptimizations(packageName);
    }
    // 在 Android 6.0（API 23）及以下版本中，不需要检查电池优化
    return true; // 默认返回 true，表示不受电池优化影响
  }

  // 申请电池优化
  protected void applyToIgnoreBatteryOptimization() {
    // 检查 Android 版本是否支持电池优化设置
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Intent intent = new Intent();
      String packageName = getPackageName();
      PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      // 检查应用是否已经忽略电池优化
      if (!pm.isIgnoringBatteryOptimizations(packageName)) {
        // 创建请求忽略电池优化的意图
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent); // 启动设置界面
      }
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

  // 新建活动
  protected void newActivity(Class<?> activity) {
    Intent intent = new Intent(this, activity);
    startActivity(intent);
  }

  // 新建活动，并传递两个动画资源 ID
  protected void newActivity(Class<?> activity, int enterAnim, int exitAnim) {
    Intent intent = new Intent(this, activity);
    startActivity(intent);
    overridePendingTransition(enterAnim, exitAnim); // 应用跳转动画
  }

  // 新建活动，并传递一个 HashMap 数据
  protected void newActivity(Class<?> activity, HashMap<String, String> data) {
    Intent intent = new Intent(this, activity);
    if (data != null) { // 检查 HashMap 是否为 null
      for (Map.Entry<String, String> entry : data.entrySet()) {
        intent.putExtra(entry.getKey(), entry.getValue()); // 将 HashMap 中的数据放入 Intent
      }
    }
    startActivity(intent);
  }

  // 新建活动，并传递两个动画资源 ID 和一个 HashMap 数据
  protected void newActivity(
      Class<?> activity, int enterAnim, int exitAnim, HashMap<String, String> data) {
    Intent intent = new Intent(this, activity);
    if (data != null) { // 检查 HashMap 是否为 null
      for (Map.Entry<String, String> entry : data.entrySet()) {
        intent.putExtra(entry.getKey(), entry.getValue()); // 将 HashMap 中的数据放入 Intent
      }
    }
    startActivity(intent);
    overridePendingTransition(enterAnim, exitAnim); // 应用跳转动画
  }

  // 新建活动，并传递 Bundle
  protected void newActivity(Class<?> activity, Bundle bundle) {
    Intent intent = new Intent(this, activity);
    startActivity(intent, bundle);
  }

  // 新建活动，并传递 Bundle 和一个 HashMap 数据
  protected void newActivity(Class<?> activity, Bundle bundle, HashMap<String, String> data) {
    Intent intent = new Intent(this, activity);
    if (data != null) { // 检查 HashMap 是否为 null
      for (Map.Entry<String, String> entry : data.entrySet()) {
        intent.putExtra(entry.getKey(), entry.getValue()); // 将 HashMap 中的数据放入 Intent
      }
    }
    startActivity(intent, bundle);
  }

  // 获取指定年份和月份的天数
  public static Integer getDaysInMonth(int year, int month) {
    // 检查月份是否有效
    if (month < 1 || month > 12) {
      return null; // 无效的月份
    }
    // 如果是二月，检查是否为闰年
    if (month == 2) {
      if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
        return 29; // 闰年，二月有29天
      } else {
        return 28; // 平年，二月有28天
      }
    }
    int[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    return daysInMonth[month - 1]; // 返回相应的天数
  }

  public static int getTime(int type) {
    Calendar calendar = Calendar.getInstance();
    switch (type) {
      case 1:
        return calendar.get(Calendar.YEAR);
      case 2:
        return calendar.get(Calendar.MONTH) + 1; // 月份从0开始
      case 3:
        return calendar.get(Calendar.DAY_OF_MONTH);
      case 4:
        return calendar.get(Calendar.HOUR_OF_DAY);
      case 5:
        return calendar.get(Calendar.MINUTE);
      case 6:
        return calendar.get(Calendar.SECOND);
      default:
        return -1; // 无效的类型
    }
  }

  // 获取剩余 (距离目标时间还剩多久)
  protected int[] surplusTime(int year, int month, int day, int hour, int minute, int second) {
    int[] current_time = new int[6];
    int[] time = new int[] {year, month, day, hour, minute, second};
    int[] t = new int[6];
    for (int i = 0; i < 6; i++) {
      current_time[i] = getTime(i + 1); // 获取当前时间
      t[i] = time[i] - current_time[i]; // 计算剩余时间
    }
    // 借位处理
    for (int i = 5; i >= 2; i--) { // 从秒到天
      if (t[i] < 0) {
        if (i == 5) { // 秒
          t[i] += 60;
          t[i - 1] -= 1; // 借1分钟
        } else if (i == 4) { // 分
          t[i] += 60;
          t[i - 1] -= 1; // 借1小时
        } else if (i == 3) { // 小时
          t[i] += 24;
          t[i - 1] -= 1; // 借1天
        } else if (i == 2) { // 天
          int days_in_prev_month = getDaysInMonth(time[0], time[1] - 1);
          if (time[1] == 1) { // 如果1月，需要借1年
            t[i] += days_in_prev_month;
            t[0] -= 1; // 借1年
          } else {
            t[i] += days_in_prev_month;
            t[i - 1] -= 1; // 借1月
          }
        }
      }
    }
    // 处理借位情况
    for (int i = 5; i >= 2; i--) { // 从秒到天
      if (t[i] < 0) {
        t[i] += (i == 5 ? 60 : (i == 4 ? 24 : 30)); // 秒和分为60，小时为24，天为30
        t[i - 1] -= 1;
      }
    }
    return t; // 返回剩余时间
  }

  // 自定义print提示
  private static StringBuilder conte = new StringBuilder();
  private static Float record;
  private static android.widget.Toast toast;
  private static boolean request_state = false;

  public void print(Object... a) {
    if (record == null) {
      record = (float) System.nanoTime();
      toast = android.widget.Toast.makeText(this, conte, 1);
    }
    if (System.nanoTime() - record > 2000000000f) {
      record = (float) System.nanoTime();
      conte = new StringBuilder();
      toast = android.widget.Toast.makeText(this, conte, 1);
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

  // tsak 延迟
  protected void task(int time, Callback method) {
    new Handler()
        .postDelayed(
            new Runnable() {
              @Override
              public void run() {
                method.call();
              }
            },
            time);
  }

  // tsak 延迟
  protected void task(Callback loading, Callback method) {
    new Thread() {
      @Override
      public void run() {
        super.run();
        loading.call();
        runOnUiThread(
            () -> {
              method.call();
            });
      }
    }.start();
  }

  // tsak 延迟
  protected void task(Callback loading, QbjectCallback method) {
    new Thread() {
      @Override
      public void run() {
        super.run();
        Object value = loading.call();
        runOnUiThread(
            () -> {
              method.call(value);
            });
      }
    }.start();
  }

  // 发送通知
  protected void sendNotification(
      IconCompat icon, String title, String message, Class<?> activity) {
    Notification test =
        new Notification(
            this, Notification.getIntent(Notification.OPEN_ACTIVITY, this, activity), icon, title);
    test.setContent(message);
    test.show();
  }

  protected void sendNotification(int icon, String title, String message, Class<?> activity) {
    Notification test =
        new Notification(
            this, Notification.getIntent(Notification.OPEN_ACTIVITY, this, activity), icon, title);
    test.setContent(message);
    test.show();
  }

  // 监听控件加载完成
  protected void monitorView(View view, Callback method) {
    view.getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                // 移除监听器，避免重复调用
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                method.call();
              }
            });
  }

  protected View loadlayout(int layout, IdContext container) {
    LayoutInflater inflater = LayoutInflater.from(this);
    // 尝试加载指定的布局
    View view = inflater.inflate(layout, null);
    FrameLayout father = new FrameLayout(this);
    father.addView(view);
    addViewsToContainer(father, container);
    father.removeView(view);
    return view;
  }

  protected View loadlayout(int layout) {
    return loadlayout(layout, new IdContext());
  }

  // 递归地将所有子视图添加到容器
  private void addViewsToContainer(View parent, IdContext container) {
    // 获取父视图的所有子视图
    if (parent instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) parent;
      for (int i = 0; i < group.getChildCount(); i++) {
        View child = group.getChildAt(i);
        String resourceName = getResourceName(child.getId());
        // 将视图添加到容器，使用视图的ID作为键
        if (resourceName != null) {
          container.a(resourceName, child);
        }
        // 递归处理子视图
        addViewsToContainer(child, container);
      }
    }
  }

  public String getFileContext(int resourceId) {
    StringBuilder stringBuilder = new StringBuilder();
    Resources resources = getResources();
    InputStream inputStream = resources.openRawResource(resourceId);
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

    String line;
    try {
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line).append("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return stringBuilder.toString();
  }

  // 根据视图的ID获取资源名称
  private String getResourceName(int id) {
    if (id == View.NO_ID) {
      return null;
    }
    // 获取资源的名称
    return getResources().getResourceEntryName(id);
  }

  // IdContext 类
  public static class IdContext extends HashMap<String, View> {

    // 添加视图的方法
    public void a(String id, View view) {
      put(id, view);
    }

    @SuppressWarnings("unchecked")
    public View g(String id_name) {
      return get(id_name);
    }
  }

  // Callback 方法
  public interface Callback {
    Object call();
  }

  // QbjectCallback 方法
  public interface QbjectCallback {
    void call(Object value);
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Return {
    Class<?>[] value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Receive {
    Class<?>[] value();
  }

  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    IS_NIGHT_MODE = isNightMode(this);
    // 获取窗口管理器
    WindowManager windowManager = getWindowManager();
    DisplayMetrics displayMetrics = new DisplayMetrics();
    windowManager.getDefaultDisplay().getMetrics(displayMetrics);
    width = displayMetrics.widthPixels; // 获取屏幕宽度
    height = displayMetrics.heightPixels; // 获取屏幕高度
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
      // 获取宽高
      width = getWindow().getDecorView().getWidth();
      height = getWindow().getDecorView().getHeight();
    }
  }
}
