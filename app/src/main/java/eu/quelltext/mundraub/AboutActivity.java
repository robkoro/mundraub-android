package eu.quelltext.mundraub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AboutActivity extends AppCompatActivity {

    private Button buttonViewSource;
    private Button buttonViewFreedoms;
    private Button buttonViewIssues;
    private Button buttonViewMIT;
    private Button buttonViewGPL;
    private TextView textSelectedLicense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        buttonViewSource = (Button) findViewById(R.id.button_view_source);
        buttonViewFreedoms = (Button) findViewById(R.id.button_view_freedoms);
        buttonViewIssues = (Button) findViewById(R.id.button_view_issues);
        buttonViewMIT = (Button) findViewById(R.id.button_mit);
        buttonViewGPL = (Button) findViewById(R.id.button_gpl);
        textSelectedLicense = (TextView) findViewById(R.id.text_selected_license);

        buttonViewSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebsite(R.string.about_view_source_url);
            }
        });
        buttonViewFreedoms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebsite(R.string.about_view_four_freedoms_url);
            }
        });
        buttonViewIssues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebsite(R.string.about_view_issues_url);
            }
        });
        buttonViewGPL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLicense("license/LICENSE");
            }
        });
        buttonViewMIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLicense("map/license.txt");
            }
        });
    }

    private void selectLicense(String path) {
        // from https://stackoverflow.com/a/9544781
        BufferedReader reader = null;
        textSelectedLicense.setText("");
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open(path), "UTF-8"));

            // do reading, usually loop until end of file reading
            String mLine;
            StringBuilder text = new StringBuilder();
            while ((mLine = reader.readLine()) != null) {
                //process line
                text.append(mLine);
                text.append("\n");
            }
            textSelectedLicense.setText(text.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void openWebsite(int urlResourceId) {
        String url = getResources().getString(urlResourceId);
        // from https://stackoverflow.com/a/3004542/1320237
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}