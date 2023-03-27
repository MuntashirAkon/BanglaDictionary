package io.github.muntashirakon.bangladictionary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = DatabaseHelper.class.getSimpleName();

    // Destination path (location) of our database on device
    private final String mDbPath;
    private final String mDbName; // Database name
    private SQLiteDatabase mDataBase;
    private final Context mContext;

    DatabaseHelper(Context context, String Database) {
        super(context, Database, null, 1);// 1? Its database Version
        mDbName = Database;
        mDbPath = context.getApplicationInfo().dataDir + "/databases/";
        this.mContext = context;
    }

    void createDatabase() throws IOException {
        // If the database does not exist, copy it from the assets.
        boolean mDataBaseExist = checkDatabase();
        if (!mDataBaseExist) {
            this.getReadableDatabase();
            this.close();
            try {
                // Copy the database from assets
                copyDatabase();
                Log.e(TAG, "createDatabase: " + mDbName + " database created");
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    // Check that the database exists here: /data/data/your package/databases/Da Name
    private boolean checkDatabase() {
        File dbFile = new File(mDbPath + mDbName);
        //Log.v("dbFile", dbFile + "   "+ dbFile.exists());
        return dbFile.exists();
    }

    // Copy the database from assets
    private void copyDatabase() throws IOException {
        InputStream mInput = mContext.getAssets().open(mDbName);
        String outFileName = mDbPath + mDbName;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    // Open the database, so we can query it
    boolean openDatabase() throws SQLException {
        String mPath = mDbPath + mDbName;
        //Log.v("mPath", mPath);
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        //mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        return mDataBase != null;
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}