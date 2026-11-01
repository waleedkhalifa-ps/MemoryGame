package com.localpulse.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.localpulse.R;

public final class Ui {
  private Ui() {}

  public static int dp(Context c, int n) {
    return Math.round(n * c.getResources().getDisplayMetrics().density);
  }

  public static int color(Context c, int id) {
    return ContextCompat.getColor(c, id);
  }

  public static LinearLayout column(Context c) {
    LinearLayout l = new LinearLayout(c);
    l.setOrientation(LinearLayout.VERTICAL);
    return l;
  }

  public static LinearLayout row(Context c) {
    LinearLayout l = new LinearLayout(c);
    l.setGravity(android.view.Gravity.CENTER_VERTICAL);
    return l;
  }

  public static TextView text(Context c, CharSequence value, int size, boolean bold) {
    TextView t = new TextView(c);
    t.setText(value);
    t.setTextSize(size);
    t.setTextColor(color(c, R.color.ink));
    t.setLineSpacing(dp(c, 3), 1.05f);
    if (bold) t.setTypeface(null, Typeface.BOLD);
    t.setTextDirection(View.TEXT_DIRECTION_LOCALE);
    return t;
  }

  public static void add(LinearLayout parent, View child) {
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
    p.bottomMargin = dp(parent.getContext(), 12);
    parent.addView(child, p);
  }

  public static void weight(LinearLayout row, View child) {
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1);
    p.setMarginEnd(dp(row.getContext(), 6));
    row.addView(child, p);
  }

  public static void note(LinearLayout parent, CharSequence label) {
    TextView t = text(parent.getContext(), label, 13, false);
    t.setTextColor(color(parent.getContext(), R.color.muted));
    t.setPadding(
        dp(parent.getContext(), 12),
        dp(parent.getContext(), 12),
        dp(parent.getContext(), 12),
        dp(parent.getContext(), 12));
    android.graphics.drawable.GradientDrawable bg =
        new android.graphics.drawable.GradientDrawable();
    bg.setColor(color(parent.getContext(), R.color.mint));
    bg.setCornerRadius(dp(parent.getContext(), 14));
    t.setBackground(bg);
    add(parent, t);
  }

  public static MaterialButton button(
      Context c, CharSequence label, boolean outline, Runnable action) {
    MaterialButton b =
        new MaterialButton(
            c,
            null,
            outline
                ? com.google.android.material.R.attr.materialButtonOutlinedStyle
                : com.google.android.material.R.attr.materialButtonStyle);
    b.setText(label);
    b.setTextSize(15);
    b.setCornerRadius(dp(c, 14));
    b.setMinHeight(dp(c, 54));
    b.setOnClickListener(v -> action.run());
    return b;
  }

  public static MaterialButton icon(
      Context c, int icon, CharSequence description, Runnable action) {
    MaterialButton b = button(c, "", true, action);
    b.setIconResource(icon);
    b.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
    b.setIconPadding(0);
    b.setContentDescription(description);
    b.setMinWidth(dp(c, 48));
    b.setMinimumWidth(dp(c, 48));
    return b;
  }

  public static TextInputEditText field(
      LinearLayout parent, String label, String value, boolean multi) {
    Context c = parent.getContext();
    TextInputLayout wrap =
        new TextInputLayout(c, null, com.google.android.material.R.attr.textInputOutlinedStyle);
    wrap.setHint(label);
    wrap.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
    wrap.setBoxCornerRadii(dp(c, 14), dp(c, 14), dp(c, 14), dp(c, 14));
    TextInputEditText edit = new TextInputEditText(wrap.getContext());
    edit.setBackground(null);
    edit.setText(value);
    edit.setTextSize(16);
    edit.setInputType(
        android.text.InputType.TYPE_CLASS_TEXT
            | (multi
                ? android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                : android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES));
    edit.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
    edit.setPadding(dp(c, 16), dp(c, 16), dp(c, 16), dp(c, 16));
    if (multi) edit.setMinLines(4);
    wrap.addView(edit, new LinearLayout.LayoutParams(-1, -2));
    add(parent, wrap);
    return edit;
  }

  public static MaterialCardView card(LinearLayout parent) {
    Context c = parent.getContext();
    MaterialCardView card = new MaterialCardView(c);
    card.setRadius(dp(c, 18));
    card.setStrokeColor(color(c, R.color.line));
    card.setStrokeWidth(dp(c, 1));
    card.setCardElevation(dp(c, 1));
    card.setCardBackgroundColor(color(c, R.color.surface));
    add(parent, card);
    return card;
  }

  public static ImageView scene(Context c, boolean event) {
    ImageView image = new ImageView(c);
    image.setImageResource(event ? R.drawable.scene_event : R.drawable.scene_park);
    image.setScaleType(ImageView.ScaleType.CENTER_CROP);
    image.setContentDescription(c.getString(R.string.ai_disclosure));
    image.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(c, 220)));
    return image;
  }

  public static Spinner spinner(LinearLayout parent, String label, String[] items, int selected) {
    Context c = parent.getContext();
    add(parent, text(c, label, 14, true));
    Spinner s = new Spinner(c);
    ArrayAdapter<String> a =
        new ArrayAdapter<>(c, android.R.layout.simple_spinner_dropdown_item, items);
    s.setAdapter(a);
    s.setSelection(Math.max(0, selected));
    s.setMinimumHeight(dp(c, 52));
    add(parent, s);
    return s;
  }

  public static void line(LinearLayout parent) {
    View l = new View(parent.getContext());
    l.setBackgroundColor(color(parent.getContext(), R.color.line));
    parent.addView(l, new LinearLayout.LayoutParams(-1, dp(parent.getContext(), 1)));
  }
}
