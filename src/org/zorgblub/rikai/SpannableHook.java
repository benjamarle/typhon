package org.zorgblub.rikai;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.UnderlineSpan;
import android.util.Pair;
import android.util.SparseArray;

import org.zorgblub.rikai.glosslist.PinchableListView;
import org.zorgblub.rikai.glosslist.SizeChangeListener;

import java.lang.ref.WeakReference;
import java.util.Stack;
import java.util.zip.Deflater;

import fuku.eb4j.EBException;
import fuku.eb4j.ExtFont;
import fuku.eb4j.SubBook;
import fuku.eb4j.hook.HookAdapter;
import fuku.eb4j.util.ByteUtil;

/**
 * Created by Benjamin on 21/03/2016.
 */
public class SpannableHook extends HookAdapter<Spannable> implements SizeChangeListener {

    private static final int INDENT_SIZE = 30; // TODO Fixed for now but should be proportional to the zoom
    private SpannableStringBuilder stringBuilder = new SpannableStringBuilder();

    private int textSize = PinchableListView.DEFAULT_SIZE;

    private boolean halfWidth;

    private SubBook subBook;

    private Context context;

    // Gaiji cache
    private SparseArray<Bitmap> resizedNarrowBitmaps = new SparseArray<Bitmap>();

    private SparseArray<Bitmap> resizedWideBitmaps = new SparseArray<Bitmap>();

    private SparseArray<Bitmap> narrowBitmaps = new SparseArray<Bitmap>();

    private SparseArray<Bitmap> wideBitmaps = new SparseArray<Bitmap>();

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

    public SpannableHook(SubBook subBook) {
        this.subBook = subBook;
    }

    public SubBook getSubBook() {
        return subBook;
    }

