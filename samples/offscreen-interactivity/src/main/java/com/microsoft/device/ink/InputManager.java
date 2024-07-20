package com.microsoft.device.ink;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.pen.data.TouchPointList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InputManager {

    private static final String TAG = "InputManager";

    private final PenInputHandler penInputHandler;
    private final PenHoverHandler penHoverHandler;
    private final long timeOffset;
    public ExtendedStroke currentStroke;
    private TouchHelper touchHelper;

    public InputManager(View view, PenInputHandler penInputHandler, PenHoverHandler penHoverHandler) {
        this.penInputHandler = penInputHandler;
        this.penHoverHandler = penHoverHandler;
        this.timeOffset = System.currentTimeMillis() - SystemClock.uptimeMillis();
        this.currentStroke = new ExtendedStroke();
        InputManagerHolder.setInputManager(this);  // Set the instance in the singleton
        setupInputEvents(view);
    }

    public interface PenInputHandler {
        void strokeStarted(PenInfo penInfo, ExtendedStroke stroke);
        void strokeUpdated(PenInfo penInfo, ExtendedStroke stroke);
        void strokeCompleted(PenInfo penInfo, ExtendedStroke stroke);
    }

    public interface PenHoverHandler {
        void hoverStarted(PenInfo penInfo);
        void hoverMoved(PenInfo penInfo);
        void hoverEnded(PenInfo penInfo);
    }

    public enum PointerType {
        MOUSE,
        FINGER,
        PEN_TIP,
        PEN_ERASER,
        UNKNOWN
    }

    public static class Point {
        public final float x;
        public final float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class PenInfo {
        public final PointerType pointerType;
        public final float x;
        public final float y;
        public final long timestamp;
        public final float pressure;

        public PenInfo(PointerType pointerType, float x, float y, long timestamp, float pressure) {
            this.pointerType = pointerType;
            this.x = x;
            this.y = y;
            this.timestamp = timestamp;
            this.pressure = pressure;
        }

        public static PenInfo createFromEvent(MotionEvent event, long timeOffset) {
            PointerType pointerType;
            switch (event.getToolType(0)) {
                case MotionEvent.TOOL_TYPE_FINGER:
                    pointerType = PointerType.FINGER;
                    break;
                case MotionEvent.TOOL_TYPE_MOUSE:
                    pointerType = PointerType.MOUSE;
                    break;
                case MotionEvent.TOOL_TYPE_STYLUS:
                    pointerType = PointerType.PEN_TIP;
                    break;
                case MotionEvent.TOOL_TYPE_ERASER:
                    pointerType = PointerType.PEN_ERASER;
                    break;
                default:
                    pointerType = PointerType.UNKNOWN;
            }

            return new PenInfo(pointerType, event.getX(), event.getY(), timeOffset + event.getEventTime(), event.getPressure());
        }

        public static PenInfo createFromTouchPoint(TouchPoint touchPoint, long timeOffset) {
            return new PenInfo(
                    PointerType.PEN_TIP,
                    touchPoint.getX(),
                    touchPoint.getY(),
                    timeOffset + touchPoint.timestamp,
                    1.0f
            );
        }

        public static PenInfo createFromHistoryEvent(MotionEvent event, int pos, long timeOffset) {
            PointerType pointerType;
            switch (event.getToolType(0)) {
                case MotionEvent.TOOL_TYPE_FINGER:
                    pointerType = PointerType.FINGER;
                    break;
                case MotionEvent.TOOL_TYPE_MOUSE:
                    pointerType = PointerType.MOUSE;
                    break;
                case MotionEvent.TOOL_TYPE_STYLUS:
                    pointerType = PointerType.PEN_TIP;
                    break;
                case MotionEvent.TOOL_TYPE_ERASER:
                    pointerType = PointerType.PEN_ERASER;
                    break;
                default:
                    pointerType = PointerType.UNKNOWN;
            }

            return new PenInfo(
                    pointerType,
                    event.getHistoricalX(pos),
                    event.getHistoricalY(pos),
                    timeOffset + event.getHistoricalEventTime(pos),
                    event.getHistoricalPressure(pos)
            );
        }
    }

    public static class ExtendedStroke {
        private final List<Point> builder = new ArrayList<>();
        private final HashMap<Integer, PenInfo> penInfos = new HashMap<>();

        private int lastPointReferenced = 0;

        public int getLastPointReferenced() {
            return lastPointReferenced;
        }

        public void setLastPointReferenced(int lastPointReferenced) {
            this.lastPointReferenced = lastPointReferenced;
        }

        public void addPoint(PenInfo penInfo) {
            Point point = new Point(penInfo.x, penInfo.y);
            builder.add(point);
            penInfos.put(builder.size() - 1, penInfo);
        }

        public List<Point> getPoints() {
            return builder;
        }

        public PenInfo getPenInfo(Point point) {
            return penInfos.get(builder.indexOf(point));
        }

        public void reset() {
            builder.clear();
            lastPointReferenced = 0;
            penInfos.clear();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupInputEvents(View view) {
        Rect limit = new Rect(0, 0, 1200, 1200);
        Rect zeroRect = new Rect(0, 0, 0, 0);
        List<Rect> exclude = new ArrayList<>();
        exclude.add(zeroRect);

        touchHelper = TouchHelper.create(view, callback);

        touchHelper.setStrokeWidth(3.0f)
                .setLimitRect(limit, exclude)
                .openRawDrawing();
        touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_PENCIL);

        Log.d(TAG, "boox setup");
        Log.d(TAG, "hello " + view.toString());
    }

    public TouchHelper getTouchHelper() {
        return touchHelper;
    }

    private final RawInputCallback callback = new RawInputCallback() {

        @Override
        public void onBeginRawDrawing(boolean isPen, TouchPoint touchPoint) {
            Log.d(TAG, "boox worksish");
            PenInfo penInfo = PenInfo.createFromTouchPoint(touchPoint, timeOffset);
            currentStroke = new ExtendedStroke();
            currentStroke.addPoint(penInfo);
            Log.d(TAG, "onBeginRawDrawing");
            penInputHandler.strokeStarted(penInfo, currentStroke);
        }

        @Override
        public void onEndRawDrawing(boolean isPen, TouchPoint touchPoint) {
            PenInfo penInfo = PenInfo.createFromTouchPoint(touchPoint, timeOffset);
            currentStroke.addPoint(penInfo);
            penInputHandler.strokeCompleted(penInfo, currentStroke);
        }

        @Override
        public void onRawDrawingTouchPointMoveReceived(TouchPoint touchPoint) {
            PenInfo penInfo = PenInfo.createFromTouchPoint(touchPoint, timeOffset);
            currentStroke.addPoint(penInfo);
            penInputHandler.strokeUpdated(penInfo, currentStroke);
        }

        @Override
        public void onRawDrawingTouchPointListReceived(TouchPointList touchPointList) {
            // Commented out for debugging
//            for (TouchPoint touchPoint : touchPointList) {
//                PenInfo penInfo = PenInfo.createFromTouchPoint(touchPoint, timeOffset);
//                currentStroke.addPoint(penInfo);
//                penInputHandler.strokeUpdated(penInfo, currentStroke);
//            }
        }

        @Override
        public void onBeginRawErasing(boolean isPen, TouchPoint touchPoint) {
            Log.d(TAG, "onBeginRawErasing");
        }

        @Override
        public void onEndRawErasing(boolean isPen, TouchPoint touchPoint) {
            Log.d(TAG, "onEndRawErasing");
        }

        @Override
        public void onRawErasingTouchPointMoveReceived(TouchPoint touchPoint) {
            Log.d(TAG, "onRawErasingTouchPointMoveReceived");
        }

        @Override
        public void onRawErasingTouchPointListReceived(TouchPointList touchPointList) {
            Log.d(TAG, "onRawErasingTouchPointListReceived");
        }
    };
}
