package com.ymsgsoft.michaeltien.popularmovies;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.ymsgsoft.michaeltien.popularmovies.data.MovieContract.MovieEntry;
import com.ymsgsoft.michaeltien.popularmovies.data.MovieDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by Michael Tien on 2014/10/31.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();
    static final String TEST_MOVIE = "99705";

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new MovieDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }
    public void testInsertReadDb() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues movieValues = createMovieValues();

        long movieRowId = db.insert(MovieEntry.TABLE_NAME, null, movieValues);
        assertTrue(movieRowId != -1);

        // A cursor is your primary interface to the query results.
        Cursor movieCursor = db.query(
                MovieEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        validateCursor(movieCursor, movieValues);

        dbHelper.close();
    }

    static ContentValues createMovieValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieEntry.COLUMN_MOVIE_ID, 12345 );
        movieValues.put(MovieEntry.COLUMN_TITLE, "movie title" );
        movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, "movie original title" );
        movieValues.put(MovieEntry.COLUMN_OVERVIEW, "movie overview" );
        movieValues.put(MovieEntry.COLUMN_POSTER_PATH, "movie poster" );
        movieValues.put(MovieEntry.COLUMN_BACKDROP_PATH, "movie backdrop" );
        movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, "2015-01-01" );
        movieValues.put(MovieEntry.COLUMN_POPULARITY, 6.7 );
        movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, 5.1 );

        return movieValues;
    }


    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            switch( valueCursor.getType(idx)) {
                case Cursor.FIELD_TYPE_STRING:
                case Cursor.FIELD_TYPE_INTEGER:
                case Cursor.FIELD_TYPE_FLOAT:
                    // the getValue always return back string value.  So the test can't use 5.0 for real type
                    String expectedValue = entry.getValue().toString();
                    assertEquals(expectedValue, valueCursor.getString(idx));
                    break;
            }
        }
        valueCursor.close();
    }
}
