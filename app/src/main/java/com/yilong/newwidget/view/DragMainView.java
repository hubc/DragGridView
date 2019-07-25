package com.yilong.newwidget.view;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import com.yilong.newwidget.DragAdapter;
import com.yilong.newwidget.R;

public class DragMainView extends FrameLayout {
    private GestureDetector detector;
    /**
     * 点击拖动
     */
    public static final int DRAG_WHEN_TOUCH = 0;
    /**
     * 长按拖动
     */
    public static final int DRAG_BY_LONG_CLICK = 1;

    private int mDragMode = DRAG_WHEN_TOUCH;
    private boolean hasSendDragMsg = false;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0x123:
                    // 启动拖拽模式
                    isDragable = true;
                    // 根据点击的位置生成该位置上的view镜像
                    if (isTouchInTop(msg.arg2)) {
                        mDragTop.setCurrentDragPosition(msg.arg1);
                        copyView(mDragTop);
                    } else {
                        mDragBottom.setCurrentDragPosition(msg.arg1);
                        copyView(mDragBottom);
                    }
                    hasSendDragMsg = false;
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private boolean isDragable = true;
    private float[] lastLocation = null;
    private View mCopyView;
    private OnTouchListener l;
    // 转交给GridView一些常用监听器
    private AdapterView.OnItemLongClickListener itemLongClickListener;
    private int mTouchArea = 0;
    private View dragSlider;
    private Point mMovePoint; // 记录移动走向，上到下，还是下到上

    /**
     * @param itemClickListener
     * @描述:item 转交给gridview一些常用监听器
     */
    public void setOnItemClickListener(AdapterView.OnItemClickListener itemClickListener) {
        mDragBottom.setOnItemClickListener(itemClickListener);
    }

