package com.yilong.newwidget;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;

import com.yilong.newwidget.view.CustomWidgetView;

public class MainActivity extends Activity {
    private CustomWidgetView custom_widget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        custom_widget = findViewById(R.id.custom_widget);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                custom_widget.onActivityResult();
            }
        }
    }
}
