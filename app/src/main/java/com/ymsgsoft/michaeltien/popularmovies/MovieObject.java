package com.ymsgsoft.michaeltien.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Michael Tien on 2015/8/24.
 */
class MovieObject implements Parcelable {
    public String id_string;
    public String title;
    public String original_tile;
    public String overview;
    public String poster_path;
    public String backdrop_path;
    public String release_date;
    public Double popularity;
    public Double vote_average;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        String[] data = new String[]{
                this.id_string,
                this.title,
                this.original_tile,
                this.overview,
                this.poster_path,
                this.backdrop_path,
                this.release_date
        };
        dest.writeStringArray(data);
        double[] data2 = new double[]{
                this.popularity,
                this.vote_average
        };
        dest.writeDoubleArray(data2);
    }

    public MovieObject(Parcel in) {
        String[] data = new String[7];
        in.readStringArray(data);
        this.id_string = data[0];
        this.title = data[1];
        this.original_tile = data[2];
        this.overview = data[3];
        this.poster_path = data[4];
        this.backdrop_path = data[5];
        this.release_date = data[6];
        double[] data2 = new double[2];
        in.readDoubleArray(data2);
        this.popularity = data2[0];
        this.vote_average = data2[1];
    }

    public MovieObject() {
    }

    public static final Creator<MovieObject> CREATOR
            = new Creator<MovieObject>() {
        public MovieObject createFromParcel(Parcel in) {
            return new MovieObject(in);
        }

        @Override
        public MovieObject[] newArray(int asize) {
            return new MovieObject[asize];
        }
    };
}
