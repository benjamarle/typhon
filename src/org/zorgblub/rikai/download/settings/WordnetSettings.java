package org.zorgblub.rikai.download.settings;

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

    @Override
    public Dictionary newInstance() throws IOException {
        DroidWordnetDictionary droidWordnetDictionary = new DroidWordnetDictionary(this.getFile().getAbsolutePath(), new DroidSqliteDatabase(), context.getResources());
        droidWordnetDictionary.setLang(this.lang);
        droidWordnetDictionary.setName(this.getName());
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
}
