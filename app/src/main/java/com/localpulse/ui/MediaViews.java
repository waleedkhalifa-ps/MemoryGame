package com.localpulse.ui;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.widget.*;
import androidx.core.content.FileProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import com.bumptech.glide.Glide;
import com.localpulse.R;
import java.io.File;

public final class MediaViews {
  private MediaViews() {}

  public static void add(LinearLayout parent, String path) {
    android.content.Context c = parent.getContext();
    File file = new File(path);
    if (path.endsWith(".pdf")) {
      Ui.add(
          parent,
          Ui.button(
              c,
              "PDF · " + file.getName(),
              true,
              () -> {
                try {
                  Uri uri = FileProvider.getUriForFile(c, c.getPackageName() + ".files", file);
                  c.startActivity(
                      new Intent(Intent.ACTION_VIEW)
                          .setDataAndType(uri, "application/pdf")
                          .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
                } catch (android.content.ActivityNotFoundException e) {
                  Toast.makeText(c, "No PDF viewer installed", Toast.LENGTH_SHORT).show();
                }
              }));
      return;
    }
    if (path.endsWith(".mp4")) {
      Ui.add(
          parent,
          Ui.button(
              c,
              c.getString(R.string.video) + "  ▶",
              true,
              () -> {
                Dialog dialog = new Dialog(c);
                ExoPlayer player = new ExoPlayer.Builder(c).build();
                PlayerView view = new PlayerView(c);
                view.setPlayer(player);
                dialog.setContentView(view);
                dialog.setOnDismissListener(d -> player.release());
                dialog.show();
                dialog.getWindow().setLayout(-1, Ui.dp(c, 360));
                player.setMediaItem(MediaItem.fromUri(Uri.fromFile(file)));
                player.prepare();
                player.play();
              }));
      return;
    }
    ImageView image = Ui.scene(c, false);
    image.setContentDescription(c.getString(R.string.photos));
    Glide.with(image).load(file).into(image);
    Ui.add(parent, image);
    image.setOnClickListener(
        v -> {
          Dialog d = new Dialog(c);
          ImageView full = new ImageView(c);
          full.setAdjustViewBounds(true);
          full.setContentDescription(c.getString(R.string.photos));
          Glide.with(full).load(file).into(full);
          d.setContentView(full);
          d.show();
          d.getWindow().setLayout(-1, -2);
        });
  }
}
