package com.localpulse.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import com.localpulse.*;
import java.util.*;

public class LocationFragment extends BaseFragment {
  private String selected;
  private int region;
  private LocationManager manager;
  private LocationListener listener;
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final ActivityResultLauncher<String> permission =
      registerForActivityResult(
          new ActivityResultContracts.RequestPermission(),
          granted -> {
            if (granted) findLocation();
            else toast(R.string.manual_fallback);
          });

  @Override
  protected void render() {
    if (selected == null) selected = repo.preference("town", "nazareth");
    title(R.string.location);
    note(R.string.choose_location_hint);
    Spinner district =
        Ui.spinner(
            page,
            getString(R.string.region),
            new String[] {
              getString(R.string.north), getString(R.string.center), getString(R.string.south)
            },
            region);
    LinearLayout selection = Ui.column(context);
    Ui.add(page, selection);
    district.setOnItemSelectedListener(
        CreateFragment.listener(
            pos -> {
              region = pos;
              selection.removeAllViews();
              String[] ids =
                  pos == 0
                      ? new String[] {"nazareth", "haifa"}
                      : pos == 1 ? new String[] {"jaffa"} : new String[] {"beersheba"};
              Spinner town =
                  Ui.spinner(
                      selection,
                      getString(R.string.town),
                      Arrays.stream(ids).map(BaseFragment::town).toArray(String[]::new),
                      Math.max(0, Arrays.asList(ids).indexOf(selected)));
              town.setOnItemSelectedListener(CreateFragment.listener(i -> selected = ids[i]));
            }));
    button(
        R.string.approximate, () -> permission.launch(Manifest.permission.ACCESS_COARSE_LOCATION));
    note(R.string.manual_fallback);
    Ui.add(page, Ui.text(context, getString(R.string.followed_towns), 19, true));
    for (String id : new String[] {"nazareth", "haifa", "jaffa", "beersheba"})
      if (repo.flag("town-follow:" + id))
        Ui.add(
            page,
            Ui.button(
                context,
                town(id) + "  ×",
                true,
                () -> {
                  repo.toggle("town-follow:" + id);
                  redraw();
                }));
    outline(
        R.string.add_town,
        () -> {
          if (!repo.flag("town-follow:" + selected)) repo.toggle("town-follow:" + selected);
          redraw();
        });
    button(
        R.string.show_news,
        () -> {
          repo.setPreference("town", selected);
          open(R.id.home);
        });
  }

  @SuppressWarnings("MissingPermission")
  private void findLocation() {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) return;
    manager = (LocationManager) context.getSystemService(android.content.Context.LOCATION_SERVICE);
    if (manager == null || !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
      toast(R.string.manual_fallback);
      return;
    }
    listener =
        location -> {
          stopLocation();
          double[][] coords = {
            {32.6996, 35.3035}, {32.794, 34.9896}, {32.05, 34.75}, {31.25, 34.79}
          };
          String[] towns = {"nazareth", "haifa", "jaffa", "beersheba"};
          float best = 100000;
          String nearest = null;
          for (int i = 0; i < coords.length; i++) {
            float[] distance = new float[1];
            Location.distanceBetween(
                location.getLatitude(),
                location.getLongitude(),
                coords[i][0],
                coords[i][1],
                distance);
            if (distance[0] < best) {
              best = distance[0];
              nearest = towns[i];
            }
          }
          if (nearest == null) {
            toast(R.string.manual_fallback);
            return;
          }
          selected = nearest;
          region = nearest.equals("jaffa") ? 1 : nearest.equals("beersheba") ? 2 : 0;
          redraw();
        };
    try {
      manager.requestLocationUpdates(
          LocationManager.NETWORK_PROVIDER, 0, 0, listener, Looper.getMainLooper());
      handler.postDelayed(
          () -> {
            stopLocation();
            if (isAdded()) toast(R.string.manual_fallback);
          },
          10000);
    } catch (SecurityException | IllegalArgumentException e) {
      toast(R.string.manual_fallback);
    }
  }

  private void stopLocation() {
    handler.removeCallbacksAndMessages(null);
    if (manager != null && listener != null) manager.removeUpdates(listener);
    listener = null;
  }

  @Override
  public void onDestroyView() {
    stopLocation();
    super.onDestroyView();
  }
}
