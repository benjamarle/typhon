package org.zorgblub.rikai.download.settings;

import android.text.Spannable;

import org.rikai.dictionary.Dictionary;
import org.rikai.dictionary.epwing.EpwingDictionary;
import org.zorgblub.rikai.SpannableHook;

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
        EpwingDictionary<Spannable> spannableEpwingDictionary = new EpwingDictionary<>(this.getBasePath(), new SpannableHook());
        spannableEpwingDictionary.setMaxQueryLength(this.getMaxQueryLength());
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
}
