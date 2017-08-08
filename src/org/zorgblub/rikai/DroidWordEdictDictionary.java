package org.zorgblub.rikai;

import android.content.res.Resources;
import android.database.CursorIndexOutOfBoundsException;

import net.zorgblub.typhon.R;

import org.rikai.deinflector.DeinflectedWord;
import org.rikai.deinflector.Deinflector;
import org.rikai.dictionary.db.ResultCursor;
import org.rikai.dictionary.db.SqliteDatabase;
import org.rikai.dictionary.edict.EdictEntry;
import org.rikai.dictionary.edict.WordEdictDictionary;

/**
 * Created by Benjamin on 16/09/2015.
 */
public class DroidWordEdictDictionary extends WordEdictDictionary {

    private String pPath;

    protected SqliteDatabase pitchDatabase;


    private Resources resources;

    private int kanjiColor;

    private int kanaColor;

    private int pitchColor;

    private int definitionColor;

    private int reasonColor;



    private String name = "Edict";


    public DroidWordEdictDictionary(String path, Deinflector deinflector, SqliteDatabase sqliteDatabaseImpl, Resources resources) {
        super(path, deinflector, sqliteDatabaseImpl);
        this.pitchDatabase = new DroidSqliteDatabase();

        this.resources = resources;
        kanjiColor = this.resources.getColor(R.color.kanji);
        kanaColor = this.resources.getColor(R.color.kana);
        definitionColor = this.resources.getColor(R.color.definition);
        reasonColor = this.resources.getColor(R.color.reason);
        pitchColor = this.resources.getColor(R.color.pitch);
    }

    @Override
    protected EdictEntry makeEntry(DeinflectedWord variant, String kanji, String kana, String entry, String reason, String pitch) {
        DroidEdictEntry droidEdictEntry = new DroidEdictEntry(variant, kanji, kana, entry, reason, pitch);
        droidEdictEntry.setKanjiColor(kanjiColor);
        droidEdictEntry.setKanaColor(kanaColor);
        droidEdictEntry.setDefinitionColor(definitionColor);
        droidEdictEntry.setReasonColor(reasonColor);
        droidEdictEntry.setPitchColor(pitchColor);
        return droidEdictEntry;
    }

    public void setPitchPath(String pPath) { this.pPath = pPath; }

    public void load() {
        super.load();
        if(pPath != null)
            this.pitchDatabase.loadEdict(this.pPath);

    }

    public boolean isLoaded() {
        return this.sqliteDatabase.isLoaded()&&((this.pitchDatabase.isLoaded()||pPath!=null));
    }

    protected EdictEntry buildEntry(ResultCursor cursor, DeinflectedWord variant) {
        String reason = "";
        if (!variant.getReason().equals("")) {
            reason = "< " + variant.getReason() + " < " + variant.getOriginalWord();
        }

        // the Table value
        // (1, 2, 3, 4, 5, 6, 7)
        // (_id, word, wmark, kana, kmark, show, defn)
        //Here we need to get pitch using a second SQLite database
        String p = "";
        String kanji = cursor.getValue("kanji");
        String kana = cursor.getValue("kana");
        if(pitchDatabase != null)
            p = findPitch(kanji, kana);

        return makeEntry(variant, kanji, kana, cursor.getValue("entry"), reason, p);
    }

    protected String findPitch(String kanji, String kana)
    {
        String pQuery;
        String pitch = "";
        if(kanji == null)
            pQuery = "SELECT pitch FROM Dict WHERE expression = '" + kana +"'";
        else
            pQuery = "SELECT pitch FROM Dict WHERE expression = '" + kanji + "' AND reading = '" + kana + "'";
        ResultCursor r = pitchDatabase.select(pQuery, new String[0]);
        r.next();
        try{ pitch = r.getValue("pitch"); }
        catch(CursorIndexOutOfBoundsException e) {
            e.printStackTrace();
            r.close(); }
        return pitch;
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
