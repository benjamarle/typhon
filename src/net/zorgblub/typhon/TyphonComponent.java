package net.zorgblub.typhon;

import net.zorgblub.typhon.fragment.ReadingFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Benjamin on 30/03/2016.
 */
@Singleton
@Component(modules = {TyphonModuleDagger.class})
public interface TyphonComponent {

    void inject(ReadingFragment readingFragment);

    // add inject methos for all target
}
