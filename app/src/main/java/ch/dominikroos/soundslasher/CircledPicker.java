package ch.dominikroos.soundslasher;

/**
 * Created by Dominik on 09.06.2015.
 */
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

public class CircledPicker extends View implements Runnable {
    static public boolean isAPI10 = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
    public static final String TAG = "CIRCLED PICKER";
    private static final int VALUE_THRESHOLD = 270;
    private ValueAnimator mAngleAnimator;
    private RectF mArcRect;
    private RectF mArcInnerRect;
    private RectF mShadowRect;
    private RectF mShadowInnerRect;
    private Paint mPaint;
    private Path mPath;
    private Rect mTextBounds;
    private PickerMode mPickerMode;
    private float mCurrentSweep;
    private float mCurrentValue;
    private float mLastAngle;
    private float mDownX;
    private float mMaxValue;
    private float mStep;
    private float mTextSize;
    private boolean mIsFilled;
    private boolean mIsEmpty;
    private int mMidX;
    private int mMidY;
    private int mThickness;
    private int mInnerThickness;
    private int mTouchSlop;
    private int mRadius;
    private int mInnerRadius;
    private volatile boolean isInterrupted;
    private Thread mUpdaterThread;
    private long mAlarmTime;


    public void start(long alarmTime){
        mAlarmTime = alarmTime;
        isInterrupted = false;
        if(mUpdaterThread == null || (mUpdaterThread != null && !mUpdaterThread.isAlive())){
            mUpdaterThread = new Thread(this);
            mUpdaterThread.start();
        }

    }

    public void stop(){
        isInterrupted = true;
    }



