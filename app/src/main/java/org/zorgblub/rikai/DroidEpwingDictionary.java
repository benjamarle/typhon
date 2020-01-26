package org.zorgblub.rikai;

import net.zorgblub.typhonkai.Typhon;

import org.rikai.dictionary.epwing.EpwingDictionary;
import org.rikai.dictionary.epwing.EpwingEntry;

import fuku.eb4j.Result;
import fuku.eb4j.SubBook;

/**
 * Created by Benjamin on 20/03/2016.
 */
public class DroidEpwingDictionary<T> extends EpwingDictionary  {

    private String name;

    private SpannableHook spannableHook;

    public DroidEpwingDictionary(String path) {
        super(path, null);
        spannableHook = new SpannableHook();
        spannableHook.setContext(Typhon.get().getApplicationContext());
        setHook(spannableHook);
    }

    @Override
    public void load() {
        super.load();
        SubBook subBook = this.getSubBook();
        spannableHook.setSubBook(subBook);
    }

    @Override
    protected EpwingEntry makeEntry(String originalWord, Result result, SubBook subBook) {
        return new DroidEpwingEntry(originalWord, result, subBook, this.getHook());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
