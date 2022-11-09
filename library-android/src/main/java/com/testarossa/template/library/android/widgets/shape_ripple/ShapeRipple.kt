package com.testarossa.template.library.android.widgets.shape_ripple

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import com.testarossa.template.library.android.R
import com.testarossa.template.library.android.utils.toPixel
import java.lang.Float.max
import java.util.*

class ShapeRipple : View {
    /**
     * Base ripple color, only used when [.enableColorTransition] flag is set to false
     */
    private var rippleColor = 0

    /**
     * Starting color for the color transition of the ripple, only
     * used when [.enableColorTransition] flag is set to true
     */
    private var rippleFromColor = 0

    /**
     * End color for the color transition of the ripple, only
     * used when [.enableColorTransition] flag is set to true
     */
    private var rippleToColor = 0

    /**
     * Base ripple duration for the animation, by default the value is {@value DEFAULT_RIPPLE_DURATION}
     */
    private var rippleDuration = 0

    /**
     * Base stroke width for each of the ripple
     */
    private var rippleStrokeWidth = 0

    /**
     * Ripple interval handles the actual timing of each spacing
     * of ripples in the list, calculated in [.onMeasure]
     */
    private var rippleInterval = 0f

    /**
     * Ripple maximum radius that will be used instead of the pre-calculated value, default value is
     * the size of the layout.
     */
    private var rippleMaximumRadius = 0f

    /**
     * Ripple interval factor is the spacing for each ripple
     * the more the factor the more the spacing
     */
    private var rippleIntervalFactor = 0f

    /**
     * Ripple count that will be rendered in the layout, default value is calculated based on the
     * layout_width / ripple_width
     */
    private var rippleCount = 0

    /**
     * The width of the view in the layout which is calculated in [.onMeasure]
     */
    private var viewWidth = 0

    /**
     * The height of the view in the layout which is calculated in [.onMeasure]
     */
    private var viewHeight = 0

    /**
     * The maximum radius of the ripple which is calculated in the [.onMeasure]
     */
    private var maxRippleRadius = 0

    /**
     * The last multiplier value of the animation after invalidation of this view
     */
    private var lastMultiplierValue = 0f
    /**
     * @return True if color transition is enabled
     */
    /**
     * Enables the color transition for each ripple
     *
     * @param enableColorTransition flag for enabling color trasition
     */
    /**
     * Enables the color transition for each ripple, it is true by default
     */
    var isEnableColorTransition = true

    /**
     * Enables the single ripple, it is false by default
     */
    private var enableSingleRipple = false

    /**
     * Enables the random positioning of the ripple, it is false by default
     */
    private var enableRandomPosition = false

    /**
     * Enable the random color of the ripple, it is false by default
     */
    private var enableRandomColor = false
    /**
     * @return True if it is using STROKE style for each ripple
     */
    /**
     * Enables the stroke style of each ripple
     *
     * @param enableStrokeStyle flag for enabling STROKE style
     */
    /**
     * Enables the stroke style of the ripples, it is false by default
     *
     * This means that if it is enabled it will use the [Paint.setStyle] as
     * [Paint.Style.STROKE], by default it will use the [Paint.Style.FILL].
     *
     */
    var isEnableStrokeStyle = false
        set(enableStrokeStyle) {
            field = enableStrokeStyle
            if (enableStrokeStyle) {
                shapePaint.style = Paint.Style.STROKE
            } else {
                shapePaint.style = Paint.Style.FILL
            }
        }

    /**
     * The list of [ShapeRippleEntry] which is rendered in [.render]
     */
    private lateinit var shapeRippleEntries: Deque<ShapeRippleEntry>

    /**
     * The list of developer predefined random colors which is used when [.enableRandomColor] is set to true.
     *
     *
     * If this is not defined by the developer it will have a default value from [ShapePulseUtil.generateRandomColours]
     */
    private lateinit var rippleRandomColors: MutableList<Int>

    /**
     * The actual animator for the ripples, used in [.render]
     */
    private var rippleValueAnimator: ValueAnimator? = null

    /**
     * The [Interpolator] of the [.rippleValueAnimator], by default it is [LinearInterpolator]
     */
    private lateinit var rippleInterpolator: Interpolator