    /**
     * 长按监听器自己触发,点击拖动模式不存在长按
     *
     * @param
     */
    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    private boolean canAddViewWhenDragChange = true;
    private int mStartPoint;
    private int START_DRAG_TOP = 0;
    private int START_DRAG_BOTTOM = 1;
    /**
     * 手势监听器,滚动和单击
     */
    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (hasSendDragMsg) {
                hasSendDragMsg = false;
                handler.removeMessages(0x123);
            }
            if (isDragable && mCopyView != null) {
                if (lastLocation == null && e1 != null) {
                    lastLocation = new float[]{e1.getRawX(), e1.getRawY()};
                }
                if (lastLocation == null)
                    lastLocation = new float[]{0, 0};
                distanceX = lastLocation[0] - e2.getRawX();
                distanceY = lastLocation[1] - e2.getRawY();
                lastLocation[0] = e2.getRawX();
                lastLocation[1] = e2.getRawY();

                mCopyView.setX(mCopyView.getX() - distanceX);
                mCopyView.setY(mCopyView.getY() - distanceY);
                int to = eventToPosition(e2);
                mCopyView.invalidate();
                //if (isDragInTop()) {
                if (isTouchInTop(e2)) {
                    if (isDragFromBottom()) {
                        //if (isDragBack(isDragInTop())) {
                        if (isDragBack(isTouchInTop(e2))) { //针对已经进入bottom区域，但是又返回来的情况
                            mStartPoint = START_DRAG_BOTTOM; //切换，保证移动过程中只执行一次
                            canAddViewWhenDragChange = true;
                        }
                        if (canAddViewWhenDragChange) {// 保证移动过程中，数据只有一次的添加
                            mDragTop.addSwapView(mDragBottom.getSwapData());
                            mDragBottom.removeSwapView();
                            canAddViewWhenDragChange = false;
                            if (hideView != null)
                                hideView.setVisibility(VISIBLE);
                        }
                        if (mDragTop.isViewInitDone()) {
                            mDragTop.setCurrentDragPosition(mDragTop.getGridChildCount() - 1);
                            hideView = mDragTop.getGridChildAt(mDragTop.getCurrentDragPosition());
                            if (hideView != null)
                                hideView.setVisibility(INVISIBLE);
                            mMovePoint = getDragViewCenterPoint(mDragTop);
                        }
                    }
                    if (mDragTop.isViewInitDone()) {
                        dragChangePosition(mDragTop, to);
                    }
                } else {
                    if (isDragFromTop()) {
                        if (isDragBack(isTouchInTop(e2))) {
                            mStartPoint = START_DRAG_TOP;
                            canAddViewWhenDragChange = true;
                        }
                        if (canAddViewWhenDragChange) {
                            mDragBottom.addSwapView(mDragTop.getSwapData());
                            mDragTop.removeSwapView();
                            canAddViewWhenDragChange = false;
                            if (hideView != null)
                                hideView.setVisibility(VISIBLE);
                        }
                        if (mDragBottom.isViewInitDone()) {
                            mDragBottom.setCurrentDragPosition(mDragBottom.getGridChildCount() - 1);
                            hideView = mDragBottom.getGridChildAt(mDragBottom.getCurrentDragPosition());
                            if (hideView != null)
                                hideView.setVisibility(INVISIBLE);
                            Log.e("mMovePoint", mMovePoint.x + "-----------" + mMovePoint.y);
                            mMovePoint = getDragViewCenterPoint(mDragBottom);
                        }
                    }
                    if (mDragBottom.isViewInitDone()) {
                        dragChangePosition(mDragBottom, to);
                    }
                }
            }
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            /** 响应长按拖拽 */
            if (mDragMode == DRAG_BY_LONG_CLICK) {
                // 启动拖拽模式
                // isDragable = true;
                // 通知父控件不拦截我的事件
                getParent().requestDisallowInterceptTouchEvent(true);
                // 根据点击的位置生成该位置上的view镜像
                int position = eventToPosition(e);
                if (isCanDragMove(isTouchInTop(e) ? mDragTop : mDragBottom, position)) {
                    // copyView(currentDragPosition = position);
                    Message msg = handler.obtainMessage(0x123, position, (int) e.getY());
                    // showpress本身大概需要170毫秒
                    handler.sendMessageDelayed(msg, dragLongPressTime - 170);

                    mMovePoint = new Point((int) e.getX(), (int) e.getY());
                    mStartPoint = isTouchInTop(e) ? START_DRAG_TOP : START_DRAG_BOTTOM;
                    hasSendDragMsg = true;
                }
            }
        }
    };

    private boolean isDragBack(boolean dragInTop) {
        return (dragInTop && mStartPoint == START_DRAG_TOP) || (!dragInTop && mStartPoint == START_DRAG_BOTTOM);
    }

    private boolean isDragFromTop() {
        if (mMovePoint != null && mDragTop != null) {
            if ((mMovePoint.x > mDragTop.getX() && mMovePoint.x < (mDragTop.getX() + mDragTop.getWidth()))
                    && (mMovePoint.y > mDragTop.getY() && mMovePoint.y < (mDragTop.getY() + mDragTop.getHeight()))) {
                return true;
            }
        }
        return false;
    }

    private Point getDragViewCenterPoint(DragView dragView) {
        Point result = new Point();
        if (dragView != null) {
            int height = dragView.getHeight();
            int width = dragView.getWidth();
            float x = dragView.getX();
            float y = dragView.getY();
            result.set((int) (x + width / 2), (int) (y + height / 2));
        }
        return result;
    }

    private boolean isDragFromBottom() {
        if (mMovePoint != null && mDragBottom != null) {
            if ((mMovePoint.x > mDragBottom.getX() && mMovePoint.x < (mDragBottom.getX() + mDragBottom.getWidth()))
                    && (mMovePoint.y > mDragBottom.getY() && mMovePoint.y < (mDragBottom.getY() + mDragBottom.getHeight()))) {
                return true;
            }
        }
        return false;
    }

    private boolean isTouchInTop(MotionEvent event) {
        float y = event.getY();
        return isTouchInTop(y);
    }

    private boolean isTouchInTop(float y) {
        return y > mDragTop.getY() && y < (mDragTop.getY() + mDragTop.getHeight());
    }
    private void dragChangePosition(DragView dragView, int to) {
        if (to != dragView.getCurrentDragPosition() && isCanDragMove(dragView, to)) {
            dragView.onDragPositionChange(dragView.getCurrentDragPosition(), to);
        }
    }

    private boolean isCanDragMove(DragView dragView, int position) {
        return position >= dragView.getHeadDragPosition() && position < dragView.getGridChildCount() - dragView.getFootDragPosition();
    }

    private FrameLayout mDragFrame;
    private DragView mDragBottom;
    private DragView mDragTop;
    private View hideView;
    private long dragLongPressTime = 600;

    public DragMainView(@NonNull Context context) {
        this(context, null);
    }

    public DragMainView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public DragMainView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        Context context = getContext();
        detector = new GestureDetector(context, simpleOnGestureListener);
        detector.setIsLongpressEnabled(false);
        mDragFrame = new FrameLayout(context);
        dragSlider = LayoutInflater.from(context).inflate(R.layout.view_drag_main, this, false);
        mDragTop = dragSlider.findViewById(R.id.drag_top);
        mDragBottom = dragSlider.findViewById(R.id.drag_bottom);
        addView(dragSlider, -1, -1);
        addView(mDragFrame, -1, -1);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (l != null) {
            l.onTouch(this, ev);
        }
        if (!isViewInitDone()) {
            return false;
        }

        if (isDragable) {
            handleScrollAndCreMirror(ev);
        } else {
            // 交给子控件自己处理
            dispatchEvent(isTouchInTop(ev) ? mDragTop : mDragBottom, ev);
        }

        // 处理拖动
        detector.onTouchEvent(ev);
        if (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP) {
            lastLocation = null;
            if (hasSendDragMsg) {
                hasSendDragMsg = false;
                handler.removeMessages(0x123);
            }
        }
        return true;
    }

    private void dispatchEvent(DragView dragView, MotionEvent ev) {
        dragView.dispatchEvent(ev);
    }

    private boolean isDragInTop() {
        if (mCopyView == null)
            return false;
        return (mCopyView.getY() + mCopyView.getHeight()) < (mDragTop.getY() + mDragTop.getBottom());
    }

    /**
     * Description :拦截所有事件
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    /**
     * 处理自动滚屏,和单击生成镜像
     */
    private void handleScrollAndCreMirror(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 通知父控件不拦截我的事件
                getParent().requestDisallowInterceptTouchEvent(true);
                // 根据点击的位置生成该位置上的view镜像
                int position = eventToPosition(ev);
                makeCopyView(isTouchInTop(ev) ? mDragTop : mDragBottom, position);
                break;
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);// 通知父控件不拦截我的事件
                // 内容太多时,移动到边缘会自动滚动
                decodeScrollArea(isDragInTop() ? mDragTop : mDragBottom, ev);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (hideView != null) {
                    hideView.setVisibility(View.VISIBLE);
                    if (onDragSelectListener != null) {
                        onDragSelectListener.onPutDown(hideView);
                    }
                }
                mDragFrame.removeAllViews();
                // mDragFrame.scrollTo(0, 0);
                // isNotifyByDragSort = true;
                updateUI(isDragInTop() ? mDragTop : mDragBottom, ev);
                mCopyView = null;
                canAddViewWhenDragChange = true;
                // 放手时取消拖动排序模式
                if (mDragMode == DRAG_BY_LONG_CLICK) {
                    isDragable = false;
                }
                break;
            default:
                break;
        }
    }

    private void updateUI(DragView dragView, MotionEvent ev) {
        if (dragView.isHasPositionChange()) {
            dragView.setHasPositionChange(false);
            dragView.getAdapter().notifyDataSetChanged();
        } else if (mDragMode == DRAG_BY_LONG_CLICK && itemLongClickListener != null) {
            dragView.onItemLongClick(itemLongClickListener);
        }
        // 停止滚动
        if (dragView.isCanScroll()) {
            int scrollStates2 = dragView.decodeTouchArea(ev);
            if (scrollStates2 != 0) {
                dragView.onTouchAreaChange(0);
                mTouchArea = 0;
            }
        }
    }

    private void decodeScrollArea(DragView dragView, MotionEvent ev) {
        if (dragView.isCanScroll()) {
            int touchArea = dragView.decodeTouchArea(ev);
            if (touchArea != mTouchArea) {
                dragView.onTouchAreaChange(touchArea);
                mTouchArea = touchArea;
            }
        }
    }

    private void makeCopyView(DragView dragView, int position) {
        if (position >= dragView.getHeadDragPosition() && position < dragView.getGridChildCount() - dragView.getFootDragPosition()) {
            dragView.setCurrentDragPosition(position);
            copyView(dragView);
        }
    }

    /**
     * 得到事件触发点, 摸到的是哪一个item
     */
    public int eventToPosition(MotionEvent ev) {

        if (ev != null) {
            if (isTouchInTop(ev)) {
                return mDragTop.eventToPosition(ev);
            } else {
                return mDragBottom.eventToPosition(ev);
            }
        }
        return 0;
    }


    /**
     * 复制一个镜像,并添加到透明层
     */
    private void copyView(DragView dragView) {
        // TODO: 2018/4/2 创建可移动的 item
        hideView = dragView.getGridChildAt(dragView.getCurrentDragPosition());
        int realPosition = dragView.getGridChildPos(hideView);
        DragAdapter adapter = dragView.getAdapter();
        if (!adapter.isUseCopyView()) {
            mCopyView = adapter.getView(realPosition, mCopyView, mDragFrame);
        } else {
            mCopyView = adapter.copyView(realPosition, mCopyView, mDragFrame);
        }
        hideView.setVisibility(View.INVISIBLE);
        if (mCopyView.getParent() == null)
            mDragFrame.addView(mCopyView, dragView.getmColWidth(), dragView.getmColHeight());

        int[] l1 = new int[2];
        int[] l2 = new int[2];
        hideView.getLocationOnScreen(l1);
        mDragFrame.getLocationOnScreen(l2);

        // mCopyView.setX(hideView.getLeft());
        // mCopyView.setY(hideView.getTop() - mCurrentY);
        mCopyView.setX(l1[0] - l2[0]);
        mCopyView.setY(l1[1] - l2[1]);
        if (onDragSelectListener == null) {
            mCopyView.setScaleX(1.2f);
            mCopyView.setScaleY(1.2f);
        } else {
            onDragSelectListener.onDragSelect(mCopyView);
        }
    }

    private OnDragSelectListener onDragSelectListener;

    /**
     * @描述:一个item view刚被拖拽和放下时起来生成镜像时调用.
     */
    public void setOnDragSelectListener(OnDragSelectListener onDragSelectListener) {
        this.onDragSelectListener = onDragSelectListener;
    }

    /**
     * @param mode int类型
     * @描述:设置拖动的策略是点击还是长按
     */
    public void setDragModel(int mode) {
        this.mDragMode = mode;
        isDragable = mode == DRAG_WHEN_TOUCH;
    }

    public void setAnimFrame(FrameLayout mDragFrame) {
        this.mDragFrame = mDragFrame;
    }

    /**
     * 设置长按需要用时
     *
     * @param time
     */
    public void setDragLongPressTime(long time) {
        dragLongPressTime = time;
    }

    public void setBottomAdapter(DragAdapter adapter) {
        mDragBottom.setAdapter(adapter);
    }

    public boolean isViewInitDone() {
        boolean result = mDragBottom.isViewInitDone();
        if (mDragTop.getVisibility() == VISIBLE)
            result &= mDragTop.isViewInitDone();
        return result;
    }

    public void setTopAdapter(DragAdapter adapter) {
        mDragTop.setAdapter(adapter);
    }

    public interface OnDragSelectListener {
        /**
         * @param mirror
         *            所拖拽起来的view生成的镜像 ,并不是实际的view.可对这个镜像实施变换效果,但是并不改变放下后的效果
         * @描述:拖拽起一个view时调用
         */
        void onDragSelect(View mirror);

        /**
         * @param itemView
         * @描述:拖拽的View放下时调用
         * @作者 [pWX273343] 2015年7月3日
         */
        void onPutDown(View itemView);
    }
}
