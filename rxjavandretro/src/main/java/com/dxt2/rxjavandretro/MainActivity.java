package com.dxt2.rxjavandretro;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.dxt2.rxjavandretro.data.Movie;
import com.dxt2.rxjavandretro.data.Subjects;
import com.dxt2.rxjavandretro.http.Api;
import com.dxt2.rxjavandretro.http.ApiMethods;
import com.dxt2.rxjavandretro.http.ApiService;
import com.dxt2.rxjavandretro.observer.MyObserver;
import com.dxt2.rxjavandretro.observer.ObserverOnNextListener;
import com.dxt2.rxjavandretro.progress.ProgressObserver;
import java.util.List;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        first(); //最基本的RxJava+Retrofit
//        second();//封装retrofit请求过程  封装线程管理和订阅
//        third();//进一步针对Observer进行封装,,使用接口+implements方式实现
                  //优雅的封装Rxjava+Retrofit
        /*网络请求一般都是耗时的，我们需要在进行网络请求的时候有一个进度框，增加用户体验
          根据上一步，我们只需要将进度框show在onSubscribe中，
          然后在onError/onComplete里cancel掉就行了。
          如果用户取消关闭了进度框，那么也随之取消了当前的Http请求。
        */
        four();
    }

    private void four() {
        ObserverOnNextListener<Movie> listener = new ObserverOnNextListener<Movie>() {
            @Override
             public void onNext(Movie movie) {
             Log.d("====","onNext"+movie.getTitle());
             List<Subjects> list = movie.getSubjects();
             for(Subjects sub:list){
                 Log.d("====", "onNext: " + sub.getId() + "," + sub.getYear() + "," + sub.getTitle());
             }
            }
        };
        ApiMethods.getTopMovie(new ProgressObserver<Movie>(listener,this),0,10);
    }

    /**
     * 重写Observer对象，实现onSubscribe，onNext，onError，onComplete的封装
     */
    private void third() {
        ObserverOnNextListener<Movie> listener = new ObserverOnNextListener<Movie>() {
            @Override
            public void onNext(Movie movie) {
                Log.d("TAG", "onNext" + movie.getTitle());
                List<Subjects> list = movie.getSubjects();
                for (Subjects sub : list) {
                    Log.d("TAG", "onNext" + sub.getId() + "," + sub.getYear() + "," + sub.getTitle());
                }
            }
        };
        ApiMethods.getTopMovie(new MyObserver<Movie>(this, listener), 0, 10);


    }

    /*1.封装retrofit请求过程
    * 2.封装线程管理和订阅
    * */
    private void second() {
        Observer<Movie> observer = new Observer<Movie>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d("====", "onSubscribe: ");
            }

            @Override
            public void onNext(Movie movie) {
                Log.d("====", "" + movie.getTitle());
                List<Subjects> list = movie.getSubjects();
                for (Subjects sub : list) {
                    Log.d("====", "onNext" + sub.getId() + "," + sub.getYear() + "," + sub.getTitle());
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e("====", "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d("====", "onComplete: Over!");
            }
        };
        ApiMethods.getTopMovie(observer, 0, 10);

    }

    private void first() {
        String baseUrl = "https://api.douban.com/v2/movie/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);
        apiService.getTopMovie(0, 10)
                .subscribeOn(Schedulers.io())//请求网络在子线程，订阅也在子线程中
//                .observeOn(AndroidSchedulers.mainThread()) //如果加上这条，则订阅发生在主线程中
                .subscribe(new Observer<Movie>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d("====", "onSubscribe: ");
                    }

                    @Override
                    public void onNext(Movie movie) {
                        Log.d("====", "onNext" + movie.getTitle());
                        List<Subjects> list = movie.getSubjects();
                        for (Subjects sub : list) {
                            Log.d("====", "onNext" + sub.getId() + "," + sub.getYear() + "," + sub.getTitle());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("====", "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d("====", "onComplete: Over!");
                    }
                });

    }
}
