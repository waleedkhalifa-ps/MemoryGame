package com.localpulse.ui;

import android.widget.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.localpulse.R;
import org.json.JSONObject;

public class CommentsFragment extends BaseFragment {
  @Override
  protected void render() {
    title(R.string.comments);
    String story = arg("story", "park");
    if (repo.comments(story).isEmpty()) note(R.string.empty);
    for (JSONObject comment : repo.comments(story)) {
      LinearLayout body = Ui.column(context);
      body.setPadding(
          Ui.dp(context, 16), Ui.dp(context, 16), Ui.dp(context, 16), Ui.dp(context, 16));
      Ui.card(page).addView(body);
      Ui.add(body, Ui.text(context, getString(R.string.demo_you), 14, true));
      Ui.add(body, Ui.text(context, comment.optString("text"), 17, false));
      Ui.add(
          body,
          Ui.text(
              context,
              java.text.DateFormat.getDateTimeInstance()
                  .format(new java.util.Date(comment.optLong("createdAt"))),
              12,
              false));
      if (comment.optString("authorUid").equals("demo-you")) {
        LinearLayout row = Ui.row(context);
        Ui.weight(
            row, Ui.button(context, getString(R.string.edit), true, () -> edit(story, comment)));
        Ui.weight(
            row,
            Ui.button(
                context,
                getString(R.string.delete),
                true,
                () ->
                    new MaterialAlertDialogBuilder(context)
                        .setMessage(R.string.confirm_delete)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(
                            R.string.delete,
                            (d, w) -> {
                              repo.comment(story, comment.optString("id"), null);
                              redraw();
                            })
                        .show()));
        Ui.add(body, row);
      }
    }
    EditText input = Ui.field(page, getString(R.string.write_comment), "", true);
    button(
        R.string.send,
        () -> {
          String text = input.getText().toString().trim();
          if (text.isEmpty()) {
            input.setError(getString(R.string.required));
            return;
          }
          if (text.length() > 2000) {
            input.setError("2000 max");
            return;
          }
          repo.comment(story, null, text);
          redraw();
        });
  }

  private void edit(String story, JSONObject comment) {
    EditText input = new EditText(context);
    input.setText(comment.optString("text"));
    input.setPadding(24, 24, 24, 24);
    androidx.appcompat.app.AlertDialog dialog =
        new MaterialAlertDialogBuilder(context)
            .setTitle(R.string.edit)
            .setView(input)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save, null)
            .create();
    dialog.setOnShowListener(
        d ->
            dialog
                .getButton(-1)
                .setOnClickListener(
                    v -> {
                      String t = input.getText().toString().trim();
                      if (t.isEmpty() || t.length() > 2000) {
                        input.setError(getString(R.string.required));
                        return;
                      }
                      repo.comment(story, comment.optString("id"), t);
                      dialog.dismiss();
                      redraw();
                    }));
    dialog.show();
  }
}
