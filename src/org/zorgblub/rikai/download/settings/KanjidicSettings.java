package org.zorgblub.rikai.download.settings;

import org.rikai.dictionary.Dictionary;
import org.zorgblub.rikai.DroidKanjiDictionary;

import java.io.IOException;

/**
 * Created by Benjamin on 25/03/2016.
 */
public class KanjidicSettings extends DownloadableSettings {

    private DictionaryType type = DictionaryType.KANJIDIC;

    private String basePath = "kanji.dat";

    private boolean stopAtFirstNonKanji = true;

    private int maxNbQueries = Integer.MAX_VALUE;

    private boolean heisig6 = true;

    private String name = "Kanjidic";

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public boolean isStopAtFirstNonKanji() {
        return stopAtFirstNonKanji;
    }

    public void setStopAtFirstNonKanji(boolean stopAtFirstNonKanji) {
        this.stopAtFirstNonKanji = stopAtFirstNonKanji;
    }

    @Override
    public Dictionary newInstance() throws IOException{
        DroidKanjiDictionary droidKanjiDictionary = new DroidKanjiDictionary(this.getFile().getAbsolutePath(), maxNbQueries, context.getResources(), this.heisig6);
        droidKanjiDictionary.setStopAtFirstNonKanji(stopAtFirstNonKanji);
        return droidKanjiDictionary;
    }

    @Override
    public DictionaryType getType() {
        return type;
    }

    public int getMaxNbQueries() {
        return maxNbQueries;
    }

    public void setMaxNbQueries(int maxNbQueries) {
        this.maxNbQueries = maxNbQueries;
    }

    public boolean isHeisig6() {
        return heisig6;
    }

    public void setHeisig6(boolean heisig6) {
        this.heisig6 = heisig6;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
