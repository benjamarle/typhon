/*
 * Copyright (C) 2012 Alex Kuiper
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
package net.zorgblub.typhon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import net.zorgblub.nucular.atom.AtomConstants;
import net.zorgblub.nucular.atom.Entry;
import net.zorgblub.nucular.atom.Feed;
import net.zorgblub.nucular.atom.Link;
import net.zorgblub.typhon.Configuration;
import net.zorgblub.typhon.CustomOPDSSite;
import net.zorgblub.typhon.Typhon;
import net.zorgblub.typhon.catalog.Catalog;
import net.zorgblub.typhon.catalog.CatalogParent;
import net.zorgblub.typhon.catalog.LoadFeedCallback;
import net.zorgblub.typhon.fragment.BookDetailsFragment;
import net.zorgblub.typhon.fragment.CatalogFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import jedi.option.Option;

import static jedi.functional.FunctionalPrimitives.isEmpty;
import static jedi.option.Options.none;
import static jedi.option.Options.option;

public class CatalogActivity extends TyphonActivity implements CatalogParent {

    private static final Logger LOG = LoggerFactory
            .getLogger("CatalogActivity");

    @Nullable
    private BookDetailsFragment detailsFragment;

    private FragmentManager fragmentManager;

    @Inject
    Provider<CatalogFragment> fragmentProvider;

    @Inject
    Configuration config;

    private String baseFeedTitle;

    @Override
    protected void onCreateTyphonActivity(Bundle savedInstanceState) {
        Typhon.getComponent().inject(this);
        fragmentManager = this.getSupportFragmentManager();
        detailsFragment = (BookDetailsFragment) fragmentManager.findFragmentById(net.zorgblub.typhon.R.id.fragment_book_details);
        hideDetailsView();

        loadFeed( null, config.getBaseOPDSFeed(), null, false );
        fragmentManager.addOnBackStackChangedListener( this::onBackStackChanged );
    }

    @Override
    protected int getMainLayoutResource() {
        return net.zorgblub.typhon.R.layout.activity_catalog;
    }

    private void hideDetailsView() {
        if ( detailsFragment != null ) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.hide(detailsFragment);
            ft.commitAllowingStateLoss();
        }
    }

    private boolean isTwoPaneView() {
        return  getResources().getConfiguration().orientation
                == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                && detailsFragment != null;
    }


    @Override
    public void onFeedLoaded(Feed feed) {

        if ( isTwoPaneView() && feed.getSize() == 1
                && feed.getEntries().get(0).getEpubLink() != null ) {
            loadFakeFeed(feed);
        } else {
            hideDetailsView();
        }

        supportInvalidateOptionsMenu();
        getSupportActionBar().setTitle(feed.getTitle());

        LOG.debug( "Changed window title to " + feed.getTitle() );

        /*
         * Work-around, since the initial fragment isn't put on
         * the back-stack. We do want to restore its title
         * when the stack becomes empty, so we save it here.
         */
        if ( fragmentManager.getBackStackEntryCount() == 0 ) {
            this.baseFeedTitle = feed.getTitle();
        }
    }

    private void onBackStackChanged() {

        LOG.debug( "Backstack change detected." );

        if ( fragmentManager.getBackStackEntryCount() > 0 ) {

            Option<Fragment> fragmentOption = getCurrentVisibleFragment();
            fragmentOption.forEach( (fragment) -> {
                if ( fragment instanceof CatalogFragment ) {
                    LOG.debug( "Notifying fragment.");
                    ((CatalogFragment) fragment).onBecameVisible();
                }
            });

        } else if ( baseFeedTitle != null ) {
            supportInvalidateOptionsMenu();
            getSupportActionBar().setTitle( baseFeedTitle);
        }

    }

    @Override
    public void loadFakeFeed(Feed fakeFeed) {

        if ( isTwoPaneView() ) {

            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            ft.show(detailsFragment);
            ft.commit();

            detailsFragment.setNewFeed(fakeFeed, null);
        } else {
            Intent intent = new Intent( this, CatalogBookDetailsActivity.class );
            intent.putExtra("fakeFeed", fakeFeed);

            startActivity(intent);
        }
    }

    @Override
    public void loadCustomSitesFeed() {

        List<CustomOPDSSite> sites = config.getCustomOPDSSites();

        if ( sites.isEmpty() ) {
            Toast.makeText(this, net.zorgblub.typhon.R.string.no_custom_sites, Toast.LENGTH_LONG).show();
            return;
        }

        CatalogFragment newCatalogFragment = fragmentProvider.get();

        Feed customSites = new Feed();
        customSites.setURL(Catalog.CUSTOM_SITES_ID);
        customSites.setTitle(getString(net.zorgblub.typhon.R.string.custom_site));

        for ( CustomOPDSSite site: sites ) {
            Entry entry = new Entry();
            entry.setTitle(site.getName());
            entry.setSummary(site.getDescription());

            Link link = new Link(site.getUrl(), AtomConstants.TYPE_ATOM, AtomConstants.REL_BUY, null);
            entry.addLink(link);
            entry.setBaseURL(site.getUrl());

            customSites.addEntry(entry);
        }

        customSites.setId(Catalog.CUSTOM_SITES_ID);

        newCatalogFragment.setStaticFeed( customSites );

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        fragmentTransaction.replace(net.zorgblub.typhon.R.id.fragment_catalog, newCatalogFragment, Catalog.CUSTOM_SITES_ID );
        fragmentTransaction.addToBackStack( Catalog.CUSTOM_SITES_ID );

        fragmentTransaction.commit();
    }

    @Override
    public void loadFeed(Entry entry, String href, String baseURL, boolean asDetailsFeed) {

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        CatalogFragment newCatalogFragment = fragmentProvider.get();
        newCatalogFragment.setBaseURL( baseURL );

        fragmentTransaction.replace(net.zorgblub.typhon.R.id.fragment_catalog, newCatalogFragment, baseURL);

        if ( ! href.equals( config.getBaseOPDSFeed() ) ) {
            fragmentTransaction.addToBackStack( baseURL );
        }

        fragmentTransaction.commit();

        newCatalogFragment.loadURL(entry, href, asDetailsFeed, false, LoadFeedCallback.ResultType.REPLACE);
    }

    private Option<Fragment> getCurrentVisibleFragment() {

        if ( fragmentManager.getBackStackEntryCount() < 1 ) {
            return none();
        }

        FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(
                fragmentManager.getBackStackEntryCount() - 1 );


        Option<Fragment> result = option(fragmentManager.findFragmentByTag(entry.getName()));

        if ( isEmpty( result ) ){
            LOG.debug("Could not find fragment with name " + entry.getName());
        }

        return result;
    }


    @Override
    public boolean onSearchRequested() {

        Option<Boolean> result = getCurrentVisibleFragment().map( fragment -> {

            if ( fragment instanceof CatalogFragment ) {
                CatalogFragment catalogFragment = (CatalogFragment) fragment;

                catalogFragment.onSearchRequested();
                return catalogFragment.supportsSearch();
            }

            return false;
        });

        return result.getOrElse(false);
    }

    @Override
    public void onBackPressed() {
        hideDetailsView();
        super.onBackPressed();
    }

}
