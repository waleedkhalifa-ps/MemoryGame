package com.localpulse.data;

import androidx.lifecycle.ViewModel;
import com.localpulse.models.News;
import java.util.UUID;
import org.json.JSONObject;

/** A persisted draft is restored after process recreation; only cleared after publication. */
public class ComposerViewModel extends ViewModel {
  public News draft;
  public int step;
  public boolean reviewed;
  public boolean importing;

  public void load(LocalRepository repository) {
    if (draft != null) return;
    try {
      draft = LocalRepository.decode(new JSONObject(repository.preference("draft", "{}")));
    } catch (Exception ignored) {
      draft = new News();
    }
    if (draft.id.isEmpty()) draft.id = UUID.randomUUID().toString();
    draft.authorUid = "demo-you";
  }

  public void persist(LocalRepository repository) {
    repository.setPreference("draft", LocalRepository.encode(draft).toString());
  }

  public void clear(LocalRepository repository) {
    repository.setPreference("draft", "{}");
    draft = null;
    step = 0;
    reviewed = false;
  }
}
