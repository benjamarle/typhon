package net.zorgblub.typhon.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ExpandableListView;

import net.zorgblub.typhon.Configuration;
import net.zorgblub.typhon.R;
import net.zorgblub.typhon.Typhon;
import net.zorgblub.typhon.view.NavigationCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import jedi.functional.Command;
import jedi.option.Option;

/**
 * Superclass for all Typhon activity classes.
 */
public abstract class TyphonActivity extends AppCompatActivity {

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;

    @BindView(R.id.left_drawer)
    ExpandableListView mDrawerOptions;

    private ActionBarDrawerToggle mToggle;

    private NavigationAdapter adapter;

    private CharSequence originalTitle;

    private boolean drawerIsOpen;

    @Inject
    Configuration config;

    private static Logger LOG = LoggerFactory.getLogger("TyphonActivity");

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        Typhon.getComponent().inject(this);
        Typhon.changeLanguageSetting(this, config);

        setTheme( getTheme(config) );

        super.onCreate(savedInstanceState);

        requestFeatures();

        setContentView(getMainLayoutResource());
        ButterKnife.bind(this);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        initDrawerItems(mDrawerOptions);

        mToggle = new ActionBarDrawerToggle(this, mDrawer, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                TyphonActivity.this.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                TyphonActivity.this.onDrawerOpened(drawerView);
            }
        };

        mToggle.setDrawerIndicatorEnabled(true);
        mDrawer.setDrawerListener(mToggle);

        onCreateTyphonActivity(savedInstanceState);
    }

    protected void requestFeatures(){
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int action = event.getAction();
        int keyCode = event.getKeyCode();

        if ( action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK && isDrawerOpen() ) {
            closeNavigationDrawer();
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    protected void closeNavigationDrawer() {
        mDrawer.closeDrawers();
    }

    protected NavigationAdapter getAdapter() {
        return this.adapter;
    }

    protected void initDrawerItems( ExpandableListView expandableListView ) {
        if ( expandableListView != null ) {

            this.adapter = new NavigationAdapter( this, getMenuItems(config), this::createExpandableListView, 0);

            setClickListeners( expandableListView, this.adapter );
        }
    }

    private ExpandableListView createExpandableListView( List<NavigationCallback> items, int level ) {
        ExpandableListView e = new ExpandableListView(this) {
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                /*
                * Adjust height
                */
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(10000, MeasureSpec.AT_MOST);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        };
        setClickListeners(e, new NavigationAdapter(this, items, this::createExpandableListView, level ));
        return e;
    }

    private void setClickListeners( ExpandableListView expandableListView, NavigationAdapter adapter ) {

        expandableListView.setAdapter( adapter );

        expandableListView.setOnGroupClickListener(
                (e, v, groupId, l) -> this.onGroupClick(adapter, groupId) );

        expandableListView.setOnChildClickListener(
                (e, v, groupId, childId, l) -> this.onChildClick(adapter, groupId, childId));

        expandableListView.setOnItemLongClickListener(
                (av, v, position, id) -> this.onItemLongClick(adapter, position, id));

        expandableListView.setGroupIndicator(null);
    }

    protected abstract int getMainLayoutResource();

    protected void onCreateTyphonActivity(Bundle savedInstanceState ) {

    }

    protected void beforeLaunchActivity() {

    }

    protected int getTheme( Configuration config ) {
        return config.getTheme();
    }

    protected List<NavigationCallback> getMenuItems( Configuration config ) {

        List result = new ArrayList<>();

        if ( new File(config.getLastOpenedFile()).exists() ) {
            String nowReading = getString( R.string.now_reading, config.getLastReadTitle() );
            result.add( navigate(nowReading, ReadingActivity.class));
        }

        result.add( navigate(getString(R.string.open_library), LibraryActivity.class) );
        result.add( navigate(getString(R.string.download), CatalogActivity.class));

        result.add( new NavigationCallback(getString(R.string.prefs)).setOnClick(this::startPreferences));

        return result;
    }

    protected void startPreferences() {
        Intent intent = new Intent(this, TyphonPrefsActivity.class);
        beforeLaunchActivity();
        startActivity(intent);
    }

    protected NavigationCallback navigate( String title, Class<? extends TyphonActivity> classToStart ) {
        return new NavigationCallback( title ).setOnClick(
                () -> launchActivity(classToStart));
    }

    public void onDrawerClosed(View view) {
        this.drawerIsOpen = false;
        getSupportActionBar().setTitle(originalTitle);
        supportInvalidateOptionsMenu();
    }

    public void onDrawerOpened(View drawerView) {

        this.drawerIsOpen = true;
        this.originalTitle = getSupportActionBar().getTitle();

        getSupportActionBar().setTitle(R.string.app_name);
        supportInvalidateOptionsMenu();
    }

    protected boolean isDrawerOpen() {
        return drawerIsOpen;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        initDrawerItems( mDrawerOptions );
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        setSupportProgressBarIndeterminate(true);
//        setSupportProgressBarIndeterminateVisibility(false);

        mToggle.syncState();
    }

    public void launchActivity(Class<? extends TyphonActivity> activityClass) {
        Intent intent = new Intent(this, activityClass);

        beforeLaunchActivity();

        config.setLastActivity( activityClass );

        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // I think there's a bug in mToggle.onOptionsItemSelected, because it always returns false.
        // The item id testing is a fix.
        if (mToggle.onOptionsItemSelected(item) || item.getItemId() == android.R.id.home) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected boolean onGroupClick(NavigationAdapter adapter, int groupId ) {

        LOG.debug( "Got onGroupClick for group " + groupId + " on level " + adapter.getLevel() );

        Option<Boolean> group =adapter.findGroup(groupId).map(g -> {
            if (g.hasChildren()) {
                return false; //Let the superclass handle it and expand the group
            } else {
                g.onClick();
                closeNavigationDrawer();
                return true;
            }
        });

        return group.getOrElse( false );
    }

    protected boolean onChildClick(NavigationAdapter adapter, int groupId, int childId) {

        LOG.debug("Got onChildClick event for group " + groupId + " and child " + childId
                + " on level " + adapter.getLevel() );

        Option<NavigationCallback> childItem = adapter.findChild(groupId, childId);

        childItem.forEach((Command<? super NavigationCallback>) item -> {
            if ( ! item.hasChildren() ) {
                item.onClick();
                closeNavigationDrawer();
            }
        });

        return false;
    }

    protected boolean onItemLongClick(NavigationAdapter adapter, int position, long id) {

        LOG.debug("Got long click on position" + position + " on level " + adapter.getLevel() );

        if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            int groupPosition = ExpandableListView.getPackedPositionGroup(id);
            int childPosition = getAdapter().getIndexForChildId( groupPosition,
                    ExpandableListView.getPackedPositionChild(id) );

            Option<NavigationCallback> childItem = adapter.findChild(groupPosition, childPosition);

            LOG.debug("Long-click on " + groupPosition + ", " + childPosition );
            LOG.debug("Child-item: " + childItem );

            childItem.match(
                    NavigationCallback::onLongClick,
                    () -> LOG.error( "Could not get child-item for " + position + " and id " + id )
            );

            closeNavigationDrawer();
            return true;
        }

        return false;
    }

}
