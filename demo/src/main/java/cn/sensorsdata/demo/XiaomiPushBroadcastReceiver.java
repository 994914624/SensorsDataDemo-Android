package cn.sensorsdata.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by yang on 2017/1/19.
 * 用于接收小米推送的 BroadcastReceiver
 */

public class XiaomiPushBroadcastReceiver extends PushMessageReceiver {


    private static XiaomiPushActivity.DemoHandler handler = null;

    public static void setHandle(XiaomiPushActivity.DemoHandler handle) {
        handler = handle;
    }

   private static final String TAG = "__小米推送__";
    private static final String TXT = "----------------------- 小米推送 ---------------------------\n";
    /**
     * 用来接收服务器发来的通知栏消息
     * （消息到达客户端时触发，并且可以接收应用在前台时不弹出通知的通知消息）
     */
    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {

        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("msg_title_xm", message.getTitle());
        bundle.putString("msg_id_xm", message.getMessageId());
        msg.obj = bundle;
        msg.what = 1;//用 1 标识用户收到消息
        handler.sendMessage(msg);

        Log.i(TAG,"onNotificationMessageArrived:"+TXT);
        try {

            JSONObject properties = new JSONObject();
            // 获取消息标题，并保存在事件属性 msg_title 中
            properties.put("msg_title_xm", message.getTitle());
            // 获取消息 ID，并保存在事件属性 msg_id 中
            properties.put("msg_id_xm", message.getMessageId());
            // 追踪 "App 消息推送成功" 事件
            SensorsDataAPI.sharedInstance(context).track("AppReceivedNotification_xm", properties);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用来接收服务器发来的通知栏消息（用户点击通知栏时触发）
     */
    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {
//        Message msg = Message.obtain();
//        Bundle bundle = new Bundle();
//        bundle.putString("msg_title", message.getTitle());
//        bundle.putString("msg_id", message.getMessageId());
//        msg.obj = bundle;
//        msg.what = 2;//用 2 标识用户打开消息
//        handler.sendMessage(msg);

        // 埋点 "App 打开推送消息" 事件
        trackAppOpenNotification(message.getExtra(),message.getTitle(),message.getDescription());

        Log.i(TAG,"onNotificationMessageClicked: getTitle:"+ message.getTitle()+"、getDescription:"+message.getDescription()+" 、 getTopic:" + message.getTopic()+"、getCategory:" + message.getCategory()+
                "、getContent:"+ message.getContent()+"、getExtra:"  +message.getExtra().toString());

    }

    /**
     * 用来接收服务器发送的透传消息
     */
    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {

        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("msg_content", message.getContent());
        msg.obj = bundle;
        msg.what = 3;//用 3 标识用户收到自定义(透传)消息
        handler.sendMessage(msg);
        Log.i(TAG,"onReceivePassThroughMessage:"+message.getContent()+"   - "+message.getExtra().toString());

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
    public static void trackAppOpenNotification(Map notificationExtras, String notificationTitle, String notificationContent) {
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
     * 用来接受客户端向服务器发送注册命令消息后返回的响应。
     */
    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        Log.i(TAG,"onReceiveRegisterResult:"+ TXT + MiPushClient.getRegId(context));

        //
        // 将推送 ID 保存到用户表 xmId 中（这里的 xmId 只是一个示例字段）
        SensorsDataAPI.sharedInstance().profilePushId("xmId",MiPushClient.getRegId(context));



        String command = message.getCommand();
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {


            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            msg.obj = bundle;
            msg.what = 4;//用 4 标识注册成功
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                //小米推送初注册成功

            }
            handler.sendMessage(msg);
        }
    }


    /**
     * 用来接收客户端向服务器发送命令消息后返回的响应
     */
    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        Log.i(TAG,"onCommandResult:"+TXT);
    }


}
