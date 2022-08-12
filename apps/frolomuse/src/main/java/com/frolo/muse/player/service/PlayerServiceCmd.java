package com.frolo.muse.player.service;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef(
    {
        PlayerServiceCmd.CMD_NO_OP,
        PlayerServiceCmd.CMD_SKIP_TO_PREVIOUS,
        PlayerServiceCmd.CMD_SKIP_TO_NEXT,
        PlayerServiceCmd.CMD_TOGGLE,
        PlayerServiceCmd.CMD_CHANGE_REPEAT_MODE,
        PlayerServiceCmd.CMD_CHANGE_SHUFFLE_MODE,
        PlayerServiceCmd.CMD_CHANGE_FAV_STATUS,
        PlayerServiceCmd.CMD_CANCEL_NOTIFICATION
    }
)
@Retention(RetentionPolicy.SOURCE)
public @interface PlayerServiceCmd {
    int CMD_NO_OP = 100;
    int CMD_SKIP_TO_PREVIOUS = 101;
    int CMD_SKIP_TO_NEXT = 102;
    int CMD_TOGGLE = 103;
    int CMD_CHANGE_REPEAT_MODE = 104;
    int CMD_CHANGE_SHUFFLE_MODE = 105;
    int CMD_CHANGE_FAV_STATUS = 106;
    int CMD_CANCEL_NOTIFICATION = 107;
}
