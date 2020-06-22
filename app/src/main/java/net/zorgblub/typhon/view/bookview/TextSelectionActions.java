/*
 * Copyright (C) 2013 Alex Kuiper
 *
 * This file is part of PageTurner
 *
 * PageTurner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PageTurner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PageTurner.  If not, see <http://www.gnu.org/licenses/>.*
 */

package net.zorgblub.typhon.view.bookview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

import net.zorgblub.typhon.PlatformUtil;
import net.zorgblub.typhon.R;
import net.zorgblub.ui.UiUtils;

import jedi.functional.Command;
import jedi.option.Option;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TextSelectionActions implements ActionMode.Callback {

    private TextSelectionCallback callBack;
    private SelectedTextProvider selectedTextProvider;

    private Context context;

    public interface SelectedTextProvider {
        Option<String> getSelectedText();

        int getSelectionStart();
        int getSelectionEnd();
    }

    public TextSelectionActions(Context context, TextSelectionCallback callBack,
                                SelectedTextProvider selectedTextProvider) {
        this.callBack = callBack;
        this.context = context;
        this.selectedTextProvider = selectedTextProvider;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        mode.finish();

        return true;
    }

    private static OnMenuItemClickListener react( ActionMode mode, UiUtils.Action action ) {
        return item -> {
            action.perform();
            mode.finish();
            return true;
        };
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, Menu menu) {

        menu.removeItem(android.R.id.selectAll);

        MenuItem copyItem = menu.findItem(android.R.id.copy);

        if ( copyItem != null ) {
            copyItem.setOnMenuItemClickListener(
                    react(mode, () -> selectedTextProvider.getSelectedText().forEach((Command<? super String>) t ->
                            PlatformUtil.copyTextToClipboard(context, t))));
        }

        menu.add( R.string.abc_shareactionprovider_share_with )
                .setOnMenuItemClickListener(
                        react(mode, () -> selectedTextProvider.getSelectedText().forEach((Command<? super String>) t ->
                                                callBack.share(
                                                        selectedTextProvider.getSelectionStart(),
                                                        selectedTextProvider.getSelectionEnd(),
                                                        t
                                                )
                                )
                        )
                ).setIcon(R.drawable.abc_ic_menu_share_mtrl_alpha);


        menu.add(R.string.highlight)
                .setOnMenuItemClickListener(
                        react( mode, () -> selectedTextProvider.getSelectedText().forEach((Command<? super String>) t ->
                                callBack.highLight(selectedTextProvider.getSelectionStart(),
                                    selectedTextProvider.getSelectionEnd(), t)
                        )));

        if (callBack.isDictionaryAvailable()) {
            menu.add(R.string.dictionary_lookup)
                    .setOnMenuItemClickListener( react( mode, () ->
                            selectedTextProvider.getSelectedText().forEach((Command<? super String>) callBack::lookupDictionary)));
        }

        menu.add(R.string.lookup_wiktionary)
                .setOnMenuItemClickListener( react(mode, () ->
                        selectedTextProvider.getSelectedText().forEach((Command<? super String>) callBack::lookupWiktionary)));

        menu.add(R.string.wikipedia_lookup)
                .setOnMenuItemClickListener( react( mode, () ->
                        selectedTextProvider.getSelectedText().forEach((Command<? super String>) callBack::lookupWikipedia)));

        menu.add(R.string.google_lookup)
                .setOnMenuItemClickListener( react( mode, () ->
                        selectedTextProvider.getSelectedText().forEach((Command<? super String>) callBack::lookupGoogle)));

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

        return true;
    }

}
