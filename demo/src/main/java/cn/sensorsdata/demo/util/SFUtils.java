package cn.sensorsdata.demo.util;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.sensorsdata.demo.JiguangPushActivity;
import cn.sensorsdata.demo.UmengActivity;

public class SFUtils {
    // 打开 App
    private static final String OPEN_APP = "OPEN_APP";
    // 打开 Link
    private static final String OPEN_LINK = "LINK";
    // 自定义
    private static final String CUSTOMIZED = "CUSTOMIZED";

    /**
     * 解析处理 SF 配置的操作，此处仅仅是模拟一些关键字段的解析的展示，具体的业务实现还要开发者根据
     * 自己的业务需求去实现。
     *
     * @param context Context
     * @param sfData  配置
     */
    public static void handleSFConfig(Context context, String sfData) {
        try {
            if (!TextUtils.isEmpty(sfData)) {
                JSONObject jsonObject = new JSONObject(sfData);
                // 解析推送过来的类型，有三种 OPEN_APP、LINK、CUSTOMIZED
                String type = jsonObject.optString("sf_landing_type");
                switch (type) {
                    case OPEN_APP:
                        break;
                    case OPEN_LINK:
                        break;
                    case CUSTOMIZED:
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 把设备推送 ID 上报到神策用户表
     *
     * @param profileKey 上报到神策用户表的字段
     * @param pushId     推送 SDK 分配给设备的标识
     */
    public static void profilePushId(String profileKey, String pushId) {
        try {
            SensorsDataAPI.sharedInstance().profilePushId(profileKey, pushId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 埋点 App 打开推送消息
     * <p>
     * 事件名：AppOpenNotification
     *
     * @param notificationExtras         推送消息中 SF 的内容
     * @param notificationTitle   推送消息的标题
     * @param notificationContent 推送消息的内容
     */
    public static void trackAppOpenNotification(Map<String, String> notificationExtras, String notificationTitle, String notificationContent) {
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
            // 使用神策分析追踪 "App 打开推送消息" 事件
            SensorsDataAPI.sharedInstance().track("AppOpenNotification", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * 埋点 App 打开推送消息
     * <p>
     * 事件名：AppOpenNotification
     *
     * @param intent              intent 带有 SF 消息时，触发 App 打开推送消息事件
     * @param notificationTitle   推送消息的标题
     * @param notificationContent 推送消息的内容
     */
    public static void trackAppOpenNotification(Intent intent, String notificationTitle, String notificationContent) {
        try {
            if (intent == null) {
                return;
            }
            if (!intent.hasExtra("sf_data")) {
                return;
            }
            String sfData = intent.getStringExtra("sf_data");
            if (TextUtils.isEmpty(sfData)) {
                return;
            }
            JSONObject sfJson = new JSONObject(sfData);
            JSONObject properties = new JSONObject();
            // 推送消息的标题
//            properties.put("$sf_msg_title", notificationTitle);//intent 中如何拿到标题
//            // 推送消息的内容
//            properties.put("$sf_msg_content", notificationContent);
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

    /**
     * 神策 SF 处理推送消息，做页面跳转 示例
     *
     * @param pushContent 推送消息的 msg.extra
     * @param context     context
     * @return true 表示神策 SF 代码处理了推送消息
     */
    public static boolean dealSFAction(Map<String, String> pushContent, Context context) {
        if (pushContent == null || context == null) {
            return false;
        }
        try {
            String sfData = new JSONObject(pushContent).optString("sf_data");
            if (TextUtils.isEmpty(sfData)) {
                return false;
            }
            JSONObject sfJson = new JSONObject(sfData);
            String custom = sfJson.optJSONObject("customized").optString("custom");
            if (TextUtils.isEmpty(custom)) {
                return false;
            }
            // TODO 解析出需要中的字段，做页面跳转
            Intent intent = new Intent(context, UmengActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            Log.i("友盟", "--- SF ---> " + custom);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
            Log.i("极光", "--- SF ---> " + sfJson.toString());
            // TODO 解析 customized 中的相应的字段，做页面跳转
            String messageType = sfJson.optJSONObject("customized").optString("messageType");
            String liveId = sfJson.optJSONObject("customized").optString("liveId");

            Intent intent = new Intent(context, JiguangPushActivity.class);
//            // TODO 把 sf_data 通过 Intent 传到目标 Activity
//            intent.putExtra("sf_data", sfData);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
            return new JSONObject(sfPushContent).optJSONObject("customized").optString("custom");
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

    public static boolean isNotificationOpen(Context context) {
        if(context == null)return false;
        try {
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}

