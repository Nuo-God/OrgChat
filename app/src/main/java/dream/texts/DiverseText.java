package dream.texts;

import android.graphics.Color;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiverseText extends android.text.SpannableStringBuilder {

  private JsonArray self;
  
  public static List<int[]> match(String regex, android.text.SpannableStringBuilder str) {
    List<int[]> result = new ArrayList<>();
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(str);

    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();
      result.add(new int[] {start, end});
    }

    return result;
  }

  public void addClick(String target, String color, Func func) {
    List<int[]> all = match(target, this);
    for (int[] p : all) {
      setSpan(
          new ClickableSpan() {

            @Override
            public void onClick(View self) {
              func.call(self);
            }

            @Override
            public void updateDrawState(TextPaint pat) {
              super.updateDrawState(pat);
              // TODO: Implement this method
              pat.setColor(Color.parseColor(color != null ? color : "#ff536696")); // 设置颜色
              pat.setUnderlineText(true); // 去掉下划线
            }
          },
          p[0],
          p[1],
          Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }

  public DiverseText(String json_code) {
    super();
    try {
      self = JsonParser.parseString(json_code).getAsJsonArray();
      for (JsonElement element : self) {
        if (element.isJsonPrimitive()) {
          append((CharSequence) element.getAsString());
        } else if (element.isJsonObject()) {
          JsonObject jsonObject = element.getAsJsonObject();
          String target = jsonObject.get("target").getAsString();
          List<int[]> all = match(target, this);
          if (jsonObject.has("style")) {
            int style = jsonObject.get("style").getAsInt();
            for (int[] p : all) {
              setSpan(new StyleSpan(style), p[0], p[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
          }
          if (jsonObject.has("color")) {
            int color = jsonObject.get("color").getAsInt();
            for (int[] p : all) {
              setSpan(
                  new ForegroundColorSpan(color), p[0], p[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
          }
          if (jsonObject.has("background")) {
            int color = jsonObject.get("background").getAsInt();
            for (int[] p : all) {
              setSpan(
                  new BackgroundColorSpan(color), p[0], p[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
          }
          if (jsonObject.has("bottomLine")) {
            for (int[] p : all) {
              setSpan(new UnderlineSpan(), p[0], p[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
          }
          if (jsonObject.has("deleteLine")) {
            for (int[] p : all) {
              setSpan(new StrikethroughSpan(), p[0], p[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
          }
        }
      }
    } catch (JsonSyntaxException e) {
      e.printStackTrace();
    }
  }
  
  public interface Func {
    void call(View self);
  }
}
