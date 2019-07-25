package com.yilong.newwidget.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.yilong.newwidget.EditWidgetActivity;
import com.yilong.newwidget.MainActivity;
import com.yilong.newwidget.R;
import com.yilong.newwidget.WidgetInfo;

import java.util.ArrayList;

public class CustomWidgetView extends FrameLayout implements AdapterView.OnItemClickListener {

    private Context mContext;
    private GridView gridView;
    private ArrayList<WidgetInfo> widgetInfos = new ArrayList<>();
    private MyAdapter myAdapter;
    private int selectPosition = -1;

    private Scroller mScroller;
    private View middleAndBottom, topView, tip, user;

    /**
     * 是否为打开状态，默认第一次为收起状态
     */
    private boolean isOpenStatus = false;
    /**
     * 默认为初始化状态
     */
    private boolean initStatus = true;
    //    最大的拖动大小
    private int maxScrollHeight = 0;

    private int mTouchSlop;

    private boolean isMoveUp = false;
    private boolean mDragging;

    private int mLastY;
    private View root, upArrow, arrowDownRoot, menu;

    private Rect userRect = new Rect();
    private Rect menuRect = new Rect();
    private Rect tipRect = new Rect();

    private int topDip = 15;

    public CustomWidgetView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CustomWidgetView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        inflate(context, R.layout.view_custom_widget, this);

        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(context));

        middleAndBottom = findViewById(R.id.middleAndBottom);
        arrowDownRoot = findViewById(R.id.arrowDownRoot);
        upArrow = findViewById(R.id.upArrow);
        topView = findViewById(R.id.topView);
        mScroller = new Scroller(mContext);
        menu = findViewById(R.id.menu);
        user = findViewById(R.id.user);
        root = findViewById(R.id.root);
        tip = findViewById(R.id.tip);

        findViewById(R.id.edit).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) mContext).startActivityForResult(new Intent(mContext, EditWidgetActivity.class), 1);
            }
        });

        ViewTreeObserver tipObserver = tip.getViewTreeObserver();
        tipObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tip.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                tipRect.left = (int) tip.getX();
                tipRect.top = 0;
                tipRect.right = tipRect.left + tip.getMeasuredWidth();
                tipRect.bottom = tipRect.top + tip.getMeasuredHeight() + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topDip, mContext.getResources().getDisplayMetrics());
            }
        });

        ViewTreeObserver menuObserver = menu.getViewTreeObserver();
        menuObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                menu.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                menuRect.left = (int) menu.getX();
                menuRect.top = (int) menu.getY() + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topDip, mContext.getResources().getDisplayMetrics());
                menuRect.right = menuRect.left + menu.getMeasuredWidth();
                menuRect.bottom = menuRect.top + menu.getMeasuredHeight();
            }
        });

        ViewTreeObserver userObserver = user.getViewTreeObserver();
        userObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                user.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                userRect.left = (int) user.getX();
                userRect.top = (int) user.getY() + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topDip, mContext.getResources().getDisplayMetrics());
                userRect.right = userRect.left + user.getMeasuredWidth();
                userRect.bottom = userRect.top + user.getMeasuredHeight();
            }
        });

        ViewTreeObserver observer = middleAndBottom.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                middleAndBottom.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                maxScrollHeight = middleAndBottom.getMeasuredHeight();
                mScroller.startScroll(0, 0, 0, -maxScrollHeight);
            }
        });

        topView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int y = (int) event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mLastY = y;

                        Log.i("eeeeeee", "ACTION_DOWN");

                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dy = y - mLastY;
                        if (Math.abs(dy) > mTouchSlop) {
                            mDragging = true;
                        }
                        if (mDragging) {
                            CustomWidgetView.this.scrollBy(0, -dy);
                            if (dy < 0) {
                                isMoveUp = true;
                            } else {
                                isMoveUp = false;
                            }
                            Log.i("zzzzzzzz", "move   dy = " + dy);
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        if (mDragging) {
                            if (isMoveUp) {
                                Log.i("zzzzzzzz", "向上滑  ");
                                showWidget();
                                isOpenStatus = true;
                                tip.setVisibility(View.VISIBLE);
                                upArrow.setVisibility(View.INVISIBLE);
                                arrowDownRoot.setVisibility(View.VISIBLE);
                            } else {
                                Log.i("zzzzzzzz", "向下滑   ");
                                hindWidget();
                                isOpenStatus = false;
                                tip.setVisibility(View.INVISIBLE);
                                upArrow.setVisibility(View.VISIBLE);
                                arrowDownRoot.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            Log.i("eeeeeee", "ACTION_UP");
                            checkTouchRange(event);
                        }
                        mDragging = false;
                        break;
                }

                return true;
            }
        });
    }

    public void onActivityResult() {
        widgetInfos = WidgetInfo.getShowedWidgetInfo();
        myAdapter.notifyDataSetChanged();
    }

    /**
     * 判断点击按钮区域 执行相应逻辑
     *
     * @param event
     */

    public void checkTouchRange(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        if (checkPointInRect(x, y, userRect)) {
            Toast.makeText(mContext, "点击了用户按钮", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkPointInRect(x, y, menuRect)) {
            Toast.makeText(mContext, "点击了菜单按钮", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkPointInRect(x, y, tipRect)) {
            if (isOpenStatus) {
                hindWidget();
                isOpenStatus = false;
                arrowDownRoot.setVisibility(View.GONE);
                tip.setVisibility(View.GONE);
                upArrow.setVisibility(View.VISIBLE);
            } else {
                showWidget();
                isOpenStatus = true;
                tip.setVisibility(View.VISIBLE);
                upArrow.setVisibility(View.GONE);
                arrowDownRoot.setVisibility(View.VISIBLE);
            }
            return;
        }
    }

    public boolean checkPointInRect(int x, int y, Rect rect) {

        Log.i("eeeeeee", "x = " + x + "   y = " + y + "   left = " + rect.left + "   right = " + rect.right + "    top = " + rect.top + "   botton=" + rect.bottom);

        if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) {
            return true;
        }
        return false;
    }

    public void showWidget() {
        mScroller.startScroll(0, getScrollY(), 0, maxScrollHeight - getScrollY(), 300);
        postInvalidate();

    }

    public void hindWidget() {
        mScroller.startScroll(0, getScrollY(), 0, -maxScrollHeight - getScrollY(), 300);
        postInvalidate();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        gridView = findViewById(R.id.gridView);
        widgetInfos = WidgetInfo.getShowedWidgetInfo();
        myAdapter = new MyAdapter();
        gridView.setAdapter(myAdapter);
        gridView.setOnItemClickListener(this);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        /**
         * 防止滑动超出边界
         */
        if (y >= 0) {
            y = 0;
        }
        if (y <= -maxScrollHeight) {
            y = -maxScrollHeight;
        }

        if (initStatus) {
            if (y == -maxScrollHeight) {
                root.setVisibility(View.VISIBLE);
                initStatus = false;
            }
        }

        super.scrollTo(x, y);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectPosition = position;
        myAdapter.notifyDataSetChanged();
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return widgetInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = View.inflate(mContext, R.layout.item_widget, null);

            ImageView imageView = convertView.findViewById(R.id.img);
            TextView textView = convertView.findViewById(R.id.text);
            final WidgetInfo widgetInfo = widgetInfos.get(position);

            if (selectPosition == position) {
                imageView.setBackgroundResource(widgetInfo.getSelectedImageId());
            } else {
                imageView.setBackgroundResource(widgetInfo.getUnselectedImageId());
            }

            textView.setText(widgetInfo.getNameId());
            return convertView;
        }
    }


}
