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
 * Created by Benjamin Marlé.
 */
public class FuriganaSpan extends ReplacementSpan implements LineHeightSpan.WithDensity {

    private static final float MIN_LETTER_SPACING = -0.5f;
    private static final float LETTER_SPACING_INCREMENT = .05f;

    private float furiganaDivider = 2f;
    private String furigana;

    private int spanLength;

    private float textSize;
    private float furiganaSize;

    private float furiganaLetterSpacing;

    private static final boolean DEBUG = false;

    public FuriganaSpan(String furigana) {
        this.furigana = furigana;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        TextPaint textPaint = null;
        if(paint instanceof TextPaint){
            textPaint = (TextPaint) paint;
        }

        float textLength = paint.measureText(text, start, end);

        this.textSize = paint.getTextSize();
        this.furiganaSize = textSize / furiganaDivider;
        paint.setTextSize(furiganaSize);

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

            paint.setLetterSpacing(letterSpacing);
        }
        paint.setTextSize(textSize);

        this.spanLength = (int) Math.max(furiganaLength, textLength);

        return this.spanLength;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();

        if(paint instanceof TextPaint){
            // Draw highlight
            TextPaint textPaint = (TextPaint) paint;
            Paint.Style style = paint.getStyle();
            int color = paint.getColor();
            paint.setColor(textPaint.bgColor);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(x, top, x + spanLength, bottom, paint);
            paint.setStyle(style);
            paint.setColor(color);
        }

        if(DEBUG)
            drawDebug(canvas, x, top, y, bottom, paint);

        canvas.drawText(text, start, end, x, y, paint);

        float textSize = paint.getTextSize();
        paint.setTextSize(textSize / furiganaDivider);
        float originalLetterSpacing = 0f;
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP){
            originalLetterSpacing = paint.getLetterSpacing();
            paint.setLetterSpacing(furiganaLetterSpacing);
        }
        canvas.drawText(this.furigana, x, y + fontMetrics.ascent, paint);
        if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP){
            paint.setLetterSpacing(originalLetterSpacing);
        }
        paint.setTextSize(textSize);
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

        int iFuriganaSize = (int) Math.ceil(furiganaSize);
        int iTextSize = (int) Math.ceil(textSize);

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
