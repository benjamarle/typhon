package org.zorgblub.rikai.download.settings.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.widget.CheckBox;

import net.zorgblub.typhon.R;

import org.zorgblub.rikai.download.settings.KanjidicSettings;

/**
 * Created by Benjamin on 03/04/2016.
 */
public class KanjidicConfigDialog extends DictionaryConfigDialog<KanjidicSettings> {

    protected CheckBox heisig6Checkbox;

    public KanjidicConfigDialog(Context context, KanjidicSettings settings) {
        super(context, settings);
    }

    @Override
    protected int getViewId() {
        return R.layout.dictionary_kanjidic_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        heisig6Checkbox = (CheckBox) findViewById(R.id.dictionary_edit_form_heisig6);
        heisig6Checkbox.setChecked(settings.isHeisig6());
        heisig6Checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.setHeisig6(isChecked);
        });
    }
}
