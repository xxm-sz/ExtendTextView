package com.zhangws;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhangws.util.SystemUtil;

/**
 * 折叠TextView
 * 当TextView行数大于{@link ExtendTextView#maxLine} 折叠情况只显示 maxLine行
 * 当行数小于{@link ExtendTextView#maxLine}则显示实际行数，并隐藏折叠指示器
 */
public class ExtendTextView extends RelativeLayout implements ExtendView {

    private static final String TAG = "ExpandTextView";

    private final static int STATUS_EXPEND = 1;

    private final static int STATUS_FOLD = 2;

    private int status = STATUS_FOLD;

    private TextView mTextView;

    private ImageView ivIndicator;

    //折叠时 显示的函数
    private int maxLine = 3;

    private int lineSpace = SystemUtil.dp2px(getContext(), 3f);

    private long mAnimationDuration = 1000;

    private String text;

    private int textSize = 15;
    private Drawable mIndicatorSrc;

    private int lineCount = 0;

    private boolean clickable = false;

    private int mTextColor;
    private float mTextSize;
    private float mTextLineSpace;
    private String mText;

    private float mLeftMargin = SystemUtil.dp2px(getContext(), 16f);
    private float mRightMargin = mLeftMargin;
    private int mIndicatorPosition;

    private float mTextPadding = 0;
    private float mTextPaddingLeft = mTextPadding;
    private float mTextPaddingRight = mTextPadding;
    private float mTextPaddingTop = mTextPadding;
    private float mTextPaddingBottom = mTextPadding;


    private OnExtendListener mListener;

    private float mSpacingMult = 1;


    public ExtendTextView(Context context) {
        this(context, null);
    }

