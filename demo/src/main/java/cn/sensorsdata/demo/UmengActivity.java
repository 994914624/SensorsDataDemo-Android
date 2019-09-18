package cn.sensorsdata.demo;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.umeng.message.PushAgent;

import org.json.JSONException;
import org.json.JSONObject;



public class UmengActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_umeng2);

        if(Build.VERSION.SDK_INT>14)setActionBar();
        initView();

        try {
            JSONObject properties = new JSONObject();
            properties.put("umengId", PushAgent.getInstance(this).getRegistrationId()+"");
            // 设置用户 Profile
            SensorsDataAPI.sharedInstance().profileSet(properties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 初始化View
     */
    private void initView() {
        TextView textView= (TextView) findViewById(R.id.tv_umeng);
        //极光推送的RegistrationID
        if(TextUtils.isEmpty(PushAgent.getInstance(this).getRegistrationId())){
            textView.setText("友盟推送初始化失败");

        }else {
            textView.setText(String.format("%s", PushAgent.getInstance(this).getRegistrationId()));
            Log.i("umeng_jg:",PushAgent.getInstance(this).getRegistrationId()+"");

        }
    }

    /**
     * 设置ActionBar
     */
    @TargetApi(18)
    private void setActionBar(){
        ActionBar actionBar=getActionBar();
        actionBar.setTitle("友盟推送");
        // 设置不显示左侧图标
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.mipmap.left_back);
        int titleId = Resources.getSystem().getIdentifier("action_bar_title",
                "id", "android");
        TextView tvTitle = (TextView) findViewById(titleId);
        int width=getResources().getDisplayMetrics().widthPixels;
        tvTitle.setWidth(width);
        tvTitle.setGravity(Gravity.CENTER);
    }

    /**
     * back键
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
