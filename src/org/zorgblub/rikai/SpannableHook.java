package org.zorgblub.rikai;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.UnderlineSpan;

import java.util.Stack;

import fuku.eb4j.hook.HookAdapter;
import fuku.eb4j.util.ByteUtil;

/**
 * Created by Benjamin on 21/03/2016.
 */
public class SpannableHook extends HookAdapter<Spannable> {

    private static final int INDENT_SIZE = 30; // TODO Fixed for now but should be proportional to the zoom
    private SpannableStringBuilder stringBuilder = new SpannableStringBuilder();

    private boolean halfWidth;

    @Override
    public void clear() {
        stringBuilder = new SpannableStringBuilder();
        halfWidth = false;
        decorationStack.clear();
        underlineStart = null;
        italicStart = null;
        boldStart = null;
        emphasisStart = null;
        referenceStart = null;
        subScriptStart = null;
        superScriptStart = null;
    }

    public SpannableHook() {

    }

    public Spannable getObject() {
        if(!indentStack.isEmpty())
            setIndent(0);
        return stringBuilder;
    }

    @Override
    public boolean isMoreInput() {
        return true;
    }

    @Override
    public void append(char ch) {
        stringBuilder.append(ch);
    }

    @Override
    public void append(int code) {
        // no gaiji for now
    }

    @Override
    public void append(String str) {
        if (halfWidth)
            str = ByteUtil.wideToNarrow(str);
        stringBuilder.append(str);
    }


    Integer boldStart;
    Integer italicStart;
    Integer underlineStart;


    Stack<Integer> decorationStack = new Stack<Integer>();

    @Override
    public void beginDecoration(int type) {
        decorationStack.push(type);
        switch (type) {
            case BOLD:
                boldStart = getLength();
                break;
            case ITALIC:
                italicStart = getLength();
                break;
            default:
                underlineStart = getLength();
                break;
        }
    }

    @Override
    public void endDecoration() {
        if (decorationStack.isEmpty())
            return;
        switch (decorationStack.pop()) {
            case ITALIC:
            if (italicStart != null) {
                setSpan(new StyleSpan(Typeface.ITALIC), italicStart);
            }
            italicStart = null;
            break;
            case BOLD:
            default:
                if (boldStart != null)
                    setSpan(new StyleSpan(Typeface.BOLD), boldStart);
                boldStart = null;
                break;
//            if (underlineStart != null)
//                    setSpan(new UnderlineSpan(), underlineStart);
//                underlineStart = null;
//                break;
        }
    }

    Stack<IndentValue> indentStack = new Stack<IndentValue>();

    private class IndentValue{
        public int start = -1;
        public int value;

        public IndentValue(int value) {
            this.start = getLength();
            this.value = value;
        }
    }

    @Override
    public void setIndent(int indent) {
        if(indent <  0)
            return;

        int currentValue = -1;
        if(indentStack.isEmpty() || indent > (currentValue = indentStack.peek().value)){
            indentStack.push(new IndentValue(indent));
            return;
        }

        while(indent < currentValue){ // decreasing indent

            IndentValue pop = indentStack.pop();
            setSpan(new LeadingMarginSpan.Standard(INDENT_SIZE *pop.value), pop.start);

            if(indentStack.isEmpty())
                break;
            currentValue = indentStack.peek().value;
        }
    }

    @Override
    public void newLine() {
        stringBuilder.append("\n");
    }

    @Override
    public void beginNarrow() {
        halfWidth = true;
    }

    @Override
    public void endNarrow() {
        halfWidth = false;
    }


    Integer subScriptStart = null;

    @Override
    public void beginSubscript() {
        subScriptStart = getLength();
    }

    @Override
    public void endSubscript() {
        if (subScriptStart != null)
            setSpan(new SubscriptSpan(), subScriptStart);
        subScriptStart = null;
    }

    Integer superScriptStart = null;

    @Override
    public void beginSuperscript() {
        superScriptStart = getLength();
    }

    @Override
    public void endSuperscript() {
        if (superScriptStart != null)
            setSpan(new SuperscriptSpan(), superScriptStart);
        superScriptStart = null;
    }

    Integer emphasisStart = null;

    @Override
    public void beginEmphasis() {
        emphasisStart = getLength();
    }

    @Override
    public void endEmphasis() {
        if (emphasisStart != null)
            setSpan(new StyleSpan(Typeface.ITALIC), emphasisStart);
        emphasisStart = null;
    }

    @Override
    public void beginKeyword() {
        super.beginKeyword();
    }

    @Override
    public void endKeyword() {
        super.endKeyword();
    }


    Integer referenceStart = null;

    @Override
    public void beginReference() {
        referenceStart = getLength();
    }

    @Override
    public void endReference(long pos) {
        if (referenceStart != null)
            setSpan(new UnderlineSpan(), referenceStart);
        referenceStart = null;
    }

    private int getLength() {
        return stringBuilder.length();
    }

    private void setSpan(Object o, int start) {
        stringBuilder.setSpan(o, start, getLength(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
