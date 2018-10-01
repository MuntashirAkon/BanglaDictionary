package io.github.muntashirakon.bangladictionary;

import java.io.IOException;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

class Database {
    static final String BN_DB = "BanglaDictionary";
    static final String EN_DB = "EnglishDictionary";
    static final String SA_DB = "SynonymAntonym";
    private static final String TAG = "DataAdapter";

    private SQLiteDatabase mDb;
    private DatabaseHelper mDbHelper;

    Database(Context context, String Database) {
        switch (Database){
            case BN_DB:
            case EN_DB:
            case SA_DB:
                break;
            default:
                Log.e(TAG, "WrongDatabase");
                throw new Error("WrongDatabase");
        }
        mDbHelper = new DatabaseHelper(context, Database);
    }

    Database createDatabase() throws SQLException {
        try {
            mDbHelper.createDatabase();
        } catch (IOException mIOException) {
            Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    Database open() throws SQLException {
        try {
            mDbHelper.openDatabase();
            mDbHelper.close();
            mDb = mDbHelper.getReadableDatabase();
        } catch (SQLException mSQLException) {
            Log.e(TAG, "open >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }

    void close() {
        mDbHelper.close();
    }

    Cursor getWordMeaning(CharSequence word) {
        try {
            String w = word.toString();
            String sql = "SELECT definition FROM words WHERE word_name = \""+w+"\"";
            return mDb.rawQuery(sql, null);
        } catch (SQLException mSQLException) {
            Log.e(TAG, "getWordMeaning >> " + mSQLException.toString());
            throw mSQLException;
        }
    }

    Cursor getSynonymAntonym(CharSequence word){
        try {
            String w = word.toString();
            String sql = "SELECT synonym, antonym FROM words WHERE word_name = \""+w+"\"";
            return mDb.rawQuery(sql, null);
        } catch (SQLException mSQLException) {
            Log.e(TAG, "getSynonymAntonym >> " + mSQLException.toString());
            throw mSQLException;
        }
    }

    Cursor getSuggestions(CharSequence lookup_text) {
        try {
            if(lookup_text.length() == 0) return null;
            String sql = "SELECT word_name FROM words WHERE word_name LIKE '" + lookup_text + "%' ORDER BY word_name LIMIT 200";
            Cursor mCur = mDb.rawQuery(sql, null);
            return mCur;
        } catch (SQLException mSQLException) {
            Log.e(TAG, "getSuggestions >> " + mSQLException.toString());
            throw mSQLException;
        }
    }
}