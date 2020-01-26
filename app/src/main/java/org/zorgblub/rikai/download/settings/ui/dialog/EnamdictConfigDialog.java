package org.zorgblub.rikai.download.settings.ui.dialog;

import android.content.Context;

import net.zorgblub.typhonkai.R;

import org.zorgblub.rikai.download.settings.EnamdictSettings;

/**
 * Created by Benjamin on 03/04/2016.
 */
public class EnamdictConfigDialog extends DictionaryConfigDialog<EnamdictSettings> {

    public EnamdictConfigDialog(Context context, EnamdictSettings settings) {
        super(context, settings);
    }

    @Override
    protected int getViewId() {
        return R.layout.dictionary_enamdict_settings;
    }
}
