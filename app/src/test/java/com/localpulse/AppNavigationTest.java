package com.localpulse;

import static org.junit.Assert.*;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.navigation.fragment.NavHostFragment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Headless Android smoke tests; these do not substitute for physical-device RTL checks. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class AppNavigationTest {
  @Test
  public void publicationRequiresReviewAndCreatesExactlyOneStory() {
    try (var controller = Robolectric.buildActivity(MainActivity.class).setup()) {
      MainActivity activity = controller.get();
      var repository = ((LocalPulseApp) activity.getApplication()).repository;
      activity.open(R.id.create);
      settle(activity);
      var vm =
          new androidx.lifecycle.ViewModelProvider(activity)
              .get(com.localpulse.data.ComposerViewModel.class);
      vm.draft.title = "Fictional community update";
      vm.draft.text = "Facts supplied for this fictional local update.";
      vm.persist(repository);
      click(activity, activity.getString(R.string.next));
      settle(activity);
      click(activity, activity.getString(R.string.preview));
      settle(activity);
      click(activity, activity.getString(R.string.publish));
      settle(activity);
      assertEquals(4, repository.all().size());
      android.widget.CheckBox review =
          find(activity.findViewById(R.id.root), android.widget.CheckBox.class, null);
      assertNotNull(review);
      review.performClick();
      click(activity, activity.getString(R.string.publish));
      settle(activity);
      assertEquals(5, repository.all().size());
      assertEquals("Fictional community update", repository.all().get(0).title);
      assertEquals(5, new com.localpulse.data.LocalRepository(activity).all().size());
    }
  }

  private static void settle(MainActivity a) {
    a.getSupportFragmentManager().executePendingTransactions();
    ((NavHostFragment) a.getSupportFragmentManager().findFragmentById(R.id.nav_host))
        .getChildFragmentManager()
        .executePendingTransactions();
    org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
  }

  private static void click(MainActivity a, String text) {
    android.widget.Button b = find(a.findViewById(R.id.root), android.widget.Button.class, text);
    assertNotNull("Missing button: " + text, b);
    b.performClick();
  }

  private static <T extends View> T find(View root, Class<T> type, String text) {
    if (type.isInstance(root)
        && (text == null
            || root instanceof TextView && ((TextView) root).getText().toString().equals(text)))
      return type.cast(root);
    if (root instanceof ViewGroup)
      for (int i = 0; i < ((ViewGroup) root).getChildCount(); i++) {
        T found = find(((ViewGroup) root).getChildAt(i), type, text);
        if (found != null) return found;
      }
    return null;
  }

  @Test
  public void launchesWithSeededFeedAndDemoDisclosure() {
    try (var controller = Robolectric.buildActivity(MainActivity.class).setup()) {
      MainActivity activity = controller.get();
      assertEquals(4, ((LocalPulseApp) activity.getApplication()).repository.all().size());
      assertNotNull(activity.findViewById(R.id.feed_list));
      assertTrue(((TextView) activity.findViewById(R.id.mode_label)).getText().length() > 10);
    }
  }

  @Test
  public void everyPrimaryScreenCanBeOpened() {
    try (var controller = Robolectric.buildActivity(MainActivity.class).setup()) {
      MainActivity activity = controller.get();
      int[] routes = {
        R.id.explore,
        R.id.create,
        R.id.notifications,
        R.id.profile,
        R.id.article,
        R.id.comments,
        R.id.location,
        R.id.ai,
        R.id.reporter,
        R.id.settings,
        R.id.auth
      };
      for (int route : routes) {
        activity.open(route);
        activity.getSupportFragmentManager().executePendingTransactions();
        NavHostFragment host =
            (NavHostFragment) activity.getSupportFragmentManager().findFragmentById(R.id.nav_host);
        host.getChildFragmentManager().executePendingTransactions();
        assertEquals(route, host.getNavController().getCurrentDestination().getId());
        assertNotNull(host.getChildFragmentManager().getPrimaryNavigationFragment().getView());
      }
    }
  }
}
