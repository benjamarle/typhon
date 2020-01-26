package org.zorgblub.rikai.download.settings;

import android.content.Context;

import org.rikai.dictionary.Dictionary;
import org.zorgblub.rikai.download.settings.ui.dialog.DictionaryConfigDialog;

import java.io.IOException;

/**
 * Created by Benjamin on 25/03/2016.
 */
public class DeinflectorSettings extends DownloadableSettings{

    private DictionaryType type = null;

    private String basePath = "deinflect.dat";

    private String name = "Deinflector";

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public boolean isHelper() {
        return true;
    }

    @Override
    public Dictionary newInstance() throws IOException {
        return null;
    }

    @Override
    public DictionaryType getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public DictionaryConfigDialog getConfigDialog(Context context) {
        return null;
    }
}
