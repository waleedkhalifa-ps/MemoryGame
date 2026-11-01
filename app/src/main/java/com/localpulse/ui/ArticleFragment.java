package com.localpulse.ui;

import android.content.Intent;
import android.widget.*;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.localpulse.*;
import com.localpulse.data.ComposerViewModel;
import com.localpulse.models.News;

public class ArticleFragment extends BaseFragment {
  @Override
  protected void render() {
    News n = repo.find(arg("story", "park"));
    if (n == null) {
      empty();
      return;
    }
    Ui.add(page, Ui.text(context, n.title, 30, true));
    Ui.add(
        page,
        Ui.button(
            context,
            n.authorUid.equals("demo-you") ? getString(R.string.demo_you) : n.authorName,
            true,
            () -> open(R.id.profile, "author", n.authorUid)));
    Ui.add(
        page,
        Ui.text(
            context,
            town(n.locationId)
                + "  ·  "
                + java.text.DateFormat.getDateInstance().format(new java.util.Date(n.createdAt)),
            13,
            false));
    if (n.media.isEmpty()) {
      Ui.add(page, Ui.scene(context, n.categoryId.equals("events")));
      note(R.string.ai_disclosure);
    } else for (String path : n.media) MediaViews.add(page, path);
    Ui.add(page, Ui.text(context, n.text, 18, false));
    LinearLayout actions = Ui.row(context);
    Ui.weight(
        actions,
        Ui.button(
            context,
            (repo.liked(n) ? "♥ " : "♡ ") + repo.likeCount(n),
            true,
            () -> {
              repo.toggle("like:" + n.id);
              redraw();
            }));
    Ui.weight(
        actions,
        Ui.button(
            context,
            getString(repo.flag("save:" + n.id) ? R.string.unsave : R.string.save),
            true,
            () -> {
              repo.toggle("save:" + n.id);
              redraw();
            }));
    Ui.add(page, actions);
    button(R.string.comments, () -> open(R.id.comments, "story", n.id));
    outline(
        R.string.share,
        () -> {
          Intent share = new Intent(Intent.ACTION_SEND);
          share.setType("text/plain");
          share.putExtra(
              Intent.EXTRA_TEXT,
              n.title + "\n\n" + n.text + "\n\n" + getString(R.string.demo_banner));
          startActivity(Intent.createChooser(share, getString(R.string.share)));
        });
    if (n.authorUid.equals("demo-you")) {
      outline(
          R.string.edit,
          () -> {
            ComposerViewModel vm =
                new ViewModelProvider(requireActivity()).get(ComposerViewModel.class);
            vm.draft =
                com.localpulse.data.LocalRepository.decode(
                    com.localpulse.data.LocalRepository.encode(n));
            vm.step = 0;
            vm.reviewed = false;
            vm.persist(repo);
            open(R.id.create);
          });
      outline(
          R.string.delete,
          () ->
              new MaterialAlertDialogBuilder(context)
                  .setMessage(R.string.confirm_delete)
                  .setNegativeButton(R.string.cancel, null)
                  .setPositiveButton(
                      R.string.delete,
                      (d, w) -> {
                        repo.delete(n.id);
                        ((MainActivity) requireActivity()).back();
                      })
                  .show());
    }
    outline(
        R.string.report_content,
        () -> {
          repo.notifyActivity(getString(R.string.report_received));
          toast(R.string.report_received);
        });
  }
}
