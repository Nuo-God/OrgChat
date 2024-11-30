package com.dream.orgchat;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import dream.http.Http;
import dream.http.TheServer;

public class AccountApplication extends Application {
  
  @Override
  public void onCreate() {
    super.onCreate();
    Http.post(this, "https://127.0.0.1:5000/user/isUser", "{}", (int state_code, String context)-> {
    });
    TheServer server = Http.client("wss://127.0.0.1:5000/socket.io/?EIO=4&transport=websocket");
    server.setOnReceiveMessage((String text) -> {
      
    });
  }

}
