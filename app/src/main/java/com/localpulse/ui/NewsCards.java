package com.localpulse.ui;

import android.content.Context;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.localpulse.R;
import com.localpulse.data.LocalRepository;
import com.localpulse.models.News;
import java.io.File;

public final class NewsCards {
  private NewsCards() {}

  public static void add(
      LinearLayout parent, News n, LocalRepository repo, Runnable read, Runnable comments) {
    Context c = parent.getContext();
    MaterialCardView card = Ui.card(parent);
    LinearLayout inner = Ui.column(c);
    card.addView(inner);
    ImageView image =
        Ui.scene(c, n.categoryId.equals("events") || n.categoryId.equals("education"));
    if (!n.media.isEmpty() && !n.media.get(0).endsWith(".pdf") && !n.media.get(0).endsWith(".mp4"))
      Glide.with(image)
          .load(new File(n.media.get(0)))
          .placeholder(R.drawable.scene_park)
          .into(image);
    inner.addView(image);
    image.setOnClickListener(v -> read.run());
    LinearLayout content = Ui.column(c);
    content.setPadding(Ui.dp(c, 16), Ui.dp(c, 14), Ui.dp(c, 16), Ui.dp(c, 10));
    inner.addView(content);
    TextView tag =
        Ui.text(
            c,
            c.getString(BaseFragment.categoryResource(n.categoryId))
                + "  ·  "
                + BaseFragment.town(n.locationId),
            12,
            true);
    tag.setTextColor(Ui.color(c, R.color.teal));
    Ui.add(content, tag);
    TextView title = Ui.text(c, n.title, 23, true);
    Ui.add(content, title);
    title.setOnClickListener(v -> read.run());
    TextView excerpt = Ui.text(c, n.text, 15, false);
    excerpt.setMaxLines(2);
    excerpt.setEllipsize(android.text.TextUtils.TruncateAt.END);
    Ui.add(content, excerpt);
    Ui.add(
        content,
        Ui.text(
            c,
            (n.authorUid.equals("demo-you") ? c.getString(R.string.demo_you) : n.authorName)
                + "  ·  "
                + c.getString(R.string.today),
            13,
            false));
    Ui.line(content);
    LinearLayout actions = Ui.row(c);
    MaterialButton like = Ui.button(c, "♡ " + repo.likeCount(n), true, () -> {});
    like.setContentDescription(c.getString(R.string.like));
    like.setText((repo.liked(n) ? "♥ " : "♡ ") + repo.likeCount(n));
    like.setOnClickListener(
        v -> {
          repo.toggle("like:" + n.id);
          like.setText((repo.liked(n) ? "♥ " : "♡ ") + repo.likeCount(n));
        });
    Ui.weight(actions, like);
    Ui.weight(actions, Ui.button(c, "▢ " + repo.comments(n.id).size(), true, comments));
    MaterialButton saved = Ui.icon(c, R.drawable.ic_bookmark, c.getString(R.string.save), () -> {});
    saved.setAlpha(repo.flag("save:" + n.id) ? 1f : .45f);
    saved.setOnClickListener(
        v -> {
          saved.setAlpha(repo.toggle("save:" + n.id) ? 1f : .45f);
        });
    Ui.weight(actions, saved);
    Ui.add(content, actions);
    card.setOnClickListener(v -> read.run());
  }
}
