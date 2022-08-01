package com.frolo.core.ui;

import android.app.Activity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public interface ActivityWatcher {

    @NotNull
    List<Activity> getCreatedActivities();

    @NotNull
    List<Activity> getStartedActivities();

    @NotNull
    List<Activity> getResumedActivities();

    @Nullable
    Activity getForegroundActivity();
}
