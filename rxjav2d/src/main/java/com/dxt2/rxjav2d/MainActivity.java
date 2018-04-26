package com.dxt2.rxjav2d;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

//参考链接 https://www.jianshu.com/p/464fa025229e
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//       rxjavaUse1();//最基础用法
//       rxjavaUse2(); //ObservableEmitter Disposable
//       rxjavaUse3();//subscribe()有多个重载的方法
//       rxjavaUse11();//上下游默认是在同一个线程工作.
//       rxjavaUse22();//在子线程中做耗时的操作, 然后回到主线程中来操作UI,
//         rxjavaUse33();//，模拟网络请求retrofit+rxjava(未完成)
//        rxjavaUse111();//Map
//        rxjavaUse222();FlatMap
        rxjava1111();//zip
        rxjava2222();//flowable backpressure背压

    }




    /*ObservableEmitter用来发出事件的,通过调用emitter的onNext(T value)、onComplete()和onError(Throwable error)就可以分别发出next事件、complete事件和error事件
    /规则：1上游可以发送无限个onNext, 下游也可以接收无限个onNext.
          2 当上游发送了一个onComplete后, 上游onComplete之后的事件将会继续发送, 而下游收到onComplete事件之后将不再继续接收事件.
         3当上游发送了一个onError后, 上游onError之后的事件将继续发送, 而下游收到onError事件之后将不再继续接收事件.
    Disposable
   可以把它理解成两根管道之间的一个机关,当调用它的dispose()方法时, 它就会将两根管道切断, 从而导致下游收不到事件.
   调用dispose()并不会导致上游不再继续发送事件, 上游会继续发送剩余的事件.
    */
    private void rxjavaUse1() {
        //创建上游Observable：
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onComplete();
            }
        });
        //创建一个下游 Observer
        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d("=====", "subscribe");
            }

            @Override
            public void onNext(Integer value) {
                Log.d("=====", "" + value);
            }

            @Override
            public void onError(Throwable e) {
                Log.d("=====", "" + e);
            }

            @Override
            public void onComplete() {
                Log.d("=====", "complete");
            }
        };
        //建立连接
        // 一旦observable订阅了observer，observable就是调用observer对象的onNext和onComplete方法，
        observable.subscribe(observer);
        //=====: subscribe
        //=====: 1
        //=====: 2
        //=====: 3
        //=====:complete


        //链式操作
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onComplete();
            }
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d("====", "subscribe");
            }

            @Override
            public void onNext(Integer value) {
                Log.d("====", "" + value);
            }

            @Override
            public void onError(Throwable e) {
                Log.d("====", "error");
            }

            @Override
            public void onComplete() {
                Log.d("====", "complete");
            }
        });
    }

    //在收到onNext 2这个事件后, 切断了水管, 但是上游仍然发送了3, complete, 4这几个事件,
    //而且上游并没有因为发送了onComplete而停止.
    // 同时可以看到下游的onSubscribe()方法是最先调用的
    private void rxjavaUse2() {

        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Log.d("====", "emit 1");
                e.onNext(1);
                Log.d("====", "emit 2");
                e.onNext(2);
                Log.d("====", "emit 3");
                e.onNext(3);
                Log.d("====", "emit complete");
                e.onComplete();
                Log.d("====", "emit 4");
                e.onNext(4);
            }
        }).subscribe(new Observer<Integer>() {
            private Disposable mDisposable;
            private int i;

            @Override
            public void onSubscribe(Disposable d) {
                Log.d("====", "subsribe");
                mDisposable = d;
            }

            @Override
            public void onNext(Integer value) {
                Log.d("====", "onNext: " + value);
                i++;
                if (i == 2) {
                    mDisposable.dispose();
                    Log.d("====", "isDisposed : " + mDisposable.isDisposed());
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d("====", "error");
            }

            @Override
            public void onComplete() {
                Log.d("====", "complete");
            }
        });
    }

    //说明subscribe()有多个重载的方法
