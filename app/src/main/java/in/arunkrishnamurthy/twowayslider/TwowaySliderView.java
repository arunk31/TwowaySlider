package in.arunkrishnamurthy.twowayslider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by arunk on 13-12-2016.
 */
public class TwowaySliderView extends View {

    // Interface definition to be implemented at calling file / module
    public interface OnTwowaySliderListener {
        public void onSliderMoveLeft();

        public void onSliderMoveRight();

        public void onSliderLongPress();
    }

    // View global variables
    private Context context;
    private int measuredWidth, measuredHeight;  // height and weigth of the view
    private float density;
    private OnTwowaySliderListener listener;
    private Paint mBackgroundPaint, mSliderPaint, mImagePaint; // paint that has to be drawn
    private float rx, ry; // Corner radius
    private Path mRoundedRectPath;
    private int sliderImage = 0, leftImage = 0, rightImage = 0;

    float x; // circles x position
    float imageTop;
    float event_x, event_y; // run time view moved position
    float radius; // circles radius
    float X_MIN, X_MAX; // min and max boundaries of background
    private boolean ignoreTouchEvents;  // Should we ignore the movement event

    private boolean cancelOnYExit; // Do we cancel when the Y coordinate leaves the view?
    private boolean useDefaultCornerRadiusX, useDefaultCornerRadiusY, noSliderImage, noLeftImage, noRightImage;  // Do we use default corner radius if not provided

    // Default values
    int backgroundColor = 0xFF807B7B;
    int sliderColor = 0xAA404040;
    boolean fillCircle = false;

    // TwowaySlider view constructors
    public TwowaySliderView(Context context) {
        super(context, null, 0);
        init(context, null, 0);
    }

