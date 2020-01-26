package org.zorgblub.rikai;

import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;

import org.rikai.deinflector.DeinflectedWord;
import org.rikai.dictionary.wordnet.WordnetEntry;
import org.rikai.dictionary.wordnet.WordnetExample;

import java.util.List;

import static org.zorgblub.rikai.HtmlEntryUtils.wrapColor;

/**
 * Created by Benjamin on 21/03/2016.
 */
public class DroidWordnetEntry extends WordnetEntry implements DroidEntry {

    // default value are set to the rikaichan style
    private int kanjiColor = -4724737;

    private int synonymColor = -4128832;

    private int exampleColor =  -4128832;

    private int reasonColor = -8032;

    private int posColor = -8032;

    private int definitionColor = -1;

    public DroidWordnetEntry(DeinflectedWord variant, String word, String synset, String reason, String pos, String gloss, String rank, int lexid, int freq) {
        super(variant, word, synset, reason, pos, gloss, rank, lexid, freq);
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

    public int getReasonColor() {
        return reasonColor;
    }

    public void setReasonColor(int reasonColor) {
        this.reasonColor = reasonColor;
    }

    public int getSynonymColor() {
        return synonymColor;
    }

    public void setSynonymColor(int synonymColor) {
        this.synonymColor = synonymColor;
    }

    public int getExampleColor() {
        return exampleColor;
    }

    public void setExampleColor(int exampleColor) {
        this.exampleColor = exampleColor;
    }

    public int getPosColor() {
        return posColor;
    }

    public void setPosColor(int posColor) {
        this.posColor = posColor;
    }

    @Override
    public int getBackgroundColor() {
        return Color.BLACK;
    }

    @Override
    public Spanned render() {
        return Html.fromHtml(toStringCompact());
    }

    @Override
    public String toStringCompact() {
        // TODO change that to a SpannableStringBuilder for efficiency
        StringBuilder result = new StringBuilder(this.getLength());

        result.append(wrapColor(kanjiColor, this.getWord()));
        if (this.getPartOfSpeech().length() != 0) {
            result.append(" {").append(wrapColor(posColor, this.getPartOfSpeech())).append("}");
        }
        if (this.getReason().length() != 0) {
            result.append(" (").append(wrapColor(reasonColor, this.getReason())).append(")");
        }

        List<String> synonyms = this.getSynonyms();
        if(!synonyms.isEmpty())
            result.append("<br/>").append(wrapColor(synonymColor, synonyms.toString()));
        result.append("<br/>").append(wrapColor(definitionColor, this.getGloss()));

        List<WordnetExample> examples = this.getExamples();
        if(!examples.isEmpty()) {
            for (WordnetExample ex : examples) {
                result.append("<br>    ").append(wrapColor(exampleColor, ex.getNumber()+". "+ex.getJapaneseSentence()));
                result.append("<br>     ").append(wrapColor(exampleColor, "<i>"+ex.getEnglishSentence()+"</i>"));
            }
        }
        return result.toString();
    }
}
