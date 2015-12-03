package org.rikai;

import android.graphics.Color;

import org.rikai.dictionary.kanji.KanjiEntry;
import org.rikai.dictionary.kanji.KanjiTag;

import java.util.Map;


import static org.rikai.HtmlEntryUtils.*;

/**
 * Created by Benjamin on 17/09/2015.
 */
public class DroidKanjiEntry extends KanjiEntry implements DroidEntity {

    // default value are set to the rikaichan style
    private int kanjiColor= -4724737;

    private int kanaColor = -4128832;

    private int definitionColor = -1;

    private int indexColor = -8032;

    public DroidKanjiEntry(Character kanji) {
        super(kanji);
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

    public void setKanaColor(int kanaColor) {
        this.kanaColor = kanaColor;
    }

    public int getDefinitionColor() {
        return definitionColor;
    }

    public void setDefinitionColor(int definitionColor) {
        this.definitionColor = definitionColor;
    }

    public int getIndexColor() {
        return indexColor;
    }

    public void setIndexColor(int indexColor) {
        this.indexColor = indexColor;
    }

    @Override
    public int getBackgroundColor() {
        return Color.GRAY;
    }

    @Override
    public String toStringCompact() {
        Map<KanjiTag, String> prop = this.getMisc();
        String heisigNumber = prop.get(KanjiTag.L);
        StringBuilder result = new StringBuilder();
        result.append(wrapColor(kanjiColor, this.getKanji().toString()));
        result.append(" [").append(wrapColor(kanaColor, this.getYomi())).append("]");
        if (heisigNumber != null) {
            result.append(" (" +wrapColor(indexColor, heisigNumber) + ")");
        }
        result.append("<br/>");
        result.append(wrapColor(this.definitionColor, this.getDefinition()));
        return result.toString();
    }
}
