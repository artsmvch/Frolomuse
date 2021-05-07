package com.frolo.muse.model;


/**
 * Enumeration of available app themes.
 */
public enum Theme {
    
    LIGHT_BLUE(21, false),
    DARK_BLUE(22, true),
    DARK_BLUE_ESPECIAL(23, true),
    DARK_PURPLE(24, true),
    DARK_ORANGE(25, true),
    DARK_GREEN(26, true),
    LIGHT_PINK(27, false),
    DARK_FANCY(28, true)
    ;

    public static Theme findById(int id) {
        final Theme[] values = Theme.values();
        for (Theme theme : values) {
            if (theme.getId() == id) {
                return theme;
            }
        }
        return null;
    }

    public static Theme findByIdOrDefault(int id, Theme defaultValue) {
        Theme theme = findById(id);
        return theme != null ? theme : defaultValue;
    }
    
    private final int id;
    private final boolean isDark;
    
    Theme(int id, boolean isDark) {
        this.id = id;
        this.isDark = isDark;
    }

    /**
     * Returns identifier of theme.
     * This must be unique among all themes.
     * @return identifier of theme
     */
    public int getId() {
        return id;
    }

    public boolean isDark() {
        return isDark;
    }
    
    public boolean isLight() {
        return !isDark();
    }

    @Override
    public String toString() {
        return "Theme{" +
                "name=" + name() +
                ", id=" + id +
                ", isDark=" + isDark +
                '}';
    }
}
