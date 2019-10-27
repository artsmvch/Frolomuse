package com.frolo.muse.model.preset;


// Preset that does nothing: it doesn't apply any settings to audio fx.
public class VoidPreset implements Preset {
    private final String name;

    public VoidPreset(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
