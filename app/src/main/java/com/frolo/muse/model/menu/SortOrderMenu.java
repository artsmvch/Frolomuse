package com.frolo.muse.model.menu;

import androidx.annotation.Nullable;

import com.frolo.music.model.SortOrder;

import java.util.List;


public final class SortOrderMenu {
    private final List<SortOrder> sortOrders;
    private final SortOrder selectedSortOrder;
    private final boolean sortOrderReversed;

    public SortOrderMenu(List<SortOrder> sortOrders, SortOrder selectedSortOrder, boolean sortOrderReversed) {
        this.sortOrders = sortOrders;
        this.selectedSortOrder = selectedSortOrder;
        this.sortOrderReversed = sortOrderReversed;
    }

    public List<SortOrder> getSortOrders() {
        return sortOrders;
    }

    public SortOrder getSelectedSortOrder() {
        return selectedSortOrder;
    }

    public boolean isSortOrderReversed() {
        return sortOrderReversed;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SortOrderMenu)) {
            return false;
        }

        SortOrderMenu other = (SortOrderMenu) obj;

        if (!selectedSortOrder.equals(other.selectedSortOrder)) {
            return false;
        }

        if (sortOrderReversed != other.sortOrderReversed) {
            return false;
        }

        if (sortOrders.size() != other.sortOrders.size()) {
            return false;
        }

        final List<SortOrder> otherSortOrders = other.sortOrders;

        return sortOrders.containsAll(otherSortOrders) && otherSortOrders.containsAll(sortOrders);
    }
}
