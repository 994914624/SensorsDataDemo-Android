package cn.sensorsdata.demo;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.exceptions.InvalidDataException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;
import cn.sensorsdata.demo.util.SFUtils;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by yang on 2016/12/28.
 * 用于接收极光推送消息的 BroadcastReceiver
 */

public class JiguangPushBroadcastReceiver extends JPushMessageReceiver {


    // 翡翠服务端端 uri_activity
    public static final String ANDROID_LIVE_INTENT = " ";
    public static final String ANDROID_LIVE_URL_ACTIVITY = "com.xindanci.zhubao.activity.live.LiveShowActivity";

    @Override
    public void onRegister(Context context, String s) {
        super.onRegister(context, s);
        Log.e(TAG, " 极光 onRegister : " + s);
        // 将推送 ID 保存到用户表 jgId 中（这里的 jgId 只是一个示例字段）
        SensorsDataAPI.sharedInstance().profilePushId("jgId", s);
    }

    /*
     * 推送消息被点击
     */
    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage message) {
        super.onNotifyMessageOpened(context, message);
        if (message == null) {
            return;
        }
        Log.i(TAG, " 极光 onNotifyMessageOpened : " + TXT + message.notificationExtras);
        Toast.makeText(context, "极光 onNotifyMessageOpened", Toast.LENGTH_SHORT).show();

        // 埋点 App 打开推送消息
        trackAppOpenNotification(message.notificationExtras, message.notificationTitle, message.notificationContent);


        // SF 消息，处理逻辑
        if (dealSFAction(context, message.notificationExtras)) {
            return;
        }
        // 之前已存在的极光消息，处理逻辑（翡翠没有这个逻辑……）
        dealJGAction(message.notificationExtras, context);

    }

    private void dealJGAction(String pushContent, Context context) {

    }

    /**
     * 埋点 App 打开推送消息
     * <p>
     * 事件名：AppOpenNotification
     *
     * @param notificationExtras  推送消息 notificationExtras
     * @param notificationTitle   推送消息的标题
     * @param notificationContent 推送消息的内容
     */
    public static void trackAppOpenNotification(String notificationExtras, String notificationTitle, String notificationContent) {
        try {
            JSONObject properties = new JSONObject();
            // 推送消息的标题
            properties.put("$sf_msg_title", notificationTitle);
            // 推送消息的内容
            properties.put("$sf_msg_content", notificationContent);
            try {
                String sfData = new JSONObject(notificationExtras).optString("sf_data");
                if (!TextUtils.isEmpty(sfData)) {
                    JSONObject sfJson = new JSONObject(sfData);
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
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 追踪 "App 打开推送消息" 事件
            SensorsDataAPI.sharedInstance().track("AppOpenNotification", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 神策 SF 处理推送消息，做页面跳转 示例
     *
     * @param context       context
     * @param sfPushContent 推送消息 SF 的内容
     * @return true 表示神策 SF 代码处理了推送消息
     */
    public static boolean dealSFAction(Context context, String sfPushContent) {
        if (TextUtils.isEmpty(sfPushContent) || context == null) {
            return false;
        }
        try {
            String sfData = new JSONObject(sfPushContent).optString("sf_data");
            if (TextUtils.isEmpty(sfData)) {
                return false;
            }
            JSONObject sfJson = new JSONObject(sfData);
            // TODO 解析 customized 中的相应的字段，做页面跳转
            String messageType = sfJson.optJSONObject("customized").optString("messageType");
            String liveId = sfJson.optJSONObject("customized").optString("liveId");
            String url = sfJson.optJSONObject("customized").optString("url");
            Log.i("极光", String.format("--- SF 消息 ---> messageType：%s ，liveId：%s，url：%s", messageType, liveId, url));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageArrived(context, notificationMessage);
        Toast.makeText(context, "极光 onNotifyMessageArrived", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "onNotifyMessageArrived : " + TXT + notificationMessage.notificationExtras);
    }

    @Override
    public void onMessage(Context context, CustomMessage customMessage) {//透传消息
        super.onMessage(context, customMessage);
        Toast.makeText(context, "极光 onMessage", Toast.LENGTH_SHORT).show();
    }


    private static final String TAG = "__ ji juang __";
    private static final String TXT = "----------------------- 极光推送 ---------------------------\n";

//    @Override
//    public void onReceive(Context context, Intent intent) {
//
//        Log.i(TAG,"onReceive : "+TXT);
//        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
//            Log.i(TAG," 极光 推送ID 注册成功: "+TXT);
//            // 极光推送注册成功，获得 RegistrationId 并保存在 Sensors Analytics用户的 Profile 中
//            appRegistrationID(context, intent);
//        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
//            // 收到了自定义消息，自定义消息不会展示在通知栏，只透传给 App ，使用 Sensors Analytics 追踪 "App 收到了自定义消息" 事件
//            appReceivedMessage(context);
//        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
//            // 收到了推送消息，使用 Sensors Analytics 追踪 "App 消息推送成功" 事件
//            appReceivedNotification(context, intent);
//        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
//            // 用户点击打开消息，使用 Sensors Analytics 记录 "App 打开消息" 事件
//            appOpenNotification(context,intent);
//        }
////        else if(JPushInterface.ACTION_STATUS){
////
////        }
//    }


    /**
     * 极光推送注册成功，
     * 获得 RegistrationId，
     * 向Sensors Analytics发送profile事件
     */
    private void appRegistrationID(Context context, Intent intent) {

        Log.e("yyyyyy", "ji guang  push  appRegistrationID");
        try {
            // 获取极光推送的 RegistrationId
            final String registrationId = intent.getExtras().getString(JPushInterface.EXTRA_REGISTRATION_ID);
            // 将 Registration Id 存储在 Sensors Analytics 的用户 Profile 中
            JSONObject properties = new JSONObject();
            // 将用户 Profile "jgAndroidId" 设为 registrationId
            properties.put("jgAndroidId", registrationId);
            // 设置用户 Profile
            SensorsDataAPI.sharedInstance(context).profileSet(properties);
            Toast.makeText(context, "极光推送注册成功\n" +
                    "已尝试向Sensors Analytics发送profile事件\n" +
                    "事件属性:{ jgAndroidId : " + registrationId + " }", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户收到了推送消息，
     * 向Sensors Analytics发送track事件，
     * 事件名为：AppReceivedNotification
     */
    private void appReceivedNotification(Context context, Intent intent) {
        try {
            Toast.makeText(context, " 极光 消息到达", Toast.LENGTH_SHORT).show();
            Bundle message = intent.getExtras();

            Log.d(TAG, "-------message----title----:" + message.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE));
            Log.d(TAG, "-------message------content--:" + message.getString(JPushInterface.EXTRA_ALERT));
            JSONObject properties = new JSONObject();
            // 获取消息标题，并保存在事件属性 msg_title 中
            properties.put("msg_title_jg", message.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE));
            //消息内容
            properties.put("msg_content_jg", message.getString(JPushInterface.EXTRA_ALERT));
            // 获取消息 ID，并保存在事件属性 msg_id 中
            properties.put("msg_id_jg", message.getString(JPushInterface.EXTRA_MSG_ID));
            // 追踪 "App 消息推送成功" 事件
            SensorsDataAPI.sharedInstance(context).track("AppReceivedNotification_jg", properties);
            Toast.makeText(context, "用户收到了推送消息\n" +
                    "已尝试向Sensors Analytics发送track事件\n" +
                    "事件名为:AppReceivedNotification_jg" +
                    "事件属性:{ msg_title_jg : " + message.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE) +
                    ",msg_id_jg : " + message.getString(JPushInterface.EXTRA_MSG_ID) +
                    " }", Toast.LENGTH_LONG).show();

            Log.e("###", message.getString(JPushInterface.EXTRA_ALERT) + "||" + message.getString(JPushInterface.EXTRA_MSG_ID) + "");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * 用户点击打开了消息，
     * 向Sensors Analytics发送track事件，
     * 事件名为：AppOpenNotification
     */
    private void appOpenNotification(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        try {

            JSONObject properties = new JSONObject();
            // 获取消息标题，并保存在事件属性 msg_title 中
            properties.put("msg_title_jg", bundle.getString(JPushInterface.EXTRA_ALERT));
            // 获取消息 ID，并保存在事件属性 msg_id 中
            properties.put("msg_id_jg", bundle.getString(JPushInterface.EXTRA_MSG_ID));
            // 追踪 "App 推送消息打开" 事件
            SensorsDataAPI.sharedInstance(context).track("AppOpenNotification_jg", properties);
            Toast.makeText(context, "用户点击打开了消息\n" +
                    "已尝试向Sensors Analytics发送track事件\n" +
                    "事件名为:AppOpenNotification_jg\n" +
                    "事件属性:{ msg_title_jg : " + bundle.getString(JPushInterface.EXTRA_ALERT) +
                    ",msg_id_jg : " + bundle.getString(JPushInterface.EXTRA_MSG_ID) +
                    " }", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }


//        Intent intent2=new Intent(context,JiguangPushActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent2);
    }

    /**
     * 用户收到了自定义消息，
     * 向Sensors Analytics发送track事件，
     * 事件名为：AppReceivedMessage
     */
    private void appReceivedMessage(Context context) {
        try {
            // 追踪 "App 收到自定义(透传)消息" 事件
            SensorsDataAPI.sharedInstance(context).track("AppReceivedMessage_jg");
            Toast.makeText(context, "用户收到了自定义消息\n" +
                            "已尝试向Sensors Analytics发送track事件\n" +
                            "事件名为: AppReceivedMessage_jg"
                    , Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}



