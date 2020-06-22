package org.zorgblub.rikai;

import android.content.res.Resources;

import net.zorgblub.typhon.R;

import org.rikai.deinflector.DeinflectedWord;
import org.rikai.dictionary.db.SqliteDatabase;
import org.rikai.dictionary.edict.EdictEntry;
import org.rikai.dictionary.edict.NamesDictionary;

/**
 * Created by Benjamin on 16/09/2015.
 */
public class DroidNamesDictionary extends NamesDictionary {

    private Resources resources;

    private int kanjiColor;

    private int kanaColor;

    private int definitionColor;

    private int reasonColor;

    private String name = "Enamdict";

    public DroidNamesDictionary(String path, SqliteDatabase sqliteDatabaseImpl, Resources resources) {
        super(path, sqliteDatabaseImpl);
        this.resources = resources;
        kanjiColor = this.resources.getColor(R.color.kanji);
        kanaColor = this.resources.getColor(R.color.kana);
        definitionColor = this.resources.getColor(R.color.definition);
        reasonColor = this.resources.getColor(R.color.reason);
    }

    @Override
    protected EdictEntry makeEntry(DeinflectedWord variant, String kanji, String kana, String entry, String reason) {
        DroidEdictEntry droidEdictEntry = new DroidEdictEntry(variant, kanji, kana, entry, reason);
        droidEdictEntry.setKanjiColor(kanjiColor);
        droidEdictEntry.setKanaColor(kanaColor);
        droidEdictEntry.setDefinitionColor(definitionColor);
        droidEdictEntry.setReasonColor(reasonColor);
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
