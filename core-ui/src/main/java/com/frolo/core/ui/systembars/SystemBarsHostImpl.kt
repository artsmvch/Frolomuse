package com.frolo.core.ui.systembars

import android.os.Handler
import android.os.Looper
import android.view.Window
import androidx.annotation.ColorInt
import java.util.*


internal class SystemBarsHostImpl(
    getWindowLambda: () -> Window
): SystemBarsHost {

    private val chain = LinkedList<ChainNode>()
    private val controller = SystemBarsControllerImpl(getWindowLambda)

    private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }

    constructor(window: Window): this({ window })

    override fun getSystemBarsController(owner: SystemBarsControlOwner): SystemBarsController? {
        val lastNode = chain.peek()
        return if (lastNode != null && lastNode.owner == owner) {
            lastNode.getControllerIfEnabled()
        } else {
            null
        }
    }

    override fun obtainSystemBarsControl(owner: SystemBarsControlOwner) {
        // handler.post { obtainSystemBarsControlImpl(owner) }
        obtainSystemBarsControlImpl(owner)
    }

    private fun obtainSystemBarsControlImpl(owner: SystemBarsControlOwner) {
        val lastNode: ChainNode? = chain.peek()
        if (lastNode != null && lastNode.owner == owner) {
            // No changes, control remains
            lastNode.enable()
            return
        }
        // Disable the last node
        lastNode?.disable()
        val targetNode: ChainNode? = removeFromChain(owner)
        when {
            targetNode != null -> {
                // Push and enable the target
                chain.push(targetNode)
                targetNode.enable()
            }
            else -> {
                val newNode = ChainNode(
                    owner = owner,
                    controller = controller,
                )
                chain.push(newNode)
                newNode.enable()
            }
        }
    }

    private fun removeFromChain(owner: SystemBarsControlOwner): ChainNode? {
        var targetNode: ChainNode? = null
        val iterator = chain.descendingIterator()
        while (iterator.hasNext()) {
            val nextNode = iterator.next()
            if (nextNode.owner == owner) {
                targetNode = nextNode
                iterator.remove()
                break
            }
        }
        return targetNode
    }

    override fun abandonSystemBarsControl(owner: SystemBarsControlOwner) {
        // handler.post { abandonSystemBarsControlImpl(owner) }
        abandonSystemBarsControlImpl(owner)
    }

    private fun abandonSystemBarsControlImpl(owner: SystemBarsControlOwner) {
        val lastNode = chain.peek()
            ?: throw IllegalStateException("No owner controls the system bars")

        val targetNode: ChainNode = removeFromChain(owner)
            ?: throw IllegalStateException("$owner did not gain control over the system bars earlier")
        targetNode.disable()

        if (targetNode == lastNode) {
            // Pass control to the next one
            chain.peek()?.enable()
        }
    }

    private class ChainNode(
        val owner: SystemBarsControlOwner,
        private val controller: SystemBarsController
    ) : SystemBarsController {
        private var isEnabled: Boolean = false

        override fun setStatusBarVisible(isVisible: Boolean) {
            if (isEnabled) {
                controller.setStatusBarVisible(isVisible)
            }
        }

        override fun setStatusBarColor(@ColorInt color: Int) {
            if (isEnabled) {
                controller.setStatusBarColor(color)
            }
        }

        override fun setStatusBarAppearanceLight(isLight: Boolean) {
            if (isEnabled) {
                controller.setStatusBarAppearanceLight(isLight)
            }
        }

        fun enable() {
            if (!isEnabled) {
                isEnabled = true
                owner.onSystemBarsControlObtained(this)
            }
        }

        fun disable() {
            if (isEnabled) {
                isEnabled = false
                owner.onSystemBarsControlAbandoned()
            }
        }

        fun getControllerIfEnabled(): SystemBarsController? {
            return if (isEnabled) this else null
        }

    }
}