package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import goodmonit.monit.com.kao.R;
import goodmonit.monit.com.kao.constants.TypeFace;

/**
 * Created by Jake on 2017-05-26.
 */

public class NotoButton extends androidx.appcompat.widget.AppCompatButton {
    private int mDrawableWidth, mDrawableHeight;
    private boolean mShowUnderLine;

    public NotoButton(Context context) {
        super(context);
        _setTypeFace(context, null);
        _setDrawable(context, null, 0, 0);
    }

    public NotoButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        _setTypeFace(context, attrs);
        _setProperty(context, attrs, 0, 0);
        _setDrawable(context, attrs, 0, 0);
    }

    public NotoButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        _setTypeFace(context, attrs);
        _setProperty(context, attrs, 0, 0);
        _setDrawable(context, attrs, defStyleAttr, 0);
    }

    private void _setTypeFace(Context context, AttributeSet attrs) {
        if (attrs == null) {
            this.setTypeface(Typeface.createFromAsset(context.getAssets(), TypeFace.REGULAR));
        } else {
            int[] fontFamily = new int[] { android.R.attr.fontFamily };
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, fontFamily);
            String font = typedArray.getString(0);
            if ("medium".equals(font)) {
                this.setTypeface(Typeface.createFromAsset(context.getAssets(), TypeFace.MEDIUM));
            } else if ("light".equals(font)) {
                this.setTypeface(Typeface.createFromAsset(context.getAssets(), TypeFace.DEMILIGHT));
            } else {
                this.setTypeface(Typeface.createFromAsset(context.getAssets(), TypeFace.REGULAR));
            }
        }
    }

    private void _setProperty(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Link, defStyleAttr, defStyleRes);
        try {
            mShowUnderLine = array.getBoolean(R.styleable.Link_setUnderline, false);
        } finally {
            array.recycle();
        }
        if (mShowUnderLine) {
            this.setPaintFlags(this.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    private void _setDrawable(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CompoundDrawable, defStyleAttr, defStyleRes);

        try {
            mDrawableWidth = array.getDimensionPixelSize(R.styleable.CompoundDrawable_compoundDrawableWidth, -1);
            mDrawableHeight = array.getDimensionPixelSize(R.styleable.CompoundDrawable_compoundDrawableHeight, -1);
        } finally {
            array.recycle();
        }

        if (mDrawableWidth > 0 || mDrawableHeight > 0) {
            _initCompoundDrawableSize();
        }
    }

    private void _initCompoundDrawableSize() {
        Drawable[] drawables = getCompoundDrawables();
        for (Drawable drawable : drawables) {
            if (drawable == null) {
                continue;
            }
            drawable.setBounds(0, 0, mDrawableWidth, mDrawableHeight);
        }
        setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
    }

    public void setTypeface(String typeface) {
        if ("medium".equals(typeface)) {
            this.setTypeface(Typeface.createFromAsset(getContext().getAssets(), TypeFace.MEDIUM));
        } else if ("light".equals(typeface)) {
            this.setTypeface(Typeface.createFromAsset(getContext().getAssets(), TypeFace.DEMILIGHT));
        } else {
            this.setTypeface(Typeface.createFromAsset(getContext().getAssets(), TypeFace.REGULAR));
        }
    }

}
