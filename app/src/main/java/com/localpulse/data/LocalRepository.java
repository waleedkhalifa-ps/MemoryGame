package com.localpulse.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.localpulse.models.News;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Device-only demonstration data; deliberately isolated from public Firebase data. */
public class LocalRepository {
  private final SharedPreferences prefs;
  private final List<News> stories = new ArrayList<>();

  public LocalRepository(Context context) {
    prefs = context.getSharedPreferences("localpulse_demo_v1", Context.MODE_PRIVATE);
    try {
      JSONArray data = new JSONArray(prefs.getString("stories", "[]"));
      for (int i = 0; i < data.length(); i++) stories.add(decode(data.getJSONObject(i)));
    } catch (JSONException e) {
      throw new IllegalStateException("Cannot read saved stories", e);
    }
    if (!prefs.getBoolean("seeded", false)) {
      News park =
          new News(
              "park",
              "sara",
              "سارة أحمد",
              "افتتاح حديقة جديدة",
              "افتُتحت حديقة جديدة في الناصرة اليوم، لتوفير مساحة خضراء آمنة للأطفال والعائلات.\n\n"
                  + "تضم الحديقة مسارات للمشي وألعاباً للأطفال ومقاعد للقاء الجيران.\n\n"
                  + "هذا خبر خيالي للعرض التجريبي، والصورة رسم توضيحي وليست توثيقاً لحدث حقيقي.",
              "community",
              "nazareth");
      park.likes = 24;
      stories.add(park);
      News school =
          new News(
              "school",
              "sara",
              "سارة أحمد",
              "معرض فني في المدرسة",
              "نظمت المدرسة معرضاً فنياً لأعمال الطلاب، بمشاركة الأهالي والمعلمين.\n\n"
                  + "محتوى خيالي تجريبي لا يصف حدثاً حقيقياً.",
              "education",
              "nazareth");
      school.likes = 18;
      stories.add(school);
      stories.add(
          new News(
              "sport",
              "mohammad",
              "محمد علي",
              "أنشطة رياضية للأطفال",
              "برنامج رياضي مجتمعي للأطفال هذا الأسبوع. هذا خبر خيالي لتجربة البحث والتصنيفات.",
              "sports",
              "haifa"));
      stories.add(
          new News(
              "event",
              "layan",
              "ليان حسن",
              "أمسية في ساحة البلدة",
              "لقاء مجتمعي خيالي يجمع الجيران حول الموسيقى والفنون المحلية. تفاصيل هذا المثال ليست"
                  + " إعلاناً عن فعالية حقيقية.",
              "events",
              "nazareth"));
      save();
      prefs.edit().putBoolean("seeded", true).apply();
    }
  }

  public String preference(String key, String fallback) {
    return prefs.getString(key, fallback);
  }

  public void setPreference(String key, String value) {
    prefs.edit().putString(key, value).apply();
  }

  public boolean flag(String key) {
    return prefs.getBoolean(key, false);
  }

  public boolean toggle(String key) {
    boolean next = !flag(key);
    prefs.edit().putBoolean(key, next).apply();
    return next;
  }

  public List<News> all() {
    return new ArrayList<>(stories);
  }

  public News find(String id) {
    for (News n : stories) if (n.id.equals(id)) return n;
    return null;
  }

  public void put(News news) {
    News previous = find(news.id);
    if (previous != null) {
      if (!previous.authorUid.equals("demo-you")) throw new SecurityException("Not the author");
      stories.set(stories.indexOf(previous), news);
    } else stories.add(0, news);
    save();
  }

  public void delete(String id) {
    stories.removeIf(n -> n.id.equals(id) && n.authorUid.equals("demo-you"));
    save();
  }

  public boolean liked(News news) {
    return flag("like:" + news.id);
  }

  public int likeCount(News news) {
    return news.likes + (liked(news) ? 1 : 0);
  }

  public JSONArray array(String key) {
    try {
      return new JSONArray(preference(key, "[]"));
    } catch (JSONException e) {
      return new JSONArray();
    }
  }

  public void append(String key, JSONObject value) {
    JSONArray data = array(key);
    data.put(value);
    setPreference(key, data.toString());
  }

  public void notifyActivity(String text) {
    JSONObject value = new JSONObject();
    try {
      value.put("text", text);
      value.put("time", System.currentTimeMillis());
    } catch (JSONException ignored) {
    }
    append("notifications", value);
  }

  public List<JSONObject> comments(String id) {
    List<JSONObject> result = new ArrayList<>();
    JSONArray data = array("comments:" + id);
    for (int i = 0; i < data.length(); i++) result.add(data.optJSONObject(i));
    return result;
  }

  public void comment(String storyId, String commentId, String text) {
    JSONArray data = array("comments:" + storyId), next = new JSONArray();
    for (int i = 0; i < data.length(); i++)
      if (!data.optJSONObject(i).optString("id").equals(commentId)) next.put(data.optJSONObject(i));
    if (text != null) {
      JSONObject value = new JSONObject();
      try {
        value.put("id", commentId == null ? UUID.randomUUID().toString() : commentId);
        value.put("text", text);
        value.put("authorUid", "demo-you");
        value.put("createdAt", System.currentTimeMillis());
      } catch (JSONException ignored) {
      }
      next.put(value);
    }
    setPreference("comments:" + storyId, next.toString());
  }

  private void save() {
    JSONArray data = new JSONArray();
    for (News n : stories) data.put(encode(n));
    setPreference("stories", data.toString());
  }

  public static JSONObject encode(News n) {
    JSONObject o = new JSONObject();
    try {
      o.put("id", n.id);
      o.put("authorUid", n.authorUid);
      o.put("authorName", n.authorName);
      o.put("title", n.title);
      o.put("text", n.text);
      o.put("locationId", n.locationId);
      o.put("categoryId", n.categoryId);
      o.put("createdAt", n.createdAt);
      o.put("likes", n.likes);
      o.put("illustration", n.illustration);
      o.put("media", new JSONArray(n.media));
    } catch (JSONException e) {
      throw new IllegalStateException(e);
    }
    return o;
  }

  public static News decode(JSONObject o) {
    News n = new News();
    n.id = o.optString("id");
    n.authorUid = o.optString("authorUid", "demo-you");
    n.authorName = o.optString("authorName");
    n.title = o.optString("title");
    n.text = o.optString("text");
    n.locationId = o.optString("locationId", "nazareth");
    n.categoryId = o.optString("categoryId", "community");
    n.createdAt = o.optLong("createdAt");
    n.likes = o.optInt("likes");
    n.illustration = o.optBoolean("illustration", true);
    JSONArray media = o.optJSONArray("media");
    if (media != null) for (int i = 0; i < media.length(); i++) n.media.add(media.optString(i));
    return n;
  }
}
