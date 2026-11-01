package com.localpulse.ui;

import android.net.Uri;
import android.text.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import com.localpulse.*;
import com.localpulse.data.ComposerViewModel;
import com.localpulse.domain.PublishPolicy;
import java.io.*;
import java.util.*;

public class CreateFragment extends BaseFragment {
  private ComposerViewModel vm;
  private final ActivityResultLauncher<String[]> picker =
      registerForActivityResult(
          new ActivityResultContracts.OpenMultipleDocuments(), this::importFiles);
  private static final String[] CATEGORIES = {
    "community", "education", "sports", "traffic", "events", "environment"
  };
  private static final String[] TOWNS = {"nazareth", "haifa", "jaffa", "beersheba"};

  @Override
  protected void render() {
    vm = new ViewModelProvider(requireActivity()).get(ComposerViewModel.class);
    vm.load(repo);
    if (vm.step == 0) editor();
    else if (vm.step == 1) attachments();
    else preview();
  }

  private void watch(EditText input, java.util.function.Consumer<String> change) {
    input.addTextChangedListener(
        new TextWatcher() {
          public void beforeTextChanged(CharSequence s, int a, int c, int f) {}

          public void afterTextChanged(Editable e) {}

          public void onTextChanged(CharSequence s, int start, int before, int count) {
            change.accept(s.toString());
            vm.reviewed = false;
            vm.persist(repo);
          }
        });
  }

  private void editor() {
    title(R.string.new_news);
    EditText headline = Ui.field(page, getString(R.string.headline), vm.draft.title, false);
    headline.setFilters(new InputFilter[] {new InputFilter.LengthFilter(160)});
    watch(headline, s -> vm.draft.title = s);
    EditText details = Ui.field(page, getString(R.string.details), vm.draft.text, true);
    details.setFilters(new InputFilter[] {new InputFilter.LengthFilter(20000)});
    watch(details, s -> vm.draft.text = s);
    String[] cats = Arrays.stream(CATEGORIES).map(this::category).toArray(String[]::new);
    Spinner category =
        Ui.spinner(
            page,
            getString(R.string.category),
            cats,
            Arrays.asList(CATEGORIES).indexOf(vm.draft.categoryId));
    category.setOnItemSelectedListener(
        listener(
            pos -> {
              vm.draft.categoryId = CATEGORIES[pos];
              vm.reviewed = false;
              vm.persist(repo);
            }));
    Spinner town =
        Ui.spinner(
            page,
            getString(R.string.town),
            Arrays.stream(TOWNS).map(BaseFragment::town).toArray(String[]::new),
            Arrays.asList(TOWNS).indexOf(vm.draft.locationId));
    town.setOnItemSelectedListener(
        listener(
            pos -> {
              vm.draft.locationId = TOWNS[pos];
              vm.reviewed = false;
              vm.persist(repo);
            }));
    button(
        R.string.ai_assist,
        () -> {
          vm.persist(repo);
          open(R.id.ai);
        });
    outline(R.string.reporter, () -> open(R.id.reporter));
    LinearLayout buttons = Ui.row(context);
    Ui.weight(
        buttons,
        Ui.button(
            context,
            getString(R.string.save_draft),
            true,
            () -> {
              vm.persist(repo);
              toast(R.string.saved);
            }));
    Ui.weight(
        buttons,
        Ui.button(
            context,
            getString(R.string.next),
            false,
            () -> {
              if (!PublishPolicy.valid(vm.draft.title, vm.draft.text, vm.draft.locationId)) {
                toast(R.string.required);
                return;
              }
              vm.step = 1;
              vm.persist(repo);
              redraw();
            }));
    Ui.add(page, buttons);
  }

  static android.widget.AdapterView.OnItemSelectedListener listener(
      java.util.function.IntConsumer change) {
    return new android.widget.AdapterView.OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> p, android.view.View v, int pos, long id) {
        change.accept(pos);
      }

