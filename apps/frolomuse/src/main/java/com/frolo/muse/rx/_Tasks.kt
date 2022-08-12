package com.frolo.muse.rx

import com.google.android.gms.tasks.Task
import io.reactivex.Single
import io.reactivex.disposables.Disposables


fun <T> Task<T>.toSingle(): Single<T> {
    val task: Task<T> = this
    return Single.create { emitter ->

        task.addOnFailureListener { error ->
            if (!emitter.isDisposed) {
                emitter.onError(error)
            }
        }

        task.addOnCompleteListener { _task ->
            if (!emitter.isDisposed) {
                if (_task.isSuccessful) {
                    emitter.onSuccess(_task.result)
                } else {
                    val err: Exception = _task.exception ?: IllegalStateException("Task is not successful but the exception is null")
                    emitter.onError(err)
                }
            }
        }

        task.addOnCanceledListener {
            if (!emitter.isDisposed) {
                val error = IllegalStateException("Task has been cancelled")
                emitter.onError(error)
            }
        }

        emitter.setDisposable(Disposables.fromAction {
            // TODO: cancel the task
        })
    }
}