package com.mehmet.codexapktest;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(36), dp(24), dp(24));
        root.setBackgroundColor(Color.rgb(241, 245, 249));

        TextView title = new TextView(this);
        title.setText("Codex APK Test");
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.rgb(15, 23, 42));
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView subtitle = new TextView(this);
        subtitle.setText("Bu uygulama GitHub Actions ile derlenmek icin olusturuldu.");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.rgb(71, 85, 105));
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.setMargins(0, dp(12), 0, dp(26));
        root.addView(subtitle, subtitleParams);

        TextView status = new TextView(this);
        status.setText("Hazir: APK telefonda acildiysa test basarili.");
        status.setTextSize(17);
        status.setTextColor(Color.rgb(15, 118, 110));
        status.setGravity(Gravity.CENTER);
        status.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(status, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView counter = new TextView(this);
        counter.setText("Buton sayaci: 0");
        counter.setTextSize(20);
        counter.setTextColor(Color.rgb(30, 41, 59));
        counter.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams counterParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        counterParams.setMargins(0, dp(28), 0, dp(18));
        root.addView(counter, counterParams);

        Button button = new Button(this);
        button.setText("Test Butonuna Bas");
        button.setTextSize(16);
        button.setAllCaps(false);
        root.addView(button, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(54)
        ));

        TextView footer = new TextView(this);
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        footer.setText("Olusturma zamani: " + date);
        footer.setTextSize(13);
        footer.setTextColor(Color.rgb(100, 116, 139));
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        footerParams.setMargins(0, dp(24), 0, 0);
        root.addView(footer, footerParams);

        button.setOnClickListener(v -> {
            count++;
            counter.setText("Buton sayaci: " + count);
            status.setText("Buton calisiyor. Test basarili: " + count);
        });

        setContentView(root);
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
