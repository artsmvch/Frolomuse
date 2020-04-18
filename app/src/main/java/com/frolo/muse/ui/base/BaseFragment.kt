package com.frolo.muse.ui.base

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.os.AsyncTask
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.frolo.muse.App
import com.frolo.muse.R
import com.frolo.muse.Trace
import com.frolo.muse.di.modules.ViewModelModule
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.repository.Preferences
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


abstract class BaseFragment: Fragment() {

    // Rx permissions
    private var rxPermissions: RxPermissions? = null
    private var rxPermissionDisposable: Disposable? = null

    // UI operations
    private val uiDisposables = CompositeDisposable()

    // UI Tasks
    private val uiAsyncTasks = ArrayList<AsyncTask<*, *, *>>(3)

    // Single progress dialog
    private var progressDialog: Dialog? = null

    // Single toast
    private var errorToast: Toast? = null

    // The following members are supposed to be injected
    private var prefs: Preferences? = null
    private var vmFactory: ViewModelModule.ViewModelFactory? = null
    private var eventLogger: EventLogger? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        rxPermissions = RxPermissions(this)
    }

    override fun onDestroyView() {
        progressDialog?.cancel()

        // Disposing UI async tasks
        uiAsyncTasks.forEach { it.cancel(true) }
        uiAsyncTasks.clear()

        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
        rxPermissions = null
    }

    override fun onStop() {
        super.onStop()
        uiDisposables.clear()
        rxPermissionDisposable?.dispose()
    }

    //<editor-fold desc="Injectors">
    internal fun prefs(): Lazy<Preferences> = lazy {
        if (prefs == null) {
            prefs = requireApp().appComponent.providePreferences()
        }

        prefs ?:
        throw IllegalStateException("Failed to inject preferences")
    }

    internal inline fun <reified T : ViewModel> viewModel(): Lazy<T> = lazy {
        if (vmFactory == null) {
            vmFactory = requireApp().appComponent.provideVMFactory()
        }

        val factory = vmFactory ?:
        throw IllegalStateException("Failed to inject vm factory")

        ViewModelProviders.of(this, factory)
                .get(T::class.java)
    }

    internal fun eventLogger(): Lazy<EventLogger> = lazy {
        if (eventLogger == null) {
            eventLogger = requireApp().appComponent.provideEventLogger()
        }

        eventLogger ?:
        throw IllegalStateException("Failed to inject event logger")
    }
    //</editor-fold>

    fun requireApp() = requireActivity().application as App

    fun isPermissionGranted(permission: String): Boolean {
        return rxPermissions?.isGranted(permission)
                ?: throw IllegalStateException("Fragment not attached")
    }

    fun requestRxPermissions(
            vararg permissions: String,
            consumer: (granted: Boolean) -> Unit) {

        val rxPermissions = rxPermissions
                ?: throw IllegalStateException("Fragment not attached")

        rxPermissionDisposable?.dispose()
        rxPermissionDisposable = rxPermissions.request(*permissions)
                .subscribe(consumer, { err ->
                    Trace.e(err)
                    toastError(err)
                })
    }

    // Checks READ_EXTERNAL_STORAGE permission.
    // If it is granted, then the [action] callback will be called immediately.
    // Otherwise, the user will be prompt to grant permission for the action.
    inline fun checkReadPermissionFor(crossinline action: () -> Unit) {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (isPermissionGranted(permission)) {
            action()
        } else {
            requestRxPermissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) { granted ->
                if (granted) action()
            }
        }
    }

    // Checks WRITE_EXTERNAL_STORAGE permission.
    // If it is granted, then the [action] callback will be called immediately.
    // Otherwise, the user will be prompt to grant permission for the action.
    inline fun checkWritePermissionFor(crossinline action: () -> Unit) {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (isPermissionGranted(permission)) {
            action()
        } else {
            requestRxPermissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) { granted ->
                if (granted) action()
            }
        }
    }

    // Checks both READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE permissions.
    // If they are granted, then the [action] callback will be called immediately.
    // Otherwise, the user will be prompt to grant permissions for the action.
    fun checkReadWritePermissionsFor(action: () -> Unit) {
        val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        requestRxPermissions(*permissions) { granted -> if (granted) action() }
    }

    fun toastShortMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun toastShortMessage(@StringRes stringId: Int) {
        Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show()
    }

    fun toastLongMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun toastLongMessage(@StringRes stringId: Int) {
        Toast.makeText(context, stringId, Toast.LENGTH_LONG).show()
    }

    fun toastError(error: Throwable?) {
        errorToast?.cancel()
        val msg = error?.message.let { msg ->
            if (msg.isNullOrBlank()) getString(R.string.sorry_exception) else msg
        }
        errorToast = Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).apply { show() }
    }

    fun showProgressDialog(message: String = getString(R.string.loading)) {
        progressDialog?.cancel()
        val context = this.context ?: return
        progressDialog = Dialog(context).apply {
            setContentView(R.layout.dialog_progress)
            findViewById<TextView>(R.id.tv_message).text = message
            show()
        }
    }

    fun hideProgressDialog() {
        progressDialog?.cancel()
    }

    /**
     * Performs an action on UI thread with a delay;
     * NOTE: the timer will be disposed if activity's state becomes STOP
     * @param action to perform
     * @param delay to postpone the action
     */
    fun runDelayedOnUI(action: () -> Unit, delay: Long) {
        val d = Completable
                .timer(delay, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action)
        uiDisposables.add(d)
    }

    protected fun invalidateOptionsMenu() {
        activity?.invalidateOptionsMenu()
    }

    /**
     * Saves the given [task] to cancel it when fragment's view is destroyed.
     * If the fragment has no created view, then the task is cancelled immediately.
     */
    protected fun saveUIAsyncTask(task: AsyncTask<*, *, *>) {
        if (view == null) {
            task.cancel(true)
        } else {
            uiAsyncTasks.add(task)
        }
    }

}