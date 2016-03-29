package org.zorgblub.rikai.download.settings;

import org.rikai.deinflector.Deinflector;
import org.rikai.dictionary.Dictionary;
import org.rikai.dictionary.wordnet.Lang;
import org.zorgblub.rikai.DroidSqliteDatabase;
import org.zorgblub.rikai.DroidWordnetDictionary;

import java.io.IOException;

/**
 * Created by Benjamin on 27/03/2016.
 */
public class WordnetSettings extends DownloadableSettings {

    private String basePath = "wordnet.sqlite";

    private DictionaryType type = DictionaryType.WORDNET;

    private String name = "Wordnet";

    private Lang lang = Lang.ENG;

    private boolean showSynonyms = true;

    private boolean showExamples = true;

    private boolean deinflect = true;

    private DeinflectorSettings deinflectorSettings = new DeinflectorSettings();

    @Override
    public Dictionary newInstance() throws IOException {
        Deinflector deinflector = null;
        if(deinflect){
            deinflector = new Deinflector(deinflectorSettings.getFile().getAbsolutePath());
        }
        DroidWordnetDictionary droidWordnetDictionary = new DroidWordnetDictionary(this.getFile().getAbsolutePath(), deinflector, new DroidSqliteDatabase(), context.getResources());
        droidWordnetDictionary.setLang(this.lang);
        droidWordnetDictionary.setName(this.getName());
        droidWordnetDictionary.setAddSynonyms(showSynonyms);
        droidWordnetDictionary.setAddExamples(showExamples);
        return droidWordnetDictionary;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public DictionaryType getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Lang getLang() {
        return lang;
    }

    public void setLang(Lang lang) {
        this.lang = lang;
    }

    public boolean isDeinflect() {
        return deinflect;
    }

    public void setDeinflect(boolean deinflect) {
        this.deinflect = deinflect;
    }

    public boolean isShowExamples() {
        return showExamples;
    }

    public void setShowExamples(boolean showExamples) {
        this.showExamples = showExamples;
    }

    public boolean isShowSynonyms() {
        return showSynonyms;
    }

    public void setShowSynonyms(boolean showSynonyms) {
        this.showSynonyms = showSynonyms;
    }
}
