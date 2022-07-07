package com.polestar.charging.ui.cluster.view;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.polestar.charging.ui.cluster.base.Cluster;
import com.polestar.charging.ui.cluster.base.ClusterItem;
import com.polestar.charging.ui.cluster.view.renderer.MarkerManager;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * ViewModifier ensures only one re-rendering of the view occurs at a time, and schedules
 * re-rendering, which is performed by the RenderTask.
 * 多任务的加入最多保持进行中任务和最后一个加入的任务，等进行中任务结束后执行最后的任务，中间任务会被忽略
 */
@SuppressLint("HandlerLeak")
class ViewModifier<T extends ClusterItem> extends Handler {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    private static final int RUN_TASK = 0;
    private static final int TASK_FINISHED = 1;
    private boolean mViewModificationInProgress = false;
    private RenderTask mRenderTask = null;

    private DefaultClusterRenderer renderer;

    public ViewModifier(DefaultClusterRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == TASK_FINISHED) {
            mViewModificationInProgress = false;
            if (mRenderTask != null) {
                // Run the task that was queued up.
                sendEmptyMessage(RUN_TASK);
            }
            return;
        }
        removeMessages(RUN_TASK);

        if (mViewModificationInProgress) {
            // Busy - wait for the callback.
            return;
        }

        if (mRenderTask == null) {
            // Nothing to do.
            return;
        }

        RenderTask renderTask;
        synchronized (this) {
            renderTask = mRenderTask;
            mRenderTask = null;
            mViewModificationInProgress = true;
        }

        renderTask.setCallback(new Runnable() {
            @Override
            public void run() {
                sendEmptyMessage(TASK_FINISHED);
            }
        });
        mExecutor.execute(renderTask);
    }

    public void queue(Set<? extends Cluster<T>> clusters) {
        synchronized (this) {
            mRenderTask = new RenderTask(clusters, renderer);
        }
        sendEmptyMessage(RUN_TASK);
    }
}