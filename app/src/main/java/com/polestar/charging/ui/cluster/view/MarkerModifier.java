package com.polestar.charging.ui.cluster.view;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.polestar.charging.ui.cluster.view.renderer.AnimationTask;
import com.polestar.charging.ui.cluster.view.renderer.AnimationTaskCallback;
import com.polestar.charging.ui.cluster.view.renderer.MarkerWithPosition;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Handles all markerWithPosition manipulations on the map. Work (such as adding, removing, or
 * animating a markerWithPosition) is performed while trying not to block the rest of the app's
 * UI.
 */
@SuppressLint("HandlerLeak")
class MarkerModifier extends Handler implements MessageQueue.IdleHandler {
    interface OnRemove{
        void onRemove(Marker marker);
    }

    private final OnRemove onRemoveListener;

    private static final int BLANK = 0;

    private final Lock lock = new ReentrantLock();
    private final Condition busyCondition = lock.newCondition();

    private Queue<CreateMarkerTask> mCreateMarkerTasks = new LinkedList<>();
    private Queue<CreateMarkerTask> mOnScreenCreateMarkerTasks = new LinkedList<>();
    private Queue<Marker> mRemoveMarkerTasks = new LinkedList<>();
    private Queue<Marker> mOnScreenRemoveMarkerTasks = new LinkedList<>();
    private Queue<AnimationTask> mAnimationTasks = new LinkedList<>();

    /**
     * Whether the idle listener has been added to the UI thread's MessageQueue.
     */
    private boolean mListenerAdded;

    public MarkerModifier(OnRemove onRemove) {
        super(Looper.getMainLooper());
        this.onRemoveListener = onRemove;
    }

    /**
     * Creates markers for a cluster some time in the future.
     *
     * @param priority whether this operation should have priority.
     */
    public void add(boolean priority, CreateMarkerTask c) {
        lock.lock();
        sendEmptyMessage(BLANK);
        if (priority) {
            mOnScreenCreateMarkerTasks.add(c);
        } else {
            mCreateMarkerTasks.add(c);
        }
        lock.unlock();
    }

    /**
     * Removes a markerWithPosition some time in the future.
     *
     * @param priority whether this operation should have priority.
     * @param m        the markerWithPosition to remove.
     */
    public void remove(boolean priority, Marker m) {
        lock.lock();
        sendEmptyMessage(BLANK);
        if (priority) {
            mOnScreenRemoveMarkerTasks.add(m);
        } else {
            mRemoveMarkerTasks.add(m);
        }
        lock.unlock();
    }

    /**
     * Animates a markerWithPosition some time in the future.
     *
     * @param marker the markerWithPosition to animate.
     * @param from   the position to animate from.
     * @param to     the position to animate to.
     */
    public void animate(MarkerWithPosition marker, LatLng from, LatLng to) {
        lock.lock();
        mAnimationTasks.add(new AnimationTask(marker, from, to, null));
        lock.unlock();
    }

    /**
     * Animates a markerWithPosition some time in the future, and removes it when the animation
     * is complete.
     *
     * @param marker the markerWithPosition to animate.
     * @param from   the position to animate from.
     * @param to     the position to animate to.
     */
    public void animateThenRemove(MarkerWithPosition marker, LatLng from, LatLng to) {
        lock.lock();
        AnimationTask animationTask = new AnimationTask(marker, from, to, new AnimationTaskCallback() {
            public void onAnimationEnd(Marker marker) {
                removeMarker(marker);
            }
        });
        mAnimationTasks.add(animationTask);
        lock.unlock();
    }

    @Override
    public void handleMessage(Message msg) {
        if (!mListenerAdded) {
            Looper.myQueue().addIdleHandler(this);
            mListenerAdded = true;
        }
        removeMessages(BLANK);

        lock.lock();
        try {

            // Perform up to 10 tasks at once.
            // Consider only performing 10 remove tasks, not adds and animations.
            // Removes are relatively slow and are much better when batched.
            for (int i = 0; i < 10; i++) {
                performNextTask();
            }

            if (!isBusy()) {
                mListenerAdded = false;
                Looper.myQueue().removeIdleHandler(this);
                // Signal any other threads that are waiting.
                busyCondition.signalAll();
            } else {
                // Sometimes the idle queue may not be called - schedule up some work regardless
                // of whether the UI thread is busy or not.
                // TODO: try to remove this.
                sendEmptyMessageDelayed(BLANK, 10);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Perform the next task. Prioritise any on-screen work.
     */
    private void performNextTask() {
        if (!mOnScreenRemoveMarkerTasks.isEmpty()) {
            removeMarker(mOnScreenRemoveMarkerTasks.poll());
        } else if (!mAnimationTasks.isEmpty()) {
            mAnimationTasks.poll().perform();
        } else if (!mOnScreenCreateMarkerTasks.isEmpty()) {
            mOnScreenCreateMarkerTasks.poll().perform(this);
        } else if (!mCreateMarkerTasks.isEmpty()) {
            mCreateMarkerTasks.poll().perform(this);
        } else if (!mRemoveMarkerTasks.isEmpty()) {
            removeMarker(mRemoveMarkerTasks.poll());
        }
    }

    private void removeMarker(Marker m) {
        onRemoveListener.onRemove(m);
    }

    /**
     * @return true if there is still work to be processed.
     */
    public boolean isBusy() {
        try {
            lock.lock();
            return !(mCreateMarkerTasks.isEmpty() &&
                    mOnScreenCreateMarkerTasks.isEmpty() &&
                    mOnScreenRemoveMarkerTasks.isEmpty() &&
                    mRemoveMarkerTasks.isEmpty() &&
                    mAnimationTasks.isEmpty()
            );
        } finally {
            lock.unlock();
        }
    }

    /**
     * Blocks the calling thread until all work has been processed.
     */
    public void waitUntilFree() {
        while (isBusy()) {
            // Sometimes the idle queue may not be called - schedule up some work regardless
            // of whether the UI thread is busy or not.
            // TODO: try to remove this.
            sendEmptyMessage(BLANK);
            lock.lock();
            try {
                if (isBusy()) {
                    busyCondition.await();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public boolean queueIdle() {
        // When the UI is not busy, schedule some work.
        sendEmptyMessage(BLANK);
        return true;
    }
}
