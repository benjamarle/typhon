package org.zorgblub.rikai;

import android.content.res.Resources;

import net.zorgblub.typhon.R;

import org.rikai.deinflector.DeinflectedWord;
import org.rikai.deinflector.Deinflector;
import org.rikai.dictionary.db.SqliteDatabase;
import org.rikai.dictionary.wordnet.WordnetDictionary;
import org.rikai.dictionary.wordnet.WordnetEntry;

/**
 * Created by Benjamin on 21/03/2016.
 */
public class DroidWordnetDictionary extends WordnetDictionary {
    private Resources resources;

    private int kanjiColor;

    private int synonymColor;

    private int exampleColor;

    private int posColor;

    private int reasonColor;

    private int definitionColor;

    private String name = "Wordnet";

    public DroidWordnetDictionary(String path, Deinflector deinflector, SqliteDatabase sqliteDatabaseImpl, Resources resources) {
        super(path, deinflector, sqliteDatabaseImpl);
        this.resources = resources;
        kanjiColor = this.resources.getColor(R.color.kanji);
        definitionColor = this.resources.getColor(R.color.definition);
        synonymColor = this.resources.getColor(R.color.synonym);
        exampleColor = this.resources.getColor(R.color.example);
        reasonColor = this.resources.getColor(R.color.reason);
        posColor = this.resources.getColor(R.color.partOfSpeech);
    }

    @Override
    protected WordnetEntry makeEntry(DeinflectedWord variant, String word, String synset, String reason, String pos, String gloss, String rank, int lexid, int freq) {
        DroidWordnetEntry droidWordnetEntry = new DroidWordnetEntry(variant, word, synset, reason, pos, gloss, rank, lexid, freq);
        droidWordnetEntry.setKanjiColor(kanjiColor);
        droidWordnetEntry.setDefinitionColor(definitionColor);
        droidWordnetEntry.setSynonymColor(synonymColor);
        droidWordnetEntry.setExampleColor(exampleColor);
        droidWordnetEntry.setReasonColor(reasonColor);
        droidWordnetEntry.setPosColor(posColor);
        return droidWordnetEntry;
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