//   public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Action onComplete) {}
//  不带任何参数的subscribe() 表示下游不关心任何事件,你上游尽管发你的数据去吧
//  带有一个Consumer参数的方法表示下游只关心onNext事件, 其他的事件我假装没看见,
    private void rxjavaUse3() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.d("====", "emit 1");
                emitter.onNext(1);
                Log.d("====", "emit 2");
                emitter.onNext(2);
                Log.d("====", "emit 3");
                emitter.onNext(3);
                Log.d("====", "emit complete");
                emitter.onComplete();
                Log.d("====", "emit 4");
                emitter.onNext(4);
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.d("====", "onNext: " + integer);
            }
        });

    }

    //当我们在主线程去创建一个下游Observer来接收事件, 则这个下游默认就在主线程中接收事件
    private void rxjavaUse11() {
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Log.d("====", "Observable thread is : " + Thread.currentThread().getName());
                Log.d("====", "emit 1");
                e.onNext(1);
            }
        });
        Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.d("====", "Observer thread is :" + Thread.currentThread().getName());
                Log.d("====", "onNext: " + integer);
            }
        };
        observable.subscribe(consumer);
        //Observable thread is : main
        //emit 1
        //Observer thread is :main
        //onNext: 1
    }

    //在子线程中做耗时的操作, 然后回到主线程中来操作UI,
    /*多次指定上游的线程只有第一次指定的有效, 也就是说多次调用subscribeOn() 只有第一次的有效, 其余的会被忽略.
    * 多次指定下游的线程是可以的, 也就是说每调用一次observeOn() , 下游的线程就会切换一次.
    * */
    private void rxjavaUse22() {
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Log.d("====", "Observable thread is : " + Thread.currentThread().getName());
                Log.d("====", "emit 1");
                e.onNext(1);
            }
        });
        Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.d("====", "Observer thread is :" + Thread.currentThread().getName());
                Log.d("====", "onNext: " + integer);
            }
        };
//  1   observable.subscribeOn(Schedulers.newThread())//subscribeOn() 指定的是上游发送事件的线程,
//             .observeOn(AndroidSchedulers.mainThread())//observeOn() 指定的是下游接收事件的线程.
//             .subscribe(consumer);
        //Observable thread is : RxNewThreadScheduler-1
        //emit 1
        //Observer thread is :main
        //onNext: 1
// 2 上游虽然指定了两次线程, 但只有第一次指定的有效,下游下游则跑到了RxCachedThreadScheduler 中, 这个CacheThread其实就是IO线程池中的一个.
//        observable.subscribeOn(Schedulers.newThread())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .observeOn(Schedulers.io())
//                .subscribe(consumer);
        //====: Observable thread is : RxNewThreadScheduler-1
        ///====: emit 1
        //====: Observer thread is :RxCachedThreadScheduler-2
        //====: onNext: 1

        //3
        observable.subscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d("====", "After observeOn(mainThread), current thread is: " + Thread.currentThread().getName());
                    }
                }).observeOn(Schedulers.io())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d("====", "After observeOn(io), current thread is : " + Thread.currentThread().getName());
                    }
                }).subscribe(consumer);
        // Observable thread is : RxNewThreadScheduler-1
        //emit 1
        //After observeOn(mainThread), current thread is: main
        //After observeOn(io), current thread is : RxCachedThreadScheduler-2
        //Observer thread is :RxCachedThreadScheduler-2
        //onNext: 1
    }

    //如果在请求的过程中Activity已经退出了, 这个时候如果回到主线程去更新UI, 那么APP肯定就崩溃了
    //调用它的dispose()方法时就会切断水管, 使得下游收不到事件, 既然收不到事件, 那么也就不会再去更新UI了. 因此我们可以在Activity中将这个Disposable 保存起来, 当Activity退出时, 切断它即可
    //RxJava中已经内置了一个容器CompositeDisposable, 每当我们得到一个Disposable时就调用CompositeDisposable.add()将它添加到容器中, 在退出的时候, 调用CompositeDisposable.clear() 即可切断所有的水管.
    private void rxjavaUse33() {

    }

    //通过Map, 可以将上游发来的事件转换为任意的类型, 可以是一个Object, 也可以是一个集合,
    private void rxjavaUse111() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                return "this is result" + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.d("=====", s);
            }
        });
        //this is result1  this is result2 this is result3
    }

    //FlatMap将一个发送事件的上游Observable变换为多个发送事件的Observables，然后将它们发射的事件合并后放进一个单独的Observable里.
    //flatMap并不保证事件的顺序,
    //concatMap 它和flatMap的作用几乎一模一样, 只是它的结果是严格按照上游发送的顺序来发送的
    private void rxjavaUse222() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                final List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; ++i) {
                    list.add("Iamvalue" + integer);
                }
                return Observable.fromIterable(list).delay(10, TimeUnit.SECONDS
                );
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.d("====", s);
            } // Iamvalue1  Iamvalue1   Iamvalue1   Iamvalue2 Iamvalue2 Iamvalue2  Iamvalue3  Iamvalue3  Iamvalue3
        });

    }

    //Zip通过一个函数将多个Observable发送的事件结合到一起，然后发送这些组合到一起的事件. 它按照严格的顺序应用这个函数
    private void rxjava1111() {
     //因为我们两根水管都是运行在同一个线程里, 同一个线程里执行代码肯定有先后顺序呀.
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.d("====", "emit 1");
                emitter.onNext(1);
                Log.d("====", "emit 2");
                emitter.onNext(2);
                Log.d("====", "emit 3");
                emitter.onNext(3);
                Log.d("====", "emit 4");
                emitter.onNext(4);
                Log.d("====", "emit complete1");
                emitter.onComplete();
            }
        });
        Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Log.d("====", "emit A");
                emitter.onNext("A");
                Log.d("====", "emit B");
                emitter.onNext("B");
                Log.d("====", "emit C");
                emitter.onNext("C");
                Log.d("====", "emit complete2");
                emitter.onComplete();
            }
        });

        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d("====", "onSubscribe");
            }

            @Override
            public void onNext(String value) {
                Log.d("====", "onNext: " + value);
            }

            @Override
            public void onError(Throwable e) {
                Log.d("====", "onError");

            }

            @Override
            public void onComplete() {
                Log.d("====", "onComplete");
            }
        });//onSubscribe   emit 1  emit 2  emit 3   emit 4  emit complete1
         //emit A  onNext: 1A  emit B  onNext: 2B  emit C  onNext: 3C  emit complete2  onComplete

   //如果让水管都在IO线程里发送事件