    public TwowaySliderView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context, attrs, 0);
    }

    public TwowaySliderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    // Setter and Getter for TwowaySlider event listeners
    public OnTwowaySliderListener getListener() {
        return listener;
    }

    public void setListener(OnTwowaySliderListener listener) {
        this.listener = listener;
    }

    // Initialization of view
    private void init(Context context, AttributeSet attrs, int style) {
        this.context = context;
        // Get the attributes set by user, if not available use default
        Resources res = getResources();
        density = res.getDisplayMetrics().density;
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TwowaySliderView, style, 0);
        rx = a.getDimension(R.styleable.TwowaySliderView_cornerRadiusX, rx);
        useDefaultCornerRadiusX = rx == 0;
        ry = a.getDimension(R.styleable.TwowaySliderView_cornerRadiusY, ry);
        useDefaultCornerRadiusY = ry == 0;
        backgroundColor = a.getColor(R.styleable.TwowaySliderView_sliderBackgroundColor, backgroundColor);
        sliderColor = a.getColor(R.styleable.TwowaySliderView_sliderColor, sliderColor);
        fillCircle = a.getBoolean(R.styleable.TwowaySliderView_fillCircle, fillCircle);
        sliderImage = a.getResourceId(R.styleable.TwowaySliderView_sliderImage, sliderImage);
        noSliderImage = sliderImage == 0;
        leftImage = a.getResourceId(R.styleable.TwowaySliderView_leftImage, leftImage);
        noLeftImage = leftImage == 0;
        rightImage = a.getResourceId(R.styleable.TwowaySliderView_rightImage, rightImage);
        noRightImage = rightImage == 0;
        cancelOnYExit = a.getBoolean(R.styleable.TwowaySliderView_cancelOnYExit, false);
        a.recycle();
        // Initialize needed view components
        mRoundedRectPath = new Path();
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(backgroundColor);
        mSliderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if(fillCircle) {
            mSliderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        } else {
            mSliderPaint.setStyle(Paint.Style.STROKE);
        }
        mSliderPaint.setColor(sliderColor);
        mSliderPaint.setStrokeWidth(2 * density);
        if (!isInEditMode()) {
            float[] direction = new float[]{0.0f, -1.0f, 0.5f};
            MaskFilter filter = new EmbossMaskFilter(direction, 0.8f, 15f, 1f);
            mSliderPaint.setMaskFilter(filter);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measuredHeight = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        measuredWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        if (useDefaultCornerRadiusX) {
            rx = measuredHeight * 0.52f;
        }
        if (useDefaultCornerRadiusY) {
            ry = measuredHeight * 0.52f;
        }
        radius = measuredHeight * 0.38f;
        X_MIN = 1.2f * radius;
        X_MAX = measuredWidth - X_MIN;
        x = measuredWidth * 0.5f;
        imageTop = measuredHeight * 0.25f;
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private void drawRoundRect(Canvas c) {
        mRoundedRectPath.reset();
        mRoundedRectPath.moveTo(rx, 0);
        mRoundedRectPath.lineTo(measuredWidth - rx, 0);
        mRoundedRectPath.quadTo(measuredWidth, 0, measuredWidth, ry);
        mRoundedRectPath.lineTo(measuredWidth, measuredHeight - ry);
        mRoundedRectPath.quadTo(measuredWidth, measuredHeight, measuredWidth - rx, measuredHeight);
        mRoundedRectPath.lineTo(rx, measuredHeight);
        mRoundedRectPath.quadTo(0, measuredHeight, 0, measuredHeight - ry);
        mRoundedRectPath.lineTo(0, ry);
        mRoundedRectPath.quadTo(0, 0, rx, 0);
        c.drawPath(mRoundedRectPath, mBackgroundPaint);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (measuredHeight <= 0 || measuredWidth <= 0) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            canvas.drawRoundRect(0, 0, measuredWidth, measuredHeight, rx, ry, mBackgroundPaint);
        } else {
            drawRoundRect(canvas);
        }
        if (!noLeftImage) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), leftImage);
            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (measuredHeight * 0.6f), (int) (measuredHeight * 0.6f), true);
            float cx = 0 + (bitmap.getWidth()*0.25f);
            float cy = (measuredHeight - bitmap.getHeight()) * 0.5f;
            canvas.drawBitmap(bitmap, cx, cy, null);
        }
        if (!noRightImage) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), rightImage);
            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (measuredHeight * 0.6f), (int) (measuredHeight * 0.6f), true);
            float cx = measuredWidth - (bitmap.getWidth()*1.25f);
            float cy = (measuredHeight - bitmap.getHeight()) * 0.5f;
            canvas.drawBitmap(bitmap, cx, cy, null);
        }
        canvas.drawCircle(x, measuredHeight * 0.5f, radius, mSliderPaint);
        if (!noSliderImage) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), sliderImage);
            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (radius * 1.5f), (int) (radius * 1.5f), true);
            float cx = x - (radius * 0.75f);
            float cy = (measuredHeight - bitmap.getHeight()) * 0.5f;
            canvas.drawBitmap(bitmap, cx, cy, null);
            // canvas.drawBitmap(bitmap, x-(radius*0.75f), imageTop, null);
        }
    }

    private void onSlideRight() {
        if (listener != null) {
            listener.onSliderMoveRight();
        }
        vibrate(30);
    }

    private void onSlideLeft() {
        if (listener != null) {
            listener.onSliderMoveLeft();
        }
        vibrate(30);
    }

    private void onLongPress() {
        if (listener != null) {
            listener.onSliderLongPress();
        }
        vibrate(30);
        reset();
    }

    final Handler handler = new Handler();
    Runnable longPress = new Runnable() {
        public void run() {
            onLongPress();
        }
    };

    private void reset() {
        radius = measuredHeight * 0.38f;
        x = measuredWidth * 0.5f;
        invalidate();
    }

    private void vibrate(int value) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(value);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                ignoreTouchEvents = false;
                handler.removeCallbacks(longPress);
                reset();
                return true;
            case MotionEvent.ACTION_DOWN:
                radius = measuredHeight * 0.45f;
                event_x = event.getX(0);
                event_y = event.getY(0);
                double squareRadius = radius * radius;
                double squaredXDistance = (event_x - x) * (event_x - x);
                double squaredYDistance = (event_y - measuredHeight / 2) * (event_y - measuredHeight / 2);
                if (squaredXDistance + squaredYDistance > squareRadius) {
                    // User touched outside the button, ignore his touch
                    ignoreTouchEvents = true;
                } else {
                    vibrate(30);
                    invalidate();
                }
                handler.postDelayed(longPress, 1500);
                return true;
            case MotionEvent.ACTION_CANCEL:
                handler.removeCallbacks(longPress);
                ignoreTouchEvents = true;
                reset();
            case MotionEvent.ACTION_MOVE:
                if (!ignoreTouchEvents) {
                    radius = measuredHeight * 0.45f;
                    event_x = event.getX(0);
                    double min_val = (measuredWidth / 2) - (radius / 3);
                    double max_val = (measuredWidth / 2) + (radius / 3);
                    if (!((max_val > event_x) && (min_val < event_x))) {
                        handler.removeCallbacks(longPress);
                    }
                    vibrate(2);
                    if (cancelOnYExit) {
                        event_y = event.getY(0);
                        if (event_y < 0 || event_y > measuredHeight) {
                            ignoreTouchEvents = true;
                            reset();
                        }
                    }
                    x = event_x > X_MAX ? X_MAX : event_x < X_MIN ? X_MIN : event_x;
                    if (event_x >= X_MAX) {
                        ignoreTouchEvents = true;
                        onSlideRight();
                    } else if (event_x <= X_MIN) {
                        ignoreTouchEvents = true;
                        onSlideLeft();
                    }
                    invalidate();
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }
}
