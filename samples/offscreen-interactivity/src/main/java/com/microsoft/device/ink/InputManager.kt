//package com.microsoft.device.ink
//
//import android.annotation.SuppressLint
//import android.content.ContentValues.TAG
//import android.graphics.Rect
//import android.os.SystemClock
//import android.util.Log
//import android.view.MotionEvent
//import android.view.View
//import com.onyx.android.sdk.data.note.TouchPoint
//import com.onyx.android.sdk.pen.RawInputCallback
//import com.onyx.android.sdk.pen.TouchHelper
//import com.onyx.android.sdk.pen.data.TouchPointList
//
//
//class InputManager(
//    view: View,
//    private val penInputHandler: PenInputHandler,
//    private val penHoverHandler: PenHoverHandler? = null,
//    private val timeOffset: Long = System.currentTimeMillis() - SystemClock.uptimeMillis()) {
//
//    var currentStroke = ExtendedStroke()
//
//    init {
//        currentStroke.reset()
//        setupInputEvents(view)
//    }
//
//    interface PenInputHandler {
//        fun strokeStarted(penInfo: PenInfo, stroke: ExtendedStroke)
//        fun strokeUpdated(penInfo: PenInfo, stroke: ExtendedStroke)
//        fun strokeCompleted(penInfo: PenInfo, stroke: ExtendedStroke)
//    }
//
//    interface PenHoverHandler {
//        fun hoverStarted(penInfo: PenInfo)
//        fun hoverMoved(penInfo: PenInfo)
//        fun hoverEnded(penInfo: PenInfo)
//    }
//
//    enum class PointerType {
//        MOUSE,
//        FINGER,
//        PEN_TIP,
//        PEN_ERASER,
//        UNKNOWN
//    }
//
//    class Point(
//        val x: Float,
//        val y: Float,
//    )
//
//    data class PenInfo(
//        val pointerType: PointerType,
//        val x: Float,
//        val y: Float,
//        val timestamp: Long,
//        val pressure: Float,
//    ) {
//        companion object {
//            fun createFromEvent(event: MotionEvent, timeOffset: Long): PenInfo {
//                val pointerType: PointerType = when (event.getToolType(0)) {
//                    MotionEvent.TOOL_TYPE_FINGER -> PointerType.FINGER
//                    MotionEvent.TOOL_TYPE_MOUSE -> PointerType.MOUSE
//                    MotionEvent.TOOL_TYPE_STYLUS -> PointerType.PEN_TIP
//                    MotionEvent.TOOL_TYPE_ERASER -> PointerType.PEN_ERASER
//                    else -> PointerType.UNKNOWN
//                }
//
//                return PenInfo(
//                    pointerType = pointerType,
//                    x = event.x,
//                    y = event.y,
//                    timestamp = timeOffset + event.eventTime,
//                    pressure = event.pressure,
//                )
//            }
//
//            fun createFromTouchPoint(touchPoint: TouchPoint, timeOffset: Long): PenInfo {
//                Log.d(TAG, "createFromTouchPoint: ")
//                return PenInfo(
//                    pointerType = PointerType.PEN_TIP,
//                    x = touchPoint.getX(),
//                    y = touchPoint.getY(),
//                    timestamp = timeOffset + touchPoint.timestamp,
//                    pressure = 1.0f,
//                )
//            }
//
//
//            fun createFromHistoryEvent(event: MotionEvent, pos: Int, timeOffset: Long): PenInfo {
//                val pointerType: PointerType = when (event.getToolType(0)) {
//                    MotionEvent.TOOL_TYPE_FINGER -> PointerType.FINGER
//                    MotionEvent.TOOL_TYPE_MOUSE -> PointerType.MOUSE
//                    MotionEvent.TOOL_TYPE_STYLUS -> PointerType.PEN_TIP
//                    MotionEvent.TOOL_TYPE_ERASER -> PointerType.PEN_ERASER
//                    else -> PointerType.UNKNOWN
//                }
//
//                return PenInfo(
//                    pointerType = pointerType,
//                    x = event.getHistoricalX(pos),
//                    y = event.getHistoricalY(pos),
//                    timestamp = timeOffset + event.getHistoricalEventTime(pos),
//                    pressure = event.getHistoricalPressure(pos),
//
//                    )
//            }
//        }
//    }
//
//    class ExtendedStroke {
//        private var builder = mutableListOf<Point>()
//        private var penInfos = HashMap<Int, PenInfo>()
//
//        private var _lastPointReferenced = 0
//        var lastPointReferenced: Int
//            get() = _lastPointReferenced
//            set(value) {
//                _lastPointReferenced = value
//            }
//
//        fun addPoint(penInfo: PenInfo) {
//            val point = Point(penInfo.x, penInfo.y)
//            builder.add(point)
//            penInfos[builder.lastIndex] = penInfo // hash codes don't serialize well, so use index
//        }
//
//        fun getPoints(): List<Point> {
//            return builder
//        }
//
//        fun getPenInfo(point: Point): PenInfo? {
//            return penInfos[builder.indexOf(point)]
//        }
//
//        fun reset() {
//            builder.clear()
//            lastPointReferenced = 0
//            penInfos.clear()
//        }
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    public fun setupInputEvents(view: View) {
//
//        // Configure TouchHelper with desired settings
//        val limit = Rect(0, 0, view.width, view.height);
//        val zeroRect = Rect(0, 0, 0, 0)
//        val exclude: MutableList<Rect> = ArrayList()
//        exclude.add(zeroRect)
//        val touchHelper = TouchHelper.create(view, callback)
//        touchHelper.setStrokeWidth(3.0f)
//            .setLimitRect(limit, exclude)
//            .openRawDrawing();
//        touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_PENCIL);
//        Log.d(TAG, "boox setup");
//
//        Log.d(TAG, "boox pen");
//        // Initialize TouchHelper for the view
//
////        touchHelper.setRawDrawingEnabled(true)
////        touchHelper.setRawDrawingEnabled(false)
////        touchHelper.setRawDrawingEnabled(true)
//    }
//
//    private val callback = object : new RawInputCallback() {
//        override fun onBeginRawDrawing(isPen: Boolean, touchPoint: TouchPoint) {
//            // Handle the beginning of a pen stroke
//            val penInfo = PenInfo.createFromTouchPoint(touchPoint, timeOffset)
//            currentStroke = ExtendedStroke()
//            currentStroke.addPoint(penInfo)
//            Log.d(TAG, "onBeginRawDrawing");
//            penInputHandler.strokeStarted(penInfo, currentStroke)
//        }
//
//        override fun onEndRawDrawing(isPen: Boolean, touchPoint: TouchPoint) {
//            // Handle the end of a pen stroke
//            val penInfo = PenInfo.createFromTouchPoint(touchPoint, timeOffset)
//            currentStroke.addPoint(penInfo)
//            penInputHandler.strokeCompleted(penInfo, currentStroke)
//        }
//
//        override fun onRawDrawingTouchPointMoveReceived(touchPoint: TouchPoint) {
//            // Handle movement of the pen
//            val penInfo = PenInfo.createFromTouchPoint(touchPoint, timeOffset)
//            currentStroke.addPoint(penInfo)
//            penInputHandler.strokeUpdated(penInfo, currentStroke)
//        }
//
//        override fun onRawDrawingTouchPointListReceived(touchPointList: TouchPointList) {
//            // Handle the list of touch points received during movement
//            for (touchPoint in touchPointList) {
//                val penInfo = PenInfo.createFromTouchPoint(touchPoint, timeOffset)
//                currentStroke.addPoint(penInfo)
//                penInputHandler.strokeUpdated(penInfo, currentStroke)
//            }
//        }
//
//        override fun onBeginRawErasing(isPen: Boolean, touchPoint: TouchPoint) {
//            // Handle the beginning of erasing
//        }
//
//        override fun onEndRawErasing(isPen: Boolean, touchPoint: TouchPoint) {
//            // Handle the end of erasing
//        }
//
//        override fun onRawErasingTouchPointMoveReceived(touchPoint: TouchPoint) {
//            // Handle movement during erasing
//        }
//
//        override fun onRawErasingTouchPointListReceived(touchPointList: TouchPointList) {
//            // Handle the list of touch points received during erasing
//        }
//    }
//
//
//}