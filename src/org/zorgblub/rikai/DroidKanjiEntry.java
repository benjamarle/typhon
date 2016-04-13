package org.zorgblub.rikai;

import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;

import org.rikai.dictionary.kanji.KanjiEntry;
import org.rikai.dictionary.kanji.KanjiTag;

import java.util.Map;

/**
 * Created by Benjamin on 17/09/2015.
 */
public class DroidKanjiEntry extends KanjiEntry implements DroidEntry {

    boolean heisig6 = true;

    // default value are set to the rikaichan style
    private int kanjiColor = -4724737;

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

    public boolean isHeisig6() {
        return heisig6;
    }

    public void setHeisig6(boolean heisig6) {
        this.heisig6 = heisig6;
    }

    @Override
    public int getBackgroundColor() {
        return Color.BLACK;
    }

    @Override
    public String toStringCompact() {
        Map<KanjiTag, String> prop = this.getMisc();
        KanjiTag heisigTag = KanjiTag.L;
        if (heisig6) {
            heisigTag = KanjiTag.LL;
        }
        String heisigNumber = prop.get(heisigTag);
        StringBuilder result = new StringBuilder();
        result.append(HtmlEntryUtils.wrapColor(kanjiColor, this.getKanji().toString()));
        result.append(" [").append(HtmlEntryUtils.wrapColor(kanaColor, this.getYomi())).append("]");
        if (heisigNumber != null) {
            result.append(" (" + HtmlEntryUtils.wrapColor(indexColor, heisigNumber) + ")");
        }
        result.append("<br/>");
        result.append(HtmlEntryUtils.wrapColor(this.definitionColor, this.getDefinition()));
        return result.toString();
    }

    @Override
    public Spanned render() {
        return Html.fromHtml(toStringCompact());
    }
}
