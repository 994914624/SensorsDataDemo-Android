package cn.sensorsdata.demo;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Intent intent = getIntent();
        if(intent != null){
            Log.i("onIntent","onCreate:"+intent.toString());
            if(intent.getExtras()!=null)
                Log.i("onIntent","onCreate:"+intent.getExtras().toString());
        }
        startActivity(new Intent(WelcomeActivity.this,MainActivity.class));
        finish();
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if(intent != null){
            Log.i("onIntent","onStart:"+intent.toString());
            if(intent.getExtras()!=null)
                Log.i("onIntent","onStart:"+intent.getExtras().toString());
        }
    }
}
