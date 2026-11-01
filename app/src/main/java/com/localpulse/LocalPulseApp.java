package com.localpulse;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import com.localpulse.data.LocalRepository;

public class LocalPulseApp extends Application {
  public LocalRepository repository;

  @Override
  public void onCreate() {
    super.onCreate();
    repository = new LocalRepository(this);
    AppCompatDelegate.setApplicationLocales(
        LocaleListCompat.forLanguageTags(repository.preference("language", "ar")));
    AppCompatDelegate.setDefaultNightMode(Integer.parseInt(repository.preference("theme", "-1")));
  }
}
