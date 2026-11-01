package com.localpulse.ui;

import android.widget.*;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import com.localpulse.R;

public class SettingsFragment extends BaseFragment {
  @Override
  protected void render() {
    title(R.string.settings);
    EditText name =
        Ui.field(
            page,
            getString(R.string.full_name),
            repo.preference("name", getString(R.string.demo_you)),
            false);
    button(
        R.string.save,
        () -> {
          repo.setPreference("name", name.getText().toString().trim());
          toast(R.string.saved);
        });
    Ui.add(page, Ui.text(context, getString(R.string.language), 20, true));
    for (String[] language : new String[][] {{"العربية", "ar"}, {"English", "en"}, {"עברית", "he"}})
      Ui.add(
          page,
          Ui.button(
              context,
              language[0],
              true,
              () -> {
                repo.setPreference("language", language[1]);
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(language[1]));
              }));
    Ui.add(page, Ui.text(context, getString(R.string.theme), 20, true));
    for (int mode :
        new int[] {
          AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
          AppCompatDelegate.MODE_NIGHT_NO,
          AppCompatDelegate.MODE_NIGHT_YES
        })
      Ui.add(
          page,
          Ui.button(
              context,
              getString(mode == -1 ? R.string.system : mode == 1 ? R.string.light : R.string.dark),
              true,
              () -> {
                repo.setPreference("theme", String.valueOf(mode));
                AppCompatDelegate.setDefaultNightMode(mode);
              }));
    title(R.string.about);
    note(R.string.local_only);
    note(R.string.emergency_notice);
  }
}
