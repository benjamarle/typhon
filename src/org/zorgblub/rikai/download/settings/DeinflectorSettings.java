package org.zorgblub.rikai.download.settings;

import org.rikai.dictionary.Dictionary;

import java.io.IOException;

/**
 * Created by Benjamin on 25/03/2016.
 */
public class DeinflectorSettings extends DownloadableSettings{

    private String basePath = "deinflect.dat";

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
        return null;
    }
}
