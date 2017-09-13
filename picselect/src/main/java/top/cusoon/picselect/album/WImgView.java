package top.cusoon.picselect.album;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Scroller;

/**
 * Created by l.wang on 2017/3/6.
 */
@SuppressLint("AppCompatCustomView")
public class WImgView extends ImageView implements View.OnTouchListener, WiewPager.Redispatch {
    private int touchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();
    private static final int DOUBLE_TAP_MIN_TIME =40;
    private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
    private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();

    Scroller scroller;
    VelocityTracker mVelocityTracker;
    Paint paint;

    private RectF mDrawableRectF = new RectF();

    private float maxScale = 2f;
    private Matrix tempMatrix = new Matrix();
    private RectF mDrawableRectFInit = new RectF();
    private ValueAnimator tempAnimator;
    private RectF mDrawableRecfStart = new RectF();
//    long uptime;

    private float mInitDistance;//2指缩放时，2指初始距离


    private static final int SHOW_PRESS = 1;
    private static final int LONG_PRESS = 2;
    private static final int TAP = 3;

    private DoubleTapListener mDoubleTapListener;
    private Handler mHandler;
    private MotionEvent mCurrentDownEvent;
    private MotionEvent mPreviousUpEvent;
    private int mTouchSlopSquare;
    private int mDoubleTapTouchSlopSquare;
    private int mDoubleTapSlopSquare;

    private boolean mStillDown;
    private boolean mDeferConfirmSingleTap;
    private boolean mInLongPress;
    private boolean mInContextClick;
    private boolean mAlwaysInTapRegion;
    private boolean mAlwaysInBiggerTapRegion;
    private boolean mIgnoreNextUpEvent;
    private boolean mIsDoubleTapping;
    private boolean mIsLongpressEnabled;

    public DoubleTapListener getDoubleTapListener(){
        return mDoubleTapListener;
    }

    public void setDoubleTapListener(DoubleTapListener mDoubleTapListener) {
        this.mDoubleTapListener = mDoubleTapListener;
    }

    public  interface DoubleTapListener{
        void onDoubleTap(MotionEvent ev);
        void onSingleTapConfirmed(MotionEvent ev);
    }
//    public interface SignalTapListener{
//        void onSignalTap(MotionEvent ev);
//    }


    public WImgView(Context context) {
        super(context);
        init(context);
    }

    public WImgView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WImgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOnTouchListener(this);

        mHandler= new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SHOW_PRESS:
                        //mListener.onShowPress(mCurrentDownEvent);
                        break;

                    case LONG_PRESS:
                        dispatchLongPress();
                        break;

                    case TAP:
                        // If the user's finger is still down, do not count it as a tap
                        if (mDoubleTapListener != null) {
                            if (!mStillDown) {
                                print("from handler");
                                mDoubleTapListener.onSingleTapConfirmed(mCurrentDownEvent);
                            } else {
                                mDeferConfirmSingleTap = true;
                            }
                        }
                        break;

