package eu.quelltext.mundraub.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import eu.quelltext.mundraub.R;

public class CodexActivity extends MundraubBaseActivity {

    private Button buttonAccept;
    private Button buttonViewLongVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_codex);

        buttonAccept = (Button) findViewById(R.id.button_accept);
        buttonViewLongVersion = (Button) findViewById(R.id.button_long_version);

        buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonViewLongVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openURLInBrowser(getResources().getString(R.string.rules_url));
            }
        });
    }
}
