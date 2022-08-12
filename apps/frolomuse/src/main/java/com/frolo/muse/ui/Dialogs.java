package com.frolo.muse.ui;

import android.content.DialogInterface;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;


public final class Dialogs {
    private Dialogs() {
    }

    public static void fixBottomSheet(@NonNull BottomSheetDialog dialog) {
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet == null) {
                    throw new NullPointerException("Could not find bottom sheet in " + d);
                }
                BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setHideable(true);
                behavior.setSkipCollapsed(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

}
