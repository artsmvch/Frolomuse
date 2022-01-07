package com.frolo.music.model;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Objects;


/**
 * An abstraction that represents a sort order.
 */
public abstract class SortOrder {

    /**
     * Checks if the given sort orders have the same key.
     * Actually, this checks if two sort orders do the same sorting.
     * @param order1 the first sort order
     * @param order2 the second sort order
     * @return true if the given sort orders have the same key, false - otherwise
     */
    public static boolean areKeysTheSame(/*non-null*/ SortOrder order1, /*non-null*/ SortOrder order2) {
        if (order1 == null)
            throw new NullPointerException("order1 is null");

        if (order2 == null)
            throw new NullPointerException("order2 is null");

        return Objects.equals(order1.getKey(), order2.getKey());
    }

    /**
     * Searches <code>sortOrders</code> for a sort order that has the same key as the given <code>desiredKey</code>.
     * @param sortOrders to search
     * @param desiredKey that the desired sort order should have
     * @return sort order with the given key
     */
    @Nullable
    public static SortOrder pick(List<SortOrder> sortOrders, String desiredKey) {
        if (sortOrders == null || sortOrders.isEmpty())
            return null;

        for (SortOrder sortOrder : sortOrders) {
            if (sortOrder != null && Objects.equals(sortOrder.getKey(), desiredKey))
                return sortOrder;
        }

        return null;
    }

    /**
     * Returns the localized name of the sort order according to the current app locale.
     * @return the localized name of the sort order.
     */
    abstract public String getLocalizedName();

    /**
     * Returns the key of the sort order.
     * This is unique for the sort order and actually defines the sorting.
     * @return the key of the sort order.
     */
    abstract public String getKey();

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (!(obj instanceof SortOrder)) return false;

        final SortOrder other = (SortOrder) obj;

        return Objects.equals(getKey(), other.getKey()) && Objects.equals(getLocalizedName(), other.getLocalizedName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getLocalizedName());
    }
}
