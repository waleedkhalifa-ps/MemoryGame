package com.localpulse.ui;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.recyclerview.widget.*;
import com.google.android.material.chip.*;
import com.localpulse.*;
import com.localpulse.data.LocalRepository;
import com.localpulse.models.News;
import java.util.*;

public class HomeFragment extends androidx.fragment.app.Fragment {
  private LocalRepository repo;
  private RecyclerView list;
  private String filter = "latest";

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
    return i.inflate(R.layout.fragment_feed, c, false);
  }

  @Override
  public void onViewCreated(@NonNull View v, @Nullable Bundle state) {
    repo = ((LocalPulseApp) requireActivity().getApplication()).repository;
    if (state != null) filter = state.getString("filter", "latest");
    LinearLayout header = v.findViewById(R.id.feed_header);
    android.content.Context c = requireContext();
    Ui.add(
        header,
        Ui.button(
            c,
            "⌖  " + BaseFragment.town(repo.preference("town", "nazareth")),
            true,
            () -> ((MainActivity) requireActivity()).open(R.id.location)));
    ChipGroup chips = new ChipGroup(c);
    chips.setSingleSelection(true);
    chips.setSelectionRequired(true);
    for (String key : new String[] {"latest", "nearby", "following"}) {
      Chip chip = new Chip(c);
      chip.setId(View.generateViewId());
      chip.setText(
          getString(
              switch (key) {
                case "nearby" -> R.string.nearby;
                case "following" -> R.string.following;
                default -> R.string.latest;
              }));
      chip.setCheckable(true);
      chip.setChecked(key.equals(filter));
      chip.setOnClickListener(
          x -> {
            filter = key;
            refresh();
          });
      chips.addView(chip);
    }
    Ui.add(header, chips);
    list = v.findViewById(R.id.feed_list);
    list.setLayoutManager(new LinearLayoutManager(c));
    refresh();
  }

  @Override
  public void onResume() {
    super.onResume();
    if (list != null) refresh();
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle out) {
    super.onSaveInstanceState(out);
    out.putString("filter", filter);
  }

  private void refresh() {
    List<News> data = repo.all();
    data.removeIf(
        n ->
            filter.equals("nearby") && !n.locationId.equals(repo.preference("town", "nazareth"))
                || filter.equals("following") && !repo.flag("follow:" + n.authorUid));
    if (data.isEmpty()) {
      list.setAdapter(
          new RecyclerView.Adapter<Holder>() {
            public int getItemCount() {
              return 1;
            }

            public Holder onCreateViewHolder(ViewGroup p, int t) {
              return holder(p);
            }

            public void onBindViewHolder(Holder h, int pos) {
              h.root.removeAllViews();
              Ui.note(h.root, getString(R.string.empty) + " · " + getString(R.string.empty_hint));
            }
          });
      return;
    }
    list.setAdapter(
        new RecyclerView.Adapter<Holder>() {
          public int getItemCount() {
            return data.size();
          }

          public Holder onCreateViewHolder(ViewGroup p, int t) {
            return holder(p);
          }

          public void onBindViewHolder(Holder h, int pos) {
            h.root.removeAllViews();
            News n = data.get(pos);
            NewsCards.add(
                h.root, n, repo, () -> open(R.id.article, n.id), () -> open(R.id.comments, n.id));
          }
        });
  }

  private Holder holder(ViewGroup parent) {
    LinearLayout root = Ui.column(parent.getContext());
    root.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
    return new Holder(root);
  }

  private static class Holder extends RecyclerView.ViewHolder {
    LinearLayout root;

    Holder(LinearLayout v) {
      super(v);
      root = v;
    }
  }

  private void open(int id, String story) {
    Bundle b = new Bundle();
    b.putString("story", story);
    ((MainActivity) requireActivity()).open(id, b);
  }
}
