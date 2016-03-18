package net.zorgblub.typhon.view.bookview;

import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;

import net.zorgblub.typhon.dto.HighLight;

/**
 * Created with IntelliJ IDEA.
 * User: alex
 * Date: 6/17/13
 * Time: 8:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class HighlightSpan extends BackgroundColorSpan {

    private HighLight highLight;

    public HighlightSpan( HighLight highLight ) {
        super( highLight.getColor() );
        this.highLight = highLight;
    }

    public HighLight getHighLight() {
        return this.highLight;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);

        ds.setUnderlineText(this.highLight.getTextNote() != null && this.highLight.getTextNote().trim().length() > 0 );
    }
}
