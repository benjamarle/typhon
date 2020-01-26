package net.zorgblub.typhonkai;

import net.zorgblub.typhonkai.activity.CatalogActivity;
import net.zorgblub.typhonkai.activity.FileBrowseActivity;
import net.zorgblub.typhonkai.activity.ManageSitesActivity;
import net.zorgblub.typhonkai.activity.ReadingActivity;
import net.zorgblub.typhonkai.activity.TyphonActivity;
import net.zorgblub.typhonkai.activity.TyphonPrefsActivity;
import net.zorgblub.typhonkai.fragment.BookDetailsFragment;
import net.zorgblub.typhonkai.fragment.CatalogFragment;
import net.zorgblub.typhonkai.fragment.FileBrowseFragment;
import net.zorgblub.typhonkai.fragment.LibraryFragment;
import net.zorgblub.typhonkai.fragment.ReadingFragment;
import net.zorgblub.typhonkai.view.AlphabetBar;
import net.zorgblub.typhonkai.view.BookCaseView;
import net.zorgblub.typhonkai.view.bookview.BookView;
import net.zorgblub.typhonkai.view.bookview.FixedPagesStrategy;
import net.zorgblub.typhonkai.view.bookview.ScrollingStrategy;

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


    // add inject methos for all target
}
