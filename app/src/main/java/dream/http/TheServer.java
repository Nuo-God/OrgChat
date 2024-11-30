package dream.http;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class TheServer extends WebSocketListener {
  protected WebSocket socket;

  protected OnReceiveMessage onReceiveMessage = (String text) -> {};

  public TheServer(OkHttpClient client, Request request) {
    socket = client.newWebSocket(request, this);
  }

  public boolean sendMessage(String message) {
    return socket.send(message);
  }

  // 可以添加用于发送特定目标用户的消息
  public boolean sendMessage(String target, String message) {
    // 这里需要根据实际的协议，可能需要将目标用户包含在消息中
    String formattedMessage =
        String.format("{\"target\":\"%s\", \"message\":\"%s\"}", target, message);
    return sendMessage(formattedMessage);
  }

  @Override
  public void onOpen(WebSocket webSocket, Response request) {
    super.onOpen(webSocket, request);
    // 连接成功时可以发送初始消息
  }

  @Override
  public void onMessage(WebSocket webSocket, String text) {
    super.onMessage(webSocket, text);
    // 接收到来自服务器的文本消息
    // System.out.println("接收到消息: " + text);
    onReceiveMessage.call(text);
  }

  @Override
  public void onMessage(WebSocket webSocket, ByteString bytes) {
    super.onMessage(webSocket, bytes);
    // 如果需要处理二进制消息
  }

  @Override
  public void onClosing(WebSocket webSocket, int code, String reason) {
    super.onClosing(webSocket, code, reason);
    webSocket.close(1000, null); // 关闭连接
  }

  @Override
  public void onFailure(WebSocket webSocket, Throwable t, Response response) {
    super.onFailure(webSocket, t, response);
    // 连接失败处理
    t.printStackTrace();
  }

  public TheServer setOnReceiveMessage(OnReceiveMessage method) {
    onReceiveMessage = method;
    return this;
  }

  public interface OnReceiveMessage {
    void call(String text);
  }
}
