package com.localpulse.ui;

import android.widget.*;
import com.localpulse.R;
import com.localpulse.models.News;

public class ProfileFragment extends BaseFragment {
  @Override
  protected void render() {
    String author = arg("author", "demo-you");
    boolean own = author.equals("demo-you");
    TextView avatar = Ui.text(context, own ? "◉" : "س", 54, true);
    avatar.setGravity(android.view.Gravity.CENTER);
    Ui.add(page, avatar);
    TextView name =
        Ui.text(
            context,
            author.equals("saved")
                ? getString(R.string.favorites)
                : own
                    ? repo.preference("name", getString(R.string.demo_you))
                    : author.equals("sara")
                        ? "سارة أحمد"
                        : author.equals("layan") ? "ليان حسن" : "محمد علي",
            28,
            true);
    name.setGravity(android.view.Gravity.CENTER);
    Ui.add(page, name);
    Ui.add(page, Ui.text(context, town(repo.preference("town", "nazareth")), 14, false));
    long count = repo.all().stream().filter(n -> n.authorUid.equals(author)).count();
    Ui.note(page, count + " " + getString(R.string.stories));
    if (own) {
      outline(R.string.settings, () -> open(R.id.settings));
      outline(R.string.login, () -> open(R.id.auth));
      outline(R.string.favorites, () -> open(R.id.profile, "author", "saved"));
      outline(R.string.drafts, () -> open(R.id.create));
    } else if (!author.equals("saved"))
      button(
          repo.flag("follow:" + author) ? R.string.unfollow : R.string.follow,
          () -> {
            repo.toggle("follow:" + author);
            redraw();
          });
    Ui.add(
        page,
        Ui.text(
            context,
            getString(author.equals("saved") ? R.string.favorites : R.string.published_stories),
            21,
            true));
    int shown = 0;
    for (News n : repo.all())
      if (author.equals("saved") ? repo.flag("save:" + n.id) : n.authorUid.equals(author)) {
        storyCard(n);
        shown++;
      }
    if (shown == 0) empty();
  }
}