    @Override
    public void run() {
        try {
            while (!isInterrupted) {
                ((Activity)getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float value = (float)((mAlarmTime - System.currentTimeMillis())/1000);
                        setValue(value);
                        invalidate();
                    }
                });

                Thread.sleep(100);
            }
        } catch (InterruptedException e) {

        }
    }

    public static enum PickerMode {
        MINUTES_AND_SECONDS,
        HOURS_AND_MINUTES,
        PERCENT,
        NUMERIC
    }

    public CircledPicker(Context context) {
        super(context);
        init(context, null);
    }

    public CircledPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircledPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void animateToValue(float value) {
        animateChange((value * 360) / mMaxValue);
    }

    public float getAngle(Point target, Point origin) {
        float angle = (float) Math.toDegrees(Math.atan2(target.x - origin.x, target.y - origin.y)) + 180;
        if(angle < 0) {
            angle += 360;
        }
        return 360 - angle;
    }

    public int getMultiply(float value) {
        return (int) (value - (value % mStep));
    }

    public int getThickness() {
        return mThickness;
    }

    public float getValue() {
        return mCurrentValue;
    }

    public void onDraw(Canvas canvas) {
        setCirclesBoundingBoxes();
        drawBackground(canvas);
        drawShadow(canvas);
        drawPickerCircle(canvas);
        drawButton(canvas);
        drawCenteredText(canvas);
    }

    private void updateCirle(float angle) {
        mCurrentSweep = angle;

        if(mCurrentSweep - mLastAngle < -VALUE_THRESHOLD
                && !mIsFilled
                && !mIsEmpty) {
            mIsFilled = true;
        } else if(mCurrentSweep - mLastAngle > VALUE_THRESHOLD
                && !mIsEmpty
                && !mIsFilled) {
            mIsEmpty = true;
        }

        if(mCurrentSweep < 360
                && mCurrentSweep > 300
                && mIsFilled) {
            mIsFilled = false;
        } else if(mCurrentSweep < 60
                && mCurrentSweep > 0
                && mIsEmpty) {
            mIsEmpty = false;
        }

        if(mIsFilled) {
            mCurrentSweep = 359.99f;
        } else if(mIsEmpty) {
            mCurrentSweep = 0;
        } else {
            mLastAngle = mCurrentSweep;
        }

        mCurrentValue = getMultiply(((mCurrentSweep + 0.01f) * mMaxValue) / 360);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        getParent().requestDisallowInterceptTouchEvent(true);

        float angle = getAngle(new Point((int) event.getX(), (int) event.getY()),
                new Point(mMidX, mMidY));
        if(isClickable()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if(Math.abs(mDownX - event.getX()) > mTouchSlop &&
                            (mAngleAnimator != null && !mAngleAnimator.isRunning())) {
                        if (!isAPI10) {
                            updateCirle(angle);
                            postInvalidate();
                        }
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    mDownX = event.getX();
                    mIsFilled = mIsEmpty = false;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if(Math.abs(mDownX - event.getX()) < mTouchSlop) {
                        if (!isAPI10) {
                            animateChange(angle);
                            mIsFilled = mIsEmpty = false;
                        }
                    }
                    break;
            }
        }
        return true;
    }

    public void setThickness(int thickness) {
        this.mThickness = thickness;
        postInvalidate();
    }

    public void setValue(float value) {
        mCurrentValue = value;
        mCurrentSweep = ((value * 360) / mMaxValue);
    }

    private void animateChange(float finalAngle) {
        if(!isAPI10) {
            if(mAngleAnimator.isRunning()) {
                mAngleAnimator.end();
            }
            mAngleAnimator.setFloatValues(mLastAngle, finalAngle);
            mAngleAnimator.setDuration(200);
            mAngleAnimator.setInterpolator(new DecelerateInterpolator());
            mAngleAnimator.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCurrentSweep = (Float) animation.getAnimatedValue();
                    mCurrentValue = getMultiply((mCurrentSweep * mMaxValue) / 360);
                    postInvalidate();
                }
            });
            mAngleAnimator.addListener(new AnimatorListener() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    mLastAngle = mCurrentSweep;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mLastAngle = mCurrentSweep;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationStart(Animator animation) {
                }
            });
            mAngleAnimator.start();
        }
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setAlpha(255);
    }

    private void drawCenteredText(Canvas canvas) {
        String centerLabel = "";
        int textVerticalOffset;

        mPaint.reset();
        mPaint.setAlpha(255);
        mPaint.setShader(null);
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(getResources().getColor(R.color.colorAccent));

        switch (mPickerMode) {
            case MINUTES_AND_SECONDS:
                centerLabel = CircledPickerUtils.getMinuesAndSecondsString(getValue());
                break;
            case HOURS_AND_MINUTES:
                centerLabel = CircledPickerUtils.getHourAndMinutesString(getValue());
                break;
            case PERCENT:
                centerLabel = getPercentString();
                break;
            case NUMERIC:
                centerLabel = String.valueOf((int) getValue());
                break;
        }

        mPaint.getTextBounds(centerLabel, 0, centerLabel.length(), mTextBounds);
        textVerticalOffset = mMidY + (mTextBounds.height() / 2);
        canvas.drawText(centerLabel, mMidX - (mTextBounds.width() / 2), textVerticalOffset, mPaint);
    }


    private void drawButton(Canvas canvas) {
       /*
        mPaint.reset();
        mPaint.setAlpha(255);
        mPaint.setShader(null);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(5);

        int mMidPlayY = mMidY + (int)(mInnerRadius * 0.5);

        double ratio = 0.3f;
        double mEdge = ((double)mInnerRadius * ratio);
        int mHeight = (int) ( mEdge * 0.86602540378);
        int mHalfHeight = (int) (mEdge * 0.43301270189);
        int mHalfEdge = (int) (mHeight * 0.5);
        int mTopLeftX = mMidX - mHalfHeight;
        int mTopLeftY = mMidPlayY + mHalfEdge;
        int mBottomLeftX = mMidX - mHalfHeight;
        int mBottomLeftY = mMidPlayY - mHalfEdge;
        int mRightX = mMidX + mHalfHeight;
        int mRightY = mMidPlayY;

        mPath.reset();
        mPath.moveTo(mTopLeftX, mTopLeftY);
        mPath.lineTo(mRightX, mRightY);
        mPath.lineTo(mBottomLeftX, mBottomLeftY);
        mPath.close();

        canvas.drawPath(mPath, mPaint);
        */
    }

    private String getPercentString() {
        int percent = (int) ((getValue() / mMaxValue) * 100f);
        return String.valueOf(percent) + "%";
    }

    private void drawPickerCircle(Canvas canvas) {
        mPath.reset();
        mPath.arcTo(mArcRect, 270, mCurrentSweep);
        mPath.arcTo(mArcInnerRect, 270 + mCurrentSweep, -mCurrentSweep);
        mPath.close();
        mPaint.setColor(getResources().getColor(R.color.colorAccent));
        canvas.drawPath(mPath, mPaint);
    }

    private void drawShadow(Canvas canvas) {
        mPath.arcTo(mShadowRect, 0, 359.9f);
        mPath.arcTo(mShadowInnerRect, 359.9f, -359.9f);
        mPath.close();
        mPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        canvas.drawPath(mPath, mPaint);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircledPicker);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        String pickerMode;

        mStep = typedArray.getInteger(R.styleable.CircledPicker_step, 1);
        mMaxValue = typedArray.getInteger(R.styleable.CircledPicker_maxValue, 100);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.CircledPicker_textSize, 0);
        mThickness = typedArray.getDimensionPixelSize(R.styleable.CircledPicker_thicknessC,
                (int) CircledPickerUtils.convertDpToPixel(context, 5));
        mInnerThickness = mThickness - typedArray.getDimensionPixelSize(R.styleable.CircledPicker_innerThickness,
                (int) CircledPickerUtils.convertDpToPixel(context, 1));
        pickerMode = typedArray.getString(R.styleable.CircledPicker_pickerMode);

        if(pickerMode != null) {
            mPickerMode = pickerMode.equalsIgnoreCase("hours") ? PickerMode.HOURS_AND_MINUTES :
                    pickerMode.equalsIgnoreCase("minutes") ? PickerMode.MINUTES_AND_SECONDS :
                            pickerMode.equalsIgnoreCase("numeric") ? PickerMode.NUMERIC :
                                    PickerMode.PERCENT;
        } else {
            mPickerMode = PickerMode.NUMERIC;
        }

        mLastAngle = mCurrentSweep;
        mShadowRect = new RectF();
        mShadowInnerRect = new RectF();
        mTextBounds = new Rect();
        mArcRect = new RectF();
        mArcInnerRect = new RectF();
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        mAngleAnimator = ValueAnimator.ofFloat(0, 0);
        mPaint = new Paint();
        mPath = new Path();


        setValue((float)typedArray.getInt(R.styleable.CircledPicker_value, 0));


        typedArray.recycle();
        setClickable(true);
    }

    private void setCirclesBoundingBoxes() {
        mPath.reset();
        // Set the shadow circle's bounding box
        mShadowRect.left = mMidX - mRadius + mInnerThickness;
        mShadowRect.top = mMidY - mRadius + mInnerThickness;
        mShadowRect.right = mMidX + mRadius - mInnerThickness;
        mShadowRect.bottom = mMidY + mRadius - mInnerThickness;
        // Set the picker circle's bounding box
        mShadowInnerRect.left = mMidX - mInnerRadius - mInnerThickness;
        mShadowInnerRect.top = mMidY - mInnerRadius - mInnerThickness;
        mShadowInnerRect.right = mMidX + mInnerRadius + mInnerThickness;
        mShadowInnerRect.bottom = mMidY + mInnerRadius + mInnerThickness;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int min = MeasureSpec.getSize(widthMeasureSpec);
        int width = min;
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if(height<width && height != 0){
            min = height;
        }

        mMidX = min / 2;
        mMidY = min / 2;
        mRadius = mMidY;

        if(mTextSize == 0) {
            mTextSize = mRadius * .3f;
        }
        mInnerRadius = mRadius - mThickness;
        mArcRect.left = mMidX - mRadius;
        mArcRect.top = mMidY - mRadius;
        mArcRect.right = mMidX + mRadius;
        mArcRect.bottom = mMidY + mRadius;
        mArcInnerRect.left = mMidX - mInnerRadius;
        mArcInnerRect.top = mMidY - mInnerRadius;
        mArcInnerRect.right = mMidX + mInnerRadius;
        mArcInnerRect.bottom = mMidY + mInnerRadius;

        if(min == width){
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }else{
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        }
    }
}