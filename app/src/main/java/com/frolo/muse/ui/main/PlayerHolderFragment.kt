package com.frolo.muse.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.service.PlayerService


/**
 * Retained fragment that binds player service to the application context and holds the reference to the connected player.
 * Activity can use it to avoid binding the service to activity's context as it may cause problems with rotation etc.
 *
 * The fragment binds the service to the application context in [onCreate] and unbinds in [onDestroy].
 * Unbinding is in [onDestroy] because it gets called only once since the fragment instance is retained (See [setRetainInstance]).
 *
 * As the target context the application context is chosen as it lives as long as the app is running and no leak may occur in this case.
 */
internal class PlayerHolderFragment: Fragment(),
        ServiceConnection {

    private var isBound: Boolean = false
    private var playerConnection: PlayerConnection? = null
    var player: Player? = null
        private set

    interface PlayerConnection {
        fun onPlayerConnected(player: Player)
        fun onPlayerDisconnected()
    }

    init {
        retainInstance = true
    }

    override fun onServiceDisconnected(component: ComponentName) {
        isBound = false
        player = null
        playerConnection?.onPlayerDisconnected()
    }

    override fun onServiceConnected(component: ComponentName, binder: IBinder?) {
        isBound = true
        if (binder is PlayerService.PlayerBinder) {
            val playerInstance = binder.service
            player = playerInstance
            playerConnection?.onPlayerConnected(playerInstance)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        playerConnection = context as? PlayerConnection
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Do we need to check isBound value?
        player.let { playerInstance ->
            if (playerInstance != null) {
                // Let the host check it itself
                //callback?.onPlayerConnected(playerInstance)
            } else {
                val hostContext = requireContext()
                val appContext = hostContext.applicationContext
                val intent = PlayerService.newIntent(hostContext)
                appContext.bindService(intent, this, Context.BIND_IMPORTANT or Context.BIND_AUTO_CREATE)
            }
        }
    }

    /**
     * Fragment's host is going to be destroyed.
     * It's time to unbind the service.
     */
    override fun onDestroy() {
        val appContext = requireContext().applicationContext
        appContext.unbindService(this)
        super.onDestroy()
    }

    override fun onDetach() {
        playerConnection = null
        super.onDetach()
    }

}