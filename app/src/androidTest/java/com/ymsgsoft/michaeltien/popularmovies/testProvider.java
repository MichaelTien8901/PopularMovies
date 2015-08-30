package com.ymsgsoft.michaeltien.popularmovies;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;

import com.ymsgsoft.michaeltien.popularmovies.data.MovieContract.MovieEntry;

import java.util.Map;
import java.util.Set;

/**
 * Created by Michael Tien on 2015/8/29.
 */
public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();
    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                MovieEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }
    public void setUp() {
        deleteAllRecords();
    }
    // The target api annotation is needed for the call to keySet -- we wouldn't want
    // to use this in our app, but in a test it's fine to assume a higher target.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void addAllContentValues(ContentValues destination, ContentValues source) {
        for (String key : source.keySet()) {
            destination.put(key, source.getAsString(key));
        }
    }
    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(MovieEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.popularmovies/movie
        assertEquals(MovieEntry.CONTENT_TYPE, type);

        long movie_id = 94074;
        // content://com.example.android.popularmovies/movie/94074
        type = mContext.getContentResolver().getType(
                MovieEntry.buildMovieUri(movie_id));
        assertEquals(MovieEntry.CONTENT_ITEM_TYPE, type);
    }
    public void testInsertDbProvider() {
        long movie_id = 94073;
        ContentValues testValues = createMovieValues(movie_id);
        Uri movieInsertUri = getContext().getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);
        assertTrue(movieInsertUri != null);
        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        validateCursor(cursor, testValues);

        cursor = mContext.getContentResolver().query(
                MovieEntry.buildMovieUri(movie_id),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        validateCursor(cursor, testValues);
    }
    public void testDeleteDbProvider(){
        long movie_id = 94073;
        ContentValues testValues = createMovieValues(movie_id);
        Uri movieInsertUri = getContext().getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);
        assertTrue(movieInsertUri != null);
        int rowCount = mContext.getContentResolver().delete(
                MovieEntry.CONTENT_URI,
                null, // cols for "where" clause
                null // values for "where" clause
            );
        assertEquals(rowCount, 1);
        movieInsertUri = getContext().getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);
        assertTrue(movieInsertUri != null);
        rowCount = mContext.getContentResolver().delete(
                MovieEntry.buildMovieUri(movie_id),
                null, // cols for "where" clause
                null // values for "where" clause
        );
        assertEquals(rowCount, 1);
    }
    public void testDeleteRecordsAtEnd() {
        deleteAllRecords();
    }

    static ContentValues createMovieValues(long id) {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieEntry.COLUMN_MOVIE_ID, id );
        movieValues.put(MovieEntry.COLUMN_TITLE, "movie title" );
        movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, "movie original title" );
        movieValues.put(MovieEntry.COLUMN_OVERVIEW, "movie overview" );
        movieValues.put(MovieEntry.COLUMN_POSTER_PATH, "movie poster" );
        movieValues.put(MovieEntry.COLUMN_BACKDROP_PATH, "movie backdrop" );
        movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, "2015-01-01" );
        movieValues.put(MovieEntry.COLUMN_POPULARITY, 6.7 );
        movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, 5.1);
        return movieValues;
    }
    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

}
