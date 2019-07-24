package com.yilong.newwidget;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class GridDragShortActivity extends AppCompatActivity {

    DragChessView drag_main;
    ArrayList<String> bottomList;
    ArrayList<String> topList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_drag_short);
        drag_main = findViewById(R.id.drag_main);
        drag_main.setDragModel(DragChessView.DRAG_BY_LONG_CLICK);

        bottomList = new ArrayList<String> ();
        topList = new ArrayList<String> ();
        for (int i = 0; i < 29; i++) {
            bottomList.add("" + i);
        }
        for (int i = 0; i < 29; i++) {
            topList.add(("A" + i).toString());
        }
        drag_main.setBottomAdapter(new MyAdapter(bottomList));
        drag_main.setTopAdapter(new MyAdapter(topList));
    }

    class MyAdapter extends DragAdapter {
        ArrayList<String> list;

        public MyAdapter(ArrayList<String> list) {
            this.list = list;
        }
        @Override
        public void onDataModelMove(int from, int to) {
            Log.d("clarkhu", "from = " + from + "; to = " + to);
            String s = list.remove(from);
            list.add(to, s);
        }

        @Override
        public Object getSwapData(int position) {
            return getItem(position);
        }

        @Override
        public void removeData(int position) {
            Log.d("clarkhu", "removeData() position = " + position);
            list.remove(position);
        }

        @Override
        public void addNewData(Object data) {
            Log.d("clarkhu", "addNewData()");
            list.add((String) data);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public String getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                FrameLayout frameLayout = new FrameLayout(GridDragShortActivity.this);
                convertView = frameLayout;
                textView = new TextView(GridDragShortActivity.this);
                frameLayout.setPadding(20, 20, 20, 20);
                textView.setPadding(20, 100, 20, 100);
                frameLayout.addView(textView);
                textView.setBackgroundColor(0x33ff00ff);
                textView.setGravity(Gravity.CENTER);
            } else {
                textView = (TextView) ((FrameLayout) convertView).getChildAt(0);
            }
            textView.setText(getItem(position));
            return convertView;
        }
    }
}
