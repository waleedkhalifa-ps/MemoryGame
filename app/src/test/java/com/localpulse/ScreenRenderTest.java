package com.localpulse;

import static org.junit.Assert.*;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import androidx.navigation.fragment.NavHostFragment;
import java.io.File;
import java.io.FileOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, qualifiers = "ar-rIL-w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ScreenRenderTest {
  @Test
  public void renderKeyArabicScreens() throws Exception {
    File directory = new File("build/screenshots");
    assertTrue(directory.exists() || directory.mkdirs());
    try (var controller = Robolectric.buildActivity(MainActivity.class).setup()) {
      MainActivity activity = controller.get();
      int[] routes = {R.id.home, R.id.explore, R.id.article, R.id.create, R.id.location, R.id.ai};
      String[] names = {"home", "explore", "article", "create", "location", "ai"};
      for (int i = 0; i < routes.length; i++) {
        if (i > 0) activity.open(routes[i]);
        activity.getSupportFragmentManager().executePendingTransactions();
        ((NavHostFragment) activity.getSupportFragmentManager().findFragmentById(R.id.nav_host))
            .getChildFragmentManager()
            .executePendingTransactions();
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
        View view = activity.getWindow().getDecorView();
        view.measure(
            View.MeasureSpec.makeMeasureSpec(822, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1782, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, 822, 1782);
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
        Bitmap bitmap = Bitmap.createBitmap(822, 1782, Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        try (FileOutputStream output =
            new FileOutputStream(new File(directory, names[i] + ".png"))) {
          assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output));
        }
        bitmap.recycle();
      }
    }
  }
}
