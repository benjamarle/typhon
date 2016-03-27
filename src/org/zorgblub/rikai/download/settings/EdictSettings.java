package org.zorgblub.rikai.download.settings;

import org.rikai.deinflector.Deinflector;
import org.rikai.dictionary.Dictionary;
import org.zorgblub.rikai.DroidSqliteDatabase;
import org.zorgblub.rikai.DroidWordEdictDictionary;

import java.io.File;
import java.io.IOException;

/**
 * Created by Benjamin on 25/03/2016.
 */
public class EdictSettings extends DownloadableSettings {

    private String basePath = "polaredict.sqlite";

    private DictionaryType type = DictionaryType.EDICT;

    private boolean collapseEntries = false;

    private DeinflectorSettings deinflectorSettings = new DeinflectorSettings();

    private String name = "Edict";

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
        DroidWordEdictDictionary droidWordEdictDictionary = new DroidWordEdictDictionary(this.getFile().getAbsolutePath(), deinflector, new DroidSqliteDatabase(), context.getResources());
        droidWordEdictDictionary.setName(this.getName());
        return droidWordEdictDictionary;
    }

    @Override
    public File[] getFiles() {
        return new File[]{this.getFile(), this.deinflectorSettings.getFile()};
    }

    @Override
    public DictionaryType getType() {
        return type;
    }

    @Override
    public void delete() {
        super.delete();
        deinflectorSettings.delete();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
