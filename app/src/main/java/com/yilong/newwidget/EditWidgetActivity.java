package com.yilong.newwidget;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yilong.newwidget.view.DragMainView;

import java.util.ArrayList;

public class EditWidgetActivity extends AppCompatActivity {

    DragMainView drag_main;
    ImageView commit;
    ImageView close;
    private ArrayList<WidgetInfo> bottomList = new ArrayList<>();
    private ArrayList<WidgetInfo> topList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_widget);
        drag_main = findViewById(R.id.drag_main);
        drag_main.setDragModel(DragMainView.DRAG_BY_LONG_CLICK);
        commit = findViewById(R.id.commit);
        close = findViewById(R.id.close);

        topList = WidgetInfo.getShowedWidgetInfo();
        bottomList = WidgetInfo.getHiddenWidgetInfo();
        for (WidgetInfo info : topList) {
            if (info.getEditState() != -1) {
                info.setEditState(-1);
            }
        }
        for (WidgetInfo info : bottomList) {
            if (info.getEditState() != -1) {
                info.setEditState(-1);
            }
        }
        drag_main.setTopAdapter(new MyAdapter(topList, 0));
        drag_main.setBottomAdapter(new MyAdapter(bottomList, 1));

        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (WidgetInfo info : WidgetInfo.getAllWidgetInfo()) {
                    if (info.getEditState() == 0) {
                        info.setShowState(true);
                        info.setEditState(-1);
                    } else if (info.getEditState() == 1) {
                        info.setShowState(false);
                        info.setEditState(-1);
                    }
                }
                setResult(RESULT_OK);
                finish();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (WidgetInfo info : WidgetInfo.getAllWidgetInfo()) {
                    if (info.getEditState() != -1) {
                        info.setEditState(-1);
                    }
                }
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    class MyAdapter extends DragAdapter {
        ArrayList<WidgetInfo> list;
        int tag;

        public MyAdapter(ArrayList<WidgetInfo> list, int tag) {
            this.list = list;
            this.tag = tag;
        }
        @Override
        public void onDataModelMove(int from, int to) {
            Log.d("clarkhu", "from = " + from + "; to = " + to);
            WidgetInfo s = list.remove(from);
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
            WidgetInfo info = (WidgetInfo) data;
            info.setEditState(tag);
            list.add(info);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public WidgetInfo getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(EditWidgetActivity.this, R.layout.item_widget, null);
            }
            ImageView imageView = convertView.findViewById(R.id.img);
            TextView textView = convertView.findViewById(R.id.text);
            WidgetInfo widgetInfo = list.get(position);
            imageView.setBackgroundResource(widgetInfo.getUnselectedImageId());
            textView.setText(widgetInfo.getNameId());
            return convertView;
        }
    }
}