    /**
     * The renderer of shape ripples which is drawn in the [BaseShape.onDraw]
     */
    private lateinit var rippleShape: BaseShape

    /**
     * The default paint for the ripple
     */
    private lateinit var shapePaint: Paint
    private var autoStart = true

    /**
     * This flag will handle that it was stopped by the user
     */
    private var isStopped = false

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        // initialize the paint for the shape ripple
        shapePaint = Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL
        }

        shapeRippleEntries = LinkedList()
        rippleShape = Circle()
        rippleShape.onSetup(context, shapePaint)
        rippleColor = DEFAULT_RIPPLE_COLOR
        rippleFromColor = DEFAULT_RIPPLE_FROM_COLOR
        rippleToColor = DEFAULT_RIPPLE_TO_COLOR
        rippleStrokeWidth = 15f.toPixel.toInt()
        rippleRandomColors = generateRandomColours()
        rippleDuration = DEFAULT_RIPPLE_DURATION
        rippleIntervalFactor = DEFAULT_RIPPLE_INTERVAL_FACTOR
        rippleInterpolator = LinearInterpolator()
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.ShapeRipple, 0, 0)
            try {
                rippleColor =
                    ta.getColor(R.styleable.ShapeRipple_ripple_color, DEFAULT_RIPPLE_COLOR)
                rippleFromColor = ta.getColor(
                    R.styleable.ShapeRipple_ripple_from_color,
                    DEFAULT_RIPPLE_FROM_COLOR
                )
                rippleToColor =
                    ta.getColor(R.styleable.ShapeRipple_ripple_to_color, DEFAULT_RIPPLE_TO_COLOR)
                setRippleDuration(
                    ta.getInteger(
                        R.styleable.ShapeRipple_ripple_duration,
                        DEFAULT_RIPPLE_DURATION
                    )
                )
                isEnableColorTransition =
                    ta.getBoolean(R.styleable.ShapeRipple_enable_color_transition, true)
                enableSingleRipple =
                    ta.getBoolean(R.styleable.ShapeRipple_enable_single_ripple, false)
                enableRandomPosition =
                    ta.getBoolean(R.styleable.ShapeRipple_enable_random_position, false)
                rippleMaximumRadius = ta.getDimensionPixelSize(
                    R.styleable.ShapeRipple_ripple_maximum_radius,
                    NO_VALUE
                ).toFloat()
                rippleCount = ta.getInteger(R.styleable.ShapeRipple_ripple_count, NO_VALUE)
                isEnableStrokeStyle =
                    ta.getBoolean(R.styleable.ShapeRipple_enable_stroke_style, false)
                setEnableRandomColor(
                    ta.getBoolean(
                        R.styleable.ShapeRipple_enable_random_color,
                        false
                    )
                )
                setRippleStrokeWidth(
                    ta.getDimensionPixelSize(
                        R.styleable.ShapeRipple_ripple_stroke_width,
                        15f.toPixel.toInt()
                    )
                )
                autoStart = ta.getBoolean(R.styleable.ShapeRipple_ripple_auto_start, true)
            } finally {
                ta.recycle()
            }
        }
        if (autoStart) {
            start(rippleDuration)
        }
    }

    private fun evaluateTransitionColor(fraction: Float, startValue: Int, endValue: Int): Int {
        val startA = startValue shr 24 and 0xff
        val startR = startValue shr 16 and 0xff
        val startG = startValue shr 8 and 0xff
        val startB = startValue and 0xff
        val endA = endValue shr 24 and 0xff
        val endR = endValue shr 16 and 0xff
        val endG = endValue shr 8 and 0xff
        val endB = endValue and 0xff
        return startA + (fraction * (endA - startA)).toInt() shl 24 or
                (startR + (fraction * (endR - startR)).toInt() shl 16) or
                (startG + (fraction * (endG - startG)).toInt() shl 8) or
                startB + (fraction * (endB - startB)).toInt()
    }

    private fun generateRandomColours(): MutableList<Int> {
        return mutableListOf(
            Color.parseColor("#673AB7"),
            Color.parseColor("#3F51B5"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#03A9F4"),
            Color.parseColor("#00BCD4"),
            Color.parseColor("#009688"),
            Color.parseColor("#8BC34A"),
            Color.parseColor("#4CAF50"),
            Color.parseColor("#FF5722"),
            Color.parseColor("#F44336")
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (shapeRippleEntry in shapeRippleEntries) {
            if (shapeRippleEntry.isRender) {
                // Each ripple entry is a rendered as a shape
                shapeRippleEntry.baseShape.onDraw(
                    canvas, shapeRippleEntry.x,
                    shapeRippleEntry.y,
                    shapeRippleEntry.radiusSize,
                    shapeRippleEntry.changingColorValue,
                    shapeRippleEntry.rippleIndex,
                    shapePaint
                )
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // Get the measure base of the measure spec
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        initializeEntries()
        rippleShape.width = (viewWidth)
        rippleShape.height = (viewHeight)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        stop()
    }

    /**
     * This method will initialize the list of [ShapeRippleEntry] with
     * initial position, color, index, and multiplier value.)
     *
     * @param shapeRipple the renderer of shape ripples
     */
    private fun initializeEntries() {
        // Sets the stroke width of the ripple
        shapePaint.strokeWidth = rippleStrokeWidth.toFloat()
        if (viewWidth == 0 && viewHeight == 0) {
            return
        }

        // we remove all the shape ripples entries
        shapeRippleEntries.clear()

        // the ripple radius based on the x or y
        maxRippleRadius =
            if (rippleMaximumRadius != NO_VALUE.toFloat()) rippleMaximumRadius.toInt() else Math.min(
                viewWidth,
                viewHeight
            ) / 2 - rippleStrokeWidth / 2

        // Calculate the max number of ripples
        rippleCount =
            if (rippleCount > NO_VALUE) rippleCount else maxRippleRadius / rippleStrokeWidth

        // Calculate the interval of ripples
        rippleInterval = DEFAULT_RIPPLE_INTERVAL_FACTOR / rippleCount
        for (i in 0 until rippleCount) {
            val shapeRippleEntry = ShapeRippleEntry(rippleShape)
            shapeRippleEntry.x =
                (if (enableRandomPosition) (0..viewWidth).random() else viewWidth / 2)
            shapeRippleEntry.y =
                (if (enableRandomPosition) (0..viewHeight).random() else viewHeight / 2)
            shapeRippleEntry.multiplierValue = (-(rippleInterval * i.toFloat()))
            shapeRippleEntry.rippleIndex = (i)
            if (enableRandomColor) {
                shapeRippleEntry.setOriginalColorValue(

                    rippleRandomColors[rippleRandomColors.indices.random()]
                )
            } else {
                shapeRippleEntry.setOriginalColorValue(rippleColor)
            }
            shapeRippleEntries.add(shapeRippleEntry)

            // we only render 1 ripple when it is enabled
            if (enableSingleRipple) {
                break
            }
        }
    }

    /**
     * Refreshes the list of ticket entries after certain options are changed such as the [.rippleColor],
     * [.rippleShape], [.enableRandomPosition], etc.
     *
     *
     * This will only execute after the [.initializeEntries], this is safe to call before it.
     */
    private fun reconfigureEntries() {

        // we do not re configure when dimension is not calculated
        // or if the list is empty
        if (viewWidth == 0 && viewHeight == 0 && (shapeRippleEntries.size == 0)) {
            return
        }

        // sets the stroke width of the ripple
        shapePaint.strokeWidth = rippleStrokeWidth.toFloat()
        for (shapeRippleEntry in shapeRippleEntries) {
            if (enableRandomColor) {
                shapeRippleEntry.setOriginalColorValue(
                    rippleRandomColors[rippleRandomColors.indices.random()]
                )
            } else {
                shapeRippleEntry.setOriginalColorValue(rippleColor)
            }
            shapeRippleEntry.baseShape = (rippleShape)
        }
    }

    /**
     * Start the [.rippleValueAnimator] with specified duration for each ripple.
     *
     * @param millis the duration in milliseconds
     */
    fun start(millis: Int) {

        // Do a ripple value renderer
        rippleValueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = millis.toLong()
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = rippleInterpolator
        }
        rippleValueAnimator?.addUpdateListener { animation -> render(animation.animatedValue as Float) }
        rippleValueAnimator?.start()
    }

    /**
     * This is the main renderer for the list of ripple, we always check that the first ripple is already
     * finished.
     *
     *
     * When the ripple is finished it is [ShapeRippleEntry.reset] and move to the end of the list to be reused all over again
     * to prevent creating a new instance of it.
     *
     *
     * Each ripple will be configured to be either rendered or not rendered to the view to prevent extra rendering process.
     *
     * @param multiplierValue the current multiplier value of the [.rippleValueAnimator]
     */
    private fun render(multiplierValue: Float) {

        // Do not render when entries are empty
        if (shapeRippleEntries.size == 0) {
            return
        }
        var firstEntry = shapeRippleEntries.peekFirst()

        // Calculate the multiplier value of the first entry
        var firstEntryMultiplierValue: Float =
            (firstEntry?.multiplierValue ?: 0f) + max(multiplierValue - lastMultiplierValue, 0f)

        // Check if the first entry is done the ripple (happens when the ripple reaches to end)
        if (firstEntryMultiplierValue >= 1.0f) {

            // Remove and relocate the first entry to the last entry
            val removedEntry = shapeRippleEntries.pop()
            removedEntry.reset()
            removedEntry.setOriginalColorValue(
                if (enableRandomColor) rippleRandomColors[rippleRandomColors.indices.random()] else rippleColor
            )
            shapeRippleEntries.addLast(removedEntry)

            // Get the new first entry of the list
            firstEntry = shapeRippleEntries.peekFirst()

            // Calculate the new multiplier value of the first entry of the list
            firstEntryMultiplierValue =
                (firstEntry?.multiplierValue ?: 0f) + max(multiplierValue - lastMultiplierValue, 0f)
            firstEntry?.x = (if (enableRandomPosition) (0..viewWidth).random() else viewWidth / 2)
            firstEntry?.y = (if (enableRandomPosition) (0..viewHeight).random() else viewHeight / 2)
            if (enableSingleRipple) {
                firstEntryMultiplierValue = 0f
            }
        }
        var index = 0
        for (shapeRippleEntry in shapeRippleEntries) {

            // set the updated index
            shapeRippleEntry.rippleIndex = (index)

            // calculate the shape multiplier by index
            val currentEntryMultiplier = firstEntryMultiplierValue - rippleInterval * index

            // Check if we render the current ripple in the list
            // We render when the multiplier value is >= 0
            if (currentEntryMultiplier >= 0) {
                shapeRippleEntry.isRender = (true)
            } else {
                // We continue to the next item
                // since we know that we do not
                // need the calculations below
                shapeRippleEntry.isRender = (false)
                continue
            }

            // We already calculated the multiplier value of the first entry of the list
            if (index == 0) {
                shapeRippleEntry.multiplierValue = (firstEntryMultiplierValue)
            } else {
                shapeRippleEntry.multiplierValue = (currentEntryMultiplier)
            }

            // calculate the color if we enabled the color transition
            shapeRippleEntry.changingColorValue = (
                    if (isEnableColorTransition) evaluateTransitionColor(
                        currentEntryMultiplier,
                        shapeRippleEntry.getOriginalColorValue(),
                        rippleToColor
                    ) else rippleColor
                    )

            // calculate the current ripple size
            shapeRippleEntry.radiusSize = (maxRippleRadius * currentEntryMultiplier)
            index += 1
        }

        // save the last multiplier value
        lastMultiplierValue = multiplierValue

        // we draw the shapes
        invalidate()
    }

    /**
     * Stop the [.rippleValueAnimator] and clears the [.shapeRippleEntries]
     */
    fun stop() {
        if (rippleValueAnimator != null) {
            rippleValueAnimator!!.cancel()
            rippleValueAnimator!!.end()
            rippleValueAnimator!!.removeAllUpdateListeners()
            rippleValueAnimator!!.removeAllListeners()
            rippleValueAnimator = null
        }
        shapeRippleEntries.clear()
        invalidate()
    }

    /**
     * Starts the ripple by stopping the current [.rippleValueAnimator] using the [.stop]
     * then initializing ticket entries using the [.initializeEntries]
     * and lastly starting the [.rippleValueAnimator] using [.start]
     */
    fun startRipple() {
        //stop the animation from previous before starting it again
        stop()
        initializeEntries()
        start(rippleDuration)
        isStopped = false
    }

    /**
     * Stops the ripple see [.stop] for more details
     */
    fun stopRipple() {
        stop()
        isStopped = true
    }

    /**
     * This restarts the ripple or continue where it was left off, this is mostly used
     */
    fun restartRipple() {
        if (isStopped) {
            return
        }
        startRipple()
    }

    /**
     * @return The max ripple radius
     */
    fun getRippleMaximumRadius(): Float {
        return maxRippleRadius.toFloat()
    }

    /**
     * @return True of single ripple is enabled
     */
    fun isEnableSingleRipple(): Boolean {
        return enableSingleRipple
    }

    /**
     * @return True of random ripple position is enabled
     */
    fun isEnableRandomPosition(): Boolean {
        return enableRandomPosition
    }

    /**
     * @return The stroke width(in pixels) for each ripple
     */
    fun getRippleStrokeWidth(): Int {
        return rippleStrokeWidth
    }

    /**
     * @return The base ripple color
     */
    fun getRippleColor(): Int {
        return rippleColor
    }

    /**
     * @return The starting ripple color of the color transition
     */
    fun getRippleFromColor(): Int {
        return rippleFromColor
    }

    /**
     * @return The end ripple color of the color transition
     */
    fun getRippleToColor(): Int {
        return rippleToColor
    }

    /**
     * @return The duration of each ripple in milliseconds
     */
    fun getRippleDuration(): Int {
        return rippleDuration
    }

    /**
     * @return The number of ripple being rendered
     */
    fun getRippleCount(): Int {
        return rippleCount
    }

    /**
     * @return The interpolator of the value animator
     */
    fun getRippleInterpolator(): Interpolator? {
        return rippleInterpolator
    }

    /**
     * @return True if random color for each ripple is enabled
     */
    fun isEnableRandomColor(): Boolean {
        return enableRandomColor
    }

    /**
     * @return The shape renderer for the shape ripples
     */
    fun getRippleShape(): BaseShape {
        return rippleShape
    }

    /**
     * @return The list of developer predefined random colors
     */
    fun getRippleRandomColors(): List<Int> {
        return rippleRandomColors
    }

    /**
     * Change the maximum size of the ripple, default to the size of the layout.
     *
     *
     * Value must be greater than 1
     *
     * @param rippleMaximumRadius The floating ripple interval for each ripple
     */
    fun setRippleMaximumRadius(rippleMaximumRadius: Float) {
        require(rippleMaximumRadius > NO_VALUE) { "Ripple max radius must be greater than 0" }
        this.rippleMaximumRadius = rippleMaximumRadius
        requestLayout()
    }

    /**
     * Enables the single ripple rendering
     *
     * @param enableSingleRipple flag for enabling single ripple
     */
    fun setEnableSingleRipple(enableSingleRipple: Boolean) {
        this.enableSingleRipple = enableSingleRipple
        initializeEntries()
    }

    /**
     * Change the stroke width for each ripple
     *
     * @param rippleStrokeWidth The stroke width in pixel
     */
    fun setRippleStrokeWidth(rippleStrokeWidth: Int) {
        require(rippleStrokeWidth > 0) { "Ripple duration must be > 0" }
        this.rippleStrokeWidth = rippleStrokeWidth
    }

    /**
     * Change the base color of each ripple
     *
     * @param rippleColor The ripple color
     */
    fun setRippleColor(rippleColor: Int) {
        setRippleColor(rippleColor, true)
    }

    /**
     * Change the base color of each ripple
     *
     * @param rippleColor The ripple color
     * @param instant     flag for when changing color is instant without delay
     */
    fun setRippleColor(rippleColor: Int, instant: Boolean) {
        this.rippleColor = rippleColor
        if (instant) {
            reconfigureEntries()
        }
    }

    /**
     * Change the starting color of the color transition
     *
     * @param rippleFromColor The starting color
     */
    fun setRippleFromColor(rippleFromColor: Int) {
        setRippleFromColor(rippleFromColor, true)
    }

    /**
     * Change the starting color of the color transition
     *
     * @param rippleFromColor The starting color
     * @param instant         flag for when changing color is instant without delay
     */
    fun setRippleFromColor(rippleFromColor: Int, instant: Boolean) {
        this.rippleFromColor = rippleFromColor
        if (instant) {
            reconfigureEntries()
        }
    }

    /**
     * Change the end color of the color transition
     *
     * @param rippleToColor The end color
     */
    fun setRippleToColor(rippleToColor: Int) {
        setRippleToColor(rippleToColor, true)
    }

    /**
     * Change the end color of the color transition
     *
     * @param rippleToColor The end color
     * @param instant       flag for when changing color is instant without delay
     */
    fun setRippleToColor(rippleToColor: Int, instant: Boolean) {
        this.rippleToColor = rippleToColor
        if (instant) {
            reconfigureEntries()
        }
    }

    /**
     * Change the ripple duration of the animator
     *
     * @param millis The duration in milliseconds
     */
    fun setRippleDuration(millis: Int) {
        require(rippleDuration > 0) { "Ripple duration must be > 0" }
        rippleDuration = millis

        // We set the duration here this will auto change the animator
        if (rippleValueAnimator != null) {
            rippleValueAnimator!!.duration = rippleDuration.toLong()
        }
    }

    /**
     * Enables the random positioning of ripples
     *
     * @param enableRandomPosition flag for enabling random position
     */
    fun setEnableRandomPosition(enableRandomPosition: Boolean) {
        this.enableRandomPosition = enableRandomPosition
        initializeEntries()
    }

    /**
     * Change the [Interpolator] of the animator
     *
     * @param rippleInterpolator The interpolator
     */
    fun setRippleInterpolator(rippleInterpolator: Interpolator?) {
        if (rippleInterpolator == null) {
            throw NullPointerException("Ripple interpolator in null")
        }
        this.rippleInterpolator = rippleInterpolator
    }

    /**
     * Enables the random coloring of each ripple
     *
     * @param enableRandomColor flag for enabling random color
     */
    fun setEnableRandomColor(enableRandomColor: Boolean) {
        this.enableRandomColor = enableRandomColor
        reconfigureEntries()
    }

    /**
     * Change the number of ripples, default value is calculated based on the
     * layout_width / ripple_width.
     *
     * @param rippleCount The number of ripples
     */
    fun setRippleCount(rippleCount: Int) {
        if (rippleCount <= NO_VALUE) {
            throw NullPointerException("Invalid ripple count")
        }
        this.rippleCount = rippleCount
        requestLayout()
    }

    /**
     * Change the shape renderer of the ripples
     *
     * @param rippleShape The renderer of shapes ripple
     */
    fun setRippleShape(rippleShape: BaseShape) {
        this.rippleShape = rippleShape

        // Make sure we call onSetup right away
        this.rippleShape.onSetup(context, shapePaint)
        reconfigureEntries()
    }

    /**
     * Change the developer predefined random colors
     *
     * @param rippleRandomColors The list of colors
     */
    fun setRippleRandomColors(rippleRandomColors: MutableList<Int>?) {
        if (rippleRandomColors == null) {
            throw NullPointerException("List of colors cannot be null")
        }

        if (rippleRandomColors.isEmpty()) {
            throw IllegalArgumentException("List of color cannot be empty");
        }

        // We clear the list of colors before adding new colors
        this.rippleRandomColors.clear()
        this.rippleRandomColors = rippleRandomColors
        reconfigureEntries()
    }

    companion object {

        private const val NO_VALUE = 0

        /**
         * Default color of the ripple
         */
        private val DEFAULT_RIPPLE_COLOR = Color.parseColor("#FFF44336")

        /**
         * Default color of the start ripple color transition
         */
        private val DEFAULT_RIPPLE_FROM_COLOR = Color.parseColor("#FFF44336")

        /**
         * Default color of the end ripple color transition
         */
        private val DEFAULT_RIPPLE_TO_COLOR = Color.parseColor("#00FFFFFF")

        /**
         * The default duration of the ripples
         */
        private const val DEFAULT_RIPPLE_DURATION = 1500

        /**
         * The default ripple interval factor see [.rippleIntervalFactor] for
         * more details
         */
        private const val DEFAULT_RIPPLE_INTERVAL_FACTOR = 1f
    }
}
