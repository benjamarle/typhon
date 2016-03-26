package org.zorgblub.rikai.download.settings;

import android.os.Bundle;

import org.rikai.dictionary.Dictionary;
import org.zorgblub.rikai.DroidNamesDictionary;
import org.zorgblub.rikai.DroidSqliteDatabase;

/**
 * Created by Benjamin on 25/03/2016.
 */
public class EnamdictSettings extends DownloadableSettings{

    private String basePath = "polarnames.sqlite";

    @Override
    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public Dictionary newInstance() {
        return new DroidNamesDictionary(this.getFile().getAbsolutePath(), new DroidSqliteDatabase(), context.getResources());
    }

    @Override
    public DictionaryType getType() {
        return DictionaryType.ENAMDICT;
    }
}
