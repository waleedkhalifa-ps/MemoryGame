package com.localpulse.ui;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.localpulse.*;
import com.localpulse.data.LocalRepository;
import com.localpulse.models.News;

public abstract class BaseFragment extends Fragment {
  protected LinearLayout page;
  protected LocalRepository repo;
  protected android.content.Context context;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
    return inflater.inflate(R.layout.fragment_page, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
    super.onViewCreated(view, state);
    context = requireContext();
    repo = ((LocalPulseApp) requireActivity().getApplication()).repository;
    page = view.findViewById(R.id.page_content);
    render();
  }

  protected abstract void render();

  protected void redraw() {
    page.removeAllViews();
    render();
  }

  protected void title(int label) {
    Ui.add(page, Ui.text(context, getString(label), 26, true));
  }

  protected void note(int label) {
    Ui.note(page, getString(label));
  }

  protected void button(int label, Runnable action) {
    Ui.add(page, Ui.button(context, getString(label), false, action));
  }

  protected void outline(int label, Runnable action) {
    Ui.add(page, Ui.button(context, getString(label), true, action));
  }

  protected void toast(int label) {
    Toast.makeText(context, label, Toast.LENGTH_SHORT).show();
  }

  protected void open(int id) {
    ((MainActivity) requireActivity()).open(id);
  }

  protected void open(int id, String key, String value) {
    Bundle args = new Bundle();
    args.putString(key, value);
    ((MainActivity) requireActivity()).open(id, args);
  }

  protected String arg(String key, String fallback) {
    return getArguments() == null ? fallback : getArguments().getString(key, fallback);
  }

  protected void article(News n) {
    open(R.id.article, "story", n.id);
  }

  protected String category(String id) {
    return getString(categoryResource(id));
  }

  public static int categoryResource(String id) {
    return switch (id) {
      case "education" -> R.string.education;
      case "sports" -> R.string.sports;
      case "traffic" -> R.string.traffic;
      case "events" -> R.string.events;
      case "environment" -> R.string.environment;
      default -> R.string.community;
    };
  }

  public static String town(String id) {
    return switch (id) {
      case "haifa" -> "حيفا · Haifa";
      case "jaffa" -> "يافا · Jaffa";
      case "beersheba" -> "بئر السبع · Beersheba";
      default -> "الناصرة · Nazareth";
    };
  }

  protected void storyCard(News n) {
    NewsCards.add(page, n, repo, () -> article(n), () -> open(R.id.comments, "story", n.id));
  }

  protected void empty() {
    Ui.add(page, Ui.text(context, getString(R.string.empty), 20, true));
    note(R.string.empty_hint);
  }
}
