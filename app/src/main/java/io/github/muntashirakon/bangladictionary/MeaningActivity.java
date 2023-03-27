package io.github.muntashirakon.bangladictionary;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MeaningActivity extends AppCompatActivity {
    static final String LANG_EN = "en";
    static final String LANG_BN = "bn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meaning);
        setSupportActionBar(findViewById(R.id.toolbar));
        // Get the word
        Intent intent = getIntent();
        Database DB;
        CharSequence lookupText = intent.getCharSequenceExtra(MainActivity.LOOKUP_TEXT);
        String language = intent.getCharSequenceExtra(MainActivity.LANGUAGE).toString();
        // Change Title
        this.setTitle(lookupText);
        // Get the data from DB and set it to text view
        // DB
        if (language.equals(LANG_BN))
            DB = new Database(getBaseContext(), Database.BN_DB);
        else // LANG_EN
            DB = new Database(getBaseContext(), Database.EN_DB);
        TextView tv = findViewById(R.id.meaning);
        ImageView iv = findViewById(R.id.meaning_img);
        iv.setVisibility(View.GONE);
        tv.setVisibility(View.VISIBLE);
        TextView syn = findViewById(R.id.syn);
        syn.setVisibility(View.GONE);
        DB.createDatabase();
        DB.open();
        Cursor cursor = DB.getWordMeaning(lookupText);
        if (cursor != null && cursor.moveToNext()) {
            int col_index = cursor.getColumnIndexOrThrow("definition");
            if (language.equals(LANG_BN)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    tv.setText(Html.fromHtml(cursor.getString(col_index), Html.FROM_HTML_MODE_COMPACT));
                else
                    tv.setText(Html.fromHtml(cursor.getString(col_index)));
            } else { // LANG_EN
                String word = cursor.getString(col_index);
                imageDownload image = new imageDownload(this, iv, tv, word);
                image.execute("https://github.com/MuntashirAkon/English-to-Bangla-Dictionary/raw/master/images/" + word);
                // Show synonym and antonym
                DB.close();
                DB = new Database(getBaseContext(), Database.SA_DB).open();
                cursor = DB.getSynonymAntonym(word);
                if (cursor != null && cursor.moveToNext()) {
                    CharSequence synonyms = cursor.getString(cursor.getColumnIndexOrThrow("synonym"));
                    CharSequence antonyms = cursor.getString(cursor.getColumnIndexOrThrow("antonym"));
                    String synAnt = "";
                    if (synonyms.length() != 0) synAnt += "<h3>Synonyms</h3>" + "<p>" + synonyms + "</p>";
                    if (antonyms.length() != 0) synAnt += "<h3>Antonyms</h3>" + "<p>" + antonyms + "</p>";
                    if (synAnt.length() != 0) {
                        syn.setVisibility(View.VISIBLE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            syn.setText(Html.fromHtml(synAnt, Html.FROM_HTML_MODE_COMPACT));
                        else
                            syn.setText(Html.fromHtml(synAnt));
                    }
                }
            }
        }
        DB.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class imageDownload extends AsyncTask<String, Integer, Bitmap> {
        Context context;
        ImageView imageView;
        TextView placeholder;
        Bitmap bitmap;
        String word;
        InputStream in = null;
        final String meaning_img;
        File img_file;
        int responseCode = -1;

        //constructor.
        imageDownload(Context context, ImageView imageView, TextView placeholder, String word) {
            this.context = context;
            this.imageView = imageView;
            this.placeholder = placeholder;
            this.word = word;
            meaning_img = getFilesDir().getPath() + File.separator + word;
            img_file = new File(meaning_img);
            placeholder.setText("No/poor internet connection and/or cache is not available. " +
                    "Please connect to the internet to download the meaning. " +
                    "Once it is downloaded, it'll be available offline."
            );
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            // Return if file has already been downloaded
            if (img_file.exists()) {
                bitmap = BitmapFactory.decodeFile(img_file.getAbsolutePath());
                return bitmap;
            }
            // Download the image
            URL url;
            try {
                url = new URL(params[0]);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.connect();
                responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    in = httpURLConnection.getInputStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int n;
                    n = in.read(buf);
                    while (n != -1) {
                        out.write(buf, 0, n);
                        n = in.read(buf);
                    }
                    out.close();
                    in.close();
                    byte[] response = out.toByteArray();
                    // Save the image
                    FileOutputStream fos = new FileOutputStream(meaning_img);
                    fos.write(response);
                    fos.close();
                    bitmap = BitmapFactory.decodeFile(img_file.getAbsolutePath());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            placeholder.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(bitmap);
        }
    }
}
