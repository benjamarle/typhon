package org.zorgblub.rikai;

import android.graphics.Color;
import android.text.Spannable;
import android.text.Spanned;

import org.rikai.dictionary.epwing.EpwingEntry;

import fuku.eb4j.Result;
import fuku.eb4j.SubBook;
import fuku.eb4j.hook.Hook;

/**
 * Created by Benjamin on 21/03/2016.
 */
public class DroidEpwingEntry extends EpwingEntry<Spannable> implements DroidEntry {

    private Spannable cachedRendering;

    public DroidEpwingEntry(String originalWord, Result result, SubBook subBook, Hook<Spannable> hook) {
        super(originalWord, result, subBook, hook);
    }

    @Override
    public Spanned render() {
        if(cachedRendering == null){
           cachedRendering = getText();
        }
        return cachedRendering;
    }

    @Override
    public int getBackgroundColor() {
        return Color.BLACK;
    }
}
