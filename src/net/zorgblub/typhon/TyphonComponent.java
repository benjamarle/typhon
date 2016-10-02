package net.zorgblub.typhon;

import net.zorgblub.typhon.activity.CatalogActivity;
import net.zorgblub.typhon.activity.FileBrowseActivity;
import net.zorgblub.typhon.activity.ManageSitesActivity;
import net.zorgblub.typhon.activity.ReadingActivity;
import net.zorgblub.typhon.activity.TyphonActivity;
import net.zorgblub.typhon.activity.TyphonPrefsActivity;
import net.zorgblub.typhon.epub.TyphonHtmlSpanner;
import net.zorgblub.typhon.fragment.BookDetailsFragment;
import net.zorgblub.typhon.fragment.CatalogFragment;
import net.zorgblub.typhon.fragment.FileBrowseFragment;
import net.zorgblub.typhon.fragment.LibraryFragment;
import net.zorgblub.typhon.fragment.ReadingFragment;
import net.zorgblub.typhon.view.AlphabetBar;
import net.zorgblub.typhon.view.BookCaseView;
import net.zorgblub.typhon.view.bookview.BookView;
import net.zorgblub.typhon.view.bookview.FixedPagesStrategy;
import net.zorgblub.typhon.view.bookview.ScrollingStrategy;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Benjamin on 30/03/2016.
 */
@Singleton
@Component(modules = {TyphonModuleDagger.class})
public interface TyphonComponent {

    void inject(ReadingFragment readingFragment);

    void inject(CatalogActivity catalogActivity);

    void inject(TyphonActivity typhonActivity);

    void inject(ManageSitesActivity manageSitesActivity);

    void inject(ReadingActivity readingActivity);

    void inject(FileBrowseActivity fileBrowseActivity);

    void inject(TyphonPrefsActivity typhonPrefsActivity);

    void inject(BookCaseView bookCaseView);

    void inject(FixedPagesStrategy fixedPagesStrategy);

    void inject(BookView bookView);

    void inject(ScrollingStrategy scrollingStrategy);

    void inject(AlphabetBar alphabetBar);

    void inject(BookDetailsFragment bookDetailsFragment);

    void inject(LibraryFragment libraryFragment);

    void inject(FileBrowseFragment fileBrowseFragment);

    void inject(CatalogFragment catalogFragment);

    void inject(TyphonHtmlSpanner typhonHtmlSpanner);

    // add inject methos for all target
}
