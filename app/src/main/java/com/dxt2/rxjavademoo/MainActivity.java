package com.dxt2.rxjavademoo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//      baseUseRxJava1();  create
//      baseUseRxJava2();  just
//      baseUseRxJava3();   from(strins)
//      useMapOperation();  map
      threadSchedulers();

    }


    //1>使用Observable的create()创建被观察者，重写call(),在该方法中调用观察者的onNext(),onCompleted(),onError()方法
    //2>new Subscriber<T>(){}创建观察者，重写onNext()(输出信息),onCompleted(),onError()
    //3》被观察者Observerable对象调用subscribe(Subscriber对象);完成subscriber对observable的订阅
    private void baseUseRxJava1() {
        Observable<String> myobservable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("测试1");
                subscriber.onCompleted();
            }
        });
        Subscriber<String> mysubscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Log.e("=====", "====完成==");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.e("=====", "====输出内容为==" + s);
            }
        };
        //完成subscriber对observable的订阅
//        /**
//         * 一旦mySubscriber订阅了myObservable，
//         * myObservable就是调用mySubscriber对象的onNext和onComplete方法，
//         * mySubscriber就会打印出Hello World！
//         */
        myobservable.subscribe(mysubscriber);
//====输出内容为==测试1
//====完成==

    }

    //使用Observable的just()创建被观察者返回被观察者对象；
    //被观察者对象调用subscribe(new Action<T>(){publid void call(T t){}})
    private void baseUseRxJava2() {
        Observable.just("222222").subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Log.e("=====", "====callgggg输出内容为==" + s);
            }
        });
        //使用范式完成
        Observable.just("22222fanshi").subscribe(
                s -> Log.e("=====", "====callgggg输出内容为==" + s)
        );

    }

    //1使用Observable的from(T[] array)创建被观察者;(接收一个集合作为输入，然后每次输出一个元素给subscriber.)
    //2被观察者对象调用subscribe(new Action<T>(){publid void call(T t){}})
    private void baseUseRxJava3() {
        String[] strings = new String[]{"数", "英", "政"};
        Observable.from(strings).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.e("=======", "===onNext======" + s);
            }
        });
    }//===onNext======数     ===onNext======英    ===onNext======政

    //把一个事件转换为另一个事件(用于变换Observable对象的，实现链式调用，最终将最简洁的数据传递给Subscriber对象)
    private void useMapOperation() {
        //案例1
        Observable.just("aaaa1").map(s ->
                Log.e("======", "======map输出内容===" + s)
        ).subscribe(s ->
                Log.e("======", "Action1输出内容为" + s));
        //======map输出内容===aaaa1
        //Action1输出内容为38
        //案例2
        Observable.just("bbbbb2").map(new Func1<String, Integer>() {
            @Override
            public Integer call(String s) {
                Log.e("=====", "=第一个map转化==" + s);
                return 123;
            }
        }).map(new Func1<Integer, String>() {
            @Override
            public String call(Integer integer) {
                Log.e("=====", "=第二个map转化=" + String.valueOf(integer) + 7);
                return String.valueOf(integer) + 7;
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Log.e("=====", "=最终结果=" + s);
            }
        });//=第一个map转化==bbbbb2    =第二个map转化=1237   =最终结果=1237
        //范式
        Observable.just("FAN")
                .map(s -> "2018")
                .map(m -> 1093)
                .subscribe(k -> Log.e("=====", "=最终结果=" + k));
        //=最终结果=1093

    }


    //.RxJava线程调度：
//    在不指定线程的情况下， RxJava 遵循的是线程不变的原则，
//    即：在哪个线程调用 subscribe()，就在哪个线程生产事件；在哪个线程生产事件，就在哪个线程消费事件。
//    如果需要切换线程，就需要用到 Scheduler（调度器:线程控制器：RxJava 通过它来指定每一段代码应该运行在什么样的线程）。

    //subscribeOn(): 指定 subscribe() 所发生的线程 ,subscribeOn() 的位置放在哪里都可以，但它是只能调用一次的
    //observeOn(): 指定 Subscriber 所运行在的线程
    private void threadSchedulers() {
        Observable.just(1, 2, 3)
                .subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) //指定 Subscriber 的回调发生在主线程
                .subscribe(num -> Log.e("===Schedulers线程调度===", "number:" + num));
         //===Schedulers线程调度===: number:1   ===Schedulers线程调度===: number:2    ...3    ...4

    Observable.just(1,2,3,4)  // IO 线程，由 subscribeOn() 指定
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .map(new Func1<Integer, Object>() {
                @Override
                public Object call(Integer integer) {
                    return integer + 4;
                }// 新线程，由 observeOn() 指定
            }).observeOn(Schedulers.io())
            .map(new Func1<Object, Object>() {
                @Override
                public Object call(Object o) {
                    return (Integer)o+2;
                }// IO 线程，由 observeOn() 指定
            }).observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<Object>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(Object o) {
                    Log.e("=====", "===onNext====" + o);
                // Android 主线程，由 observeOn() 指定
                }
            });
    }
}






















































