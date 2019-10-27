package com.frolo.muse.repository;

import com.frolo.muse.model.preset.CustomPreset;
import com.frolo.muse.model.preset.VoidPreset;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public interface PresetRepository {
    Flowable<List<CustomPreset>> getPresets();

    Single<VoidPreset> getVoidPreset();

    Flowable<CustomPreset> getPresetById(long id);

    Single<CustomPreset> create(String name, short[] levels);

    Completable delete(CustomPreset preset);
}
