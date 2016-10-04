/*
This file is part of JRikai.

JRikai is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

JRikai is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with JRikai.  If not, see <http://www.gnu.org/licenses/>.

Author: Benjamin Marlé
*/
package net.zorgblub.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.LineHeightSpan;
import android.text.style.ReplacementSpan;

/**
 *
 */
public class FuriganaSpan extends ReplacementSpan implements LineHeightSpan.WithDensity {

    private static final float MIN_LETTER_SPACING = -0.5f;
    private static final float LETTER_SPACING_INCREMENT = .05f;

    private float furiganaDivider = 2f;
    private String furigana;
    private String kanji;

    private int spanLength;

    private float textFontSize;
    private float furiganaFontSize;

    private int renderedSize = 0;

    private float furiganaLetterSpacing;

    private float furiganaX;
    private float furiganaY;

    private int lastStart = -1;

    private int furiganaColor = Color.BLACK;

    private static final boolean DEBUG = false;

    public FuriganaSpan(String furigana, String kanji) {
        this.furigana = furigana;
        this.kanji = kanji;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        int spanSize = end - start;
        if(spanLength > 0)
            return spanLength * spanSize / this.kanji.length();

        float textLength = paint.measureText(text, start, end);

        this.textFontSize = paint.getTextSize();
        this.furiganaFontSize = textFontSize / furiganaDivider;
        paint.setTextSize(furiganaFontSize);

        float furiganaLength = paint.measureText(this.furigana);

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            float letterSpacing = paint.getLetterSpacing();

            float lsinc = LETTER_SPACING_INCREMENT;
            paint.setLetterSpacing(letterSpacing + lsinc);
            float newFuriganaLength = paint.measureText(this.furigana);

            float pixelDelta = newFuriganaLength - furiganaLength;

            this.furiganaLetterSpacing = lsinc * (textLength - furiganaLength) / pixelDelta;
            if (furiganaLength > textLength) {
                furiganaLetterSpacing = Math.max(MIN_LETTER_SPACING, furiganaLetterSpacing);
            }

            paint.setLetterSpacing(furiganaLetterSpacing);
            furiganaLength = paint.measureText(this.furigana);
            paint.setLetterSpacing(letterSpacing);
        }
        paint.setTextSize(textFontSize);

        this.spanLength = (int) Math.max(furiganaLength, textLength);

        return spanLength * spanSize / this.kanji.length();
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        int startDelta = start - lastStart;
        if(lastStart > 0 && (startDelta < 0 || renderedSize + startDelta > kanji.length())){
            lastStart = -1;
            renderedSize = 0;
        }

        lastStart = start;

        Paint.FontMetrics fontMetrics = paint.getFontMetrics();

        if(paint instanceof TextPaint){
            // Draw highlight
            TextPaint textPaint = (TextPaint) paint;
            Paint.Style style = paint.getStyle();
            int color = paint.getColor();
            paint.setColor(textPaint.bgColor);
            paint.setStyle(Paint.Style.FILL);
            float renderedWidth = paint.measureText(text, start, end);
            canvas.drawRect(x, top, x + renderedWidth, bottom, paint);
            paint.setStyle(style);
            paint.setColor(color);
        }

        if(DEBUG)
            drawDebug(canvas, x, top, y, bottom, paint);

        canvas.drawText(text, start, end, x, y, paint);
        if(renderedSize == 0){
            furiganaX = x;
            furiganaY = y;
        }


        renderedSize += end - start;


        // Furigana rendering
        if(renderedSize < kanji.length())
            return;
        else
            renderedSize = 0;

        renderFurigana(canvas, paint, fontMetrics);
    }

    private void renderFurigana(Canvas canvas, Paint paint, Paint.FontMetrics fontMetrics) {
        float originalTextSize = paint.getTextSize();
        int originalColor = paint.getColor();
        float originalLetterSpacing = 0f;
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP){
            originalLetterSpacing = paint.getLetterSpacing();
            paint.setLetterSpacing(furiganaLetterSpacing);
        }
        paint.setTextSize(originalTextSize / furiganaDivider);
        paint.setColor(furiganaColor);
        canvas.drawText(this.furigana, furiganaX, furiganaY + fontMetrics.ascent, paint);
        paint.setColor(originalColor);
        paint.setTextSize(originalTextSize);
        if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP){
            paint.setLetterSpacing(originalLetterSpacing);
        }
    }

    @NonNull
    private Paint.FontMetrics drawDebug(Canvas canvas, float x, int top, int y, int bottom, Paint paint) {
        Paint.Style style = paint.getStyle();
        paint.setStyle(Paint.Style.STROKE);
        int color = paint.getColor();
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        paint.setColor(Color.BLUE);
        canvas.drawLine(x, y + fontMetrics.ascent, x + spanLength, y + fontMetrics.ascent, paint);
        paint.setColor(Color.GREEN);
        canvas.drawLine(x, y + fontMetrics.descent, x + spanLength, y + fontMetrics.descent, paint);
        paint.setColor(Color.CYAN);
        canvas.drawLine(x, y + fontMetrics.top, x + spanLength, y + fontMetrics.top, paint);
        paint.setColor(Color.RED);
        canvas.drawRect(x, top, x + spanLength, bottom, paint);

        for(int i = bottom; i > top; i -= 10){
            canvas.drawLine(x, i, x + 10, i, paint);
        }
        paint.setColor(color);
        paint.setStyle(style);
        return fontMetrics;
    }

    public void chooseHeight(CharSequence text, int start, int end,
                             int istartv, int v,
                             Paint.FontMetricsInt fm) {
        chooseHeight(text, start, end, istartv, v, fm, null);
    }

    @Override
    public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm, TextPaint paint) {
        int ht = fm.descent - fm.ascent;

        int iFuriganaSize = (int) Math.ceil(furiganaFontSize);
        int iTextSize = (int) Math.ceil(textFontSize);

        if(ht > iTextSize + iFuriganaSize) {
            return;
        }

        int neededHt = iTextSize + iFuriganaSize;

        int need = neededHt - (fm.descent - fm.ascent);
        if (need > 0)
            fm.ascent -= need;

        need = neededHt - (fm.bottom - fm.top);

        if (need > 0)
            fm.top -= need;
    }
}
