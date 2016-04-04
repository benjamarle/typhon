package org.zorgblub.rikai.download.settings;

import android.content.Context;
import android.text.Spannable;

import org.rikai.dictionary.Dictionary;
import org.zorgblub.rikai.DroidEpwingDictionary;
import org.zorgblub.rikai.download.settings.ui.dialog.DictionaryConfigDialog;
import org.zorgblub.rikai.download.settings.ui.dialog.EpwingConfigDialog;

import java.io.IOException;

/**
 * Created by Benjamin on 01/04/2016.
 */
public class EpwingSettings extends DictionarySettings {

    private String name = "Epwing";

    private DictionaryType type = DictionaryType.EPWING;

    private String basePath;

    private int maxQueryLength = 8;

    @Override
    public Dictionary newInstance() throws IOException {
        if(this.basePath == null || this.basePath.length() ==0 )
            return null;
        DroidEpwingDictionary<Spannable> spannableEpwingDictionary = new DroidEpwingDictionary<Spannable>(this.getBasePath());
        spannableEpwingDictionary.setMaxQueryLength(this.getMaxQueryLength());
        spannableEpwingDictionary.setName(this.getName());
        return spannableEpwingDictionary;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public DictionaryType getType() {
        return type;
    }

    public void setType(DictionaryType type) {
        this.type = type;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public int getMaxQueryLength() {
        return maxQueryLength;
    }

    public void setMaxQueryLength(int maxQueryLength) {
        this.maxQueryLength = maxQueryLength;
    }

    @Override
    public DictionaryConfigDialog getConfigDialog(Context context) {
        return new EpwingConfigDialog(context, this);
    }
}
