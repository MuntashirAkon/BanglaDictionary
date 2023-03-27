package io.github.muntashirakon.bangladictionary;

import java.io.Closeable;
import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class Database implements Closeable {
    static final String BN_DB = "BanglaDictionary";
    static final String EN_DB = "EnglishDictionary";
    static final String SA_DB = "SynonymAntonym";
    private static final String TAG = "DataAdapter";

    private SQLiteDatabase mDb;
    private final DatabaseHelper mDbHelper;

    public Database(Context context, String Database) {
        switch (Database) {
            case BN_DB:
            case EN_DB:
            case SA_DB:
                break;
            default:
                throw new IllegalArgumentException("Wrong database");
        }
        mDbHelper = new DatabaseHelper(context, Database);
    }

    @NonNull
    public Database createDatabase() throws SQLException {
        try {
            mDbHelper.createDatabase();
        } catch (IOException e) {
            throw new SQLException("UnableToCreateDatabase", e);
        }
        return this;
    }

    @NonNull
    public Database open() throws SQLException {
        mDbHelper.openDatabase();
        mDbHelper.close();
        mDb = mDbHelper.getReadableDatabase();
        return this;
    }

    @Override
    public void close() {
        mDbHelper.close();
    }

    @Nullable
    public Cursor getWordMeaning(@NonNull CharSequence word) {
        try {
            String w = word.toString();
            String sql = "SELECT definition FROM words WHERE word_name = \"" + w + "\"";
            return mDb.rawQuery(sql, null);
        } catch (SQLException e) {
            Log.e(TAG, "Error while fetching word meaning", e);
            return null;
        }
    }

    @Nullable
    public Cursor getSynonymAntonym(@NonNull CharSequence word) {
        try {
            String w = word.toString();
            String sql = "SELECT synonym, antonym FROM words WHERE word_name = \"" + w + "\"";
            return mDb.rawQuery(sql, null);
        } catch (SQLException e) {
            Log.e(TAG, "Error while fetching synonyms and antonyms", e);
            return null;
        }
    }

    @Nullable
    public Cursor getSuggestions(@NonNull CharSequence lookup_text) {
        try {
            if (lookup_text.length() == 0) return null;
            String sql = "SELECT word_name FROM words WHERE word_name LIKE '" + lookup_text + "%' ORDER BY word_name LIMIT 200";
            return mDb.rawQuery(sql, null);
        } catch (SQLException e) {
            Log.e(TAG, "Error while fetching suggestions", e);
            return null;
        }
    }
}