package com.lobot.HiwonderDemo.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.lobot.HiwonderDemo.R;


/**
 * 轨迹球控件
 * Created by hejie on 2015/8/18.
 */
public class HandShake extends View {

    public interface DirectionListener {

        /**
         * 方向，不靠近任何一方
         */
        int DIREACTION_NONE = -2;
        /**
         * 方向，初始位置
         */
        int DIREACTION_INITIAL = -1;
        /**
         * 方向，
         */
        int DIREACTION_UP = 0;
        /**
         * 方向，
         */
        int DIREACTION_RIGHT = 1;
        /**
         * 方向，
         */
        int DIREACTION_DOWN = 2;
        /**
         * 方向，
         */
        int DIREACTION_LEFT = 3;

        /**
         * 方向变化时调用
         *
         * @param direction {@link #DIREACTION_INITIAL} 初始位置<br/>
         *                  {@link #DIREACTION_UP} up<br/>
         *                  {@link #DIREACTION_RIGHT} right<br/>
         *                  {@link #DIREACTION_INITIAL} down<br/>
         *                  {@link #DIREACTION_INITIAL} left
         */
        void onDirection(int direction);
    }

    private static final String TAG = "HandShake";

    private Vibrator mVibrator;

    private Paint paint;

    /**
     * 是否正在触摸
     */
    private boolean isTouching;
    /**
     * 轨迹球半径
     */
    private int ballRadius;

    /**
     * 控件半径
     */
    private int radius;

    /**
     * 轨迹球图片
     */
    private Bitmap ballBmp;
    private Bitmap ballBgBmp;
    private Rect dest;
    private Rect bgDest;

    private float ringWidth;

    private int ringColor;

    private Point center;

    private Point ballCenter;

    /**
     * 当前方向
     */
    private int currentDirection;

    private final Object syncLock = new Object();

    private DirectionListener directionListener;

    public DirectionListener getDirectionListener() {
        return directionListener;
    }

    public void setDirectionListener(DirectionListener directionListener) {
        this.directionListener = directionListener;
    }

    public HandShake(Context context) {
        super(context);
        init();
    }

    public HandShake(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        setAttribute(context, attrs, 0, 0);
    }

