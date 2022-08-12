package com.frolo.muse.model;


public final class ABState {
    private final boolean aPointed;
    private final boolean bPointed;

    public ABState(boolean aPointed, boolean bPointed) {
        this.aPointed = aPointed;
        this.bPointed= bPointed;
    }

    public boolean isAPointed() {
        return aPointed;
    }

    public boolean isBPointed() {
        return bPointed;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ABState)) return false;

        ABState other = (ABState) obj;
        return aPointed == other.aPointed && bPointed == other.bPointed;
    }

    @Override
    public String toString() {
        return "ABState:[aPointed=" + aPointed + ", bPointed=" + bPointed + "]";
    }

}