//         onSubscribe  emit A  emit 1  onNext: 1A
//         emit B  emit 2  onNext: 2B
//         emit C  emit 3  onNext: 3C
//         emit  complete2  onComplete

    }
   //只使用Observable如何去解决上下游流速不均衡的问题
    //当上下游工作在同一个线程中时, 这时候是一个同步的订阅关系, (正常)也就是说上游每发送一个事件必须等到下游接收处理完了以后才能接着发送下一个事件
    //当上下游工作在不同的线程中时, 这时候是一个异步的订阅关系,（oom） 这个时候上游发送数据不需要等待下游接收,

//    这个时候把上游切换到了IO线程中去, 下游到主线程去接收,

    // new Predicate<Integer>() 通过减少进入水缸的事件数量的确可以缓解上下游流速不均衡的问题,(减少放进水缸的事件的数量,丢失了大部分的事件)
    //用一个sample操作符，这个操作符每隔指定的时间就从上游中取出一个事件发送给下游.

    //,sleep 既然上游发送事件的速度太快, 那我们就适当减慢发送事件的速度, 从速度上取胜


    //上游和下游分别是Observable和Observer,
    // 这次不一样的是上游变成了Flowable, 下游变成了Subscriber
    private void rxjava2222() {
        Flowable<Integer> upstream = Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                Log.d("====", "emit 1");
                emitter.onNext(1);
                Log.d("====", "emit 2");
                emitter.onNext(2);
                Log.d("====", "emit 3");
                emitter.onNext(3);
                Log.d("====", "emit complete");
                emitter.onComplete();
            }
        }, BackpressureStrategy.ERROR);//BackpressureStrategy.ERROR用来选择背压

        Subscriber<Integer> downstream = new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                Log.d("====", "onSubscribe");
                s.request(Long.MAX_VALUE);  //注意这句代码
            }

            @Override
            public void onNext(Integer integer) {
                Log.d("====", "onNext: " + integer);
            }

            @Override
            public void onError(Throwable t) {
                Log.w("====", "onError: ", t);
            }

            @Override
            public void onComplete() {
                Log.d("====", "onComplete");
            }
        };
        upstream.subscribe(downstream);
    //onSubscribe    emit 1  onNext: 1  emit 2  onNext: 2  emit 3  onNext: 3    emit complete onComplete
    //去掉   s.request(Long.MAX_VALUE); 出现MissingBackpressureException
    //写成异步onSubscrib emit 1  emit 2  emit 3 emit complete 上游正确的发送了所有的事件, 但是下游一个事件也没有收到（因为在Flowable里默认有一个大小为128的水缸）
    //把request当做是一种能力, 当成下游处理事件的能力, 下游能处理几个就告诉上游我要几个, 这样只要上游根据下游的处理能力来决定发送多少事件
    }
}
























