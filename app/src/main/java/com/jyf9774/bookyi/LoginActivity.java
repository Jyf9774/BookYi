package com.jyf9774.bookyi;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.snackbar.Snackbar;

import static com.jyf9774.bookyi.simpleToast.showToast;

public class LoginActivity extends AppCompatActivity {

    EditText username, passwd;
    Button login, register;
    View login_processbar;
    String result, input_username, input_passwd;
    boolean rememberAll = false;
    Switch remember;

    //主线程的handler
    private Handler mainhandler;
    Thread mThread;

    SQL_Client client;
    private long mExitTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = (EditText) findViewById(R.id.login_username);
        username.setText((String) getIntent().getSerializableExtra("username"));
        passwd = (EditText) findViewById(R.id.login_passwd);
        login = (Button) findViewById(R.id.button_login);
        register = (Button) findViewById(R.id.button_register);
        login_processbar = findViewById(R.id.login_progressBar);
        remember = findViewById(R.id.remember_switch);
        remember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rememberAll = isChecked;
            }
        });
        login_processbar.setVisibility(View.INVISIBLE);

        SharedPreferences sp2 = getSharedPreferences("Login_db", MODE_PRIVATE);
        if (sp2.getBoolean("save", false) == true) {    //判断是否写入了数值save==true
            //showToast("检测到保存的密码",getApplicationContext());
            remember.setChecked(true);
            getDB();
        }

        View.OnClickListener loginListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        };
        View.OnClickListener registerListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        };
        login.setOnClickListener(loginListener);
        register.setOnClickListener(registerListener);

        client = new SQL_Client();

        mainhandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 1:
                        if (msg.obj.toString().equals("登录成功")) {
                            login_processbar.setVisibility(View.INVISIBLE);
                            if (rememberAll) {
                                saveDB();
                            } else {
                                clearDB();
                            }
                            saveUserName();
                            showToast(msg.obj.toString(), getApplicationContext());
                            jump_Main();
                        } else if (msg.obj.toString().equals("JDBC错误代码0")) {
                            login_processbar.setVisibility(View.INVISIBLE);
                            showToast("请检查网络连接", getApplicationContext());
                        } else {
                            login_processbar.setVisibility(View.INVISIBLE);
                            showToast(msg.obj.toString(), getApplicationContext());
                        }

                }
            }
        };


    }


    protected void login() {

        input_username = username.getText().toString();
        input_passwd = passwd.getText().toString();
        if (input_username.equals("")) {
            Snackbar.make(findViewById(R.id.button_login), "请输入用户名", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (input_username.equals("null") || input_username.contains("#")) {
            Snackbar.make(findViewById(R.id.button_login), "用户名包含非法字符", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (input_passwd.equals("")) {
            Snackbar.make(findViewById(R.id.button_login), "请输入密码", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (input_passwd.contains("#")) {
            Snackbar.make(findViewById(R.id.button_login), "密码包含非法字符", Snackbar.LENGTH_SHORT).show();
            return;
        }

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                result = client.login(input_username, AesUtility.encrypt(getString(R.string.aes_key), input_passwd));
                Message msg = new Message();
                msg.obj = result;
                msg.what = 1;
                mainhandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
        login_processbar.setVisibility(View.VISIBLE);
        return;
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Object mHelperUtils;
            showToast("再按一次退出", getApplicationContext());
            //System.currentTimeMillis()系统当前时间
            mExitTime = System.currentTimeMillis();
            return;
        } else {
            LoginActivity.this.finish();
        }

    }

    public void jump_Main() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("username", username.getText().toString());
        startActivity(intent);
    }

    protected void register() {
        Intent intent = new Intent(this, RegistActivity.class);
        startActivity(intent);
    }

    //清除
    private void clearDB() {
        SharedPreferences sp = getSharedPreferences("Login_db", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }

    private void saveUserName(){
        SharedPreferences sp = getSharedPreferences("username_DB", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("username", input_username);
        editor.commit();            //写入数据
    }

    //保存数据
    private void saveDB() {
        SharedPreferences sp = getSharedPreferences("Login_db", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("username", input_username);
        editor.putString("password", AesUtility.encrypt(getString(R.string.aes_key), input_passwd));
        editor.putBoolean("save", true);
        editor.commit();            //写入数据
    }

    //读取数据
    private void getDB() {
        SharedPreferences sp = getSharedPreferences("Login_db", MODE_PRIVATE);
        String localname = sp.getString("username", "");
        String localpassword = AesUtility.decrypt(getString(R.string.aes_key), sp.getString("password", ""));
        username.setText(localname);
        passwd.setText(localpassword);
    }
}
