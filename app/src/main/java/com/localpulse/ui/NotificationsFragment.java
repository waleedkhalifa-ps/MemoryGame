package com.localpulse.ui;

import com.localpulse.R;
import org.json.JSONArray;
import org.json.JSONObject;

public class NotificationsFragment extends BaseFragment {
  @Override
  protected void render() {
    title(R.string.notifications);
    note(R.string.offline_notifications);
    JSONArray data = repo.array("notifications");
    if (data.length() == 0) empty();
    for (int i = data.length() - 1; i >= 0; i--) {
      JSONObject n = data.optJSONObject(i);
      Ui.note(
          page,
          n.optString("text")
              + "\n"
              + java.text.DateFormat.getDateTimeInstance()
                  .format(new java.util.Date(n.optLong("time"))));
    }
  }
}
