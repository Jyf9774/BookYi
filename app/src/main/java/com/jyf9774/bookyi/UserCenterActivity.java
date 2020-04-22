package com.jyf9774.bookyi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import static com.jyf9774.bookyi.simpleToast.showToast;

public class UserCenterActivity extends AppCompatActivity {
    private String username;
    private User user;

    private SQL_Client client;
    Handler userHandler;
    Thread mThread;

    EditText ed_phoneNumber,ed_qqNumber,ed_weChatNumber;
    EditText ed_oldPassword,ed_newPassword,ed_repeatPassword;
    View processBar;
    Button editButton,saveButton,changeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_center);
        username = (String) getIntent().getSerializableExtra("username");

        SharedPreferences sp = getSharedPreferences("username_DB", MODE_PRIVATE);
        String localusername = sp.getString("username", "");
        if (username == ""||username == null) {
            username = localusername;
        }
        client = new SQL_Client();
        processBar = findViewById(R.id.user_progressBar);
        TextView txv = findViewById(R.id.user_title);
        txv.setText(username+"的个人中心");

        ed_phoneNumber = findViewById(R.id.user_phonenumber);
        ed_phoneNumber.setEnabled(false);
        ed_qqNumber = findViewById(R.id.user_qqnumber);
        ed_qqNumber.setEnabled(false);
        ed_weChatNumber = findViewById(R.id.user_wechatnumber);
        ed_weChatNumber.setEnabled(false);

        ed_oldPassword = findViewById(R.id.user_old_password);
        ed_newPassword = findViewById(R.id.user_new_password);
        ed_repeatPassword = findViewById(R.id.user_repeat_password);

        editButton = findViewById(R.id.user_edit_button);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ed_phoneNumber.setEnabled(true);
                ed_qqNumber.setEnabled(true);
                ed_weChatNumber.setEnabled(true);
            }
        });
        saveButton = findViewById(R.id.user_save_button);
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(changeButton.getText().toString().equals("更改密码")){

                    ed_phoneNumber.setEnabled(false);
                    ed_qqNumber.setEnabled(false);
                    ed_weChatNumber.setEnabled(false);
                    updateUser();
                }else{
                    updatePassword();
                }

            }
        });

        changeButton = findViewById(R.id.user_change_passwd);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(changeButton.getText().toString().equals("更改密码")){
                    ed_phoneNumber.setVisibility(View.INVISIBLE);
                    ed_qqNumber.setVisibility(View.INVISIBLE);
                    ed_weChatNumber.setVisibility(View.INVISIBLE);

                    findViewById(R.id.user_show1).setVisibility(View.INVISIBLE);
                    findViewById(R.id.user_show2).setVisibility(View.INVISIBLE);
                    findViewById(R.id.user_show3).setVisibility(View.INVISIBLE);

                    findViewById(R.id.user_show4).setVisibility(View.VISIBLE);
                    findViewById(R.id.user_show5).setVisibility(View.VISIBLE);
                    findViewById(R.id.user_show6).setVisibility(View.VISIBLE);

                    ed_oldPassword.setVisibility(View.VISIBLE);
                    ed_newPassword.setVisibility(View.VISIBLE);
                    ed_repeatPassword.setVisibility(View.VISIBLE);
                    changeButton.setText("联系方式");
                    editButton.setEnabled(false);
                }else if(changeButton.getText().toString().equals("联系方式")){
                    findViewById(R.id.user_show4).setVisibility(View.INVISIBLE);
                    findViewById(R.id.user_show5).setVisibility(View.INVISIBLE);
                    findViewById(R.id.user_show6).setVisibility(View.INVISIBLE);

                    ed_oldPassword.setVisibility(View.INVISIBLE);
                    ed_oldPassword.setText("");
                    ed_newPassword.setVisibility(View.INVISIBLE);
                    ed_newPassword.setText("");
                    ed_repeatPassword.setVisibility(View.INVISIBLE);
                    ed_repeatPassword.setText("");

                    ed_phoneNumber.setVisibility(View.VISIBLE);
                    ed_qqNumber.setVisibility(View.VISIBLE);
                    ed_weChatNumber.setVisibility(View.VISIBLE);

                    findViewById(R.id.user_show1).setVisibility(View.VISIBLE);
                    findViewById(R.id.user_show2).setVisibility(View.VISIBLE);
                    findViewById(R.id.user_show3).setVisibility(View.VISIBLE);
                    changeButton.setText("更改密码");
                    editButton.setEnabled(true);
                }

            }
        });

        userHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 1:
                        updateUI();
                        processBar.setVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        showToast(msg.obj.toString(),getApplicationContext());
                        processBar.setVisibility(View.INVISIBLE);
                        if(msg.obj.toString().equals("密码更新成功")){
                            SharedPreferences sp = getSharedPreferences("Login_db",MODE_PRIVATE);
                            SharedPreferences.Editor editor=sp.edit();
                            editor.putString("password",AesUtility.encrypt(getString(R.string.aes_key),ed_newPassword.getText().toString()));
                            editor.putBoolean("save",true);
                            editor.commit();            //写入数据
                            showToast("新密码已保存",getApplicationContext());
                        }

                }
            }
        };

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                  User temp = client.getUserByName(username);
                  user = temp;
                Message msg = new Message();
                msg.what = 1;
                userHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
    }

    private void updateUI(){
        ed_phoneNumber.setText(user.phoneNumber);
        ed_qqNumber.setText(user.QQNumber);
        ed_weChatNumber.setText(user.weChatNumber);
    }

    private void updateUser(){
        if (ed_phoneNumber.getText().toString().equals("")) {
            Snackbar.make(findViewById(R.id.user_title), "请输入电话号码", Snackbar.LENGTH_SHORT).show();
            return;
        }
        final String phone = ed_phoneNumber.getText().toString();
        final String qq = ed_qqNumber.getText().toString();
        final String wechat = ed_weChatNumber.getText().toString();

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = client.updateUser(username,phone,qq,wechat);
                Message msg = new Message();
                msg.what = 2;
                msg.obj = result;
                userHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
        processBar.setVisibility(View.VISIBLE);
    }

    private void updatePassword(){
        if (ed_oldPassword.getText().toString().equals("")) {
            Snackbar.make(findViewById(R.id.user_title), "请输入旧密码", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (ed_oldPassword.getText().toString().contains("#")) {
            Snackbar.make(findViewById(R.id.user_title), "密码包含非法字符", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (ed_newPassword.getText().toString().equals("")) {
            Snackbar.make(findViewById(R.id.user_title), "请输入新密码", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (ed_newPassword.getText().toString().contains("#")) {
            Snackbar.make(findViewById(R.id.user_title), "密码包含非法字符", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!ed_repeatPassword.getText().toString().equals(ed_newPassword.getText().toString())) {
            Snackbar.make(findViewById(R.id.user_title), "密码不一致，请重新输入", Snackbar.LENGTH_SHORT).show();
            return;
        }

        final String old_password = ed_oldPassword.getText().toString();
        final String new_password = ed_newPassword.getText().toString();

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = client.updateUserPassword(username,AesUtility.encrypt(getString(R.string.aes_key),old_password),AesUtility.encrypt(getString(R.string.aes_key),new_password));
                Message msg = new Message();
                msg.what = 2;
                msg.obj = result;
                userHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
        processBar.setVisibility(View.VISIBLE);
    }
}
