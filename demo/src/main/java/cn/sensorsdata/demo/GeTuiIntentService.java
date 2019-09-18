package cn.sensorsdata.demo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.PushManager;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import cn.jpush.android.api.JPushInterface;
import cn.sensorsdata.demo.util.SFUtils;

import static android.app.NotificationManager.*;
import static android.support.v4.app.NotificationManagerCompat.IMPORTANCE_HIGH;

/**
 * 个推 IntentService 用于接收消息
 * <p>
 * 1d511524bd62caa63040b838766d98a4
 * 注意：个推 的消息 点击之后，会默认重新打开App
 * <p>
 * 透传消息
 * 在线：走 SDK 的回调 onReceiveMessageData，拿到数据，启动自己的通知栏
 * 离线：走厂商通道，个推自己启动通知栏，点击启动一个指定的页面，我们在页面里面从intent取参数
 */

public class GeTuiIntentService extends GTIntentService {


    private static final String TAG = "个推";
    private static final String TXT = "----------------------- 个推 ---------------------------\n";

    @Override
    public void onReceiveServicePid(Context context, int i) {
        Log.i(TAG, "onReceiveServicePid:" + TXT + i);
    }

    /**
     * 接收 clientid
     */
    @Override
    public void onReceiveClientId(Context context, String clientid) {
        try {
            Log.i(TAG, "onReceiveClientId:" + TXT + clientid);
            // 将推送 ID 保存到用户表 gtId 中（这里的 gtId 只是一个示例字段）
            SensorsDataAPI.sharedInstance().profilePushId("gtId", clientid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理透传消息。
     * （享物说）
     */
    @Override
    public void onReceiveMessageData(Context context, final GTTransmitMessage gtTransmitMessage) {
        try {
            if (gtTransmitMessage == null) {
                return;
            }
            // 获取推送消息的内容
            byte[] payload = gtTransmitMessage.getPayload();
            String pushData = new String(payload);
            Intent intent = new Intent();
            // TODO 解析处理神策 SF 推送消息的内容
            String custom = getSFContent(pushData);
            if (!TextUtils.isEmpty(custom)) {
                // TODO 把 SF 消息内容，放到 intent 参数中(用于后边从 intent 中获取出来做埋点用)
                intent.putExtra("sf_data", pushData);
                // TODO 拿到 custom 中的 json 字符串， 给原来处理跳转逻辑的代码做处理
            }
            // 之前的解析推送数据的逻辑……
            // 展示通知
            sendNotification(intent);
            Log.i(TAG, "onReceiveMessageData: pushData ----->" + pushData);
            Log.i(TAG, "onReceiveMessageData: custom -----> " + custom);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * cid 离线上线通知
     */
    @Override
    public void onReceiveOnlineState(Context context, boolean b) {
        Log.i(TAG, "onReceiveOnlineState:" + TXT);
    }

    /**
     * 各种事件处理回执
     */
    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage gtCmdMessage) {
        Log.i(TAG, "onReceiveCommandResult:" + TXT);

    }


    @Override
    public void onNotificationMessageClicked(Context context, GTNotificationMessage gtNotificationMessage) {
        Log.i(TAG, "onNotificationMessageClicked:" + TXT + gtNotificationMessage.getContent());
        Log.i(TAG, "onNotificationMessageClicked:" + TXT + gtNotificationMessage.getTitle());
        Toast.makeText(App.get(), "onNotificationMessageClicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotificationMessageArrived(Context context, GTNotificationMessage gtNotificationMessage) {
        Log.i(TAG, "onNotificationMessageArrived:" + TXT);
        Toast.makeText(App.get(), " 个推 消息到达", Toast.LENGTH_SHORT).show();
    }


    /**
     * 处理神策 SF 推送消息的内容
     *
     * @param sfPushContent SF 推送消息的内容
     * @return 返回自定义跳转需要的 String
     * 返回内容示例：{"path":"/pages/home/detail/index?code=101&topicId=746F1T5K1Q0G60393567561606","image_url":"https://imghera.xiangwushuo.com/85c1e939-68ea-3604-b236-db1503188995","callback":"xxxx"}
     */
    public static String getSFContent(String sfPushContent) {
        try {
            if (TextUtils.isEmpty(sfPushContent)) {
                return "";
            }
            return new JSONObject(sfPushContent).optJSONObject("customized").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 埋点 App 打开 SF 推送消息
     * <p>
     * 事件名：AppOpenNotification
     *
     * @param sfPushContent       推送消息中 SF 的内容
     * @param notificationTitle   推送消息的标题
     * @param notificationContent 推送消息的内容
     */
    public static void trackSFAppOpenNotification(String sfPushContent, String notificationTitle, String notificationContent) {
        try {
            if (TextUtils.isEmpty(sfPushContent)) {
                return;
            }
            JSONObject sfJson = new JSONObject(sfPushContent);
            if (!sfJson.has("sf_landing_type")) {
                return;// 推送内容没有神策的相关字段，则不处理
            }
            JSONObject properties = new JSONObject();
            // 推送消息的标题
            properties.put("$sf_msg_title", notificationTitle);
            // 推送消息的内容
            properties.put("$sf_msg_content", notificationContent);
            // 推送消息中 SF 的内容
            properties.put("$sf_msg_id", sfJson.optString("sf_msg_id", null));
            properties.put("$sf_plan_id", sfJson.optString("sf_plan_id", null));
            properties.put("$sf_audience_id", sfJson.optString("sf_audience_id", null));
            properties.put("$sf_link_url", sfJson.optString("sf_link_url", null));
            properties.put("$sf_plan_name", sfJson.optString("sf_plan_name", null));
            properties.put("$sf_plan_strategy_id", sfJson.optString("sf_plan_strategy_id", null));
            JSONObject customized = sfJson.optJSONObject("customized");
            if (customized != null) {
                Iterator<String> iterator = customized.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    properties.put(key, customized.opt(key));
                }
            }
            // 使用神策分析追踪 "App 打开推送消息" 事件
            SensorsDataAPI.sharedInstance().track("AppOpenNotification", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(Intent intent) {
        String channelID = "100";
        //获取NotificationManager实例
        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifyManager == null) return;
        //获取PendingIntent
        intent.setClass(App.get(),GeTuiActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //Intent intent2 = new Intent(this, MainActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //实例化NotificationCompat.Builde并设置相关属性
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,channelID)
                //设置小图标
                .setSmallIcon(R.mipmap.ic_launcher)
                //设置通知标题
                .setContentTitle("个推简单的 Notification")
                //点击通知后自动清除
                .setAutoCancel(true)
                //设置通知内容
                .setContentText("xxx 内容")
                .setContentIntent(mainPendingIntent);

        if(Build.VERSION.SDK_INT >= 26){
            NotificationChannel channel = new NotificationChannel(channelID, "channel_name", android.app.NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("我是一个通知通道");
            notifyManager.createNotificationChannel(channel);
        }

        //通过builder.build()方法生成Notification对象,并发送通知,id=1
        notifyManager.notify(1, builder.build());
    }
}
