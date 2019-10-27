package com.frolo.muse.model.menu;

import androidx.annotation.Nullable;

import java.util.Map;


public final class SortOrderMenu {
    private final Map<String, String> sortOrders;
    private final String selectedSortOrder;
    private final boolean sortOrderReversed;

    public SortOrderMenu(
            Map<String, String> sortOrders,
            String selectedSortOrder,
            boolean sortOrderReversed) {

        this.sortOrders = sortOrders;
        this.selectedSortOrder = selectedSortOrder;
        this.sortOrderReversed = sortOrderReversed;
    }

    public Map<String, String> getSortOrders() {
        return sortOrders;
    }

    public String getSelectedSortOrder() {
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

        Map<String, String> otherSortOrders = other.sortOrders;
        for (Map.Entry<String, String> e : sortOrders.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            if (value == null) {
                if (!(otherSortOrders.get(key) == null
                        && otherSortOrders.containsKey(key))) {
                    return false;
                }
            } else {
                if (!value.equals(otherSortOrders.get(key))) {
                    return false;
                }
            }
        }

        return true;
    }
}
