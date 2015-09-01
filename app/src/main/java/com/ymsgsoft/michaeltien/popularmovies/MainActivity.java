package com.ymsgsoft.michaeltien.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        MainActivityFragment.Callbacks
{
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    public int menu_selected;
    public boolean mTwoPane;

    @Override
    public void onItemSelected(int position) {
        MainActivityFragment mFragment = (MainActivityFragment)
                getSupportFragmentManager().findFragmentById(R.id.container);
        MovieObject mv = mFragment.getMovieObject(position);
        String ARG_ITEM_ID = getString(R.string.package_prefix) + getString(R.string.intent_key_movie_object);
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(ARG_ITEM_ID, mv);
            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragment)
                    .commit();
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detail_intent = new Intent(this, DetailActivity.class);
            // putExtra a parcel movie object
            detail_intent.putExtra(ARG_ITEM_ID, mv);
            startActivity(detail_intent);
        }

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        menu_selected = position;
        switch (position) {
            case 0:
                mTitle = getString(R.string.menu_popular_movies);
                break;
            case 1:
                mTitle = getString(R.string.menu_favorite);
                break;
        }
        MainActivityFragment mFragment =
                (MainActivityFragment) getSupportFragmentManager()
                        /* use container id when add fragment */
                        .findFragmentById(R.id.container);
        if (mFragment != null)  // can't find it if not initialized
           mFragment.UpdateMoviesList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                fragmentManager.findFragmentById(R.id.navigation_drawer);

        mTitle = getTitle();
        if ( savedInstanceState != null) {
           mTitle = savedInstanceState.getCharSequence("Title");
        }

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        // attach main fragment
        // setup mainActivity Fragment
        if (savedInstanceState == null) { // very important to check if it is already there
            fragmentManager.beginTransaction()
                    .add(R.id.container, new MainActivityFragment() )
                    .commit();
        }
        if (findViewById(R.id.detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            MainActivityFragment mFragment = (MainActivityFragment)
                    getSupportFragmentManager().findFragmentById(R.id.container);
        }


    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("Title", mTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
