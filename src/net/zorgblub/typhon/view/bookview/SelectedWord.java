package net.zorgblub.typhon.view.bookview;

/**
 * Created by alex on 10/14/14.
 */
public class SelectedWord {

    private int startOffset;
    private int endOffset;
    private CharSequence text;
    private CharSequence contextSentence;

    public SelectedWord( int startOffset, int endOffset, CharSequence text, CharSequence contextSentence) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.text = text;
        this.contextSentence = contextSentence;
    }

    public SelectedWord(CharSequence text){
        this.text = text;
        contextSentence = "No context sentence";
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public CharSequence getText() {

        if ( text == null ) {
            return "";
        }

        return text;
    }

    public CharSequence getContextSentence() {
        return contextSentence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SelectedWord that = (SelectedWord) o;

        return text.equals(that.text);

    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
}