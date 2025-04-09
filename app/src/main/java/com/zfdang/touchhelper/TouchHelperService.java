package com.zfdang.touchhelper;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class TouchHelperService extends AccessibilityService {

    public final static int ACTION_REFRESH_KEYWORDS = 1;
    public final static int ACTION_REFRESH_PACKAGE = 2;
    public final static int ACTION_REFRESH_CUSTOMIZED_ACTIVITY = 3;
    public final static int ACTION_ACTIVITY_CUSTOMIZATION = 4;
    public final static int ACTION_STOP_SERVICE = 5;
    public final static int ACTION_START_SKIPAD = 6;
    public final static int ACTION_STOP_SKIPAD = 7;

    private static WeakReference<TouchHelperService> sServiceRef;
    private TouchHelperServiceImpl serviceImpl;

    private final String TAG = getClass().getName();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        sServiceRef = new WeakReference<>(this);
        if (serviceImpl == null) {
            serviceImpl = new TouchHelperServiceImpl(this);
        }
        if (serviceImpl != null) {
            serviceImpl.onServiceConnected();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (serviceImpl != null) {
            serviceImpl.onAccessibilityEvent(event);
        }
    }

    @Override
    public void onInterrupt() {
        // if (serviceImpl != null) {
        //     serviceImpl.onInterrupt();
        // }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (serviceImpl != null) {
            serviceImpl.onUnbind(intent);
            serviceImpl = null;
        }
        sServiceRef = null;
        return super.onUnbind(intent);
    }

    public static boolean dispatchAction(int action) {
        final TouchHelperService service = sServiceRef != null ? sServiceRef.get() : null;
        if (service == null || service.serviceImpl == null) {
            return false;
        }
        service.serviceImpl.receiverHandler.sendEmptyMessage(action);
        return true;
    }

    public static boolean isServiceRunning() {
        final TouchHelperService service = sServiceRef != null ? sServiceRef.get() : null;
        return service != null && service.serviceImpl != null;
    }

    public static void warnFraud(Context context) {
        // 确保在主线程中执行
        new Handler(Looper.getMainLooper()).post(() -> {
            // 创建通知管理器
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // 创建通知渠道（适用于 Android 8.0 及以上版本）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = "fraud_warning_channel";
                CharSequence channelName = "诈骗警告";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
                notificationManager.createNotificationChannel(channel);
            }

            // 构建通知
            Notification.Builder builder = new Notification.Builder(context)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert) // 设置图标
                    .setContentTitle("诈骗警告") // 设置标题
                    .setContentText("您可能正在受到诈骗") // 设置内容
                    .setPriority(Notification.PRIORITY_HIGH); // 设置优先级

            // 如果是 Android 8.0 及以上版本，需要指定通知渠道
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId("fraud_warning_channel");
            }

            // 发送通知
            Notification notification = builder.build();
            notificationManager.notify(1, notification); // 使用唯一的 ID（如 1）来标识通知
        });

//        if (context instanceof Activity) {
//            showAlertDialog((Activity) context);
//        } else {
//            Log.e("warnFraud", "Context is not an instance of Activity, cannot show AlertDialog.");
//        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter("TRIGGER_WARN_FRAUD");
        filter.addAction("TRIGGER_SAFE");
        registerReceiver(fraudReceiver, filter);
    }


    private BroadcastReceiver fraudReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("TRIGGER_WARN_FRAUD".equals(intent.getAction())) {
                Log.d("Broadcast", "Received TRIGGER_WARN_FRAUD broadcast");
                warnFraud(TouchHelperService.this); // 调用提醒方法
            } else if ("TRIGGER_SAFE".equals(intent.getAction())) {
                warnNotFraud(TouchHelperService.this);
            }
        }
    };

    private static void showAlertDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("诈骗警告")
                .setMessage("您可能正在受到诈骗")
                .setPositiveButton("确认", null)
                .setNegativeButton("取消", null)
                .show();
    }

    public static void warnNotFraud(Context context) {
        // 确保在主线程中执行
        new Handler(Looper.getMainLooper()).post(() -> {
            // 创建通知管理器
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // 创建通知渠道（适用于 Android 8.0 及以上版本）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = "fraud_warning_channel";
                CharSequence channelName = "防御正在运行";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
                notificationManager.createNotificationChannel(channel);
            }

            // 构建通知
            Notification.Builder builder = new Notification.Builder(context)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert) // 设置图标
                    .setContentTitle("防御正在运行") // 设置标题
                    .setContentText("防御正在运行") // 设置内容
                    .setPriority(Notification.PRIORITY_HIGH); // 设置优先级

            // 如果是 Android 8.0 及以上版本，需要指定通知渠道
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId("fraud_warning_channel");
            }

            // 发送通知
            Notification notification = builder.build();
            notificationManager.notify(1, notification); // 使用唯一的 ID（如 1）来标识通知
        });

//        if (context instanceof Activity) {
//            showAlertDialog((Activity) context);
//        } else {
//            Log.e("warnFraud", "Context is not an instance of Activity, cannot show AlertDialog.");
//        }
    }
}
