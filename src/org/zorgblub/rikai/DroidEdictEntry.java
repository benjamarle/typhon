package org.zorgblub.rikai;

import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;

import org.rikai.deinflector.DeinflectedWord;
import org.rikai.dictionary.edict.EdictEntry;

import static org.zorgblub.rikai.HtmlEntryUtils.wrapColor;

/**
 * Created by Benjamin on 16/09/2015.
 */
public class DroidEdictEntry extends EdictEntry implements DroidEntry {
    // default value are set to the rikaichan style
    private int kanjiColor = -4724737;

    private int kanaColor = -4128832;

    private int definitionColor = -1;

    private int reasonColor = -8032;

    private int pitchColor = -1;


    public DroidEdictEntry() {
    }

    public DroidEdictEntry(DeinflectedWord variant, String word, String reading, String gloss, String reason, String pitch) {
        super(variant, word, reading, gloss, reason, pitch);
    }

    public DroidEdictEntry(String msg){
        super(new DeinflectedWord(msg), msg, "", "", "", "");
    }

    public int getKanjiColor() {
        return kanjiColor;
    }

    public void setKanjiColor(int kanjiColor) {
        this.kanjiColor = kanjiColor;
    }

    public int getKanaColor() {
        return kanaColor;
    }

    public int getReasonColor() {
        return reasonColor;
    }

    public int getDefinitionColor() {
        return definitionColor;
    }

    public int getPitchColor() { return pitchColor; }

    public void setDefinitionColor(int definitionColor) {
        this.definitionColor = definitionColor;
    }

    public void setKanaColor(int kanaColor) {
        this.kanaColor = kanaColor;
    }

    public void setReasonColor(int reasonColor) {
        this.reasonColor = reasonColor;
    }

    public void setPitchColor(int pitchColor) { this.pitchColor = pitchColor; }

    @Override
    public int getBackgroundColor() {
        return Color.BLACK;
    }

    @Override
    public String toStringCompact() {
        StringBuilder result = new StringBuilder(this.getLength());

        result.append(wrapColor(kanjiColor, this.getWord()));

        if (this.getReading().length() != 0) {
            result.append(" [").append(wrapColor(kanaColor, this.getReading())).append("]");
        }

        if(this.getPitch().length() != 0) {
            result.append("「").append(wrapColor(pitchColor, this.getPitch())).append("」");
        }

        if (this.getReason().length() != 0) {
            result.append(" (").append(wrapColor(reasonColor, this.getReason())).append(")");
        }

        result.append("<br/>").append(wrapColor(definitionColor, this.getGloss()));

        return result.toString();
    }

    @Override
    public Spanned render() {
        return Html.fromHtml(this.toStringCompact());
    }
}
