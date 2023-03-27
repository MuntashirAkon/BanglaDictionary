package io.github.muntashirakon.bangladictionary;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static final String LOOKUP_TEXT = "io.github.muntashirakon.bangladictionary.LOOKUP_TEXT";
    public static final String LANGUAGE   = "io.github.muntashirakon.bangladictionary.LANGUAGE";
    private LinearLayout suggestionBox;
    private String textLanguage;
    private Database DB_BN;
    private Database DB_EN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        // Set suggestion box
        suggestionBox = findViewById(R.id.suggestions);
        // Set Databases
        DB_BN = new Database(getBaseContext(), Database.BN_DB);
        DB_EN = new Database(getBaseContext(), Database.EN_DB);
        (new Database(getBaseContext(), Database.SA_DB)).createDatabase();
        DB_BN.createDatabase().open();
        DB_EN.createDatabase().open();
        // Get lookup word
        EditText lookup_word = findViewById(R.id.lookup);
        lookup_word.addTextChangedListener(new TextWatcher() {
            // Show suggestions
            public void afterTextChanged(final Editable lookup_text) {
                // FIXME: Do this in a separate thread
                runOnUiThread(() -> {
                    suggestionBox.removeAllViews();
                    Cursor cursor;
                    // Check for language
                    if(lookup_text.toString().matches("^[A-Za-z0-9_+-.]+")){ // English
                        cursor = DB_EN.getSuggestions(lookup_text);
                        textLanguage = MeaningActivity.LANG_EN;
                    } else { // Bangla
                        cursor = DB_BN.getSuggestions(lookup_text);
                        textLanguage = MeaningActivity.LANG_BN;
                    }
                    while (cursor != null && cursor.moveToNext()){
                        int col_index = cursor.getColumnIndexOrThrow("word_name");
                        TextView tv = new TextView(getBaseContext());
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            tv.setText(Html.fromHtml(cursor.getString(col_index), Html.FROM_HTML_MODE_COMPACT));
                        else
                            tv.setText(Html.fromHtml(cursor.getString(col_index)));
                        tv.setTextSize(25);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        tv.setOnClickListener(v -> findMeaning(v));
                        suggestionBox.addView(tv);
                    }

                });
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DB_BN.close();
        DB_EN.close();
    }

    public void findMeaning(View v){
        TextView textView = (TextView) v;
        // Load the MeaningActivity using Intent
        Intent meaningIntent = new Intent(this, MeaningActivity.class);
        meaningIntent.putExtra(LOOKUP_TEXT, textView.getText());
        meaningIntent.putExtra(LANGUAGE, textLanguage);
        startActivity(meaningIntent);
    }
}
