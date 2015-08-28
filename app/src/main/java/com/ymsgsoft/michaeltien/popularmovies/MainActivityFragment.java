package com.ymsgsoft.michaeltien.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

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

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private MovieAdapter mMovieAdapter;
    private String previousSortOrder = null;
    final static String MOVIE_LIST_KEY = "movie_list";
    final static String SORT_KEY = "sort";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mMovieAdapter =
                new MovieAdapter(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_movie, // The name of the layout ID.
                        new ArrayList<MovieObject>());
        GridView gridView = (GridView) rootView.findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                MovieObject movie_object = mMovieAdapter.getItem(position);
                Intent detail_intent = new Intent(getActivity(), DetailActivity.class);
                String prefix = getString(R.string.package_prefix);
                detail_intent.putExtra(prefix+getString(R.string.intent_key_id), movie_object.id_string );
                detail_intent.putExtra(prefix+getString(R.string.intent_key_original_title), movie_object.original_tile);
                detail_intent.putExtra(prefix+getString(R.string.intent_key_overview), movie_object.overview);
                detail_intent.putExtra(prefix+getString(R.string.intent_key_poster_path), movie_object.poster_path);
                detail_intent.putExtra(prefix+getString(R.string.intent_key_backdrop_path), movie_object.backdrop_path);
                detail_intent.putExtra(prefix+getString(R.string.intent_key_release_date), movie_object.release_date);
                detail_intent.putExtra(prefix+getString(R.string.intent_key_vote_average), movie_object.vote_average);
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
        if ( savedInstanceState != null) {
            //popularMovieJsonStr = savedInstanceState.getString(MOVIE_LIST_KEY);
            mMoveList = (List<MovieObject>) savedInstanceState.get(MOVIE_LIST_KEY);
            previousSortOrder = savedInstanceState.getString(SORT_KEY);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        UpdateMoviesList();
    }

    private void UpdateMoviesList() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_by = sharedPref.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popularity));
        if (!sort_by.equals(previousSortOrder)) {
//            popularMovieJsonStr = null;
            mMoveList = null;
        }
        previousSortOrder = sort_by;
        new FetchMovieTask().execute(sort_by);
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

            //if ( popularMovieJsonStr == null ) {
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
                mobj.overview = movie.getString(OWN_OVERVIEW);
                mobj.original_tile = movie.getString(OWN_ORIGINAL_TITLE);
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
            }
        }
    }
}
