package org.zorgblub.rikai.download.settings.ui.dialog;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.zorgblub.typhonkai.R;

import org.zorgblub.rikai.download.settings.DictionarySettings;

/**
 * Created by Benjamin on 03/04/2016.
 */
public abstract class DictionaryConfigDialog<T extends DictionarySettings> extends AlertDialog {

    protected EditText nameView;

    protected TextView typeView;

    protected Button okButton;

    protected Button cancelButton;

    protected T settings;

    public DictionaryConfigDialog(Context context, T settings) {
        super(context);
        this.settings = settings;
    }

    public DictionaryConfigDialog(Context context, int theme, T settings) {
        super(context, theme);
        this.settings = settings;
    }

    public DictionaryConfigDialog(Context context, boolean cancelable, OnCancelListener cancelListener, T settings) {
        super(context, cancelable, cancelListener);
        this.settings = settings;
    }

    protected abstract int getViewId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(getViewId());

        setTitle(R.string.dictionary_edit_title);
        nameView = (EditText) this.findViewById(R.id.dictionary_name);

        nameView.setText(settings.getName());
        TextView typeView = (TextView) this.findViewById(R.id.dictionary_type);
        typeView.setText(settings.getType().getName());

        Context context = getContext();
        Resources resources = context.getResources();

        okButton = (Button) findViewById(R.id.dictionary_settings_save);
        okButton.setOnClickListener(v -> {
            if(saveSettings()) dismiss();
        });

        cancelButton = (Button) findViewById(R.id.dictionary_settings_cancel);
        cancelButton.setOnClickListener(v -> {
            dismiss();
        });
    }

    protected boolean saveSettings() {
        settings.setName(nameView.getText().toString());
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }


}
