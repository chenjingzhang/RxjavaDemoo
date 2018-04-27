package com.dxt2.rxjavandretro.progress;


import android.content.Context;
import android.util.Log;

import com.dxt2.rxjavandretro.observer.ObserverOnNextListener;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2018/4/27 0027.
 */

public class ProgressObserver<T> implements Observer<T>, ProgressCancelListener {
    private static final String TAG = "====";
    private ObserverOnNextListener listener;
    private ProgressDialogHandler progressDialogHandler;
    private Context context;
    private Disposable d;

    public ProgressObserver(ObserverOnNextListener listener, Context context) {
        this.listener = listener;
        this.context = context;
        this.progressDialogHandler = new ProgressDialogHandler(context, this, true);
    }

    private void showProgressDialog() {
        if (progressDialogHandler != null) {
            progressDialogHandler.obtainMessage(ProgressDialogHandler.SHOW_PROGRESS_DIALOG);
        }
    }

    private void dismissProgressDialog() {
        if (progressDialogHandler != null) {
            progressDialogHandler.obtainMessage(ProgressDialogHandler.DISMISS_PROGRESS_DIALOG).sendToTarget();
            progressDialogHandler = null;

        }
    }


    @Override
    public void onSubscribe(Disposable d) {
        this.d = d;
        showProgressDialog();
    }

    @Override
    public void onNext(T value) {
        listener.onNext(value);
    }

    @Override
    public void onError(Throwable e) {
        dismissProgressDialog();
        Log.e("====", "onError");
    }

    @Override
    public void onComplete() {
        dismissProgressDialog();
        Log.d(TAG, "onComplete: ");
    }

    @Override
    public void onCancelProgress() {
        if (!d.isDisposed()) {
            d.dispose();
        }
    }
}
