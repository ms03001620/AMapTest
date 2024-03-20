package com.example.amaptest.life;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

public class MyOwner implements LifecycleOwner {
    private LifecycleRegistry lifecycleRegistry;

    public MyOwner(LifecycleOwner w) {
        lifecycleRegistry = new LifecycleRegistry(w);
        lifecycleRegistry.markState(Lifecycle.State.CREATED);
    }

    public void start(){
        lifecycleRegistry.markState(Lifecycle.State.RESUMED);
    }

    public void stop() {
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED);
    }


    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }
}
