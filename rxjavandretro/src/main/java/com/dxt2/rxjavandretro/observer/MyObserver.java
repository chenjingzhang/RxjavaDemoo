package com.dxt2.rxjavandretro.observer;


import android.content.Context;
import android.util.Log;
import android.util.LongSparseArray;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2018/4/27 0027.
 */
//重写Observer
public class MyObserver<T> implements Observer<T> {
    private static final String TAG = "MyObserver";
    private ObserverOnNextListener listener;
    private Context context;

    public MyObserver( Context context,ObserverOnNextListener listener) {
        this.listener = listener;
        this.context = context;
    }

    @Override
    public void onSubscribe(Disposable d) {
        Log.d(TAG, "onSubscribe: ");
    }

    @Override
    public void onNext(T t) {
         listener.onNext(t);
    }

    @Override
    public void onError(Throwable e) {
        Log.d(TAG,"onError"+e);
    }

    @Override
    public void onComplete() {
     Log.d(TAG,"onComplete");
    }
}
