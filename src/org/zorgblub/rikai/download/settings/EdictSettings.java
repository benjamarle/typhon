package org.zorgblub.rikai.download.settings;

import android.content.Context;

import org.rikai.deinflector.Deinflector;
import org.rikai.dictionary.Dictionary;
import org.zorgblub.rikai.DroidSqliteDatabase;
import org.zorgblub.rikai.DroidWordEdictDictionary;
import org.zorgblub.rikai.download.settings.ui.dialog.DictionaryConfigDialog;
import org.zorgblub.rikai.download.settings.ui.dialog.EdictConfigDialog;

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

    private boolean deinflect = true;

    @Override
    public Dictionary newInstance() throws IOException {
        Deinflector deinflector = null;
        if(deinflect)
            deinflector = new Deinflector(deinflectorSettings.getFile().getAbsolutePath());
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

    public boolean isDeinflect() {
        return deinflect;
    }

    public void setDeinflect(boolean deinflect) {
        this.deinflect = deinflect;
    }

    @Override
    public DictionaryConfigDialog getConfigDialog(Context context) {
        return new EdictConfigDialog(context, this);
    }
}
