package com.jyf9774.bookyi;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import static com.jyf9774.bookyi.simpleToast.showToast;

public class RegistActivity extends AppCompatActivity {

    String username, confirmed_username, passwd, phone, qq, wechat;
    String result;
    EditText ed_username, ed_passwd, ed_passwdConfirm, ed_phone, ed_qq, ed_wechat;
    Button check, register;
    SQL_Client client;
    //主线程的handler
    private Handler mainhandler;
    //子线程
    private Thread myThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        ed_username = findViewById(R.id.reg_username);
        ed_passwd = findViewById(R.id.reg_password);
        ed_passwdConfirm = findViewById(R.id.reg_password_confirm);
        ed_phone = findViewById(R.id.reg_phone_number);
        ed_qq = findViewById(R.id.reg_qq_number);
        ed_wechat = findViewById(R.id.reg_wechat_number);

        check = findViewById(R.id.reg_button_check);
        register = findViewById(R.id.reg_button);


        View.OnClickListener checkListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameCheck();
            }
        };
        View.OnClickListener registerListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regist();
            }
        };

        check.setOnClickListener(checkListener);
        register.setOnClickListener(registerListener);

        mainhandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 1:
                        if (msg.obj.toString().equals("注册成功")) {
                            showToast("注册成功，返回登录页面", getApplicationContext());
                            JUMP_Login();
                            break;
                        } else if (msg.obj.toString().equals("用户名可用")) {
                            confirmed_username = ed_username.getText().toString();
                        }
                        showToast(msg.obj.toString(), getApplicationContext());
                }
            }
        };

    }

    private void JUMP_Login() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("username", confirmed_username);
        startActivity(intent);
        this.onDestroy();
        return;
    }

    protected void nameCheck() {
        if (ed_username.getText().toString().equals("")) {
            Snackbar.make(findViewById(R.id.reg_title), "请输入用户名", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (ed_username.getText().toString().equals("null") || ed_username.getText().toString().contains("#")) {
            Snackbar.make(findViewById(R.id.reg_title), "用户名包含非法字符", Snackbar.LENGTH_SHORT).show();
            return;
        }
        myThread = new Thread(new Runnable() {

            @Override
            public void run() {
                client = new SQL_Client();
                result = client.check(ed_username.getText().toString());
                Message msg = new Message();
                msg.obj = result;
                msg.what = 1;
                mainhandler.sendMessage(msg);
                return;
            }

        });

        myThread.start();
        return;

    }

    protected void regist() {
        if (ed_username.getText().toString().equals("")) {
            Snackbar.make(findViewById(R.id.reg_title), "请输入用户名", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (ed_username.getText().toString().equals("null") || ed_username.getText().toString().contains("#")) {
            Snackbar.make(findViewById(R.id.reg_title), "用户名包含非法字符", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!ed_username.getText().toString().equals(confirmed_username)) {
            Snackbar.make(findViewById(R.id.reg_title), "用户名未经确认", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (ed_passwd.getText().toString().equals("")) {
            Snackbar.make(findViewById(R.id.reg_title), "请输入密码", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (ed_passwd.getText().toString().contains("#")) {
            Snackbar.make(findViewById(R.id.reg_title), "密码包含非法字符", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!ed_passwdConfirm.getText().toString().equals(ed_passwd.getText().toString())) {
            Snackbar.make(findViewById(R.id.reg_title), "密码不一致，请重新输入", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (ed_phone.getText().toString().equals("")) {
            Snackbar.make(findViewById(R.id.reg_title), "请输入电话号码", Snackbar.LENGTH_SHORT).show();
            return;
        }
        username = ed_username.getText().toString();
        passwd = ed_passwd.getText().toString();
        phone = ed_phone.getText().toString();
        qq = ed_qq.getText().toString();
        wechat = ed_wechat.getText().toString();

        myThread = new Thread(new Runnable() {

            @Override
            public void run() {
                client = new SQL_Client();
                result = client.regist(username, AesUtility.encrypt(getString(R.string.aes_key),passwd), phone, qq, wechat);
                Message msg = new Message();
                msg.obj = result;
                msg.what = 1;
                mainhandler.sendMessage(msg);
                return;
            }

        });
        myThread.start();
        return;
    }
}