                    default:
                        throw new RuntimeException("Unknown message " + msg); //never
                }
            }
        };
        int touchSlop, doubleTapSlop, doubleTapTouchSlop;
        if (context == null) {
            touchSlop = ViewConfiguration.getTouchSlop();
            doubleTapTouchSlop = touchSlop;
            doubleTapSlop = touchSlop;
        } else {
            final ViewConfiguration configuration = ViewConfiguration.get(context);
            touchSlop = configuration.getScaledTouchSlop();
            doubleTapTouchSlop = touchSlop;
            doubleTapSlop = configuration.getScaledDoubleTapSlop();
        }
        mTouchSlopSquare = touchSlop * touchSlop;
        mDoubleTapTouchSlopSquare = doubleTapTouchSlop * doubleTapTouchSlop;
        mDoubleTapSlopSquare = doubleTapSlop * doubleTapSlop;



        scroller = new Scroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        this.touchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        scroller = new Scroller(context);
        paint = new TextPaint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(50);
        setScaleType(ScaleType.MATRIX);
        initMatrix();
    }


    @Override
    public void computeScroll() {
        //System.out.println(scroller.getCurrX() + ":" + scroller.getCurrY());
        if (scroller.computeScrollOffset()) {
            //scrollTo(scroller.getCurrX(), scroller.getCurrY());
            int minx = (int) (getWidth() - mDrawableRecfStart.right);
            int maxx = (int) -mDrawableRecfStart.left;
            int miny = Integer.MIN_VALUE, maxy = Integer.MAX_VALUE, ty;
            if (mDrawableRecfStart.height() >= getHeight()) {
                miny = (int) (getHeight() - mDrawableRecfStart.bottom);
                maxy = (int) -mDrawableRecfStart.top;
                ty = Math.min(Math.max(scroller.getCurrY(), miny), maxy);
            } else {
                ty = 0;
            }
            int tx = Math.min(Math.max(scroller.getCurrX(), minx), maxx);
            if ((tx == minx || tx == maxx) && (ty == miny || ty == maxy)) {
                scroller.forceFinished(true);
            }
            Matrix m = getImageMatrix();
            m.set(tempMatrix);
            m.postTranslate(tx, ty);
            invalidate();
        }
    }

    private float mLastX, mLastY,mDownFocusX,mDownFocusY;
    private float mLastScale;
