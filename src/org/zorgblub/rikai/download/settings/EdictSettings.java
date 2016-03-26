package org.zorgblub.rikai.download.settings;

import org.rikai.deinflector.Deinflector;
import org.rikai.dictionary.Dictionary;
import org.zorgblub.rikai.DroidSqliteDatabase;
import org.zorgblub.rikai.DroidWordEdictDictionary;

import java.io.IOException;

/**
 * Created by Benjamin on 25/03/2016.
 */
public class EdictSettings extends DownloadableSettings {

    private String basePath = "polaredict.sqlite";

    private boolean collapseEntries = false;

    private DeinflectorSettings deinflectorSettings = new DeinflectorSettings();

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public boolean isCollapseEntries() {
        return collapseEntries;
    }

    public void setCollapseEntries(boolean collapseEntries) {
        this.collapseEntries = collapseEntries;
    }

    @Override
    public Dictionary newInstance() throws IOException {
        Deinflector deinflector = new Deinflector(deinflectorSettings.getFile().getAbsolutePath());
        return new DroidWordEdictDictionary(this.getFile().getAbsolutePath(), deinflector, new DroidSqliteDatabase(), context.getResources());
    }

    @Override
    public DictionaryType getType() {
        return DictionaryType.EDICT;
    }

    @Override
    public void delete() {
        super.delete();
        deinflectorSettings.delete();
    }
}
