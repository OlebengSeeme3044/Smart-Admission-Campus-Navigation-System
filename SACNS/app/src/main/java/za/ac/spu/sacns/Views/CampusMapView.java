package za.ac.spu.sacns.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import za.ac.spu.sacns.models.NavigationNode;

public class CampusMapView extends View {

    private List<NavigationNode> routePath = new ArrayList<>();
    private String selectedDestinationId = "";

    private float translateX = 0f;
    private float translateY = 0f;

    private float lastTouchX;
    private float lastTouchY;

    private boolean isDragging = false;

    private float scaleFactor = 1.0f;

    private static final float MIN_SCALE = 0.8f;
    private static final float MAX_SCALE = 4.0f;

    private ScaleGestureDetector scaleGestureDetector;

    private static final float BUILDING_W = 110f;
    private static final float BUILDING_H = 64f;
    private static final float BASE_SCALE = 1.15f;

    private final Paint roadPaint            = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint buildingPaint        = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint buildingBorderPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint buildingTextPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint buildingLabelBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint routePaint           = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint routeOutlinePaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gatePaint            = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gateTextPaint        = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CampusMapView(Context context) {
        super(context);
        initialize();
    }

    public CampusMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CampusMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {

        scaleGestureDetector = new ScaleGestureDetector(
                getContext(),
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        scaleFactor *= detector.getScaleFactor();
                        scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));
                        invalidate();
                        return true;
                    }
                }
        );

        roadPaint.setStyle(Paint.Style.STROKE);
        roadPaint.setStrokeWidth(50);
        roadPaint.setStrokeCap(Paint.Cap.ROUND);

        buildingPaint.setStyle(Paint.Style.FILL);

        buildingBorderPaint.setStyle(Paint.Style.STROKE);
        buildingBorderPaint.setStrokeWidth(4);
        buildingBorderPaint.setColor(0xFF1F2933);

        buildingTextPaint.setStyle(Paint.Style.FILL);
        buildingTextPaint.setTextSize(20);
        buildingTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        buildingTextPaint.setTextAlign(Paint.Align.CENTER);
        buildingTextPaint.setColor(0xFF0A2342);

        buildingLabelBgPaint.setStyle(Paint.Style.FILL);
        buildingLabelBgPaint.setColor(0xF5FFFFFF);

        routeOutlinePaint.setStyle(Paint.Style.STROKE);
        routeOutlinePaint.setStrokeWidth(18);
        routeOutlinePaint.setStrokeCap(Paint.Cap.ROUND);
        routeOutlinePaint.setStrokeJoin(Paint.Join.ROUND);
        routeOutlinePaint.setColor(0xFF1F2933);

        routePaint.setStyle(Paint.Style.STROKE);
        routePaint.setStrokeWidth(12);
        routePaint.setStrokeCap(Paint.Cap.ROUND);
        routePaint.setStrokeJoin(Paint.Join.ROUND);
        routePaint.setColor(0xFFE8AA42);

        gatePaint.setStyle(Paint.Style.FILL);

        gateTextPaint.setStyle(Paint.Style.FILL);
        gateTextPaint.setTextSize(24);
        gateTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        gateTextPaint.setTextAlign(Paint.Align.CENTER);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setRoute(List<NavigationNode> path) {
        if (path == null) {
            routePath = new ArrayList<>();
        } else {
            routePath = new ArrayList<>(path);
        }
        invalidate();
    }

    public void setSelectedDestination(String destinationId) {
        if (destinationId == null) {
            selectedDestinationId = "";
        } else {
            selectedDestinationId = destinationId;
        }
        invalidate();
    }

    public void zoomIn() {
        scaleFactor += 0.35f;
        if (scaleFactor > MAX_SCALE) scaleFactor = MAX_SCALE;
        invalidate();
    }

    public void zoomOut() {
        scaleFactor -= 0.35f;
        if (scaleFactor < MIN_SCALE) scaleFactor = MIN_SCALE;
        invalidate();
    }

    public void resetMap() {
        scaleFactor = 1.0f;
        translateX = 0f;
        translateY = 0f;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        // Whole view is the map now — no legend inside the view.
        float w = getWidth();
        float h = getHeight();

        canvas.save();
        canvas.clipRect(0, 0, w, h);

        float scaleX = (w / 1000f) * BASE_SCALE * scaleFactor;
        float scaleY = (h / 1200f) * BASE_SCALE * scaleFactor;

        float centerOffsetX = (w - 1000f * scaleX) / 2f;
        float centerOffsetY = (h - 1200f * scaleY) / 2f;

        canvas.translate(centerOffsetX + translateX, centerOffsetY + translateY);
        canvas.scale(scaleX, scaleY);

        drawBackground(canvas);
        drawRoads(canvas);
        drawBuildings(canvas);
        drawGate(canvas);
        drawRoute(canvas);

        canvas.restore();
    }

    private void drawBackground(Canvas canvas) {
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0xFFF4F7F9);
        canvas.drawRect(0, 0, 1000, 1200, bgPaint);
    }

    private void drawRoads(Canvas canvas) {

        roadPaint.setColor(0xFFCBD5E1);

        canvas.drawLine(50, 420, 950, 420, roadPaint);
        canvas.drawLine(350, 80, 350, 1150, roadPaint);
        canvas.drawLine(700, 80, 700, 1150, roadPaint);

        Paint roadLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        roadLabelPaint.setColor(0xFF607080);
        roadLabelPaint.setTextSize(22);
        roadLabelPaint.setTypeface(Typeface.DEFAULT_BOLD);

        canvas.drawText("Scanlan St", 420, 400, roadLabelPaint);
        canvas.drawText("Lawson St", 360, 620, roadLabelPaint);
        canvas.drawText("Bishops Ave", 710, 620, roadLabelPaint);
    }

    private void drawBuildings(Canvas canvas) {

        drawBuilding(canvas, "William Pescod (WP)",       "wp",   0xFF1E3A8A, 620, 200);
        drawBuilding(canvas, "Teaching Practice (C008)",  "c008", 0xFFFFEB3B, 560, 380);
        drawBuilding(canvas, "Foundation Phase (C009)",   "c009", 0xFF43A047, 720, 300);

        drawBuilding(canvas, "Moroka Residence (C001)",   "c001", 0xFFE53935, 400, 450);
        drawBuilding(canvas, "Student Affairs (C002)",    "c002", 0xFFE53935, 450, 540);
        drawBuilding(canvas, "Academic Building (C003)",  "c003", 0xFFF57C00, 660, 450);
        drawBuilding(canvas, "Library (C004)",            "c004", 0xFFF8BBD0, 600, 560);
        drawBuilding(canvas, "Applied Sciences (C005)",   "c005", 0xFF8E24AA, 600, 640);
        drawBuilding(canvas, "Data Science Labs (C006)",  "c006", 0xFFF57C00, 510, 700);
        drawBuilding(canvas, "Science Lab (C007)",        "c007", 0xFF26C6DA, 660, 700);

        drawBuilding(canvas, "Humanities Labs (C010)",    "c010", 0xFFB71C1C, 500, 800);
        drawBuilding(canvas, "Agriculture (C011)",        "c011", 0xFFB71C1C, 620, 800);

        drawBuilding(canvas, "Sports Pavilion (C017)",    "c017", 0xFF8E24AA, 660, 1050);
        drawBuilding(canvas, "Spectator Seating (C018)",  "c018", 0xFF8E24AA, 350, 1050);
        drawBuilding(canvas, "Sports Entrance (C019)",    "c019", 0xFF6D4C41, 400, 950);
    }

    private void drawBuilding(Canvas canvas, String name, String id, int color, float x, float y) {

        buildingPaint.setColor(color);

        RectF rect = new RectF(
                x - BUILDING_W / 2f,
                y - BUILDING_H / 2f,
                x + BUILDING_W / 2f,
                y + BUILDING_H / 2f
        );

        canvas.drawRoundRect(rect, 22, 22, buildingPaint);
        canvas.drawRoundRect(rect, 22, 22, buildingBorderPaint);

        String[] words = name.split(" ");
        float labelY1;
        float labelY2;
        String line1;
        String line2 = null;

        if (words.length <= 2) {
            line1 = name;
            labelY1 = y + 8;
            labelY2 = y + 8;
        } else {
            line1 = words[0];
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < words.length; i++) {
                sb.append(words[i]);
                if (i < words.length - 1) sb.append(" ");
            }
            line2 = sb.toString();
            labelY1 = y - 6;
            labelY2 = y + 22;
        }

        float textWidth = Math.max(
                buildingTextPaint.measureText(line1),
                line2 != null ? buildingTextPaint.measureText(line2) : 0f
        );

        float padX = 8f;
        float padY = 5f;
        float bgTop    = labelY1 - buildingTextPaint.getTextSize() + padY;
        float bgBottom = labelY2 + padY;
        float bgLeft   = x - textWidth / 2f - padX;
        float bgRight  = x + textWidth / 2f + padX;

        canvas.drawRoundRect(
                new RectF(bgLeft, bgTop, bgRight, bgBottom),
                8, 8, buildingLabelBgPaint
        );

        buildingTextPaint.setColor(0xFF0A2342);
        canvas.drawText(line1, x, labelY1, buildingTextPaint);
        if (line2 != null) {
            canvas.drawText(line2, x, labelY2, buildingTextPaint);
        }

        drawDestinationMarker(canvas, id, x, y);
    }

    private void drawDestinationMarker(Canvas canvas, String id, float x, float y) {

        if (!id.equals(selectedDestinationId)) return;

        Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setStrokeWidth(7);
        markerPaint.setColor(0xFFE8AA42);

        canvas.drawCircle(x, y, 72, markerPaint);

        markerPaint.setStyle(Paint.Style.FILL);
        markerPaint.setColor(0xFFE8AA42);

        canvas.drawCircle(x, y, 14, markerPaint);
    }

    private void drawGate(Canvas canvas) {

        gatePaint.setColor(0xFF3B7BBF);
        canvas.drawCircle(100, 1150, 30, gatePaint);

        gateTextPaint.setColor(0xFFFFFFFF);
        canvas.drawText("G", 100, 1160, gateTextPaint);

        gateTextPaint.setColor(0xFF0A2342);
        canvas.drawText("Main Gate", 100, 1200, gateTextPaint);
    }

    private void drawRoute(Canvas canvas) {

        if (routePath == null || routePath.size() < 2) return;

        Path path = new Path();
        NavigationNode first = routePath.get(0);
        path.moveTo(first.getX(), first.getY());

        for (int i = 1; i < routePath.size(); i++) {
            NavigationNode node = routePath.get(i);
            path.lineTo(node.getX(), node.getY());
        }

        canvas.drawPath(path, routeOutlinePaint);
        canvas.drawPath(path, routePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        scaleGestureDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                isDragging = true;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1 && isDragging) {
                    float dx = event.getX() - lastTouchX;
                    float dy = event.getY() - lastTouchY;
                    translateX += dx;
                    translateY += dy;
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                return true;
        }

        return true;
    }
}
