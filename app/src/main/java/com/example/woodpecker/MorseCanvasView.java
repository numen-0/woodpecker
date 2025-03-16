package com.example.woodpecker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MorseCanvasView extends View {
    static final long BPM = 240;
    static final long DOT_THRESHOLD = 1;
    static final long CHAR_THRESHOLD = 3;
    static final long millisPerBeat = 60 * 1000 / BPM;
    static final long beatsInCanvas = 150;
    static final long millisInCanvas = beatsInCanvas * millisPerBeat;
    private Paint paint;
    private Handler handler;
    private TextView tv;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
            handler.postDelayed(this, 16); // Refresh every 16ms (~60FPS)
        }
    };
    private List<Line> morseSequence; // Stores the dots and dashes
    private Line drawing;
    private MorseCharSet morseCharSet;

    public MorseCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.WHITE); // White lines on a dark background
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true);
        morseSequence = new ArrayList<>();

        // Replace the infinite loop with a periodic refresh
        handler = new Handler();
        handler.postDelayed(refreshRunnable, 16); // Refresh ~60FPS
    }

    public void setCharSet(MorseCharSet mcs) {
        this.morseCharSet = mcs;
    }
    public void setTextView(TextView tv) {
        this.tv = tv;
    }

    public void tap() {
        if (this.drawing != null) return;

        this.drawing = new Line().start();
    }

    public void untap() {
        if (this.drawing == null) return;

        final Line last = this.drawing.stop();
        morseSequence.add(last);
        this.drawing = null;


        // Set a timeout event based on the threshold to check if sequence is completed
        long timeoutMillis = MorseCanvasView.millisPerBeat * MorseCanvasView.CHAR_THRESHOLD;

        // Schedule a check to see if no tap was registered within this timeout
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if morseSequence is completed (i.e., if no new taps occurred within the timeout)
                if ( drawing == null && !morseSequence.isEmpty() && morseSequence.get(morseSequence.size()-1).equals(last)) {
                    // Convert morse sequence to characters and display them
                    String sequence = getSequence(morseSequence);
                    MorseCharSet.MorseChar mc = morseCharSet.getRep(sequence);
                    String t = (mc == null) ? "?" : mc.rep;

                    tv.setText(t);

                    morseSequence.clear();
                }
            }
        }, timeoutMillis);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int startX = this.getWidth(); // draw point
        final int midY = this.getHeight() / 2;

        final long current_t = System.currentTimeMillis();

        long timeThreshold = current_t - MorseCanvasView.millisInCanvas;
        for ( int i = morseSequence.size() - 1; i >= 0; i-- ) {
            Line line = morseSequence.get(i);

            if ( line.endTime < timeThreshold ) continue;

            this.paint.setColor(line.isDot() ? Color.GREEN : Color.CYAN);

            long delta = current_t - line.endTime;
            int length = (int) (delta * MorseCanvasView.millisPerBeat / MorseCanvasView.beatsInCanvas);
            int eX = startX - length;

            delta = line.getDelta();
            length = (int) (delta * MorseCanvasView.millisPerBeat / MorseCanvasView.beatsInCanvas);
            int sX = eX - length;

            canvas.drawLine(sX, midY, eX, midY, paint);
        }

        if ( drawing != null ) { // Draw active line (growing)
            long delta = current_t - drawing.startTime;
            boolean isDot = delta < MorseCanvasView.millisPerBeat * MorseCanvasView.DOT_THRESHOLD;

            this.paint.setColor(isDot ? Color.GREEN : Color.CYAN);

            int length = (int) (delta * MorseCanvasView.millisPerBeat / MorseCanvasView.beatsInCanvas);
            int endX = startX - length;

            canvas.drawLine(startX, midY, endX, midY, paint);
        }
    }

    class Line {
        private long startTime;
        private long endTime;
        private long delta;
        private boolean isDot;

        public Line() {
        }

        public Line start() {
            this.startTime = System.currentTimeMillis();
            return this;
        }

        public Line stop() {
            this.endTime = System.currentTimeMillis();
            this.delta = this.endTime - this.startTime;
            this.isDot = this.delta < MorseCanvasView.millisPerBeat * MorseCanvasView.DOT_THRESHOLD;
            return this;
        }

        public long getStartTime() {
            return this.startTime;
        }
        public long getDelta() {
            return this.delta;
        }

        public boolean isDot() {
            return this.isDot;
        }

    }
    private static String getSequence(List<Line> sequence) {
        StringBuilder morseString = new StringBuilder();
        for ( Line line : sequence ) {
            morseString.append(line.isDot() ? "." : "-");
        }
        return morseString.toString();
    }
}