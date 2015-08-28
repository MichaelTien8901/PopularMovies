package com.ymsgsoft.michaeltien.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    private Button mTrailerButton;
    private final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();
        String intent_key_prefix = getString(R.string.package_prefix);
        String movie_id;
        movie_id = intent.getStringExtra(intent_key_prefix + getString(R.string.intent_key_id));
        new FetchMovieExtraTask().execute(movie_id);

        String title = intent.getStringExtra(intent_key_prefix + getString(R.string.intent_key_original_title));
        String poster_url = getString(R.string.picture_url_prefix) + intent.getStringExtra(intent_key_prefix+getString(R.string.intent_key_poster_path ));
        String backdrop_url = getString(R.string.backdrop_url_prefix) + intent.getStringExtra(intent_key_prefix+getString(R.string.intent_key_backdrop_path ));
        String overview = intent.getStringExtra(intent_key_prefix + getString(R.string.intent_key_overview));
        String release_date = intent.getStringExtra(intent_key_prefix + getString(R.string.intent_key_release_date));
        Double vote_average = intent.getDoubleExtra(intent_key_prefix + getString(R.string.intent_key_vote_average), 0);

        Picasso.with(getActivity())
                .load(poster_url)
                .placeholder(R.drawable.loading)
                .error(R.drawable.nomovie)
                .into((ImageView) rootView.findViewById(R.id.detail_imageView));
        Picasso.with(getActivity())
                .load(backdrop_url)
                .placeholder(R.drawable.loading)
                .error(R.drawable.nomovie)
                .into((ImageView) rootView.findViewById(R.id.detail_backdrop_imageView));
        // loading
        TextView textView = (TextView) rootView.findViewById(R.id.detail_title_textView);
        textView.setText(title);
        ((TextView) rootView.findViewById(R.id.overview_textView)).setText(overview);
        ((TextView) rootView.findViewById(R.id.release_date_text_view)).setText(release_date.substring(0, 4));
        ((TextView) rootView.findViewById(R.id.rating_text_view)).setText(String.format("%.1f / 10", vote_average));
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
        String trailer;
        String[] reviews;
    };

    public class FetchMovieExtraTask extends AsyncTask<String, Void, MovieExtraData> {
        private final String LOG_TAG = FetchMovieExtraTask.class.getSimpleName();
        @Override
        protected MovieExtraData doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieExtraDataJsonStr = null;

            try {
                //  http://api.themoviedb.org/3/movie/102899?api_key=[key]&append_to_response=trailers,reviews
                final String MOVIEDB_BASE_URL =
                        "http://api.themoviedb.org/3/movie";
                String id_string = params[0];
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
                movieExtraDataJsonStr = buffer.toString();
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
                return getMovieExtraDataFromJson(movieExtraDataJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }

            return null;
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

            for (int i = 0; i < youtube.length(); i++) {
                JSONObject movie = youtube.getJSONObject(i);
                String type = movie.getString(OWN_TYPE);
                if ( type.equals( "Trailer")) {
                    movieExtra.trailer = movie.getString(OWN_SOURCE);
                    break;
                }
            }
            movieExtra.reviews = new String[review_results.length()];
            for (int i = 0; i < review_results.length(); i++) {
                movieExtra.reviews[i] = review_results.getJSONObject(i).getString(OWN_CONTENT);
            }

            Log.v( LOG_TAG, "trailer: " + movieExtra.trailer);
            for (String s : movieExtra.reviews) {
                Log.v(LOG_TAG, "review: " + s);
            }
            return movieExtra;
        }
        @Override
        protected void onPostExecute(MovieExtraData result) {
            if (result != null) {
                if (result.trailer != null) {
                    mTrailerButton.setText("Trailer");
                    mTrailerButton.setClickable(true);
                    mTrailerButton.setTag((Object) result.trailer);
                } else {
                    mTrailerButton.setText("No Trailer");
                    mTrailerButton.setClickable(false);
                }
                //mMovieAdapter.clear();
                //mMovieAdapter.addAll(result);
            } else {
                //
            }
        }
    }
}
