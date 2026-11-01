package com.localpulse;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
  private NavController nav;

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    setContentView(R.layout.activity_main);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.root),
        (view, insets) -> {
          Insets bars =
              insets.getInsets(
                  WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
          view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
          return insets;
        });
    nav =
        ((NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host))
            .getNavController();
    BottomNavigationView bottom = findViewById(R.id.bottom_nav);
    bottom.setOnItemSelectedListener(
        item -> {
          if (nav.getCurrentDestination() == null
              || nav.getCurrentDestination().getId() != item.getItemId()) {
            nav.navigate(
                item.getItemId(),
                null,
                new NavOptions.Builder()
                    .setPopUpTo(R.id.home, false)
                    .setLaunchSingleTop(true)
                    .build());
          }
          return true;
        });
    MaterialToolbar bar = findViewById(R.id.toolbar);
    bar.setNavigationOnClickListener(v -> nav.navigateUp());
    nav.addOnDestinationChangedListener(
        (controller, destination, args) -> {
          boolean root = destination.getId() == R.id.home;
          bar.setTitle(
              root ? getString(R.string.app_name) + "  ·  LocalPulse" : destination.getLabel());
          bar.setNavigationIcon(root ? R.drawable.ic_pulse : R.drawable.ic_back);
          int tab = destination.getId();
          if (bottom.getMenu().findItem(tab) != null && bottom.getSelectedItemId() != tab)
            bottom.setSelectedItemId(tab);
        });
  }

  public void open(int destination) {
    open(destination, null);
  }

  public void open(int destination, Bundle args) {
    nav.navigate(destination, args);
  }

  public void back() {
    nav.navigateUp();
  }
}
