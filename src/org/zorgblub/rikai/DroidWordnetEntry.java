package org.zorgblub.rikai;

import android.graphics.Color;

import org.rikai.dictionary.wordnet.WordnetEntry;

import static org.zorgblub.rikai.HtmlEntryUtils.wrapColor;

/**
 * Created by Benjamin on 21/03/2016.
 */
public class DroidWordnetEntry extends WordnetEntry implements DroidEntity {

    // default value are set to the rikaichan style
    private int kanjiColor = -4724737;

    private int definitionColor = -1;

    public DroidWordnetEntry(String word, String gloss, String rank, int lexid, int freq) {
        super(word, gloss, rank, lexid, freq);
    }

    public int getKanjiColor() {
        return kanjiColor;
    }

    public void setKanjiColor(int kanjiColor) {
        this.kanjiColor = kanjiColor;
    }

    public int getDefinitionColor() {
        return definitionColor;
    }

    public void setDefinitionColor(int definitionColor) {
        this.definitionColor = definitionColor;
    }

    @Override
    public int getBackgroundColor() {
        return Color.DKGRAY;
    }


    @Override
    public String toStringCompact() {
        StringBuilder result = new StringBuilder(this.getLength());

        result.append(wrapColor(kanjiColor, this.getWord()));

        result.append("<br/>").append(wrapColor(definitionColor, this.getGloss()));

        return result.toString();
    }
}