    public ExtendTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExtendTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAttrs(context, attrs);
        initView(context);
        initListener();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExtendTextView);

        mTextColor = typedArray.getColor(R.styleable.ExtendTextView_text_color, Color.BLACK);
        mTextSize = typedArray.getDimension(R.styleable.ExtendTextView_text_size, mLeftMargin);
        mTextLineSpace = typedArray.getDimension(R.styleable.ExtendTextView_text_line_space, SystemUtil.dp2px(context, 3f));
        CharSequence text = typedArray.getText(R.styleable.ExtendTextView_text);
        if (text != null) {
            mText = text.toString();
        }

        mTextPadding = typedArray.getDimension(R.styleable.ExtendTextView_text_padding, 0);
        mIndicatorSrc = typedArray.getDrawable(R.styleable.ExtendTextView_indicator);
        mAnimationDuration = typedArray.getInteger(R.styleable.ExtendTextView_animation_duration, 1000);

        mTextPaddingLeft = typedArray.getDimension(R.styleable.ExtendTextView_text_padding_left, mLeftMargin);
        mTextPaddingRight = typedArray.getDimension(R.styleable.ExtendTextView_text_padding_right, mLeftMargin);
        mTextPaddingTop = typedArray.getDimension(R.styleable.ExtendTextView_text_padding_top, mLeftMargin);
        mTextPaddingBottom = typedArray.getDimension(R.styleable.ExtendTextView_text_padding_bottom, mLeftMargin);
        mLeftMargin = typedArray.getDimension(R.styleable.ExtendTextView_indicator_margin_left, mLeftMargin);
        mRightMargin = typedArray.getDimension(R.styleable.ExtendTextView_indicator_margin_right, mLeftMargin);

        mIndicatorPosition = typedArray.getInt(R.styleable.ExtendTextView_indicator_position, CENTER_HORIZONTAL);
    }

    private void initListener() {

        setOnClickListener(v -> {
            if (!clickable) {
                return;
            }
            mTextView.clearAnimation();
            if (status == STATUS_FOLD) {
                startExtend();
            } else {
                startFold();
            }

        });
    }

    private void startFold() {
        status = STATUS_FOLD;
        if (mListener != null) {
            mListener.fold();
        }
        foldIndicatorAnimator();
        textAnimation(mTextView.getHeight(), calculateHeight(maxLine) - mTextView.getHeight());
    }

    private void startExtend() {
        status = STATUS_EXPEND;
        if (mListener != null) {
            mListener.expand();
        }

        extendIndicatorAnimation();
        textAnimation(mTextView.getHeight(), calculateHeight(mTextView.getLineCount()) - mTextView.getHeight());


    }

    private int calculateHeight(int lineCount) {
        return mTextView.getLayout().getLineTop(lineCount) + mTextView.getCompoundPaddingTop() + mTextView.getCompoundPaddingBottom();
        //return (int) (lineCount * mTextView.getLineHeight() + lineCount * mLineSpaceHeight + mTextView.getPaddingTop() + mTextView.getPaddingBottom());
    }


    /**
     * 每次调用{@link ExtendTextView#setText(CharSequence)}函数，需要重新获取行数，进行TextView高度设置
     */
    public void registerGlobalLayoutListener() {
        mTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                lineCount = mTextView.getLineCount();

                if (lineCount < maxLine||lineCount==maxLine) {
                    clickable = false;
                    mTextView.setHeight(calculateHeight(mTextView.getLineCount()));
                    ivIndicator.setVisibility(View.GONE);
                } else {
                    clickable = true;
                    ivIndicator.setVisibility(View.VISIBLE);
                    mTextView.setHeight(calculateHeight(maxLine));
                }
                mTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }


    public String getText() {
        return mTextView.getText().toString();
    }

    public void setText(CharSequence sequence) {
        mTextView.setText(sequence);
        registerGlobalLayoutListener();
    }

    private void initView(Context mContext) {
        createTextView(mContext);
        createIndicatorView();
    }

    private void createTextView(Context mContext) {
        mTextView = new TextView(mContext);
        mTextView.setId(View.generateViewId());
        addView(mTextView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mTextView.setMaxLines(maxLine);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        mTextView.setText(mText);
        mTextView.setLineSpacing(mTextLineSpace, mSpacingMult);
        mTextView.setTextColor(mTextColor);
        if (mTextPadding == 0) {
            mTextView.setPadding((int) mTextPaddingLeft, (int) mTextPaddingTop, (int) mTextPaddingRight, (int) mTextPaddingBottom);
        } else {
            mTextView.setPadding((int) mTextPadding, (int) mTextPadding, (int) mTextPadding, (int) mTextPadding);
        }

        registerGlobalLayoutListener();
    }

    private void createIndicatorView() {
        ivIndicator = new ImageView(getContext());
        if (mIndicatorSrc != null) {
            ivIndicator.setImageDrawable(mIndicatorSrc);
        } else {
            ivIndicator.setImageResource(R.mipmap.extend_view_indicator);
        }
        addView(ivIndicator);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.width = SystemUtil.dp2px(getContext(), 15f);
        layoutParams.height = layoutParams.width;
        layoutParams.addRule(CENTER_HORIZONTAL);
        layoutParams.addRule(BELOW, mTextView.getId());
        layoutParams.bottomMargin = SystemUtil.dp2px(getContext(), 10f);
        ivIndicator.setLayoutParams(layoutParams);
        setIndicatorPosition(mIndicatorPosition);
    }


    @Override
    public void extend() {
        if (lineCount > maxLine) {
            startExtend();
        }
    }


    /**
     * 设置指示器的位置，左，中，下
     *
     * @param position
     * @see android.widget.RelativeLayout#CENTER_HORIZONTAL
     * @see android.widget.RelativeLayout#ALIGN_PARENT_LEFT
     * @see android.widget.RelativeLayout#ALIGN_PARENT_RIGHT
     */
    public void setIndicatorPosition(int position) {
        if (position == ALIGN_PARENT_LEFT) {
            LayoutParams layoutParams = (LayoutParams) ivIndicator.getLayoutParams();
            layoutParams.removeRule(CENTER_HORIZONTAL);
            layoutParams.removeRule(ALIGN_PARENT_RIGHT);
            layoutParams.addRule(position);
            layoutParams.leftMargin = (int) mLeftMargin;
            ivIndicator.setLayoutParams(layoutParams);
        } else if (position == ALIGN_PARENT_RIGHT) {
            LayoutParams layoutParams = (LayoutParams) ivIndicator.getLayoutParams();
            layoutParams.removeRule(CENTER_HORIZONTAL);
            layoutParams.removeRule(ALIGN_PARENT_LEFT);
            layoutParams.addRule(position);
            layoutParams.rightMargin = (int) mRightMargin;
            ivIndicator.setLayoutParams(layoutParams);
        } else {
            LayoutParams layoutParams = (LayoutParams) ivIndicator.getLayoutParams();
            layoutParams.removeRule(ALIGN_PARENT_RIGHT);
            layoutParams.removeRule(ALIGN_PARENT_LEFT);
            layoutParams.addRule(CENTER_HORIZONTAL);
            layoutParams.rightMargin = 0;
            layoutParams.leftMargin = 0;
            ivIndicator.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void fold() {
        if (lineCount > maxLine) {
            startFold();
        }
    }

    public interface OnExtendListener {
        void expand();

        void fold();
    }

    public void setListener(OnExtendListener listener) {
        mListener = listener;
    }

    public void setTextSize(int textSize) {
        mTextView.setTextSize(textSize);
    }

    /**
     * 折叠情况下，最大行数
     *
     * @return
     */
    public int getMaxLine() {
        return maxLine;
    }

    /**
     * 设置最大行数
     *
     * @param maxLine
     */
    public void setMaxLine(int maxLine) {
        this.maxLine = maxLine;
    }

    /**
     * TextView的边距
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setTextPadding(int left, int top, int right, int bottom) {
        mTextView.setPadding(left, top, right, bottom);
    }

    /**
     * 通过该行数设置自定义的参数，例如TextSize,TextColor等,涉及到布局大小改变的，设置之后
     * 需要再调用{@link ExtendTextView#registerGlobalLayoutListener()}修正大小
     *
     * @return
     * @see ExtendTextView#setTextPadding(int, int, int, int)
     */
    public TextView getTextView() {
        return mTextView;
    }

    /**
     * 指示器开始伸展动画
     */
    private void extendIndicatorAnimation() {
        Animation animation = new RotateAnimation(0f, 180f, Animation
                .RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(mAnimationDuration);
        animation.setFillAfter(true);
        ivIndicator.startAnimation(animation);
    }

    /**
     * 指示器开始折叠动画
     */
    private void foldIndicatorAnimator() {
        Animation animation = new RotateAnimation(180f, 0f, Animation
                .RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillAfter(true);
        animation.setDuration(mAnimationDuration);
        ivIndicator.startAnimation(animation);
    }

    /**
     * TextView折叠和伸展动画
     *
     * @param endValue
     * @param startValue
     */
    private void textAnimation(int startValue, int endValue) {
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                mTextView.setHeight((int) (startValue + endValue * interpolatedTime));
            }
        };
        animation.setDuration(mAnimationDuration);
        mTextView.startAnimation(animation);
    }

    public long getAnimationDuration() {
        return mAnimationDuration;
    }

    public void setAnimationDuration(long mAnimationDuration) {
        this.mAnimationDuration = mAnimationDuration;
    }
}
