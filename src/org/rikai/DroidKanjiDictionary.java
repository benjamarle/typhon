package org.rikai;

import android.content.res.Resources;

import net.nightwhistler.pageturner.R;

import org.rikai.dictionary.kanji.KanjiDictionary;
import org.rikai.dictionary.kanji.KanjiEntry;

/**
 * Created by Benjamin on 17/09/2015.
 */
public class DroidKanjiDictionary extends KanjiDictionary {

    private Resources resources;

    private int kanjiColor;

    private int kanaColor;

    private int definitionColor;

    private int indexColor;

    public DroidKanjiDictionary(String path, Resources resources) {
        super(path);
        this.resources = resources;
        kanjiColor = this.resources.getColor(R.color.kanji);
        kanaColor = this.resources.getColor(R.color.kana);
        definitionColor = this.resources.getColor(R.color.definition);
        indexColor = this.resources.getColor(R.color.index);
    }

    @Override
    protected KanjiEntry makeEntry(char kanjiChar) {
        DroidKanjiEntry droidEdictEntry = new DroidKanjiEntry(kanjiChar);
        droidEdictEntry.setKanjiColor(kanjiColor);
        droidEdictEntry.setKanaColor(kanaColor);
        droidEdictEntry.setDefinitionColor(definitionColor);
        droidEdictEntry.setIndexColor(indexColor);
        return droidEdictEntry;
    }
}
