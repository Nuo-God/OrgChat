package dream.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

public class Notification {
  public static final int OPEN_ACTIVITY = 0b0000001; /* 打开活动 */
  public static final int SHARE_CONTENT = 0b0000010; /* 分享内容 */
  public static final int OPEN_TELEPHONE = 0b0000100; /* 拨打电话 */
  public static final int START_SERVICE = 0b0001000; /* 启动服务 */
  public static final int VIEW_LOCATION = 0b0010000; /* 查看位置 */
  private String title;
  private String message;
  private Boolean icon_state;
  protected PendingIntent pendingIntent;
  protected NotificationCompat.Builder notification;
  protected NotificationManager notificationManager;

  protected void createNotification(Context context, Intent intention) {
    pendingIntent = PendingIntent.getActivity(context, 0, intention, 0);
    notification = new NotificationCompat.Builder(context, "default");
    notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    // For Android Oreo and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel =
          new NotificationChannel(
              "default", "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
      notificationManager.createNotificationChannel(channel);
    }
  }

  public Notification(Context context, Intent intention) {
    createNotification(context, intention);
  }

  public Notification(Context context, Intent intention, String title) {
    createNotification(context, intention);
    setTitle(title);
  }

  public Notification(Context context, Intent intention, int icon) {
    createNotification(context, intention);
    setIcon(icon);
  }

  public Notification(Context context, Intent intention, int icon, String title) {
    createNotification(context, intention);
    setIcon(icon);
    setTitle(title);
  }

  public Notification(Context context, Intent intention, IconCompat icon) {
    createNotification(context, intention);
    setIcon(icon);
  }

  public Notification(Context context, Intent intention, IconCompat icon, String title) {
    createNotification(context, intention);
    setIcon(icon);
    setTitle(title);
  }

  public static Intent getIntent(int type, Object... parameter) {
    Intent intent = null;
    try {
      switch (type) {
        case OPEN_ACTIVITY:
          if (parameter.length < 2
              || !(parameter[0] instanceof Context)
              || !(parameter[1] instanceof Class<?>)) {
            throw new IllegalArgumentException("Invalid parameters for OPEN_ACTIVITY");
          }
          Context context = (Context) parameter[0];
          Class<?> activityClass = (Class<?>) parameter[1];
          intent = new Intent(context, activityClass);
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          break;

        case SHARE_CONTENT:
          if (parameter.length < 1 || !(parameter[0] instanceof String)) {
            throw new IllegalArgumentException("Invalid parameters for SHARE_CONTENT");
          }
          String shareText = (String) parameter[0];
          String chooserTitle =
              parameter.length >= 2 && parameter[1] instanceof String
                  ? (String) parameter[1]
                  : "分享内容";
          intent = new Intent(Intent.ACTION_SEND);
          intent.setType("text/plain");
          intent.putExtra(Intent.EXTRA_TEXT, shareText);
          intent = Intent.createChooser(intent, chooserTitle);
          break;

        case OPEN_TELEPHONE:
          if (parameter.length < 1 || !(parameter[0] instanceof String)) {
            throw new IllegalArgumentException("Invalid parameters for OPEN_TELEPHONE");
          }
          String phoneNumber = (String) parameter[0];
          intent = new Intent(Intent.ACTION_DIAL);
          intent.setData(Uri.parse("tel:" + phoneNumber));
          break;

        case START_SERVICE:
          if (parameter.length < 2
              || !(parameter[0] instanceof Context)
              || !(parameter[1] instanceof Class<?>)) {
            throw new IllegalArgumentException("Invalid parameters for START_SERVICE");
          }
          Context serviceContext = (Context) parameter[0];
          Class<?> serviceClass = (Class<?>) parameter[1];
          intent = new Intent(serviceContext, serviceClass);
          break;

        case VIEW_LOCATION:
          if (parameter.length < 2
              || !(parameter[0] instanceof Context)
              || !(parameter[1] instanceof String)) {
            throw new IllegalArgumentException("Invalid parameters for VIEW_LOCATION");
          }
          Context locationContext = (Context) parameter[0];
          String locationUri = (String) parameter[1]; // 例如 "geo:0,0?q=latitude,longitude"
          intent = new Intent(Intent.ACTION_VIEW);
          intent.setData(Uri.parse(locationUri));
          break;

        default:
          throw new UnsupportedOperationException("Unsupported intent type: " + type);
      }
    } catch (Exception e) {
      e.printStackTrace();
      // 你可以在这里记录日志或者执行其他错误处理
    }
    return intent;
  }

  public void setTitle(String title) {
    this.title = title;
    notification.setContentTitle(this.title);
  }

  public void setContent(String message) {
    this.message = message;
    notification.setContentText(this.message);
  }

  public void setIcon(int icon) {
    this.icon_state = true;
    notification.setSmallIcon(icon);
  }

  public void setIcon(IconCompat icon) {
    this.icon_state = true;
    notification.setSmallIcon(icon);
  }

  public void show() {
    if (title == null) setTitle("Test");
    if (message == null) setContent("Test Notification");
    if (icon_state == null) setIcon(android.R.drawable.ic_dialog_info);
    notification.setContentIntent(pendingIntent);
    notificationManager.notify(0, notification.build());
  }
}
