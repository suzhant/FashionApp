package com.sushant.fashionapp.Inteface;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

public abstract class SwipeHelper extends SimpleCallback {

    public static final int BUTTON_WIDTH = 200;
    private final RecyclerView recyclerView;
    private List<UnderlayButton> buttons;
    private final GestureDetector gestureDetector;
    private int swipedPos = -1;
    private float swipeThreshold = 0.5f;
    private final Map<Integer, List<UnderlayButton>> buttonsBuffer;
    private final Queue<Integer> recoverQueue;
    private final ItemSwipeListener itemSwipeListener;

    public interface ItemSwipeListener{
        void onSwipe(int from,int to);
    }

    public SwipeHelper(Context context, RecyclerView recyclerView,ItemSwipeListener swipeListener) {
        super(0, ItemTouchHelper.LEFT);
        this.recyclerView = recyclerView;
        this.buttons = new ArrayList<>();
        this.itemSwipeListener = swipeListener;
        GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                for (UnderlayButton button : buttons) {
                    if (button.onClick(e.getX(), e.getY()))
                        break;
                }

                return true;
            }
        };
        this.gestureDetector = new GestureDetector(context, gestureListener);
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                if (swipedPos < 0) return false;
                Point point = new Point((int) e.getRawX(), (int) e.getRawY());

                RecyclerView.ViewHolder swipedViewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPos);
                View swipedItem = null;
                if (swipedViewHolder != null) {
                    swipedItem = swipedViewHolder.itemView;
                }
                Rect rect = new Rect();
                if (swipedItem != null) {
                    swipedItem.getGlobalVisibleRect(rect);
                }

                if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_MOVE) {
                    if (rect.top < point.y && rect.bottom > point.y)
                        gestureDetector.onTouchEvent(e);
                    else {
                        recoverQueue.add(swipedPos);
                        swipedPos = -1;
                        recoverSwipedItem();
                    }
                }
                return false;
            }
        };
        this.recyclerView.setOnTouchListener(onTouchListener);
        buttonsBuffer = new HashMap<>();
        recoverQueue = new LinkedList<Integer>() {
            @Override
            public boolean add(Integer o) {
                if (contains(o))
                    return false;
                else
                    return super.add(o);
            }
        };

        attachSwipe();
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN; // Specify drag directions
        int swipeFlags = ItemTouchHelper.START; // Specify swipe directions
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target) {
        int from = source.getAbsoluteAdapterPosition();
        int to = target.getAbsoluteAdapterPosition();
        itemSwipeListener.onSwipe(from,to);
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getAbsoluteAdapterPosition();

        if (swipedPos != pos)
            recoverQueue.add(swipedPos);

        swipedPos = pos;

        if (buttonsBuffer.containsKey(swipedPos))
            buttons = buttonsBuffer.get(swipedPos);
        else
            buttons.clear();

        buttonsBuffer.clear();
        swipeThreshold = 0.5f * buttons.size() * BUTTON_WIDTH;
        recoverSwipedItem();
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return swipeThreshold;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return 0.1f * defaultValue;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return 5.0f * defaultValue;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int pos = viewHolder.getAbsoluteAdapterPosition();
        float translationX = dX;
        View itemView = viewHolder.itemView;

        if (pos < 0) {
            swipedPos = pos;
            return;
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX < 0) {
                List<UnderlayButton> buffer = new ArrayList<>();

                if (!buttonsBuffer.containsKey(pos)) {
                    instantiateUnderlayButton(viewHolder, buffer);
                    buttonsBuffer.put(pos, buffer);
                } else {
                    buffer = buttonsBuffer.get(pos);
                }

                assert buffer != null;
                translationX = dX * buffer.size() * BUTTON_WIDTH / itemView.getWidth();
                drawButtons(c, itemView, buffer, pos, translationX);
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);
    }

    private synchronized void recoverSwipedItem() {
        while (!recoverQueue.isEmpty()) {
            int pos = Objects.requireNonNull(recoverQueue.poll());
            if (pos > -1) {
                Objects.requireNonNull(recyclerView.getAdapter()).notifyItemChanged(pos);
            }
        }
    }

    private void drawButtons(Canvas c, View itemView, List<UnderlayButton> buffer, int pos, float dX) {
        float right = itemView.getRight();
        float dButtonWidth = (-1) * dX / buffer.size();

        for (UnderlayButton button : buffer) {
            float left = right - dButtonWidth;
            button.onDraw(
                    c,
                    new RectF(
                            left,
                            itemView.getTop(),
                            right,
                            itemView.getBottom()
                    ),
                    pos
            );

            right = left;
        }
    }

    public void attachSwipe() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public abstract void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons);

    public static class UnderlayButton {
        private final String text;
        private int imageResId;
        private final Bitmap bitmap;
        private final int color;
        private int pos;
        private RectF clickRegion;
        private final UnderlayButtonClickListener clickListener;

        public UnderlayButton(String text, Bitmap bitmap, int color, UnderlayButtonClickListener clickListener) {
            this.text = text;
            this.bitmap = bitmap;
            this.color = color;
            this.clickListener = clickListener;
        }

        public boolean onClick(float x, float y) {
            if (clickRegion != null && clickRegion.contains(x, y)) {
                clickListener.onClick(pos);
                return true;
            }

            return false;
        }

        public void onDraw(Canvas c, RectF rect, int pos) {
            Paint p = new Paint();

            // Draw background
            p.setColor(color);
            c.drawRect(rect, p);

            // Draw Text
            p.setColor(Color.WHITE);
            p.setTextSize(convertDpToPixel(12));

            Rect r = new Rect();
            float cHeight = rect.height();
            float cWidth = rect.width();
            p.setTextAlign(Paint.Align.LEFT);
            p.getTextBounds(text, 0, text.length(), r);
            float x = cWidth / 2f - r.width() / 2f - r.left;
            float y = cHeight / 2f + r.height() / 2f - r.bottom;
            //    c.drawText(text, rect.left + x, rect.top + y, p);

            clickRegion = rect;
            this.pos = pos;

            //Draw Image
            float textWidth = p.measureText(text);
            float spaceHeight = 10; // change to whatever you deem looks better
            float combinedHeight = bitmap.getHeight() + spaceHeight + r.height();
            c.drawBitmap(bitmap, rect.centerX() - (bitmap.getWidth() / 2.0f), rect.centerY() - (combinedHeight / 2), null);
            //If you want text as well with bitmap
            c.drawText(text, rect.centerX() - (textWidth / 2), rect.centerY() + (combinedHeight / 2), p);
        }
    }

    public interface UnderlayButtonClickListener {
        void onClick(int pos);
    }

    public static float convertDpToPixel(float dp) {
        return dp * ((float) Resources.getSystem().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}