//    private int mActPointId;// mActPoint2Id;

    private enum State {
        NONE, SCROLL_Y, SCROLL_X, SCROLL_XY, SCALE
    }

    State mState = State.NONE;

    private int mCurItem, mTotal;

    @Override
    public boolean onDispatch(MotionEvent ev, int diffScroll, int curItem, int total) {
        if (getDrawable() == null || getDrawable().getBounds().width() == 0 || getDrawable().getBounds().height() == 0) {
            return false;
        }
        mCurItem = curItem;
        mTotal = total;
        boolean consumed = false;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                resetState();
                onTouch(this, ev);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mState = State.SCALE;
                onTouch(this, ev);
                break;
            case MotionEvent.ACTION_MOVE:
                int pointCount = ev.getPointerCount();
                if (pointCount == 1) {
                    //onr finger
                    float x = ev.getX();
                    float y = ev.getY();
                    float dx = Math.abs(x - mLastX);
                    float dy = Math.abs(y - mLastY);
                    int vw = getWidth();
                    int vh = getHeight();
                    getImageMatrix().mapRect(mDrawableRecfStart, mDrawableRectF);
                    float drawableWidth = mDrawableRecfStart.width();
                    float drawableHeight = mDrawableRecfStart.height();
                    if (mState == State.NONE) {
                        //first time,judge  state by  drawable  w&h,   view   w&h   and  delta  x&y
                        if (drawableHeight > vh) {
                            if (drawableWidth > vw) {
                                //when  drawable  w&h  great than  view   w&h  ,we treat it as  drag(all direction,named SCROLL_XY);
                                mState = State.SCROLL_XY;
                            } else {
                                if (dy > dx) {
                                    //this situation ,we treat it as  up&down drag(named SCROLL_Y);
                                    mState = State.SCROLL_Y;
                                } else {
                                    //this situation ,we treat it as  left&right drag(named SCROLL_X);
                                    mState = State.SCROLL_X;
                                }
                            }
                        } else {
                            //when drable height  is small than view height ,we treat it as  left&right drag(named SCROLL_X);
                            mState = State.SCROLL_X;
                        }
                    }
//                    print("mState:" + mState + "  dscroll:" + diffScroll);
                    switch (mState) {
                        case SCROLL_X:
                            consumed = needEvent(ev, mLastX, diffScroll, mDrawableRecfStart);
                            if (consumed) {
                                onTouch(this, ev);
                            } else {
                                mLastX = x;
                                mLastY = y;

                                mAlwaysInTapRegion = false;
                                mHandler.removeMessages(TAP);
                                mHandler.removeMessages(SHOW_PRESS);
                                mHandler.removeMessages(LONG_PRESS);
                            }
                            break;
                        case SCROLL_Y:
                            onTouch(this, ev);
                            consumed = true;
                            break;
                        case SCROLL_XY:
                            if (dy > dx) {
                                onTouch(this, ev);
                                consumed = true;
                            } else {
                                consumed = needEvent(ev, mLastX, diffScroll, mDrawableRecfStart);
                                if (consumed) {
                                    onTouch(this, ev);
                                } else {
                                    mLastX = x;
                                    mLastY = y;

                                    mAlwaysInTapRegion = false;
                                    mHandler.removeMessages(TAP);
                                    mHandler.removeMessages(SHOW_PRESS);
                                    mHandler.removeMessages(LONG_PRESS);
                                }
                            }
                            break;
                        case SCALE:
                            consumed = true;
                            mLastX = x;
                            mLastY = y;
                            break;
                    }
                } else {
                    onTouch(this, ev);
                    consumed = true;
                }

                break;
            case MotionEvent.ACTION_POINTER_UP:
                onTouch(this, ev);
                break;
            case MotionEvent.ACTION_UP:
                resetState();
                onTouch(this, ev);
                break;
            case MotionEvent.ACTION_CANCEL:
                resetState();
                onTouch(this, ev);
                break;
        }
        return consumed;
    }

    /*
    * 左右移动时，是否需要事件
    * */
    private boolean needEvent(MotionEvent ev, float lastX, int diffScroll, RectF drawableRectf) {
        boolean need = false;
        float x = ev.getX();
        float dx = Math.abs(x - lastX);
        int vw = getWidth();
        float drawableWidth = drawableRectf.width();
        if (drawableWidth != getWidth()) {
            if (drawableRectf.right > vw && x < lastX) {
                if (diffScroll >= 0) {
//                    if(dx>drawableRectf.right-vw){
//                       ev.setLocation(lastX-(drawableRectf.right-vw),ev.getY());
//                    }
                    need = true;
                } else {
                    if (dx > Math.abs(diffScroll)) {
                        ev.setLocation(lastX + diffScroll, ev.getY());
                    }
                }
            } else if (drawableRectf.left < 0 && x > lastX) {
                if (diffScroll <= 0) {
//                    if(dx > - drawableRectf.left){
//                        ev.setLocation(lastX-drawableRectf.left,ev.getY());
//                    }
                    need = true;
                } else {
                    if (dx > diffScroll) {
                        ev.setLocation(lastX + diffScroll, ev.getY());
                    }
                }
            }
        }
        return need;
    }


    private void resetState() {
        mState = State.NONE;
    }

    private boolean mIgnoreNextEvent;
    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        //动画进行中时，捕捉但是忽略触摸事件
        if ((tempAnimator != null && tempAnimator.isRunning()) || mIgnoreNextEvent) {
            if(ev.getActionMasked() == MotionEvent.ACTION_DOWN){
                mIgnoreNextEvent =true;
            }else if(ev.getActionMasked() == MotionEvent.ACTION_UP){
                mIgnoreNextEvent =false;
            }
            return true;
        }
        int vw = getWidth();
        int vh = getHeight();

        initVelocityTrackerIfNotExists();
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                boolean hadTapMessage = mHandler.hasMessages(TAP);
                if (hadTapMessage) mHandler.removeMessages(TAP);
                if ((mCurrentDownEvent != null) && (mPreviousUpEvent != null) && hadTapMessage &&
                        isConsideredDoubleTap(mCurrentDownEvent, mPreviousUpEvent, ev)) {
                    // This is a second tap
                    mIsDoubleTapping = true;

                    getImageMatrix().mapRect(mDrawableRecfStart, mDrawableRectF);
//                    print("双击" + mDrawableRecfStart.width() / vw);
                        if ((int) (mDrawableRecfStart.width() / vw * 100) <= 100) {
                            // 放大
                            anim(maxScale / (mDrawableRecfStart.width() / vw));
                        } else {
                            // 缩小
                            anim(vw / mDrawableRecfStart.width());
                        }
                    // Give a callback with the first tap of the double-tap
                    if(mDoubleTapListener!=null) {
                        mDoubleTapListener.onDoubleTap(mCurrentDownEvent);
                    }
                } else {
                    // This is a first tap
                    mHandler.sendEmptyMessageDelayed(TAP, DOUBLE_TAP_TIMEOUT);
                }
                if (mCurrentDownEvent != null) {
                    mCurrentDownEvent.recycle();
                }
                mCurrentDownEvent = MotionEvent.obtain(ev);
                mAlwaysInTapRegion = true;
                mAlwaysInBiggerTapRegion = true;
                mStillDown = true;
                mInLongPress = false;
                mDeferConfirmSingleTap = false;

                if (mIsLongpressEnabled) {
                    mHandler.removeMessages(LONG_PRESS);
                    mHandler.sendEmptyMessageAtTime(LONG_PRESS, mCurrentDownEvent.getDownTime()
                            + TAP_TIMEOUT + LONGPRESS_TIMEOUT);
                }
                mHandler.sendEmptyMessageAtTime(SHOW_PRESS, mCurrentDownEvent.getDownTime() + TAP_TIMEOUT);

                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                scroller.forceFinished(true);
                mDownFocusX =mLastX = ev.getX();
                mDownFocusY =  mLastY = ev.getY();
                tempMatrix.set(getImageMatrix());
                getImageMatrix().mapRect(mDrawableRectFInit, mDrawableRectF);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                cancelTaps();
                onPointerDown(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                print("mIsDoubleTapping:"+mIsDoubleTapping+"---mAlwaysInTapRegion:"+mAlwaysInTapRegion);
                if (mIsDoubleTapping) {
                } else if (mAlwaysInTapRegion) {
                    final int deltaX = (int) (ev.getX() - mDownFocusX);
                    final int deltaY = (int) (ev.getY() - mDownFocusY);
                    int distance = (deltaX * deltaX) + (deltaY * deltaY);
                    print("distance:"+distance+"---mTouchSlopSquare:"+mTouchSlopSquare);
                    if (distance > mTouchSlopSquare) {
                        mAlwaysInTapRegion = false;
                        mHandler.removeMessages(TAP);
                        mHandler.removeMessages(SHOW_PRESS);
                        mHandler.removeMessages(LONG_PRESS);
                    }
                    if (distance > mDoubleTapTouchSlopSquare) {
                        mAlwaysInBiggerTapRegion = false;
                    }
                }



                initVelocityTrackerIfNotExists();
                mVelocityTracker.addMovement(ev);
                int pcount = ev.getPointerCount();
                if (pcount == 1) {
                    if (mState == State.SCALE) {
                        return true;
                    }
                    move(ev, vw, vh);
                } else if (pcount == 2) {
                    initOrResetVelocityTracker();
                    scale(ev, vw);
                }
                break;
            case MotionEvent.ACTION_UP:
                mStillDown = false;
                MotionEvent currentUpEvent = MotionEvent.obtain(ev);
                if (mIsDoubleTapping) {
                } else if (mInLongPress) {
                    mHandler.removeMessages(TAP);
                    mInLongPress = false;
                } else if (mAlwaysInTapRegion && !mIgnoreNextUpEvent) {
                    if (mDeferConfirmSingleTap && mDoubleTapListener != null) {
                        mDoubleTapListener.onSingleTapConfirmed(ev);
                    }
                } else if (!mIgnoreNextUpEvent) {

                }
                print("up");
                if(tempAnimator ==null || !tempAnimator.isRunning()){
                    onEnd();
                }

                if (mPreviousUpEvent != null) {
                    mPreviousUpEvent.recycle();
                }
                // Hold the event we obtained above - listeners may have changed the original.
                mPreviousUpEvent = currentUpEvent;
                mIsDoubleTapping = false;
                mDeferConfirmSingleTap = false;
                mIgnoreNextUpEvent = false;
                mHandler.removeMessages(SHOW_PRESS);
                mHandler.removeMessages(LONG_PRESS);


//                getImageMatrix().mapRect(mDrawableRecfStart, mDrawableRectF);
//                int dtime = (int) (System.currentTimeMillis() - uptime);
//                if (dtime < DOUBLE_TAP_TIMEOUT && dtime > DOUBLE_TAP_MIN_TIME) {
////                    print("双击" + mDrawableRecfStart.width() / vw);
//                    if ((int) (mDrawableRecfStart.width() / vw * 100) <= 100) {
//                        // 放大
//                        anim(maxScale / (mDrawableRecfStart.width() / vw));
//                    } else {
//                        // 缩小
//                        anim(vw / mDrawableRecfStart.width());
//                    }
//                    uptime = 0;
//                    break;
//                } else {
//                    uptime = System.currentTimeMillis();
//                }
//                onEnd();
                break;
            case MotionEvent.ACTION_CANCEL:
                onEnd();
                cancel();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onPointerUp(ev, vw);
                break;
        }

        //     print("ActionMasked:" + e.getActionMasked() + "  Action:" + e.getAction() + "   PointerCount:" + e.getPointerCount() + " STR:" + e.toString());
        return true;
    }

    private void onPointerDown(MotionEvent ev) {
        if (ev.getPointerCount() == 2) {
            //保存缩放时初始matrix,和2指距离
            tempMatrix.set(getImageMatrix());
            getImageMatrix().mapRect(mDrawableRectFInit, mDrawableRectF);
            mInitDistance = getDistance(ev.getX(0), ev.getY(0), ev.getX(1), ev.getY(1));
        }
    }

    private void scale(MotionEvent ev, int vw) {
        float x1 = ev.getX(0);
        float y1 = ev.getY(0);
        float x2 = ev.getX(1);
        float y2 = ev.getY(1);
        float d2 = getDistance(x1, y1, x2, y2);
        float scale = (d2 - mInitDistance) / 1000 + 1;
        if (mDrawableRectFInit.width() * scale / vw < 0.75f && scale < mLastScale) {
            scale = vw / mDrawableRectFInit.width() * 0.75f;
        }
        mLastScale = scale;
        Matrix m = getImageMatrix();
        m.set(tempMatrix);
        m.postScale(scale, scale, (x1 + x2) / 2, (y1 + y2) / 2);
        setImageMatrix(m);//由于getImageMatrix获取到的可能是单位矩阵,不一定是mDrawMatrix,所以必须set
        // print("scale:" + scale);
        invalidate();
    }

    private void onPointerUp(MotionEvent ev, int vw) {
        //重新收集计算速度的点
        initOrResetVelocityTracker();
        int pcount = ev.getPointerCount();
        if (pcount == 2) {
            //2指变1指时，重置单指坐标和id;
            int point1Index = ev.getActionIndex() == 0 ? 1 : 0;
            mLastX = ev.getX(point1Index);
            mLastY = ev.getY(point1Index);
            getImageMatrix().mapRect(mDrawableRecfStart, mDrawableRectF);
            if (mDrawableRecfStart.width() / vw > maxScale) {
                //超过最大缩放倍数时
                anim(vw * maxScale / mDrawableRecfStart.width());
            } else if (mDrawableRecfStart.width() / vw < 1) {
                //小于最小缩放倍数
                anim(vw / mDrawableRecfStart.width());
            }
        } else if (pcount == 3) {
            //3指变2指时，重置2点坐标和id;
            int point1Index;
            int point2Index;
            int upPointIndex = ev.getActionIndex();
            if (upPointIndex == 0) {
                point1Index = 1;
                point2Index = 2;
            } else if (upPointIndex == 1) {
                point1Index = 0;
                point2Index = 2;
            } else {
                point1Index = 0;
                point2Index = 1;
            }
            tempMatrix.set(getImageMatrix());
            getImageMatrix().mapRect(mDrawableRectFInit, mDrawableRectF);
            mInitDistance = getDistance(ev.getX(point1Index), ev.getY(point1Index), ev.getX(point2Index), ev.getY(point2Index));
        }
    }

    /**
     * 平移
     */
    private void move(MotionEvent ev, int vw, int vh) {
        float x = ev.getX();
        float y = ev.getY();
        float dx = x - mLastX;
        float dy = y - mLastY;
        if (vw >= mDrawableRectFInit.width()) {
            dx = 0;//图片宽度小于view宽度时，不允许图片水平移动
        }
        if (vh >= mDrawableRectFInit.height()) {
            dy = 0;//图片高度小于view高度时，不允许图片垂直移动
        }

        if ((mDrawableRecfStart.top > 0 && dy > 0) || (mDrawableRecfStart.bottom < vh && dy < 0)) {
            dy *= 0.25f;
        }
        if ((mDrawableRecfStart.left > 0 && dx > 0) || (mDrawableRecfStart.right < vw && dx < 0)) {
            dx *= 0.25f;
        }

        Matrix matrix = getImageMatrix();
        matrix.postTranslate(dx, dy);
        setImageMatrix(matrix);
        invalidate();
        mLastX = x;
        mLastY = y;
    }

    private void onEnd() {
        getImageMatrix().mapRect(mDrawableRecfStart, mDrawableRectF);
        print("dh:"+mDrawableRecfStart.height()+";h:"+getHeight()+";top:"+mDrawableRecfStart.top+";bottom:"+mDrawableRecfStart.bottom);
        int dWidth = Math.round(mDrawableRecfStart.width());
        int dHeight = Math.round(mDrawableRecfStart.height());
        int width = getWidth();
        int height = getHeight();
        int dLeft = Math.round(mDrawableRecfStart.left);
        int dRight = Math.round(mDrawableRecfStart.right);
        int dTop = Math.round(mDrawableRecfStart.top);
        int dBottom = Math.round(mDrawableRecfStart.bottom);


        if ((dWidth >= width && dHeight >= height && dLeft <= 0 && dRight >= width && dTop <= 0 && dBottom >= getHeight() && dWidth <= maxScale * width)
                || (dWidth >= getWidth() && dHeight <= getHeight() && dLeft<= 0 && dRight >= width && dWidth <= maxScale * width)) {
            //适当的时侯 进行惯性移动
            mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
            int vx = (int) mVelocityTracker.getXVelocity();
            int vy = (int) mVelocityTracker.getYVelocity();
            if (height >= dHeight) {
                vy = 0;
            }
//            print(vx+":"+vy);
            if (Math.abs(vx) >= mMinimumVelocity || Math.abs(vy) >= mMinimumVelocity) {
                //scroller.fling(0, 0, vx, vy, (int) -mDrawableRecfStart.width(), (int) mDrawableRecfStart.width(), (int) -mDrawableRecfStart.height(), (int) mDrawableRecfStart.height());
                scroller.fling(0, 0, vx, vy, (int) -mDrawableRecfStart.width(), (int) mDrawableRecfStart.width(), (int) -mDrawableRecfStart.height(), (int) mDrawableRecfStart.height());
                tempMatrix.set(getImageMatrix());
                invalidate();
            }
        } else {
            float scale = mDrawableRecfStart.width() / getWidth();
            print(scale);
            if (scale > maxScale || scale < 1) {
                anim(1 / scale);
            } else {
                anim(1);
            }
        }
        recycleVelocityTracker();
    }

    private void cancel() {
        mHandler.removeMessages(SHOW_PRESS);
        mHandler.removeMessages(LONG_PRESS);
        mHandler.removeMessages(TAP);
        mVelocityTracker.recycle();
        mVelocityTracker = null;
        mIsDoubleTapping = false;
        mStillDown = false;
        mAlwaysInTapRegion = false;
        mAlwaysInBiggerTapRegion = false;
        mDeferConfirmSingleTap = false;
        mInLongPress = false;
        mInContextClick = false;
        mIgnoreNextUpEvent = false;
    }
    private void cancelTaps() {
        mHandler.removeMessages(SHOW_PRESS);
        mHandler.removeMessages(LONG_PRESS);
        mHandler.removeMessages(TAP);
        mIsDoubleTapping = false;
        mAlwaysInTapRegion = false;
        mAlwaysInBiggerTapRegion = false;
        mDeferConfirmSingleTap = false;
        mInLongPress = false;
        mInContextClick = false;
        mIgnoreNextUpEvent = false;
    }

    private void anim(final float scale) {
        if (tempAnimator != null && tempAnimator.isRunning()) {
            return;
        }
        Matrix imageMatrix = getImageMatrix();
        tempMatrix.set(imageMatrix);
        RectF curRectF = new RectF();
        imageMatrix.mapRect(curRectF, mDrawableRectF);
        RectF dstRectF = new RectF();
        Matrix m = new Matrix();
        m.postScale(scale, scale, getWidth() / 2, getHeight() / 2);
        m.mapRect(dstRectF, curRectF);
//        print(scale + "-------" + curRectF + "--------" + dstRectF);
        float dx = 0, dy = 0;

        if (dstRectF.height() >= getHeight()) {//竖屏图片
            if (dstRectF.left > 0) {
                dx = dstRectF.left;
            }
            if (dstRectF.right < getWidth()) {
                dx = dstRectF.right - getWidth();
            }
            if (dstRectF.top > 0) {
                dy = dstRectF.top;
            }
            if (dstRectF.bottom < getHeight()) {
                dy = dstRectF.bottom - getHeight();
            }
        } else {
            //横屏图片
            dy = -((getHeight() - dstRectF.height()) / 2 - dstRectF.top);
            if (dstRectF.left > 0) {
                dx = dstRectF.left;
            }
            if (dstRectF.right < getWidth()) {
                dx = dstRectF.right - getWidth();
            }
        }

        final float finalDx = dx;
        final float finalDy = dy;
        tempAnimator = ValueAnimator.ofFloat(0, 1).setDuration(150);
        tempAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Matrix m = getImageMatrix();
                m.set(tempMatrix);
                m.postScale(1 + (float) animation.getAnimatedValue() * (scale - 1), 1 + (float) animation.getAnimatedValue() * (scale - 1), getWidth() / 2, getHeight() / 2);
                m.postTranslate(-finalDx * (float) animation.getAnimatedValue(), -finalDy * (float) animation.getAnimatedValue());
                setImageMatrix(m);
                ViewCompat.postInvalidateOnAnimation(WImgView.this);
            }
        });
        tempAnimator.start();
    }


    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }


    private float getDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    static void print(Object o) {
        System.out.println(o);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        initMatrix();
    }

    private void initMatrix() {
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (getDrawable() == null) {
                    return true;
                }
                getViewTreeObserver().removeOnPreDrawListener(this);
                //print("onPreDraw");
                int vwidth = getWidth();
                int vheight = getHeight();
                mDrawableRectF.set(getDrawable().getBounds());
                int dwidth = getDrawable().getBounds().width();
                int dheight = getDrawable().getBounds().height();


                float dy = 0;
                float scale = (float) vwidth / (float) dwidth;

                if (dwidth < dheight) {
                    maxScale = Math.max(maxScale, dheight / dwidth);
                } else if (dwidth <= dheight*3) {
                    maxScale = getHeight() / (dheight * scale);
                } else{
                    maxScale = Math.min(5, dwidth / dheight);
                }

                // print(mInitScale);
                if (dheight * scale < vheight) {
                    dy = (vheight - dheight * scale) * 0.5f;
                }
                Matrix m = new Matrix();
                m.setScale(scale, scale);
                m.postTranslate(0, Math.round(dy));
                setImageMatrix(m);
                return true;
            }
        });
    }


    private boolean isConsideredDoubleTap(MotionEvent firstDown, MotionEvent firstUp,
                                          MotionEvent secondDown) {
        if (!mAlwaysInBiggerTapRegion) {
            return false;
        }

        final long deltaTime = secondDown.getEventTime() - firstUp.getEventTime();
        if (deltaTime > DOUBLE_TAP_TIMEOUT || deltaTime < DOUBLE_TAP_MIN_TIME) {
            return false;
        }

        int deltaX = (int) firstDown.getX() - (int) secondDown.getX();
        int deltaY = (int) firstDown.getY() - (int) secondDown.getY();
        return (deltaX * deltaX + deltaY * deltaY < mDoubleTapSlopSquare);
    }

    private void dispatchLongPress() {
        mHandler.removeMessages(TAP);
        mDeferConfirmSingleTap = false;
        mInLongPress = true;
        //mListener.onLongPress(mCurrentDownEvent);
    }
}
