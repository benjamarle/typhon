package org.zorgblub.rikai.download.settings.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.widget.CheckBox;

import net.zorgblub.typhon.R;

import org.zorgblub.rikai.download.settings.EdictSettings;

/**
 * Created by Benjamin on 03/04/2016.
 */
public class EdictConfigDialog extends DictionaryConfigDialog<EdictSettings> {

    protected CheckBox deinflectCheckbox;

    public EdictConfigDialog(Context context, EdictSettings settings) {
        super(context, settings);
    }

    @Override
    protected int getViewId() {
        return R.layout.dictionary_edict_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdictSettings edictSettings = settings;
        deinflectCheckbox = (CheckBox) findViewById(R.id.dictionary_edit_form_deinflect);
        deinflectCheckbox.setChecked(edictSettings.isDeinflect());
        deinflectCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            edictSettings.setDeinflect(isChecked);
        });
    }
}
