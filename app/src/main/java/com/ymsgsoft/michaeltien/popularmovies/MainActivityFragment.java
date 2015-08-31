package com.ymsgsoft.michaeltien.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.ymsgsoft.michaeltien.popularmovies.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    @Bind(R.id.gridView) GridView gridView;

    private MovieAdapter mMovieAdapter;
    private String previousSortOrder = null;
    final static String MOVIE_LIST_KEY = "movie_list";
    final static String SORT_KEY = "sort";
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_TITLE = 2;
    public static final int COL_ORIGINAL_TITLE = 3;
    public static final int COL_OVERVIEW = 4;
    public static final int COL_POSTER_PATH = 5;
    public static final int COL_BACKDROP_PATH = 6;
    public static final int COL_RELEASE_DATE = 7;
    public static final int COL_POPULARITY = 8;
    public static final int COL_VOTE_AVERAGE = 9;

    private void processDirtyFlag() {
        MainActivity mainActivity = (MainActivity) getActivity();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        Boolean dirty_flag = sharedPref.getBoolean("FavoriteDirty", false);
        if ( dirty_flag) {
            sharedPref.edit().putBoolean("FavoriteDirty", false).commit();
            if (mainActivity.menu_selected == 1 ) {
                mMoveList = null;
            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);
        MainActivity mainActivity = (MainActivity) getActivity();
        mMovieAdapter =
                new MovieAdapter(
                        mainActivity, // The current context (this activity)
                        R.layout.list_item_movie, // The name of the layout ID.
                        new ArrayList<MovieObject>());

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                MovieObject movie_object = mMovieAdapter.getItem(position);
                Intent detail_intent = new Intent(getActivity(), DetailActivity.class);
                String prefix = getString(R.string.package_prefix);
                // putExtra a parcel movie object
                detail_intent.putExtra(prefix + getString(R.string.intent_key_movie_object), movie_object);
                startActivity(detail_intent);
            }
        });
        gridView.setAdapter(mMovieAdapter);
        //UpdateMoviesList();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save movie list json
        //if ( popularMovieJsonStr != null) {
        if ( mMoveList != null) {
            //outState.putCharSequence(MOVIE_LIST_KEY, popularMovieJsonStr);
            outState.putParcelableArrayList(MOVIE_LIST_KEY, (ArrayList<? extends Parcelable>) mMoveList);
            outState.putCharSequence(SORT_KEY, previousSortOrder);
            super.onSaveInstanceState(outState);
        }
    }
    private List<MovieObject> mMoveList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mMoveList = (List<MovieObject>) savedInstanceState.get(MOVIE_LIST_KEY);
            previousSortOrder = savedInstanceState.getString(SORT_KEY);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        processDirtyFlag();
        UpdateMoviesList();
    }
    private MovieObserver mMovieObserver;
    @Override
    public void onResume() {
        super.onResume();
        // for two panel only
        mMovieObserver = new MovieObserver(null);
        getActivity().getContentResolver().registerContentObserver(
                MovieEntry.CONTENT_URI, true, mMovieObserver );

    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getContentResolver().unregisterContentObserver( mMovieObserver);
    }

    public void UpdateMoviesList() {
        if (((MainActivity) getActivity()).menu_selected == 0 ) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sort_by = sharedPref.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popularity));
            if (!sort_by.equals(previousSortOrder)) {
                mMoveList = null;
            }
            previousSortOrder = sort_by;
            new FetchMovieTask().execute(sort_by);
        } else {
            String sort_by = "favorite";
            if (!sort_by.equals(previousSortOrder)) {
                mMoveList = null;
            }
            previousSortOrder = sort_by;
            new FetchFavoriteTask().execute();
        }
    }
    public class FetchFavoriteTask extends AsyncTask<String, Void, Cursor> {
        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        @Override
        protected Cursor doInBackground(String... params) {
            if ( mMoveList != null ) return null;
            Cursor cursor = getActivity()
                    .getContentResolver()
                    .query(MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);
            return cursor;
        }
        List<MovieObject> getListMovieObjectFromCursor(Cursor cursor)
        {
            List<MovieObject> result = new ArrayList<MovieObject>();
            if ( cursor.moveToFirst()) {
               do {
                   MovieObject mv = new MovieObject();
                   mv.id_string = Integer.toString(cursor.getInt(COL_MOVIE_ID));
                   mv.title = cursor.getString(COL_TITLE);
                   mv.original_tile = cursor.getString(COL_ORIGINAL_TITLE);
                   mv.overview = cursor.getString(COL_OVERVIEW);
                   mv.poster_path = cursor.getString(COL_POSTER_PATH);
                   mv.backdrop_path = cursor.getString(COL_BACKDROP_PATH);
                   mv.release_date = cursor.getString(COL_RELEASE_DATE);
                   mv.popularity = cursor.getDouble(COL_POPULARITY);
                   mv.vote_average = cursor.getDouble(COL_VOTE_AVERAGE);
                   result.add(mv);
               } while( cursor.moveToNext());
               return result;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor != null) {
                // create List<MoveObject> for cursor
                List<MovieObject> result = getListMovieObjectFromCursor( cursor);
                mMovieAdapter.clear();
                mMovieAdapter.addAll(result);
                mMoveList = result;
                cursor.close();
            } else
            if (mMoveList != null ) {
                mMovieAdapter.clear();
                mMovieAdapter.addAll(mMoveList);
            } else {
                // nothing retrieved, show error
                Context context = getActivity();
                CharSequence text = "Can't get favorite list";
                int duration = Toast.LENGTH_LONG;
                Toast.makeText(context, text, duration).show();
            }
        }

    }
    public class FetchMovieTask extends AsyncTask<String, Void, List<MovieObject>> {
        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        @Override
        protected List<MovieObject> doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String popularMovieJsonStr = null;
            // Will contain the raw JSON response as a string.
            if ( mMoveList != null ) return null;
            try {
                // http://openweathermap.org/API#forecast
                final String MOVIEDB_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";
                String api_key = getString(R.string.API_key);
                // sort string from preference settings
                //String sort_by = sharedPref.getString(getString(R.string.pref_sort_key), "popularity.desc");
                String sort_by = params[0];

                Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, sort_by)
                        .appendQueryParameter(API_KEY_PARAM, api_key)
                        .build();
                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());
                // Create the request to themoviedb.org, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                popularMovieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getMovieDataFromJson(popularMovieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
            return null;
        }
        private List<MovieObject> getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESULTS = "results";
            final String OWM_TITLE = "title";
            final String OWN_OVERVIEW = "overview";
            final String OWN_ID = "id";
            final String OWN_ORIGINAL_TITLE = "original_title";
            final String OWN_POSTER_PATH = "poster_path";
            final String OWN_BACKDROP_PATH = "backdrop_path";
            final String OWN_RELEASE_DATE = "release_date";
            final String OWN_POPULARITY = "popularity";
            final String OWN_VOTE_AVERAGE = "vote_average";
            //final String OWN_ADULT = "adult";
            //final String OWN_PAGE = "page";
            //final String OWN_TOTAL_PAGES = "total_pages";
            //final String OWN_TOTAL_RESULTS = "total_results";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OWM_RESULTS);
            //int page = movieJson.getInt(OWN_PAGE);
            //int total_pages = movieJson.getInt( OWN_TOTAL_PAGES);
            //int total_results = movieJson.getInt( OWN_TOTAL_RESULTS);

           List<MovieObject> results = new ArrayList<MovieObject>();
            for (int i = 0; i < movieArray.length(); i++) {
                // Get the JSON object representing one movie
                MovieObject mobj = new MovieObject();
                JSONObject movie = movieArray.getJSONObject(i);
                mobj.id_string = movie.getString(OWN_ID);
                mobj.title = movie.getString(OWM_TITLE);
                mobj.overview = movie.isNull(OWN_OVERVIEW)? "--- No Overview ---": movie.getString(OWN_OVERVIEW);
                mobj.original_tile = movie.isNull(OWN_ORIGINAL_TITLE)? "--- No Title ---": movie.getString(OWN_ORIGINAL_TITLE);
                mobj.poster_path = movie.getString(OWN_POSTER_PATH);
                mobj.backdrop_path = movie.getString(OWN_BACKDROP_PATH);
                mobj.release_date = movie.getString(OWN_RELEASE_DATE);
                mobj.popularity = movie.getDouble(OWN_POPULARITY);
                mobj.vote_average = movie.getDouble(OWN_VOTE_AVERAGE);
                results.add(mobj);
            }

            for (MovieObject s : results) {
                Log.v(LOG_TAG, "movie entry: " + s.title);
            }
            return results;
        }
        @Override
        protected void onPostExecute(List<MovieObject> result) {
            if (result != null) {
                mMovieAdapter.clear();
                mMovieAdapter.addAll(result);
                mMoveList = result;
            } else
            if (mMoveList != null ) {
                mMovieAdapter.clear();
                mMovieAdapter.addAll(mMoveList);
            } else {
                // nothing retrieved, show error
                Context context = getActivity();
                CharSequence text = context.getString(R.string.server_error);
                int duration = Toast.LENGTH_LONG;
                Toast.makeText(context, text, duration).show();
            }
        }
    }
    public class MovieObserver extends ContentObserver {
        private final String LOG_TAG = MovieObserver.class.getSimpleName();
        public MovieObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            // two panel mode, update favorite grid view
            this.onChange(selfChange, null);
            Log.v(LOG_TAG, "OnChange");
            MainActivityFragment fm= (MainActivityFragment)getActivity()
                    .getSupportFragmentManager()
                    .findFragmentById(R.id.container);
            new FetchFavoriteTask();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.v(LOG_TAG, "OnChange with Uri");

        }
    }
}