      public void onNothingSelected(AdapterView<?> p) {}
    };
  }

  private void attachments() {
    title(R.string.attachments);
    note(R.string.attachment_hint);
    LinearLayout types = Ui.row(context);
    Ui.weight(
        types,
        Ui.button(
            context,
            getString(R.string.photos),
            true,
            () -> picker.launch(new String[] {"image/jpeg", "image/png", "image/webp"})));
    Ui.weight(
        types,
        Ui.button(
            context,
            getString(R.string.video),
            true,
            () -> picker.launch(new String[] {"video/mp4"})));
    Ui.weight(
        types,
        Ui.button(context, "PDF", true, () -> picker.launch(new String[] {"application/pdf"})));
    Ui.add(page, types);
    if (vm.importing) {
      ProgressBar progress = new ProgressBar(context);
      Ui.add(page, progress);
      note(R.string.loading);
    }
    for (String path : new ArrayList<>(vm.draft.media)) {
      MediaViews.add(page, path);
      Ui.add(
          page,
          Ui.button(
              context,
              getString(R.string.remove_attachment),
              true,
              () -> {
                vm.draft.media.remove(path);
                vm.reviewed = false;
                vm.persist(repo);
                redraw();
              }));
    }
    com.google.android.material.button.MaterialButton next =
        Ui.button(
            context,
            getString(R.string.preview),
            false,
            () -> {
              vm.step = 2;
              redraw();
            });
    next.setEnabled(!vm.importing);
    Ui.add(page, next);
    outline(
        R.string.back_edit,
        () -> {
          vm.step = 0;
          redraw();
        });
  }

  private void preview() {
    title(R.string.preview);
    if (vm.draft.media.isEmpty()) {
      Ui.add(page, Ui.scene(context, false));
      note(R.string.ai_disclosure);
    } else for (String path : vm.draft.media) MediaViews.add(page, path);
    Ui.add(page, Ui.text(context, vm.draft.title, 25, true));
    Ui.add(
        page,
        Ui.text(
            context, town(vm.draft.locationId) + " · " + category(vm.draft.categoryId), 14, false));
    Ui.add(page, Ui.text(context, vm.draft.text, 18, false));
    CheckBox facts = new CheckBox(context);
    facts.setText(R.string.check_facts);
    facts.setChecked(vm.reviewed);
    facts.setOnCheckedChangeListener((b, on) -> vm.reviewed = on);
    Ui.add(page, facts);
    note(R.string.responsibility);
    note(R.string.local_only);
    button(
        R.string.publish,
        () -> {
          if (vm.draft == null) return;
          if (!PublishPolicy.canPublish(
              vm.draft.title, vm.draft.text, vm.draft.locationId, vm.reviewed)) {
            toast(R.string.review_required);
            return;
          }
          vm.draft.authorName = repo.preference("name", getString(R.string.demo_you));
          vm.draft.createdAt = System.currentTimeMillis();
          repo.put(vm.draft);
          String id = vm.draft.id;
          repo.notifyActivity(getString(R.string.published) + ": " + vm.draft.title);
          vm.clear(repo);
          toast(R.string.published);
          open(R.id.article, "story", id);
        });
    outline(
        R.string.back_edit,
        () -> {
          vm.step = 0;
          redraw();
        });
  }

  private void importFiles(List<Uri> uris) {
    if (uris.isEmpty() || !isAdded()) return;
    if (vm.importing || vm.draft.media.size() + uris.size() > 6) {
      toast(R.string.file_error);
      return;
    }
    vm.importing = true;
    String importingDraft = vm.draft.id;
    redraw();
    android.content.Context app = context.getApplicationContext();
    java.util.concurrent.ExecutorService executor =
        java.util.concurrent.Executors.newSingleThreadExecutor();
    executor.execute(
        () -> {
          List<String> paths = new ArrayList<>();
          boolean failed = false;
          for (Uri uri : uris) {
            File destination = null;
            try {
              String mime = app.getContentResolver().getType(uri);
              String extension =
                  switch (mime == null ? "" : mime) {
                    case "image/jpeg" -> ".jpg";
                    case "image/png" -> ".png";
                    case "image/webp" -> ".webp";
                    case "video/mp4" -> ".mp4";
                    case "application/pdf" -> ".pdf";
                    default -> throw new IOException("Unsupported MIME");
                  };
              File dir = new File(app.getFilesDir(), "media");
              if (!dir.exists() && !dir.mkdirs())
                throw new IOException("Cannot create media directory");
              destination = new File(dir, UUID.randomUUID() + extension);
              try (InputStream input = app.getContentResolver().openInputStream(uri);
                  OutputStream out = new FileOutputStream(destination)) {
                if (input == null) throw new IOException("Missing input");
                byte[] buffer = new byte[8192];
                long size = 0;
                int read;
                while ((read = input.read(buffer)) != -1) {
                  size += read;
                  if (size > 20 * 1024 * 1024) throw new IOException("File too large");
                  out.write(buffer, 0, read);
                }
                if (size == 0) throw new IOException("Empty file");
              }
              paths.add(destination.getAbsolutePath());
            } catch (Exception e) {
              failed = true;
              if (destination != null) destination.delete();
            }
          }
          boolean error = failed;
          new android.os.Handler(android.os.Looper.getMainLooper())
              .post(
                  () -> {
                    vm.importing = false;
                    if (vm.draft != null && vm.draft.id.equals(importingDraft)) {
                      vm.draft.media.addAll(paths);
                      vm.reviewed = false;
                      vm.persist(repo);
                    } else for (String path : paths) new File(path).delete();
                    if (isAdded() && getView() != null) {
                      if (error) toast(R.string.file_error);
                      redraw();
                    }
                  });
          executor.shutdown();
        });
  }

  @Override
  public void onPause() {
    if (vm != null && vm.draft != null) vm.persist(repo);
    super.onPause();
  }
}
