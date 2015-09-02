package com.ymsgsoft.michaeltien.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
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
public class DetailActivityFragment extends Fragment {
    public static final String DETAIL_MOVIE_ID = "DETAIL_MOVIE_ID";
    public static final String DETAIL_REVIEWS = "DETAIL_REVIEWS";
    public static final String DETAIL_TRAILERS = "DETAIL_TRAILERS";
    @Bind(R.id.detail_title_textView) TextView titleTextView;
    @Bind(R.id.overview_textView) TextView overviewTextView;
    @Bind(R.id.release_date_text_view) TextView dateTextView;
    @Bind(R.id.rating_text_view) TextView rateTextView;
    @Bind(R.id.favorite_checkbox) CheckBox favoriteCheckBox;
    @Bind(R.id.review_textView) TextView reviewTextView;
    @Bind(R.id.detail_linearLayout)  LinearLayout detailLinearLayout;
    private Button mTrailerButton;
    //private CheckedTextView favoriteTextView;
    private final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static FetchMovieExtraTask mTask = null;
    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);
        if ( savedInstanceState != null && savedInstanceState.containsKey(DETAIL_MOVIE_ID)) {
            mExtraData = new MovieExtraData();
            mExtraData.movie_id = savedInstanceState.getString(DETAIL_MOVIE_ID);
            mExtraData.trailers = savedInstanceState.getStringArrayList(DETAIL_TRAILERS);
            mExtraData.reviews = savedInstanceState.getStringArrayList(DETAIL_REVIEWS);
        }
        /*
        String intent_key_prefix = getString(R.string.package_prefix);
        Intent intent = getActivity().getIntent();
        final MovieObject movieObject = intent.getParcelableExtra(intent_key_prefix + getString(R.string.intent_key_movie_object));
        */
        String ARG_ITEM_ID = getString(R.string.package_prefix) + getString(R.string.intent_key_movie_object);
        Bundle arguments = getArguments();
        if (arguments == null) return rootView;

        final MovieObject movieObject = arguments.getParcelable(ARG_ITEM_ID);
        favoriteCheckBox.setOnClickListener(new CompoundButton.OnClickListener() {

            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("FavoriteDirty", true);
                editor.commit();

                if (!favoriteCheckBox.isChecked()) {
                    // delete from list
                    int rowCount = getActivity().getContentResolver().delete(MovieEntry.buildMovieUri(Integer.parseInt(movieObject.id_string)), null, null);
                } else {
                    // add to list
                    ContentValues values = createMovieValues(movieObject);
                    Uri movieInsertUri = getActivity().getContentResolver().insert(MovieEntry.CONTENT_URI, values);
                }
            }

            private ContentValues createMovieValues(MovieObject mv) {
                ContentValues movieValues = new ContentValues();
                movieValues.put(MovieEntry.COLUMN_MOVIE_ID, Integer.parseInt(mv.id_string));
                movieValues.put(MovieEntry.COLUMN_TITLE, mv.title);
                movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, mv.original_tile);
                movieValues.put(MovieEntry.COLUMN_OVERVIEW, mv.overview);
                movieValues.put(MovieEntry.COLUMN_POSTER_PATH, mv.poster_path);
                movieValues.put(MovieEntry.COLUMN_BACKDROP_PATH, mv.backdrop_path);
                movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, mv.release_date);
                movieValues.put(MovieEntry.COLUMN_POPULARITY, mv.popularity);
                movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, mv.vote_average);
                return movieValues;
            }
        });

        // prevent multiple AsyncTask running
        if ( mTask != null) {
            if ( mTask.getStatus() != AsyncTask.Status.FINISHED )
                mTask.cancel(true);
        }
        mTask = new FetchMovieExtraTask();
        mTask.execute(movieObject.id_string);

        String post_url = getString(R.string.picture_url_prefix) + movieObject.poster_path;
        String backdrop_url = getString(R.string.backdrop_url_prefix) + movieObject.backdrop_path;
                Picasso.with(getActivity())
                .load(post_url)
                .placeholder(R.drawable.loading)
                .error(R.drawable.nomovie)
                .into((ImageView) rootView.findViewById(R.id.detail_imageView));
        Picasso.with(getActivity())
                .load(backdrop_url)
                .placeholder(R.drawable.loading)
                .error(R.drawable.nomovie)
                .into((ImageView) rootView.findViewById(R.id.detail_backdrop_imageView));
        // loading
        titleTextView.setText(movieObject.title);
        overviewTextView.setText(movieObject.overview);
        dateTextView.setText(movieObject.release_date.substring(0, 4));
        rateTextView.setText(String.format("%.1f / 10", movieObject.vote_average));
        mTrailerButton = (Button) rootView.findViewById(R.id.trailer_button);
        mTrailerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String source = (String) mTrailerButton.getTag();
                Log.v(LOG_TAG, "trailer click source: " + source);
                OpenMediaPlayer(source);
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if ( mExtraData != null) {
            outState.putString(DETAIL_MOVIE_ID, mExtraData.movie_id);
                    outState.putStringArrayList(DETAIL_REVIEWS, mExtraData.reviews);
            outState.putStringArrayList(DETAIL_TRAILERS, mExtraData.trailers);
        }
    }

    private void OpenMediaPlayer(String path) {
        String youtube_prefix = "https://www.youtube.com/v/";
        // String youtube_prefix = "http://www.youtube.com/watch?v=";
        Intent mediaIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtube_prefix+path));
        if (mediaIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(mediaIntent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + path + ", no receiving apps installed!");
        }
    }
    private class MovieExtraData {
        ArrayList<String> trailers;
        ArrayList<String> reviews;
        Boolean isFavorite;
        String movie_id;
        public MovieExtraData()
        {
            this("");
        }
        public MovieExtraData(String id) {
            trailers = new ArrayList<String>();
            reviews = new ArrayList<String>();
            isFavorite = false;
            movie_id = id;
        }
    };
    private MovieExtraData mExtraData;

    public class FetchMovieExtraTask extends AsyncTask<String, Void, MovieExtraData> {
        private final String LOG_TAG = FetchMovieExtraTask.class.getSimpleName();
        @Override
        protected MovieExtraData doInBackground(String... params) {
            MovieExtraData retData;
            String id_string = params[0];
            if (mExtraData != null && id_string.equals(mExtraData.movie_id)) {
                retData = mExtraData;
            } else {
                retData = new MovieExtraData(id_string); // given initial value prevent exception
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                // Will contain the raw JSON response as a string.
                String movieExtraDataJsonStr = null;
                try {
                    //  http://api.themoviedb.org/3/movie/102899?api_key=[key]&append_to_response=trailers,reviews
                    final String MOVIEDB_BASE_URL =
                            "http://api.themoviedb.org/3/movie";
                    final String API_KEY_PARAM = "api_key";
                    String api_key = getString(R.string.API_key);
                    final String EXTRA_PARAM = "append_to_response";
                    final String extra_data = "trailers,reviews";

                    Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                            .appendPath(id_string)
                            .appendQueryParameter(API_KEY_PARAM, api_key)
                            .appendQueryParameter(EXTRA_PARAM, extra_data)
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
                        throw new IOException("can't open IO Stream");
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
                        throw new IOException("stream empty");
                    }
                    movieExtraDataJsonStr = buffer.toString();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attemping
                    // to parse it.
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
                    if (movieExtraDataJsonStr != null) {
                        retData = getMovieExtraDataFromJson(movieExtraDataJsonStr);
                        retData.movie_id = id_string;
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
            // isFarorite value using persistent storage
            try {
                Cursor cursor = getContext().getContentResolver().query(
                        MovieEntry.buildMovieUri(Integer.parseInt(id_string)),
                        null,
                        null,
                        null,
                        null
                );
                retData.isFavorite = (cursor.getCount() >= 1);
                cursor.close();
                return retData;
            } catch (Exception e) {
                return null;
            }
        }
        private MovieExtraData getMovieExtraDataFromJson(String jsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_TRAILERS = "trailers";
            final String OWM_REVIEWS = "reviews";
            final String OWM_YOUTUBE = "youtube";
            final String OWN_TYPE = "type";
            final String OWN_SOURCE = "source";
            final String OWN_RESULTS = "results";
            final String OWN_CONTENT = "content";
            MovieExtraData movieExtra = new MovieExtraData();

            JSONObject movieExtraJson = new JSONObject(jsonStr);
            JSONObject trailers = movieExtraJson.getJSONObject(OWM_TRAILERS);
            JSONObject review = movieExtraJson.getJSONObject(OWM_REVIEWS);
            JSONArray youtube = trailers.getJSONArray(OWM_YOUTUBE);
            JSONArray review_results = review.getJSONArray(OWN_RESULTS);
            List<String> tl = new ArrayList<String>();
            for (int i = 0; i < youtube.length(); i++) {
                JSONObject movie = youtube.getJSONObject(i);
                String type = movie.getString(OWN_TYPE);
                if ( type.equals( "Trailer")) {
                    tl.add(movie.getString(OWN_SOURCE));
                }
            }
            movieExtra.trailers.clear();
            for (int i = 0; i <tl.size() ; i++) {
                movieExtra.trailers.add(tl.get(i));
            }

            movieExtra.reviews.clear();
            for (int i = 0; i < review_results.length(); i++) {
                movieExtra.reviews.add(review_results.getJSONObject(i).getString(OWN_CONTENT));
            }
            for (String s : movieExtra.reviews) {
                Log.v(LOG_TAG, "review: " + s);
            }
            return movieExtra;
        }
        @Override
        protected void onPostExecute(MovieExtraData result) {
            if (result != null) {
                favoriteCheckBox.setChecked(result.isFavorite);
                mExtraData = result;
                if (result.trailers.size() > 0) {
                    mTrailerButton.setClickable(true);
                    mTrailerButton.setTag(result.trailers.get(0));
                } else {
                    mTrailerButton.setText("No Trailer");
                    mTrailerButton.setClickable(false);
                }
                if (result.reviews.size() > 0) {
                    addReviewText(result.reviews);
                } else {
                    reviewTextView.setVisibility(View.INVISIBLE);
                }
            } else {
                mTrailerButton.setVisibility(View.INVISIBLE);
                // nothing retrieved, show error
                Context context = getActivity();
                if (context != null) {
                    CharSequence text = context.getString(R.string.server_error);
                    int duration = Toast.LENGTH_LONG;
                    Toast.makeText(context, text, duration).show();
                }
            }
            DetailActivityFragment.mTask = null;
        }
    }
    public void addReviewText(List<String> texts) {
        Context context = getActivity();
        for (String s: texts) {
                // add separator
            View line = new View(context);
            line.setBackgroundColor(Color.DKGRAY);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 4);
            lp.topMargin = 10;
            lp.bottomMargin = 10;
            lp.leftMargin = 5;
            lp.rightMargin = 5;
            detailLinearLayout.addView(line, lp);

            TextView tv = new TextView(context);
            tv.setText(s);
            detailLinearLayout.addView(tv);
        }
    }
}
