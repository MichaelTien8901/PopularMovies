package com.ymsgsoft.michaeltien.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    ArrayAdapter<String> mMovieAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        // test using string adapter
        String moviesData[] = {
                "AAA",
                "BBB",
                "CCC",
                "DDD",
                "EEE",
                "FFF",
                "GGG",
                "HHH",
                "III",
                "JJJ",
                "KKK",
                "LLL",
                "MMM",
                "NNN",
                "OOO",
                "PPP",
                "QQQ",
                "RRR",
                "SSS",
                "TTT",
                "UUU",
                "VVV",
                "WWW",
                "XXX",
                "YYY",
                "ZZZ"
        };
        List<String> movieListString = new ArrayList<String>(Arrays.asList(moviesData));
        mMovieAdapter =
        new ArrayAdapter<String>(
           getActivity(), // The current context (this activity)
              R.layout.list_item_movie, // The name of the layout ID.
              R.id.list_item_movie_textview, // The ID of the textview to populate.
              movieListString);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridView);
        gridView.setAdapter(mMovieAdapter);
        new FetchMovieTask().execute();

        return rootView;
    }
    public class FetchMovieTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String popularMovieJsonStr = null;

            try {
                // http://openweathermap.org/API#forecast
                final String MOVIEDB_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";
                String api_key = getString(R.string.API_key);
                Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, "popularity.desc")
                                .appendQueryParameter(API_KEY_PARAM, api_key )
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
        private String[] getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESULTS = "results";
            final String OWM_TITLE = "title";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OWM_RESULTS);

            String[] resultStrs = new String[movieArray.length()];
            for (int i = 0; i < movieArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String title;
                // Get the JSON object representing one movie
                JSONObject movie = movieArray.getJSONObject(i);
                title = movie.getString(OWM_TITLE);
                resultStrs[i] = title;
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "moviet entry: " + s);
            }
            return resultStrs;
        }
        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mMovieAdapter.clear();
                for(String movieStr : result) {
                    mMovieAdapter.add(movieStr);
                }
                // New data is back from the server.  Hooray!
            }
        }

    }
}
