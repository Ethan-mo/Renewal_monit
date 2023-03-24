package goodmonit.monit.com.kao.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

/**
 * Created by Jake on 2017-05-26.
 */

public class NotoEditText extends androidx.appcompat.widget.AppCompatEditText {
    private String mFontFamily;
    private Context mContext;

    public NotoEditText(Context context) {
        super(context);
        _setType(context, null);
    }

    public NotoEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        int[] fontFamily = new int[] { android.R.attr.fontFamily };
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, fontFamily);
        String font = typedArray.getString(0);
        _setType(context, font);
    }

    public NotoEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int[] fontFamily = new int[] { android.R.attr.fontFamily };
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, fontFamily);
        String font = typedArray.getString(0);
        _setType(context, font);
    }

    private void _setType(Context context, String fontFamily) {
        this.mContext = context;
        this.mFontFamily = fontFamily;
        /*
        if ("medium".equals(mFontFamily)) {
            this.setTypeface(Typeface.createFromAsset(context.getAssets(), "NotoSansKR-Medium-Hestia.otf"));
        } else if ("light".equals(mFontFamily)) {
            this.setTypeface(Typeface.createFromAsset(context.getAssets(), "NotoSansKR-DemiLight-Hestia.otf"));
        } else {
            this.setTypeface(Typeface.createFromAsset(context.getAssets(), "NotoSansKR-Regular-Hestia.otf"));
        }
        */

        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.length() == 0) { // 아무것도 입력되지 않았을때, Hint 글꼴은 regular
                    setTypeface(Typeface.createFromAsset(mContext.getAssets(), "NotoSansKR-Regular-Hestia.otf"));
                } else {
                    if ("medium".equals(mFontFamily)) {
                        setTypeface(Typeface.createFromAsset(mContext.getAssets(), "NotoSansKR-Medium-Hestia.otf"));
                    } else if ("light".equals(mFontFamily)) {
                        setTypeface(Typeface.createFromAsset(mContext.getAssets(), "NotoSansKR-DemiLight-Hestia.otf"));
                    } else {
                        setTypeface(Typeface.createFromAsset(mContext.getAssets(), "NotoSansKR-Regular-Hestia.otf"));
                    }
                }
            }
        });
    }
}
