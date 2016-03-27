package org.zorgblub.rikai.download.settings;

import org.rikai.dictionary.Dictionary;
import org.zorgblub.rikai.DroidNamesDictionary;
import org.zorgblub.rikai.DroidSqliteDatabase;

/**
 * Created by Benjamin on 25/03/2016.
 */
public class EnamdictSettings extends DownloadableSettings{

    private String basePath = "polarnames.sqlite";

    private DictionaryType type = DictionaryType.ENAMDICT;

    private String name = "Enamdict";

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
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