    public HandShake(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        setAttribute(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HandShake(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
        setAttribute(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init() {
        isTouching = false;
        paint = new Paint();
        paint.setAntiAlias(true);
        dest = new Rect(0, 0, 0, 0);
        bgDest = new Rect(0, 0, 0, 0);
        center = new Point();
        ballCenter = new Point();
        this.mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        currentDirection = -1;
    }

    protected void setAttribute(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HandShake, defStyleAttr, defStyleRes);
        Drawable d = a.getDrawable(R.styleable.HandShake_ball_src);
        if (d != null) {
            ballBmp = ((BitmapDrawable) d).getBitmap();
        }
        Drawable bg = a.getDrawable(R.styleable.HandShake_ball_bg);
        if (bg != null) {
            ballBgBmp = ((BitmapDrawable) bg).getBitmap();
        }
        ringWidth = a.getDimension(R.styleable.HandShake_ring_width, dpToPx(1, getResources()));
        ringColor = a.getColor(R.styleable.HandShake_ring_color, Color.argb(200, 250, 250, 250));
        a.recycle();
    }

    /**
     * 震动一下
     */
    private void playVibrator() {
        if (mVibrator != null)
            mVibrator.vibrate(30);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int w = right - left;
        int h = bottom - top;
        int len = w < h ? w : h;
        ballRadius = len / 8;
        radius = len / 2 - ballRadius;
        center.x = w / 2;
        center.y = h / 2;
        int padding = (int) dpToPx(2, getResources());
        bgDest.top = center.y - radius - padding;
        bgDest.right = center.x + radius + padding;
        bgDest.bottom = center.y + radius + padding;
        bgDest.left = center.x - radius - padding;
        if(changed)
            setBallRect(center.x, center.y);
    }

    /**
     * 设置圆球的位置
     *
     * @param x 点击位置x坐标
     * @param y 点击位置y坐标
     */
    private void setBallRect(float x, float y) {
        if(x == center.x && y == center.y)
        {
            ballCenter.x = (int) x;
            ballCenter.y = (int) y;
        }
        double distance = distance(center.x, center.y, x, y);
        double k = radius / distance;
        if (k < 1) { // 超出了圆环范围
            x = (float) (x * k + (1 - k) * center.x);
            y = (float) (y * k + (1 - k) * center.y);
        }
        ballCenter.x = (int) x;
        ballCenter.y = (int) y;
        dest.left = (int) (x - ballRadius);
        dest.top = (int) (y - ballRadius);
        dest.right = (int) (x + ballRadius);
        dest.bottom = (int) (y + ballRadius);
        if (distance > radius - (ballRadius / 2)) { // 轨迹球位置接近边缘
            synchronized (syncLock) {
                int direction = getDirection(ballCenter);
                if (currentDirection != direction) {
                    currentDirection = direction;
                    if (directionListener != null) {
                        directionListener.onDirection(currentDirection);
                    }
                    playVibrator();
                }
            }
        } else { // 轨迹球位置没有靠近边缘
            if (distance != 0 && currentDirection != DirectionListener.DIREACTION_NONE) {
                currentDirection = DirectionListener.DIREACTION_NONE;
                if (directionListener != null) {
                    directionListener.onDirection(currentDirection);
                }
            }
        }
    }

    /**
     * 计算两点之间的距离
     *
     * @param x1 p1的x坐标
     * @param y1 p1的y坐标
     * @param x2 p2的x坐标
     * @param y2 p2的y坐标
     * @return 距离
     */
    private double distance(float x1, float y1, float x2, float y2) {
        return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        LogUtil.d(TAG, "action = " + event.getAction());
//        LogUtil.d(TAG, "x = " + event.getX() + ", y = " + event.getY());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isTouching)
                    isTouching = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isTouching)
                    isTouching = true;
                setBallRect(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                if (isTouching)
                    isTouching = false;
                stop();
                break;
            default:
                if (isTouching)
                    isTouching = false;
                stop();
                break;
        }
        invalidate();
        return true;
    }

    public void setOriention(int oriention)
    {
        switch (oriention)
        {
            case DirectionListener.DIREACTION_UP:
                setBallRect(center.x,ballRadius);
                break;

            case DirectionListener.DIREACTION_DOWN:
                setBallRect(center.x,center.y * 2 - ballRadius);
                break;

            case DirectionListener.DIREACTION_LEFT:
                setBallRect(ballRadius,center.y);
                break;

            case DirectionListener.DIREACTION_RIGHT:
                setBallRect(center.x * 2 - ballRadius,center.y);
                 break;

            case DirectionListener.DIREACTION_NONE:
                setBallRect(center.x,center.y);
                break;
        }
        invalidate();
    }

    private synchronized void stop() {
        setBallRect(center.x, center.y);
        if (currentDirection != DirectionListener.DIREACTION_INITIAL) {
            currentDirection = DirectionListener.DIREACTION_INITIAL;
            if (directionListener != null) {
                directionListener.onDirection(currentDirection);
            }
        }
    }

    /**
     * 根据当前点获取方向
     *
     * @param point 点位置
     * @return 方向（-1未知；0 up；1 right；2 down；3 left）
     */
    private int getDirection(Point point) {
        Point points[] = new Point[4];
        points[0] = new Point(center.x, center.y - radius);
        points[1] = new Point(center.x + radius, center.y);
        points[2] = new Point(center.x, center.y + radius);
        points[3] = new Point(center.x - radius, center.y);
        int index = 0;
        for (int i = 1; i < points.length; i++) {
            if (distance(points[i].x, points[i].y, point.x, point.y) < distance(points[index].x, points[index].y, point.x, point.y)) {
                index = i;
            }
        }
        return index;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (ballBgBmp == null) {
            paint.setStyle(Paint.Style.STROKE); // 空心
            paint.setColor(ringColor);
            paint.setStrokeWidth(ringWidth);
            canvas.drawCircle(center.x, center.y, radius, paint);
        } else {
            canvas.drawBitmap(ballBgBmp, null, bgDest, null);
        }
        if (ballBmp != null) {
            canvas.drawBitmap(ballBmp, null, dest, null);
        }
    }

    public float dpToPx(float dp, Resources resources) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

}
