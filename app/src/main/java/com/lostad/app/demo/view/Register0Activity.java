package com.lostad.app.demo.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lostad.app.base.util.Validator;
import com.lostad.app.base.view.BaseActivity;
import com.lostad.app.demo.R;
import com.lostad.app.demo.manager.SysManager;
import com.lostad.applib.Config;
import com.lostad.applib.entity.BaseBeanRsult;
import com.lostad.applib.util.DialogUtil;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * 用户注册
 *
 * @Author sszvip@qq.com
 */

public class Register0Activity extends BaseActivity {

    @ViewInject(R.id.et_phone)
    private EditText et_phone;
    @ViewInject(R.id.et_vercode)
    private EditText et_vercode;

    @ViewInject(R.id.btn_get_vercode)
    private TextView btn_get_vercode;

    public int num = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register0);
        setTitle("用户注册");
        x.view().inject(this);
        super.initToolBarWithBack((Toolbar) findViewById(R.id.toolbar));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_next, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            String phone = et_phone.getText().toString();
            if (Validator.isNotMobile(phone)) {
                et_phone.requestFocus();
                et_phone.setError("请输入正确的手机号");
                return true;
            }
            final String vcode = et_vercode.getText().toString().trim();
            if (Validator.isBlank(vcode)) {
                et_vercode.requestFocus();
                et_vercode.setError("请输入验证码");
                return true;
            }
            //未testmode赞杰
            //if (Config.isTestMode) {
//            if (true) {
//                toNextActivity(phone);
//            }
            checkVerCode(phone, vcode);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void toNextActivity(String phone) {
        Intent i = new Intent(ctx, Register1Activity.class);
        i.putExtra("phone", phone);
        startActivity(i);
    }

    private void checkVerCode(final String phone, final String vercode) {
        DialogUtil.showProgress(this);
        BaseBeanRsult bb = null;
        bb = SysManager.getInstance().validateCode(phone, vercode, Register0Activity.this);
    }

    @Event(R.id.btn_get_vercode)
    private void onClickGetVercode(View v) {
        // 验证码
        final String mPhone = et_phone.getText().toString();
        if (Validator.isBlank(mPhone)) {
            Toast.makeText(this, "请输入您的手机号!", Toast.LENGTH_LONG).show();
            return;
        }

        if (Validator.isNotMobile(mPhone)) {
            Toast.makeText(this, "请输入您正确的手机号!", Toast.LENGTH_LONG).show();
            return;
        }
        DialogUtil.showProgress(this);
        SysManager.getInstance().getVerifyCode(mPhone, Register0Activity.this);
    }

    @Override
    public void startToRecordTime() {
        new Thread() {
            @Override
            public void run() {
                num = 60;
                while (num > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btn_get_vercode.setText("" + num);
                            btn_get_vercode.setEnabled(false);
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    num--;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn_get_vercode.setText("重发");
                        btn_get_vercode.setEnabled(true);
                    }
                });

            }

        }.start();
    }
}