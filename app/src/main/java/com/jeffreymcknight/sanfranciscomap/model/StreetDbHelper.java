package com.jeffreymcknight.sanfranciscomap.model;

/**
 * Created by jeffmcknight on 4/9/17.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Manages a local database for weather data.
 */
public class StreetDbHelper extends SQLiteOpenHelper {
    private static final String TAG = StreetDbHelper.class.getSimpleName();

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    public StreetDbHelper(Context context) {
        super(context, StreetContract.DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createStreetNameTable(sqLiteDatabase);
    }

    private void createStreetNameTable(SQLiteDatabase sqLiteDatabase) {
        TableBuilder locTableBuilder = new TableBuilder(StreetContract.StreetnameEntry.TABLE_NAME)
                .addColumn(StreetContract.StreetnameEntry.COLUMN_FULLSTREETNAME, TableBuilder.SQL_TYPE_TEXT, TableBuilder.CONSTRAINT_UNIQUE, TableBuilder.CONSTRAINT_NOT_NULL)
                ;

        Log.i(TAG, "createStreetNameTable()"
//                        + "\n\t -- locationTableBuilder.toString(): " + locationTableBuilder.toString()
                        + "\n\t -- locTableBuilder.build():         " + locTableBuilder.build()
        );
        sqLiteDatabase.execSQL(locTableBuilder.build());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StreetContract.StreetnameEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void printTable(Cursor cursor) {
        boolean moveSucceeded = false;
        int columnCount = cursor.getColumnCount();
        int rowCount = cursor.getCount();
        Log.i(TAG, "printTable()"
                +"\t -- columnCount: "+columnCount
                +"\t -- rowCount: "+rowCount
        );
        if (cursor.isBeforeFirst() || cursor.isAfterLast()){
            cursor.moveToFirst();
        }
        StringBuilder rowAsString = new StringBuilder("printTable():");
        for (int j=0; j<rowCount; j++) {
            rowAsString.append("\n");
            for (int i = 0; i < columnCount; i++) {
                rowAsString.append("\t - ")
                        .append(cursor.getColumnName(i))
                        .append(": ")
                ;
                switch (cursor.getType(i)){
                    case Cursor.FIELD_TYPE_BLOB:
                        rowAsString.append("BLOB");
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        rowAsString.append(cursor.getFloat(i));
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        rowAsString.append(cursor.getInt(i));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        rowAsString.append(cursor.getString(i));
                        break;
                    case Cursor.FIELD_TYPE_NULL:
                        rowAsString.append("NULL");
                        break;
                    default:
                        break;
                }
                moveSucceeded = cursor.moveToNext();
                if (!moveSucceeded){
                    cursor.moveToFirst();
                }
            }
        }
        Log.i(TAG, rowAsString.toString());
    }

    /**
     *
     */
    private class TableBuilder {
        public final String TAG = TableBuilder.class.getSimpleName();
        // Include a space before and after all SQL String constants so we never have to manually add white space
        public static final String CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS";
        public static final String SQL_TYPE_INTEGER = "INTEGER";
        public static final String SQL_TYPE_TEXT = "TEXT";
        public static final String SQL_TYPE_REAL = "REAL";
        public static final String CONSTRAINT_NOT_NULL = "NOT NULL";
        public static final String CONSTRAINT_AUTOINCREMENT = "AUTOINCREMENT";
        public static final String CONSTRAINT_UNIQUE = "UNIQUE";
        public static final String CONSTRAINT_PRIMARY_KEY = "PRIMARY KEY AUTOINCREMENT";
        public static final String COLUMN_SEPARATOR = ", ";
        private static final String WHITE_SPACE = " ";
        private final String mTableName;
        private Set<TableColumn> columnSet;
        private String mForeignKeyString;
        private String mUniqueReplace;

        public TableBuilder(String tableName) {
            mTableName = tableName;
            columnSet = new LinkedHashSet<>();
        }

        /**
         *
         * @param columnName
         * @param type
         * @param constraint
         * @return the {@link StreetDbHelper.TableBuilder} object so more builder items can be added
         */
        public TableBuilder addColumn(String columnName, String type, String... constraint) {
            TableColumn tableColumn = new TableColumn(columnName, type);
            for (int i=0; i<constraint.length; i++){
                tableColumn.addConstraint(constraint[i]);
            }
            columnSet.add(tableColumn);
            return this;
        }

        /**
         *
         * @param column
         * @param foreignTableName
         * @param foreignColumn
         * @return the {@link StreetDbHelper.TableBuilder} object so more builder items can be added
         */
        public TableBuilder addForeignKey(String column, String foreignTableName, String foreignColumn) {
            mForeignKeyString = " FOREIGN KEY (" + column + ") REFERENCES " +
                    foreignTableName + " (" + foreignColumn + ")";
            return this;
        }

        /**
         * Add a "UNIQUE" constraint to {@code column}
         * @param column the name of the column to add the constraint to
         * @param replaceWithColumn
         * @return the {@link StreetDbHelper.TableBuilder} object so more builder items can be added
         */
        public TableBuilder addUniqueReplace(String column, String replaceWithColumn) {
            mUniqueReplace =
                    " UNIQUE ("
                            + column
                            + ", " +
                            replaceWithColumn
                            + ") ON CONFLICT REPLACE";
            return this;
        }

        /**
         * Build a single raw SQLite statement to create an {@link SQLiteDatabase} table using
         * all the items (columns, constraints, etc) added to this
         * {@link StreetDbHelper.TableBuilder}.  The
         * statement does not terminate in a semicolon because SQLiteDatabase.execRaw() does not
         * support multiple statements, so a statement delimiter is neither required nor desirable.
         *
         * @return
         */
        public String build() {
            StringBuilder locationTableBuilder = new StringBuilder();
            locationTableBuilder
                    .append(CREATE_TABLE_IF_NOT_EXISTS)
                    .append(WHITE_SPACE).append(mTableName)
                    .append(" (").append(StreetContract.StreetnameEntry._ID)
                    .append(WHITE_SPACE).append(TableBuilder.SQL_TYPE_INTEGER)
                    .append(WHITE_SPACE).append(TableBuilder.CONSTRAINT_PRIMARY_KEY);
            for (TableColumn eachColumn : columnSet){
                locationTableBuilder.append(eachColumn.toBuilder());
            }
            if (mForeignKeyString != null){
                locationTableBuilder.append(COLUMN_SEPARATOR);
                locationTableBuilder.append(mForeignKeyString);
            }
            if (mUniqueReplace != null){
                locationTableBuilder.append(COLUMN_SEPARATOR);
                locationTableBuilder.append(mUniqueReplace);
            }
            locationTableBuilder.append(")");
            String builderString = locationTableBuilder.toString();
            Log.i(TAG, "build()"
                    + "\t -- builderString: " + builderString
            );
            return builderString;
        }

        /**
         *
         */
        private class TableColumn implements Comparable{
            private final String mColumnName;
            private final String mDataType;
            private Set<String> mConstaintSet = new LinkedHashSet<>();

            public TableColumn(String columnName, String dataType) {
                mColumnName = columnName;
                mDataType = dataType;
            }

            public void addConstraint(String constraint) {
                mConstaintSet.add(constraint);
            }

            public StringBuilder toBuilder() {
                StringBuilder stringBuilder = new StringBuilder()
                        .append(TableBuilder.COLUMN_SEPARATOR)
                        .append(mColumnName)
                        .append(WHITE_SPACE)
                        .append(mDataType);
                for (String eachConstraint : mConstaintSet){
                    stringBuilder.append(WHITE_SPACE).append(eachConstraint);
                }
                return stringBuilder;
            }

            @Override
            public int compareTo(Object another) {
                return this.mColumnName.compareTo(((TableColumn)another).mColumnName);
            }
        }
    }
}
