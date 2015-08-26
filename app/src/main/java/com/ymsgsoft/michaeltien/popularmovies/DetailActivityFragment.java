package com.ymsgsoft.michaeltien.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();
        String prefix = getString(R.string.package_prefix);
        String title = intent.getStringExtra(prefix + getString(R.string.intent_key_original_title));
        String poster_url = getString(R.string.picture_url_prefix) + intent.getStringExtra(prefix+getString(R.string.intent_key_poster_path ));
        String overview = intent.getStringExtra(prefix + getString(R.string.intent_key_overview));
        String release_date = intent.getStringExtra(prefix + getString(R.string.intent_key_release_date));
        Double vote_average = intent.getDoubleExtra(prefix + getString(R.string.intent_key_vote_average), 0);

        Picasso.with(getActivity()).load(poster_url).into((ImageView) rootView.findViewById( R.id.detail_imageView));
        // loading
        TextView textView = (TextView) rootView.findViewById(R.id.detail_textview);
        textView.setText(title);
        ((TextView) rootView.findViewById(R.id.overview_textView)).setText(overview);
        ((TextView) rootView.findViewById(R.id.release_date_text_view)).setText(release_date.substring(0,4));
        ((TextView) rootView.findViewById(R.id.rating_text_view)).setText( String.format("%.1f / 10", vote_average));
        return rootView;
    }
}
