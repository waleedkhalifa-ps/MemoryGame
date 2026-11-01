package com.localpulse.ui;

import android.widget.*;
import com.localpulse.R;
import com.localpulse.domain.ReportPolicy;
import java.util.*;
import org.json.*;

/** Local demonstration of append-only transitions. Server rules remain authoritative online. */
public class ReporterFragment extends BaseFragment {
  @Override
  protected void render() {
    title(R.string.reporter);
    note(R.string.emergency_notice);
    EditText observation =
        Ui.field(page, getString(R.string.observation), repo.preference("report-draft", ""), true);
    Spinner type =
        Ui.spinner(
            page,
            getString(R.string.report_type),
            new String[] {
              getString(R.string.traffic),
              getString(R.string.environment),
              getString(R.string.community)
            },
            0);
    outline(
        R.string.location,
        () -> {
          repo.setPreference("report-draft", observation.getText().toString());
          open(R.id.location);
        });
    button(
        R.string.submit_report,
        () -> {
          String text = observation.getText().toString().trim();
          if (text.length() < 10) {
            observation.setError(getString(R.string.required));
            return;
          }
          JSONObject report = new JSONObject();
          try {
            report.put("id", UUID.randomUUID().toString());
            report.put("authorUid", "demo-you");
            report.put("description", text);
            report.put("type", type.getSelectedItem().toString());
            report.put("locationId", repo.preference("town", "nazareth"));
            report.put("createdAt", System.currentTimeMillis());
          } catch (JSONException e) {
            throw new IllegalStateException(e);
          }
          repo.append("community-reports", report);
          repo.setPreference("report-draft", "");
          repo.notifyActivity(getString(R.string.submit_report));
          redraw();
        });
    Ui.add(page, Ui.text(context, getString(R.string.timeline), 24, true));
    JSONArray reports = repo.array("community-reports");
    if (reports.length() == 0) note(R.string.empty);
    for (int i = reports.length() - 1; i >= 0; i--) {
      JSONObject report = reports.optJSONObject(i);
      String id = report.optString("id");
      JSONArray updates = repo.array("updates:" + id);
      String status =
          updates.length() == 0
              ? "reported"
              : updates.optJSONObject(updates.length() - 1).optString("newStatus");
      LinearLayout card = Ui.column(context);
      int pad = Ui.dp(context, 16);
      card.setPadding(pad, pad, pad, pad);
      Ui.card(page).addView(card);
      Ui.add(card, Ui.text(context, report.optString("description"), 20, true));
      Ui.note(
          card,
          town(report.optString("locationId"))
              + " · "
              + report.optString("type")
              + " · "
              + statusText(status));
      event(card, "reported", report.optLong("createdAt"), getString(R.string.demo_you));
      for (int j = 0; j < updates.length(); j++) {
        JSONObject update = updates.optJSONObject(j);
        event(
            card,
            update.optString("newStatus"),
            update.optLong("createdAt"),
            update.optString("evidence"));
      }
      Ui.add(
          card,
          Ui.button(
              context,
              getString(repo.flag("report-follow:" + id) ? R.string.unfollow : R.string.follow),
              true,
              () -> {
                repo.toggle("report-follow:" + id);
                redraw();
              }));
      if (report.optString("authorUid").equals("demo-you")) {
        String next =
            switch (status) {
              case "reported", "reopened" -> "in_progress";
              case "in_progress" -> "resolved";
              default -> "reopened";
            };
        EditText evidence = Ui.field(card, getString(R.string.evidence), "", true);
        Ui.add(
            card,
            Ui.button(
                context,
                getString(R.string.add_update) + " · " + statusText(next),
                false,
                () -> {
                  String note = evidence.getText().toString().trim();
                  if (!ReportPolicy.allows(
                      "demo-you", report.optString("authorUid"), status, next, note)) {
                    evidence.setError(getString(R.string.progress_note));
                    return;
                  }
                  JSONObject entry = new JSONObject();
                  try {
                    entry.put("actorUid", "demo-you");
                    entry.put("previousStatus", status);
                    entry.put("newStatus", next);
                    entry.put("evidence", note);
                    entry.put("createdAt", System.currentTimeMillis());
                  } catch (JSONException e) {
                    throw new IllegalStateException(e);
                  }
                  repo.append("updates:" + id, entry);
                  repo.notifyActivity(
                      getString(R.string.add_update) + ": " + report.optString("description"));
                  redraw();
                }));
      }
    }
  }

  private String statusText(String state) {
    return getString(
        switch (state) {
          case "in_progress" -> R.string.in_progress;
          case "resolved" -> R.string.resolved;
          case "reopened" -> R.string.reopened;
          default -> R.string.reported;
        });
  }

  private void event(LinearLayout parent, String state, long time, String text) {
    Ui.add(
        parent,
        Ui.text(
            context,
            "●  "
                + statusText(state)
                + "\n"
                + java.text.DateFormat.getDateTimeInstance().format(new Date(time))
                + "\n"
                + text,
            14,
            false));
    Ui.line(parent);
  }
}
