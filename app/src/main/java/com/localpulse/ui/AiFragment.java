package com.localpulse.ui;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import com.localpulse.*;
import com.localpulse.data.ComposerViewModel;

public class AiFragment extends BaseFragment {
  private ComposerViewModel vm;
  private String facts = "", proposal = "";
  private int tab;
  private final ActivityResultLauncher<Intent> voice =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK
                && result.getData() != null) {
              java.util.ArrayList<String> words =
                  result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
              if (words != null && !words.isEmpty()) {
                facts = words.get(0);
                repo.setPreference("ai-facts", facts);
                if (isAdded()) redraw();
              }
            }
          });

  @Override
  protected void render() {
    vm = new ViewModelProvider(requireActivity()).get(ComposerViewModel.class);
    vm.load(repo);
    facts = repo.preference("ai-facts", vm.draft.text);
    proposal = repo.preference("ai-proposal", "");
    title(R.string.ai_studio);
    LinearLayout tabs = Ui.row(context);
    for (int i = 0; i < 3; i++) {
      final int index = i;
      Ui.weight(
          tabs,
          Ui.button(
              context,
              getString(i == 0 ? R.string.writer : i == 1 ? R.string.illustration : R.string.video),
              tab != i,
              () -> {
                tab = index;
                redraw();
              }));
    }
    Ui.add(page, tabs);
    if (tab != 0) {
      title(tab == 1 ? R.string.illustration : R.string.video_creator);
      note(R.string.provider_needed);
      if (tab == 1) {
        Ui.add(page, Ui.scene(context, false));
        note(R.string.ai_disclosure);
      }
      outline(
          R.string.back_edit,
          () -> {
            tab = 0;
            redraw();
          });
      return;
    }
    note(R.string.offline_writer);
    EditText input = Ui.field(page, getString(R.string.facts), facts, true);
    input.setFilters(
        new android.text.InputFilter[] {new android.text.InputFilter.LengthFilter(5000)});
    input.addTextChangedListener(
        new android.text.TextWatcher() {
          public void beforeTextChanged(CharSequence s, int a, int c, int f) {}

          public void afterTextChanged(android.text.Editable e) {}

          public void onTextChanged(CharSequence s, int a, int b, int c) {
            facts = s.toString();
            repo.setPreference("ai-facts", facts);
          }
        });
    Ui.add(
        page,
        Ui.button(
            context,
            "🎙  " + getString(R.string.facts),
            true,
            () -> {
              Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
              intent.putExtra(
                  RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
              intent.putExtra(
                  RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault().toLanguageTag());
              try {
                voice.launch(intent);
              } catch (android.content.ActivityNotFoundException e) {
                toast(R.string.provider_needed);
              }
            }));
    button(
        R.string.generate_draft,
        () -> {
          if (facts.trim().length() < 10) {
            input.setError(getString(R.string.required));
            return;
          }
          proposal = facts.trim().replaceAll("[ \\t]+", " ");
          repo.setPreference("ai-proposal", proposal);
          redraw();
        });
    if (!proposal.isEmpty()) {
      Ui.add(page, Ui.text(context, getString(R.string.suggested_draft), 19, true));
      EditText suggestion = Ui.field(page, getString(R.string.details), proposal, true);
      note(R.string.review_ai);
      button(
          R.string.approve,
          () -> {
            vm.draft.text = suggestion.getText().toString();
            if (vm.draft.title.trim().isEmpty()) {
              String first = vm.draft.text.split("[.\\n]", 2)[0];
              vm.draft.title = first.substring(0, Math.min(first.length(), 100));
            }
            vm.reviewed = false;
            vm.step = 0;
            vm.persist(repo);
            open(R.id.create);
          });
      outline(
          R.string.cancel,
          () -> {
            repo.setPreference("ai-proposal", "");
            proposal = "";
            redraw();
          });
    }
  }
}
