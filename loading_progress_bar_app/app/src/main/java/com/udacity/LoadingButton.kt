package com.udacity

import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.animation.ValueAnimator.RESTART
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.widget.ProgressBar
import androidx.core.content.withStyledAttributes
import kotlin.math.min
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ProgressBar(context, attrs, defStyleAttr) {
    //Share by Progress Bar and circle
    private val DURATION = 3000L

    private var progressCircleRadius = 0f
    private var currentProgressAngle = 0f
    private var widthSize = 0
    private var heightSize = 0

    private lateinit var progressAnimator: ValueAnimator

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Loading, ButtonState.Clicked -> {
                progressAnimator.start()
            }
            ButtonState.Completed -> {
                rectProgressBar.right = 0 // reset
            }
        }
    }

    // Colours
    private var buttonBgColor = 0
    private var buttonProgressColor = 0
    private var circleColor = 0
    private var loadingTextColor = 0

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val buttonBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val buttonLoadingPaint = Paint(buttonBackgroundPaint)
    private val circlePaint = Paint(buttonBackgroundPaint)

    // Button Rect which we set the size in onMeasured()
    private val rectButtonBackground = Rect(0, 0, widthSize, heightSize)
    private val rectProgressBar = Rect(0, 0, widthSize, heightSize)
    private val rectProgressCircle = RectF()

    // Button Text
    private var textLoading = ""
    private var textDownload = ""

    init {
        isClickable = true

        context.apply {
            textLoading = getString(R.string.button_loading)
            textDownload = getString(R.string.button_download)
        }

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonBgColor = getColor(R.styleable.LoadingButton_defaultBackgroundColor, 0)
            buttonProgressColor = getColor(R.styleable.LoadingButton_loadingBackgroundColor, 0)
            circleColor = getColor(R.styleable.LoadingButton_circleColor, 0)
            loadingTextColor = getColor(R.styleable.LoadingButton_loadingTextColor, 0)
        }

        // TextPaint init
        textPaint.apply {
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
            color = loadingTextColor
            textSize = 55f
        }

        buttonBackgroundPaint.apply {
            style = Paint.Style.FILL
            color = buttonBgColor
        }

        buttonLoadingPaint.color = buttonProgressColor
        circlePaint.color = circleColor
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.drawColor(buttonBgColor)
            it.drawProgress()
            it.drawButtonText()
        }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.i("ONSizechanged", "onSizeChanged() is called")
        super.onSizeChanged(w, h, oldw, oldh)
        initProgressAnimator()
        calculateProgressCircle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.i("ONMeasure", "onMeasure() is called")
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)

        // update rect
        rectButtonBackground.apply {
            right = widthSize
            bottom = heightSize
        }

        // We just need bottom to get height of the progress bar
        rectProgressBar.bottom = heightSize

        // circle set to 15% of whichever it is minimum
        progressCircleRadius = (min(w, h) * 0.15f)
    }

    override fun performClick(): Boolean {
        Log.i("STATUS", "${buttonState}")
        if (super.performClick()) {
            invalidate()
            return true
        }
        return true
    }

    // Expose public method for main activity to set state
    fun setState(state: ButtonState) {
        if (state != buttonState) {
            Log.i("STATE", "Setting the state to ${state}")
            buttonState = state
        } //else no_op
    }

    // Calculate Progress circle
    private fun calculateProgressCircle() {
        // circle offset by the button text
        // we are making assumption that textLoading is longer than textDownload
        val circleX = rectButtonBackground.exactCenterX() + textPaint.measureText(textLoading) / 1.5f
        val circleY = rectButtonBackground.exactCenterY()
        rectProgressCircle.set(
            circleX - progressCircleRadius,
            circleY - progressCircleRadius,
            circleX + progressCircleRadius,
            circleY + progressCircleRadius
        )
    }

    //Animator
    private fun initProgressAnimator() {
        ValueAnimator.ofInt(0, widthSize).apply {
            duration = DURATION
            repeatMode = RESTART
            repeatCount = INFINITE
        }.also {
            progressAnimator = it
        }.addUpdateListener {
            rectProgressBar.right = it.animatedValue as Int

            // This will calculate the progress angle corresponding to the
            // width. This helps to get progress bar and circle in-sync
            currentProgressAngle = rectProgressBar.right.toFloat() / widthSize.toFloat() * 360f

            // Change the state to Complete, when the bar reaches the end.
            if (rectProgressBar.right == widthSize - 1) {
                buttonState = ButtonState.Completed
            }
            invalidate()
        }
    }

    private fun Canvas.drawButtonText() {
        val text = when (buttonState) {
            ButtonState.Loading, ButtonState.Clicked -> textLoading
            else -> textDownload
        }
        drawText(
            text,
            rectButtonBackground.exactCenterX(),
            rectButtonBackground.exactCenterY(), textPaint
        )
    }

    private fun Canvas.drawProgress() {
        // button background
        if (buttonState != ButtonState.Completed) {
            drawRect(rectProgressBar, buttonLoadingPaint)
            drawArc(
                rectProgressCircle,
                0f,
                currentProgressAngle,
                true,
                circlePaint
            )
        } // else no-op
    }
}