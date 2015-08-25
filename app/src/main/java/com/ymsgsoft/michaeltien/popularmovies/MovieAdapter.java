package com.ymsgsoft.michaeltien.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Michael Tien on 2015/8/24.
 */
public class MovieAdapter extends ArrayAdapter<MovieObject> {
    Context context;
    int layoutResId;
    List<MovieObject> data = null;

    public MovieAdapter(Context context, int resource, List<MovieObject> objects)
    {
        super(context, resource, objects);
        this.context = context;
        this.layoutResId = resource;
        this.data = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieHolder holder;
        if(convertView == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(layoutResId, parent, false);

            holder = new MovieHolder();
            holder.imageIcon = (ImageView)convertView.findViewById(R.id.list_item_imageIcon);
            // 1. measure screen width (pixel),
            // 2. convert to dp:  / (metrics.densityDpi / 160f)
            // 3. and calculate minimum height
            DisplayMetrics metrics = new DisplayMetrics();
            ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

            int height = (int) (metrics.widthPixels / (metrics.densityDpi / 160f) *1.8);
            convertView.setMinimumHeight(height);
            holder.imageIcon.setMinimumHeight(height);
            //holder.textTitle = (TextView)convertView.findViewById(R.id.list_item_movie_textview);
            convertView.setTag(holder);
        }
        else
        {
            holder = (MovieHolder)convertView.getTag();
        }

        MovieObject movieObject = data.get(position);
        //holder.textTitle.setText(movieObject.title);
        String url = "http://image.tmdb.org/t/p/w185/" + movieObject.poster_path;
        Picasso.with(this.context).load(url).into(holder.imageIcon);

        return convertView;

    }

}
class MovieHolder {
    ImageView imageIcon;
    TextView textTitle;
}