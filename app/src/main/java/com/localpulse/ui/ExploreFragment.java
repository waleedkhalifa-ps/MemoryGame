package com.localpulse.ui;

import android.text.*;
import android.widget.*;
import com.google.android.material.chip.*;
import com.localpulse.R;
import com.localpulse.models.News;

public class ExploreFragment extends BaseFragment {
  private String query = "", selected = "all";

  @Override
  protected void render() {
    title(R.string.explore);
    EditText search = Ui.field(page, getString(R.string.search), query, false);
    ChipGroup group = new ChipGroup(context);
    group.setSingleSelection(true);
    for (String key : new String[] {"community", "education", "sports", "traffic", "events"}) {
      Chip chip = new Chip(context);
      chip.setText(category(key));
      chip.setCheckable(true);
      chip.setOnCheckedChangeListener(
          (b, on) -> {
            selected = on ? key : "all";
            showResults();
          });
      group.addView(chip);
    }
    Ui.add(page, group);
    outline(R.string.location, () -> open(R.id.location));
    results = Ui.column(context);
    Ui.add(page, results);
    search.addTextChangedListener(
        new TextWatcher() {
          public void beforeTextChanged(CharSequence s, int a, int c, int f) {}

          public void onTextChanged(CharSequence s, int start, int before, int count) {
            query = s.toString();
            showResults();
          }

          public void afterTextChanged(Editable e) {}
        });
    showResults();
  }

  private LinearLayout results;

  private void showResults() {
    if (results == null) return;
    results.removeAllViews();
    Ui.add(results, Ui.text(context, getString(R.string.suggested), 20, true));
    int count = 0;
    for (News n : repo.all())
      if ((selected.equals("all") || n.categoryId.equals(selected))
          && (n.title + " " + n.text + " " + n.authorName)
              .toLowerCase(java.util.Locale.ROOT)
              .contains(query.toLowerCase(java.util.Locale.ROOT))) {
        count++;
        NewsCards.add(results, n, repo, () -> article(n), () -> open(R.id.comments, "story", n.id));
      }
    if (count == 0) Ui.note(results, getString(R.string.empty));
  }
}
