package com.dxt2.rxjavandretro.observer;

/**
 * Created by Administrator on 2018/4/27 0027.
 */
//使用通配泛型，适用于所有类型的数据。
public interface ObserverOnNextListener<T> {
    void onNext(T t);
}
