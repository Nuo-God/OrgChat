package dream.http;

import android.app.Activity;
import android.content.Context;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Http {

  public static void get(Context context, String url_str, Call callback) {
    OkHttpClient client = getUnsafeOkHttpClient();
    MediaType mediaType = MediaType.parse("application/json");

    Request request =
        new Request.Builder()
            .url(url_str)
            .get()
            .addHeader("Content-Type", "application/json")
            .build();

    executeRequest(client, context, request, callback);
  }

  public static void post(Context context, String url_str, String requestBody, Call callback) {
    OkHttpClient client = getUnsafeOkHttpClient();
    MediaType mediaType = MediaType.parse("application/json");
    RequestBody body = RequestBody.create(mediaType, requestBody);

    Request request =
        new Request.Builder()
            .url(url_str)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build();

    executeRequest(client, context, request, callback);
  }

  public static TheServer client(String url_str) {
    OkHttpClient client = getUnsafeOkHttpClient();
    Request request = new Request.Builder().url(url_str).build();
    return new TheServer(client, request);
  }

  private static void executeRequest(
      OkHttpClient client, Context context, Request request, Call callback) {
    new Thread(
            () -> {
              try {
                Response response = client.newCall(request).execute();
                handleResponse(context, response, callback);
              } catch (IOException e) {
                e.printStackTrace();
                handleError(context, e, callback);
              }
            })
        .start();
  }

  private static void handleResponse(Context context, Response response, Call callback) {
    try {
      String responseData = response.body().string();
      if (context instanceof Activity) {
        ((Activity) context).runOnUiThread(() -> callback.call(response.code(), responseData));
      }
    } catch (IOException e) {
      e.printStackTrace();
      if (context instanceof Activity) {
        callback.call(-1, "响应处理失败! (" + e.getMessage() + ")");
      }
    }
  }

  private static void handleError(Context context, IOException e, Call callback) {
    if (context instanceof Activity) {
      ((Activity) context).runOnUiThread(() -> callback.call(-1, "请求失败! (" + e.getMessage() + ")"));
    }
  }

  private static OkHttpClient getUnsafeOkHttpClient() {
    try {
      final TrustManager[] trustAllCerts =
          new TrustManager[] {
            new X509TrustManager() {
              public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
              }

              public void checkClientTrusted(X509Certificate[] chain, String authType) {}

              public void checkServerTrusted(X509Certificate[] chain, String authType) {}
            }
          };

      final SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

      return new OkHttpClient.Builder()
          .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
          .hostnameVerifier((hostname, session) -> true) // 允许所有主机名
          .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间
          .writeTimeout(10, TimeUnit.SECONDS) // 设置写入超时时间
          .readTimeout(30, TimeUnit.SECONDS) // 设置读取超时时间
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public interface Call {
    void call(int state_code, String context);
  }
}
