package cn.sensorsdata.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.umeng.message.UmengNotifyClickActivity;

import org.android.agoo.common.AgooConstants;

/**
 * Created by yzk on 2019/8/9
 * 友盟 小米通道
 */

public class UmengMUIActivity  extends UmengNotifyClickActivity {

        private static String TAG = UmengMUIActivity.class.getName();

        @Override
        protected void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            setContentView(R.layout.activity_base);
        }

        @Override
        public void onMessage(Intent intent) {
            super.onMessage(intent);  //此方法必须调用，否则无法统计打开数
            String body = intent.getStringExtra(AgooConstants.MESSAGE_BODY);
            Log.i(TAG, body);
        }
}
