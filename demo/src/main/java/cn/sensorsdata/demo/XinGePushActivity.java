package cn.sensorsdata.demo;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;

import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

public class XinGePushActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xin_ge_push);
        setActionBar();
        initView();

    }

    /**
     * 初始化View
     */
    private void initView() {
        TextView textView = (TextView) findViewById(R.id.textView_XinGePushActivity);
        //小米推送的RegistrationID
        if(TextUtils.isEmpty(XGPushConfig.getToken(this))){
            textView.setText("信鸽推送初始化失败");

        }else {
            textView.setText(String.format("%s", XGPushConfig.getToken(this)));
            Log.i("信鸽:",XGPushConfig.getToken(this) + "");
        }
    }

    /**
     * 设置ActionBar
     */
    @TargetApi(18)
    private void setActionBar(){
        ActionBar actionBar = getActionBar();
        if(actionBar == null)return;
        actionBar.setTitle("信鸽推送");
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
