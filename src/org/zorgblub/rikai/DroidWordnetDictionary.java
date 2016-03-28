package org.zorgblub.rikai;

import android.content.res.Resources;

import net.zorgblub.typhon.R;

import org.rikai.dictionary.db.SqliteDatabase;
import org.rikai.dictionary.wordnet.WordnetDictionary;
import org.rikai.dictionary.wordnet.WordnetEntry;

/**
 * Created by Benjamin on 21/03/2016.
 */
public class DroidWordnetDictionary extends WordnetDictionary {
    private Resources resources;

    private int kanjiColor;

    private int definitionColor;

    public DroidWordnetDictionary(String path, SqliteDatabase sqliteDatabaseImpl, Resources resources) {
        super(path, sqliteDatabaseImpl);

        this.resources = resources;
        kanjiColor = this.resources.getColor(R.color.kanji);
        definitionColor = this.resources.getColor(R.color.definition);
    }


    @Override
    protected WordnetEntry makeEntry(String word, String gloss, String rank, int lexid, int freq) {
        DroidWordnetEntry droidWordnetEntry = new DroidWordnetEntry(word, gloss, rank, lexid, freq);
        droidWordnetEntry.setKanjiColor(kanjiColor);
        droidWordnetEntry.setDefinitionColor(definitionColor);
        return droidWordnetEntry;
    }

}