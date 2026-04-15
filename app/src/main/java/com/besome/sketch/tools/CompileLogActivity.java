package com.besome.sketch.tools;

import android.annotation.SuppressLint;
import android.content.*;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import mod.hey.studios.util.*;
import mod.jbk.diagnostic.CompileErrorSaver;
import mod.jbk.util.AddMarginOnApplyWindowInsetsListener;
import pro.sketchware.databinding.CompileLogBinding;
import pro.sketchware.utility.SketchwareUtil;

public class CompileLogActivity extends BaseAppCompatActivity {

    private CompileErrorSaver saver;
    private SharedPreferences pref;
    private CompileLogBinding b;
    private Intent i;

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle s) { // ✅ FIXED (public)
        enableEdgeToEdgeNoContrast();
        super.onCreate(s);

        b = CompileLogBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // attach toolbar
        setSupportActionBar(b.topAppBar);

        i = getIntent();
        pref = getPreferences(0);

        ViewCompat.setOnApplyWindowInsetsListener(
                b.optionsLayout,
                new AddMarginOnApplyWindowInsetsListener(
                        WindowInsetsCompat.Type.navigationBars(),
                        WindowInsetsCompat.CONSUMED));

        b.topAppBar.setNavigationOnClickListener(
                Helper.getBackPressedClickListener(this));

        b.topAppBar.setTitle(
                i.getBooleanExtra("showingLastError", false)
                        ? "Last compile log" : "Compile log"
        );

        // remove old buttons
        b.clearButton.setVisibility(View.GONE);
        b.formatButton.setVisibility(View.GONE);

        String id = i.getStringExtra("sc_id");
        if (id == null) { finish(); return; }

        saver = new CompileErrorSaver(id);

        apply();
        setText();

        b.copyButton.setEnabled(hasLog());
        b.copyButton.setOnClickListener(v -> copy());
    }

    // ===== STATE =====

    private boolean hasLog() {
        return i.getStringExtra("error") != null || saver.logFileExists();
    }

    // ===== MENU =====

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        boolean has = hasLog();

        MenuItem clear = m.add(0,1,0,"Clear");
        clear.setIcon(R.drawable.delete);
        clear.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        clear.setEnabled(has);

        MenuItem format = m.add(0,2,1,"Format");
        format.setIcon(R.drawable.StyleFilter);
        format.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        format.setEnabled(has);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem it) {
        return switch (it.getItemId()) {
            case 1 -> { clear(); yield true; }
            case 2 -> { format(); yield true; }
            default -> super.onOptionsItemSelected(it);
        };
    }

    // ===== LOG =====

    private void setText() {
        String e = i.getStringExtra("error");
        if (e == null) e = saver.getLogsFromFile();

        if (e == null) {
            b.noContentLayout.setVisibility(View.VISIBLE);
            b.optionsLayout.setVisibility(View.GONE);
            return;
        }

        b.optionsLayout.setVisibility(View.VISIBLE);
        b.noContentLayout.setVisibility(View.GONE);

        b.tvCompileLog.setText(
                CompileLogHelper.getColoredLogs(this, e));
        b.tvCompileLog.setTextIsSelectable(true);
    }

    private void clear() {
        if (saver.logFileExists()) {
            saver.deleteSavedLogs();
            i.removeExtra("error");
            SketchwareUtil.toast("Compile logs cleared");
        }

        b.copyButton.setEnabled(hasLog());
        invalidateOptionsMenu();
        setText();
    }

    // ===== FORMAT =====

    private void format() {
        PopupMenu p = new PopupMenu(this, b.topAppBar);

        p.getMenu().add("Wrap text")
                .setCheckable(true)
                .setChecked(pref.getBoolean("wrapped_text", false));

        p.getMenu().add("Monospaced font")
                .setCheckable(true)
                .setChecked(pref.getBoolean("use_monospaced_font", true));

        p.getMenu().add("Font size");

        p.setOnMenuItemClickListener(it -> {
            String t = it.getTitle().toString();
            if (t.equals("Wrap text")) wrap(it);
            else if (t.equals("Monospaced font")) mono(it);
            else size();
            return true;
        });

        p.show();
    }

    private void wrap(MenuItem it) {
        boolean v = !it.isChecked();
        it.setChecked(v);
        pref.edit().putBoolean("wrapped_text", v).apply();

        b.errVScroll.removeAllViews();
        if (b.tvCompileLog.getParent()!=null)
            ((ViewGroup)b.tvCompileLog.getParent()).removeView(b.tvCompileLog);

        if (v) b.errVScroll.addView(b.tvCompileLog);
        else {
            b.errHScroll.removeAllViews();
            b.errHScroll.addView(b.tvCompileLog);
            b.errVScroll.addView(b.errHScroll);
        }
    }

    private void mono(MenuItem it) {
        boolean v = !it.isChecked();
        it.setChecked(v);
        pref.edit().putBoolean("use_monospaced_font", v).apply();
        b.tvCompileLog.setTypeface(v ? Typeface.MONOSPACE : Typeface.DEFAULT);
    }

    private void size() {
        NumberPicker p = new NumberPicker(this);
        p.setMinValue(10);
        p.setMaxValue(70);
        p.setValue(pref.getInt("font_size", 11));

        LinearLayout l = new LinearLayout(this);
        l.addView(p, new LinearLayout.LayoutParams(-2,-2,Gravity.CENTER));

        new MaterialAlertDialogBuilder(this)
                .setTitle("Font size")
                .setView(l)
                .setPositiveButton("Save",(d,w)->{
                    pref.edit().putInt("font_size", p.getValue()).apply();
                    b.tvCompileLog.setTextSize(p.getValue());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void apply() {
        wrapState(pref.getBoolean("wrapped_text", false));
        b.tvCompileLog.setTypeface(
                pref.getBoolean("use_monospaced_font", true)
                        ? Typeface.MONOSPACE : Typeface.DEFAULT);
        b.tvCompileLog.setTextSize(pref.getInt("font_size", 11));
    }

    private void wrapState(boolean v){
        b.errVScroll.removeAllViews();
        if (b.tvCompileLog.getParent()!=null)
            ((ViewGroup)b.tvCompileLog.getParent()).removeView(b.tvCompileLog);

        if (v) b.errVScroll.addView(b.tvCompileLog);
        else {
            b.errHScroll.removeAllViews();
            b.errHScroll.addView(b.tvCompileLog);
            b.errVScroll.addView(b.errHScroll);
        }
    }

    // ===== COPY =====

    private void copy() {
        String e = saver.getLogsFromFile();
        ClipboardManager c = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        if (c != null)
            c.setPrimaryClip(ClipData.newPlainText("error", e));

        SketchwareUtil.toast("Copied");
    }
}