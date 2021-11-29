package com.polestar.charging.ui.cluster.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.example.amaptest.R;

public class IconGenerator {
    private final Context mContext;
    private final float mDensity;

    private ViewGroup mContainer;
    private RotationLayout mRotationLayout;
    private TextView mTextView;

    private int mRotation;

    private float mAnchorU = 0.5f;
    private float mAnchorV = 1f;
    private BubbleDrawable mBackground;

    private static final int[] BUCKETS = {10, 20, 50, 100, 200, 500, 1000};
    private ShapeDrawable mColoredCircleBackground;
    private SparseArray<BitmapDescriptor> mIcons = new SparseArray<>();

    /**
     * Creates a new IconGenerator with the default style.
     */
    public IconGenerator(Context context) {
        mContext = context;
        mDensity = context.getResources().getDisplayMetrics().density;
        mBackground = new BubbleDrawable(mContext);
        mContainer = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.amu_text_bubble, null);
        mRotationLayout = (RotationLayout) mContainer.getChildAt(0);

        initDefaultStyle();
    }

    private void initDefaultStyle(){
        setStyle(STYLE_DEFAULT);
        setContentView(makeSquareTextView(mContext));
        setTextAppearance(R.style.amu_ClusterIcon_TextAppearance);
        setBackground(makeClusterBackground());
    }

    private LayerDrawable makeClusterBackground() {
        mColoredCircleBackground = new ShapeDrawable(new OvalShape());
        ShapeDrawable outline = new ShapeDrawable(new OvalShape());
        outline.getPaint().setColor(0x80ffffff); // Transparent white.
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline, mColoredCircleBackground});
        int strokeWidth = (int) (mDensity * 3);
        background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        return background;
    }

    protected int getBucket(int size) {
        if (size <= BUCKETS[0]) {
            return size;
        }
        for (int i = 0; i < BUCKETS.length - 1; i++) {
            if (size < BUCKETS[i + 1]) {
                return BUCKETS[i];
            }
        }
        return BUCKETS[BUCKETS.length - 1];
    }

    protected int getColor(int clusterSize) {
        final float hueRange = 220;
        final float sizeRange = 300;
        final float size = Math.min(clusterSize, sizeRange);
        final float hue = (sizeRange - size) * (sizeRange - size) / (sizeRange * sizeRange) * hueRange;
        return Color.HSVToColor(new float[]{
                hue, 1f, .6f
        });
    }

    public BitmapDescriptor getDescriptorForCluster(int size) {
        int bucket = getBucket(size);
        BitmapDescriptor descriptor = mIcons.get(bucket);
        if (descriptor == null) {
            mColoredCircleBackground.getPaint().setColor(getColor(bucket));
            descriptor = BitmapDescriptorFactory.fromBitmap(makeIcon(getClusterText(bucket)));
            mIcons.put(bucket, descriptor);
        }
        return descriptor;
    }

    private String getClusterText(int bucket) {
        if (bucket < BUCKETS[0]) {
            return String.valueOf(bucket);
        }
        return bucket + "+";
    }

    private SquareTextView makeSquareTextView(Context context) {
        SquareTextView squareTextView = new SquareTextView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        squareTextView.setLayoutParams(layoutParams);
        squareTextView.setId(R.id.amu_text);
        int twelveDpi = (int) (12 * mDensity);
        squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi);
        return squareTextView;
    }

    public Bitmap makeIcon(CharSequence text) {
        if (mTextView != null) {
            mTextView.setText(text);
        }

        return makeIcon();
    }

    public Bitmap makeIcon() {
        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        mContainer.measure(measureSpec, measureSpec);

        int measuredWidth = mContainer.getMeasuredWidth();
        int measuredHeight = mContainer.getMeasuredHeight();

        mContainer.layout(0, 0, measuredWidth, measuredHeight);

        if (mRotation == 1 || mRotation == 3) {
            measuredHeight = mContainer.getMeasuredWidth();
            measuredWidth = mContainer.getMeasuredHeight();
        }

        Bitmap r = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        r.eraseColor(Color.TRANSPARENT);

        Canvas canvas = new Canvas(r);

        switch (mRotation) {
            case 0:
                // do nothing
                break;
            case 1:
                canvas.translate(measuredWidth, 0);
                canvas.rotate(90);
                break;
            case 2:
                canvas.rotate(180, measuredWidth / 2, measuredHeight / 2);
                break;
            case 3:
                canvas.translate(0, measuredHeight);
                canvas.rotate(270);
                break;
        }
        mContainer.draw(canvas);
        return r;
    }

    public void setContentView(View contentView) {
        mRotationLayout.removeAllViews();
        mRotationLayout.addView(contentView);
        final View view = mRotationLayout.findViewById(R.id.amu_text);
        mTextView = view instanceof TextView ? (TextView) view : null;
    }

    public void setContentRotation(int degrees) {
        mRotationLayout.setViewRotation(degrees);
    }

    public void setRotation(int degrees) {
        mRotation = ((degrees + 360) % 360) / 90;
    }

    public float getAnchorU() {
        return rotateAnchor(mAnchorU, mAnchorV);
    }

    public float getAnchorV() {
        return rotateAnchor(mAnchorV, mAnchorU);
    }

    private float rotateAnchor(float u, float v) {
        switch (mRotation) {
            case 0:
                return u;
            case 1:
                return 1 - v;
            case 2:
                return 1 - u;
            case 3:
                return v;
        }
        throw new IllegalStateException();
    }

    public void setTextAppearance(Context context, int resid) {
        if (mTextView != null) {
            mTextView.setTextAppearance(context, resid);
        }
    }

    public void setTextAppearance(int resid) {
        setTextAppearance(mContext, resid);
    }

    public void setStyle(int style) {
        setColor(getStyleColor(style));
        setTextAppearance(mContext, getTextStyle(style));
    }

    public void setColor(int color) {
        mBackground.setColor(color);
        setBackground(mBackground);
    }

    public void setBackground(Drawable background) {
        mContainer.setBackgroundDrawable(background);

        // Force setting of padding.
        // setBackgroundDrawable does not call setPadding if the background has 0 padding.
        if (background != null) {
            Rect rect = new Rect();
            background.getPadding(rect);
            mContainer.setPadding(rect.left, rect.top, rect.right, rect.bottom);
        } else {
            mContainer.setPadding(0, 0, 0, 0);
        }
    }

    public static final int STYLE_DEFAULT = 1;
    public static final int STYLE_WHITE = 2;
    public static final int STYLE_RED = 3;
    public static final int STYLE_BLUE = 4;
    public static final int STYLE_GREEN = 5;
    public static final int STYLE_PURPLE = 6;
    public static final int STYLE_ORANGE = 7;

    private static int getStyleColor(int style) {
        switch (style) {
            default:
            case STYLE_DEFAULT:
            case STYLE_WHITE:
                return 0xffffffff;
            case STYLE_RED:
                return 0xffcc0000;
            case STYLE_BLUE:
                return 0xff0099cc;
            case STYLE_GREEN:
                return 0xff669900;
            case STYLE_PURPLE:
                return 0xff9933cc;
            case STYLE_ORANGE:
                return 0xffff8800;
        }
    }

    private static int getTextStyle(int style) {
        switch (style) {
            default:
            case STYLE_DEFAULT:
            case STYLE_WHITE:
                return R.style.amu_Bubble_TextAppearance_Dark;
            case STYLE_RED:
            case STYLE_BLUE:
            case STYLE_GREEN:
            case STYLE_PURPLE:
            case STYLE_ORANGE:
                return R.style.amu_Bubble_TextAppearance_Light;
        }
    }
}