    public void setSubBook(SubBook subBook) {
        this.subBook = subBook;
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


    public class CenteredImageSpan extends ImageSpan {
        private WeakReference<Drawable> mDrawableRef;

        public CenteredImageSpan(Bitmap b) {
            super(b);
        }

        public CenteredImageSpan(Bitmap b, int verticalAlignment) {
            super(b, verticalAlignment);
        }

        public CenteredImageSpan(Context context, Bitmap b) {
            super(context, b);
        }

        public CenteredImageSpan(Context context, Bitmap b, int verticalAlignment) {
            super(context, b, verticalAlignment);
        }

        public CenteredImageSpan(Drawable d) {
            super(d);
        }

        public CenteredImageSpan(Drawable d, int verticalAlignment) {
            super(d, verticalAlignment);
        }

        public CenteredImageSpan(Drawable d, String source) {
            super(d, source);
        }

        public CenteredImageSpan(Drawable d, String source, int verticalAlignment) {
            super(d, source, verticalAlignment);
        }

        public CenteredImageSpan(Context context, Uri uri) {
            super(context, uri);
        }

        public CenteredImageSpan(Context context, Uri uri, int verticalAlignment) {
            super(context, uri, verticalAlignment);
        }

        public CenteredImageSpan(Context context, int resourceId) {
            super(context, resourceId);
        }

        public CenteredImageSpan(Context context, int resourceId, int verticalAlignment) {
            super(context, resourceId, verticalAlignment);
        }

        @Override
        public int getSize(Paint paint, CharSequence text,
                           int start, int end,
                           Paint.FontMetricsInt fm) {
            Drawable d = getCachedDrawable();
            Rect rect = d.getBounds();

            if (fm != null) {
                Paint.FontMetricsInt pfm = paint.getFontMetricsInt();
                // keep it the same as paint's fm
                fm.ascent = pfm.ascent;
                fm.descent = pfm.descent;
                fm.top = pfm.top;
                fm.bottom = pfm.bottom;
            }

            return rect.right;
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text,
                         int start, int end, float x,
                         int top, int y, int bottom, @NonNull Paint paint) {
            Drawable b = getCachedDrawable();
            canvas.save();

            int drawableHeight = b.getIntrinsicHeight();
            int fontAscent = paint.getFontMetricsInt().ascent;
            int fontDescent = paint.getFontMetricsInt().descent;
            int transY = bottom - b.getBounds().bottom +  // align bottom to bottom
                    (drawableHeight - fontDescent + fontAscent) / 2;  // align center to center

            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
        }

        // Redefined locally because it is a private member from DynamicDrawableSpan
        private Drawable getCachedDrawable() {
            WeakReference<Drawable> wr = mDrawableRef;
            Drawable d = null;

            if (wr != null)
                d = wr.get();

            if (d == null) {
                d = getDrawable();
                mDrawableRef = new WeakReference<>(d);
            }

            return d;
        }
    }

    @Override
    public void append(int code) {
        if(subBook == null)
            return;
        try {
            ExtFont font = subBook.getFont();
            Bitmap scaledBitmap = null;
            if(halfWidth && font.hasNarrowFont()){
                scaledBitmap = resizedNarrowBitmaps.get(code);
            }else if(font.hasWideFont()){
                scaledBitmap = resizedWideBitmaps.get(code);
            }
            if(scaledBitmap == null) {
                Pair<Bitmap, Boolean> bitmapNarrowPair = getScaledBitmap(code);
                scaledBitmap = bitmapNarrowPair.first;
                Boolean narrow = bitmapNarrowPair.second;
                if(narrow){
                    resizedNarrowBitmaps.put(code, scaledBitmap);
                }else{
                    resizedWideBitmaps.put(code, scaledBitmap);
                }
            }
            int charIndex = stringBuilder.length();
            stringBuilder.append(" "); // Gaiji space
            stringBuilder.setSpan(new CenteredImageSpan(context, scaledBitmap), charIndex, charIndex+1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        } catch (EBException e) {
            e.printStackTrace();
        }


    }

    private Pair<Bitmap, Boolean> getScaledBitmap(int code) throws EBException {
        ExtFont font = subBook.getFont();
        int width = 0, height;

        Bitmap bitmap = null;
        if(halfWidth && font.hasNarrowFont()){
            width = font.getNarrowFontWidth();
            bitmap = narrowBitmaps.get(code);
        }else if(font.hasWideFont()){
            width = font.getWideFontWidth();
            bitmap = wideBitmaps.get(code);
        }
        height = font.getFontHeight();


        boolean narrow = false;
        if(bitmap == null) {
            Pair<Bitmap, Boolean> bitmapNarrowPair = getBitmap(code, width, height);

            if(bitmapNarrowPair == null)
                return null;

            bitmap = bitmapNarrowPair.first;
            narrow = bitmapNarrowPair.second;
            if(narrow){
                narrowBitmaps.put(code, bitmap);
            }else{
                wideBitmaps.put(code, bitmap);
            }
        }

        if(bitmap == null)
            return null;

        int textWidth = textSize * width / height;
        return new Pair(Bitmap.createScaledBitmap(bitmap, textWidth, textSize, false), narrow);
    }

    private Pair<Bitmap, Boolean> getBitmap(int code, int width, int height) throws EBException {
        ExtFont font = subBook.getFont();
        byte[] fontImage = null;

        boolean narrow = false;
        if(halfWidth && font.hasNarrowFont()){
            fontImage = font.getNarrowFont(code);
            narrow = true;
        }else if(font.hasWideFont()){
            fontImage = font.getWideFont(code);
        }

        if(fontImage == null)
            return null;

        byte[] pngImage = org.zorgblub.rikai.ImageUtil.bitmapToPNG(fontImage, width, height,
                Color.WHITE, Color.BLACK,
                false, Deflater.DEFAULT_COMPRESSION);
        return new Pair(BitmapFactory.decodeByteArray(pngImage , 0, pngImage.length), narrow);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
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

    @Override
    public void onSizeChange(int newSize) {
        this.textSize = newSize;

        resizedNarrowBitmaps.clear();
        resizedWideBitmaps.clear();
    }



}
