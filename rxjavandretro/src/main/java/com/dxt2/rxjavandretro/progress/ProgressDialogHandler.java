package com.dxt2.rxjavandretro.progress;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;

import java.util.logging.LogRecord;

/**
 * Created by Administrator on 2018/4/27 0027.
 */

public class ProgressDialogHandler extends Handler {
    public static final int SHOW_PROGRESS_DIALOG = 1;
    public static final int DISMISS_PROGRESS_DIALOG = 2;

    private ProgressDialog pd;
    private Context context;
    private boolean cancelabe;
    private ProgressCancelListener listener;

    public ProgressDialogHandler(Context context, ProgressCancelListener listener, boolean cancelabe) {
        this.context = context;
        this.cancelabe = cancelabe;
        this.listener = listener;
    }

    private void initProgresDialog() {
        if (pd == null) {
            pd = new ProgressDialog(context);
            pd.setCancelable(cancelabe);
            if (cancelabe) {
                pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        listener.onCancelProgress();
                    }
                });
            }
            if (!pd.isShowing()) {
                pd.show();
            }
        }
    }

    private void dismissProgressDialog(){
        if(pd!=null){
            pd.dismiss();
            pd = null;
        }
    }

    @Override
    public void handleMessage(Message msg) {
       switch (msg.what){
           case SHOW_PROGRESS_DIALOG:
               initProgresDialog();
               break;
           case DISMISS_PROGRESS_DIALOG:
               dismissProgressDialog();
               break;
       }


    }
}
