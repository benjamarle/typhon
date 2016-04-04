package org.zorgblub.rikai.download.settings.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import net.zorgblub.typhon.R;

import org.rikai.dictionary.wordnet.Lang;
import org.zorgblub.rikai.download.settings.WordnetSettings;

/**
 * Created by Benjamin on 03/04/2016.
 */
public class WordnetConfigDialog extends DictionaryConfigDialog<WordnetSettings> {

    protected Spinner langSpinner;

    protected CheckBox synonymsCheckbox;

    protected CheckBox examplesCheckbox;
    
    protected CheckBox deinflectCheckbox;

    public WordnetConfigDialog(Context context, WordnetSettings settings) {
        super(context, settings);
    }

    @Override
    protected int getViewId() {
        return R.layout.dictionary_wordnet_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        langSpinner = (Spinner) findViewById(R.id.dictionary_lang);
        ArrayAdapter<Lang> adapter = new ArrayAdapter<>(getContext(), R.layout.dictionary_lang_item, Lang.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        langSpinner.setAdapter(adapter);
        langSpinner.setSelection(settings.getLang().ordinal());

        langSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settings.setLang(Lang.values()[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // not supposed to happen
            }
        });

        synonymsCheckbox = (CheckBox) findViewById(R.id.dictionary_edit_form_synonyms);
        synonymsCheckbox.setChecked(settings.isShowSynonyms());
        synonymsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.setShowSynonyms(isChecked);
        });

        examplesCheckbox = (CheckBox) findViewById(R.id.dictionary_edit_form_examples);
        examplesCheckbox.setChecked(settings.isShowExamples());
        examplesCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.setShowExamples(isChecked);
        });

        deinflectCheckbox = (CheckBox) findViewById(R.id.dictionary_edit_form_deinflect);
        deinflectCheckbox.setChecked(settings.isDeinflect());
        deinflectCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.setDeinflect(isChecked);
        });
    }
}
