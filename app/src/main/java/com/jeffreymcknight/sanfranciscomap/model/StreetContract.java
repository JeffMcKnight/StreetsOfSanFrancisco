package com.jeffreymcknight.sanfranciscomap.model;

/**
 * Created by jeffmcknight on 4/9/17.
 */

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Defines table and column names for the weather database.
 */
public class StreetContract {

    public static final String DATABASE_NAME = "street.db";
    public static final String CONTENT_SCHEME = "content";
    public static final String CONTENT_AUTHORITY = "com.jeffreymcknight.sanfranciscomap";
    private static final Uri BASE_CONTENT_URI = (new Uri.Builder())
            .scheme(CONTENT_SCHEME)
            .authority(CONTENT_AUTHORITY)
            .build();
    public static final String SORT_ORDER_ASCENDING = "ASC";
    public static final String DECENDING = "DEC";

    /**
     * Inner class that defines the table contents of the streetname table
     */
    public static final class StreetnameEntry implements BaseColumns {
        private static final String TAG = StreetnameEntry.class.getSimpleName();
        public static final String TABLE_NAME = "street_name";
        public static final String PATH = TABLE_NAME;
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();
        /**  The name of the columns in the location database */
        public static final String COLUMN_FULLSTREETNAME = "fullstreetname";
        public static final int INDEX_FULLSTREETNAME = 1;
        public static final String[] COLUMN_NAMES = {COLUMN_FULLSTREETNAME};
        private static final String KEY = "id";
        /** The MIME type */
        public static final String CONTENT_TYPE = "x-" + TABLE_NAME;

        public static Uri buildAllStreetsUri(){
            return CONTENT_URI;
        }

        /**
         * Use this method to build a URI to query the {@link #TABLE_NAME} table by ID
         * @param id
         * @return
         */
        public static Uri buildStreetUri(long id){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();

        }

        /**
         * Use this method to build a URI to query the {@link #TABLE_NAME} table by street name
         * @param fullStreetName
         * @return
         */
        public static Uri buildStreetUri(String fullStreetName) {
            return CONTENT_URI.buildUpon().appendPath(fullStreetName).build();
        }

        /**
         * Builds a URI that indicates the table and row affected by the {@link android.content.ContentProvider}
         * @param id
         * @return
         */
        public static Uri buildReturnUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }

        /**
         *
         * @param uri
         * @return
         */
        public static long getRowIdFromReturnUri(Uri uri){
            Long rowId = 0L;
            String lastPathSegment = null;
            if (StreetNameProvider.buildUriMatcher().match(uri) == StreetNameProvider.STREETNAME){
                lastPathSegment = uri.getLastPathSegment();
                rowId = Long.parseLong(lastPathSegment);
            }
            Log.i(TAG, "getRowIdFromReturnUri()"
                    +"\t -- uri: "+uri
                    +"\t -- lastPathSegment: "+lastPathSegment
                    +"\t -- rowId: "+rowId
            );
            try {
                if (rowId == null){
                    throw new ContractViolationException("*** Last path segment should be a long *** lastPathSegment: " + lastPathSegment);
                }
            } catch (ContractViolationException e) {
                e.printStackTrace();
            }
            return rowId;
        }

        /**
         *
         * @param fullStreetName
         * @return
         */
        @NonNull
        public static ContentValues buildContentValues(@NonNull String fullStreetName) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_FULLSTREETNAME, fullStreetName);
            return contentValues;
        }
    }

    private static class ContractViolationException extends Exception {
        public ContractViolationException(String detailMessage) {
            super(detailMessage);
        }
    }
}
