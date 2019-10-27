package com.frolo.muse.model.menu;

import androidx.annotation.Nullable;

import com.frolo.muse.model.Recently;

import java.util.List;


public final class RecentPeriodMenu {
    private final List<Integer> periods;
    private final @Recently.Period int selectedPeriod;

    public RecentPeriodMenu(
            List<Integer> periods,
            @Recently.Period int selectedPeriod) {
        this.periods = periods;
        this.selectedPeriod = selectedPeriod;
    }

    public List<Integer> getPeriods() {
        return periods;
    }

    public int getSelectedPeriod() {
        return selectedPeriod;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof RecentPeriodMenu)) {
            return false;
        }

        RecentPeriodMenu other = (RecentPeriodMenu) obj;

        List<Integer> otherPeriods = other.periods;

        return (periods.containsAll(otherPeriods) && otherPeriods.containsAll(periods))
                && selectedPeriod == other.selectedPeriod;
    }
}
