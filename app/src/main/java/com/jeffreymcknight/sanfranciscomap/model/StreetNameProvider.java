package com.jeffreymcknight.sanfranciscomap.model;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.commons.lang3.text.WordUtils;

/**
 * Created by jeffmcknight on 4/9/17.
 */

public class StreetNameProvider extends ContentProvider{
    public static final String TAG = StreetNameProvider.class.getSimpleName();
    public static final int STREETNAME = 1000;
    public static final String MSG_UNKNOWN_URI = "Unknown uri: ";
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private StreetDbHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new StreetDbHelper(getContext());
        return true;
    }


    /**
     * Given a URI, determine what kind of request it is, and query the database accordingly.
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case STREETNAME: {
                retCursor = getCursorForTable(StreetContract.StreetnameEntry.TABLE_NAME, projection, selection, selectionArgs, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return StreetContract.StreetnameEntry.CONTENT_TYPE;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        Log.i(TAG, "insert()"
                + " -- match:" + match
                + " -- uri:" + uri
                + "\n\t -- values:" + values
        );
        switch (match) {
            case STREETNAME: {
                validateContentValue(values, StreetContract.StreetnameEntry.COLUMN_FULLSTREETNAME);
                long _id = db.insert(StreetContract.StreetnameEntry.TABLE_NAME, null, values);
                Log.d(TAG, "insert()"
                        +"\t _id: "+_id
                );
                if ( _id > 0 ){
                    returnUri = StreetContract.StreetnameEntry.buildReturnUri(_id);
                    Log.d(TAG, "insert()"
                            +"\t returnUri: "+returnUri
                    );
                }
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException(MSG_UNKNOWN_URI + uri);
        }
        notifyListeners(uri);
        db.close();
        return returnUri;
    }

    /**
     *
     * @param uri
     * @param values
     * @return
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STREETNAME:
                cleanContentValues(values);
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(StreetContract.StreetnameEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                notifyListeners(uri);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    /**
     * Switch all street names to title case
     * @param values
     */
    private void cleanContentValues(ContentValues[] values) {
        for (ContentValues eachValue : values){
            convertStreetNameToTitleCase(eachValue);
        }
    }

    /**
     *
     * @param eachValue
     */
    private void convertStreetNameToTitleCase(ContentValues eachValue) {
        String streetName = WordUtils.capitalizeFully(
                eachValue.getAsString(StreetContract.StreetnameEntry.COLUMN_FULLSTREETNAME));
        eachValue.put(StreetContract.StreetnameEntry.COLUMN_FULLSTREETNAME, streetName);
    }

    /**
     * Use the uriMatcher to match the {@link StreetContract.StreetnameEntry} URI's we are going
     * to handle.  If it doesn't match these, throw an UnsupportedOperationException.
     *
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rowsDeleted = 0;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int matchCode = sUriMatcher.match(uri);

        // We always substitute a "1" for a null selection parameter because this will cause a
        // SQLiteDatabase to perform the same action (delete all rows), but return a useful
        // value (number of rows deleted), instead of 0.
        String whereClause;
        if (selection == null) {
            whereClause = "1";
        } else {
            whereClause = selection;
        }
        switch (matchCode){
            case STREETNAME:
                rowsDeleted = db.delete(StreetContract.StreetnameEntry.TABLE_NAME, whereClause, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(MSG_UNKNOWN_URI + uri);
        }

        // only notify the uri listeners if the rowsDeleted != 0
        if(rowsDeleted != 0 ){
            notifyListeners(uri);
        }
        db.close();

        return rowsDeleted;
    }

    /**
     *
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated=0;
        int matchCode = sUriMatcher.match(uri);
        switch (matchCode){
            case STREETNAME:
                rowsUpdated = db.update(StreetContract.StreetnameEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(MSG_UNKNOWN_URI+uri);
        }
        if (rowsUpdated > 0) {
            notifyListeners(uri);
        }
        db.close();

        return rowsUpdated;
    }

    /**
     * Do a simple query with whatever query parameters we get
     *
     * @param tableName
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param orderBy
     * @return
     */
    private Cursor getCursorForTable(String tableName, String[] columns, String selection, String[] selectionArgs, String orderBy) {
        SQLiteDatabase database = mOpenHelper.getReadableDatabase();
        return database.query(tableName, columns, selection, selectionArgs, null, null, orderBy);
    }

    /**
     *
     * @param uri
     */
    private void notifyListeners(@NonNull Uri uri) {
        if (getContext() != null){
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    /**
     * Log a warning if a required key is missing
     * @param value
     * @param keyName
     */
    private void validateContentValue(ContentValues value, String keyName) {
        if (value.containsKey(keyName)) {
            convertStreetNameToTitleCase(value);
        }
        Log.w(TAG, "validateLocationContentValues()"
                + " *** DOES NOT CONTAIN KEY: " + keyName
        );
    }

    /**
     * Use the addURI function to match each of the types.  Use the constants from
     * {@link StreetContract} to help define the types to the UriMatcher.
     */
    public static UriMatcher buildUriMatcher() {
        // The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(
                StreetContract.CONTENT_AUTHORITY,
                StreetContract.StreetnameEntry.PATH,
                StreetNameProvider.STREETNAME);
        return uriMatcher;
    }
}
