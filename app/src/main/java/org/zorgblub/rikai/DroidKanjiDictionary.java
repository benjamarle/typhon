package org.zorgblub.rikai;

import android.content.res.Resources;

import net.zorgblub.typhonkai.R;

import org.rikai.dictionary.kanji.KanjiDictionary;
import org.rikai.dictionary.kanji.KanjiEntry;

import java.io.FileNotFoundException;

/**
 * Created by Benjamin on 17/09/2015.
 */
public class DroidKanjiDictionary extends KanjiDictionary {

    private Resources resources;

    private int kanjiColor;

    private int kanaColor;

    private int definitionColor;

    private int indexColor;

    private boolean heisig6 = true;

    private String name = "Kanjidic";

    public DroidKanjiDictionary(String path, int maxNbQueries, Resources resources, boolean heisig6) throws FileNotFoundException {
        super(path, maxNbQueries);
        this.resources = resources;
        kanjiColor = this.resources.getColor(R.color.kanji);
        kanaColor = this.resources.getColor(R.color.kana);
        definitionColor = this.resources.getColor(R.color.definition);
        indexColor = this.resources.getColor(R.color.index);
        this.heisig6 = heisig6;
    }

    @Override
    protected KanjiEntry makeEntry(char kanjiChar) {
        DroidKanjiEntry droidEdictEntry = new DroidKanjiEntry(kanjiChar);
        droidEdictEntry.setKanjiColor(kanjiColor);
        droidEdictEntry.setKanaColor(kanaColor);
        droidEdictEntry.setDefinitionColor(definitionColor);
        droidEdictEntry.setIndexColor(indexColor);
        droidEdictEntry.setHeisig6(this.heisig6);
        return droidEdictEntry;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
