package com.jyf9774.bookyi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import static com.jyf9774.bookyi.simpleToast.showToast;

public class OrderDetailActivity extends AppCompatActivity {
    private Order myOrder;
    private Book myBook;
    private User myOwner;

    private String username;
    private String orderId;
    private String bookId;

    View processBar;
    ScrollView rootView;

    TextView headerState, headerId, headerRequestStatement, headerCreateTime, headerConfirmTime, headerBuyerName;
    TextView bookBookId, bookBookName, bookBookStatement, bookBookPriceOrDate, bookOwnerName, bookBookTime;
    TextView show1, show2, confirmedStatement;

    ImageView bookImage;

    EditText confirmingText;

    Button acceptButton, refuseButton, deleteButton;

    SQL_Client client;
    Handler orderDetailHandler;
    Thread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        username = (String) getIntent().getSerializableExtra("username");
        orderId = (String) getIntent().getSerializableExtra("orderId");
        bookId = (String) getIntent().getSerializableExtra("bookId");
        SharedPreferences sp = getSharedPreferences("username_DB", MODE_PRIVATE);
        String localusername = sp.getString("username", "");
        if (username == ""||username == null) {
            username = localusername;
        }
        client = new SQL_Client();

        orderDetailHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 0:
                        getBook();
                        break;
                    case 1:
                        updateUI();
                        break;
                    case 2:
                        showToast(msg.obj.toString(), getApplicationContext());
                        orderDetailHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                back();
                            }
                        }, 200);
                        OrderDetailActivity.this.finish();
                        break;
                    case 3:
                        showToast(msg.obj.toString(), getApplicationContext());
                        orderDetailHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                back();
                            }
                        }, 200);
                        OrderDetailActivity.this.finish();
                        break;
                    case 4:
                        displayUserInfo();
                }
            }
        };

        initView();
        getOrder();
    }

    private void initView() {
        //进度条及根View初始化
        rootView = findViewById(R.id.order_detail_root);
        processBar = findViewById(R.id.order_detail_progress);
        rootView.setVisibility(View.INVISIBLE);
        processBar.setVisibility(View.VISIBLE);
        //header区域初始化
        headerState = findViewById(R.id.order_detail_state);
        headerId = findViewById(R.id.order_detail_order_id);
        headerCreateTime = findViewById(R.id.order_detail_create_time);
        headerConfirmTime = findViewById(R.id.order_detail_confirm_time);
        headerRequestStatement = findViewById(R.id.order_detail_request_statement);
        headerBuyerName = findViewById(R.id.order_detail_buyer_name);
        //book区域初始化
        bookBookId = findViewById(R.id.order_detail_book_id);
        bookBookName = findViewById(R.id.order_detail_book_name);
        bookBookPriceOrDate = findViewById(R.id.order_detail_book_price_or_date);
        bookBookStatement = findViewById(R.id.order_detail_book_statement);
        bookBookTime = findViewById(R.id.order_detail_book_time);
        bookOwnerName = findViewById(R.id.order_detail_owner_name);
        bookImage = findViewById(R.id.order_detail_book_img);
        //操作区域初始化
        confirmingText = findViewById(R.id.order_detail_confirming_statement);
        acceptButton = findViewById(R.id.order_detail_accept);
        refuseButton = findViewById(R.id.order_detail_refuse);
        deleteButton = findViewById(R.id.order_detail_delete_button);
        confirmedStatement = findViewById(R.id.order_detail_confirmed_statement);
        show1 = findViewById(R.id.order_detail_show1);
        show2 = findViewById(R.id.order_detail_show2);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptConfirm();
            }
        });

        refuseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refuseConfirm();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteConfirm();
            }
        });


    }

    private void getOrder() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Order temp = client.findOrder(orderId);
                myOrder = temp;
                Message msg = new Message();
                msg.what = 0;
                orderDetailHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
    }

    private void getBook() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Book temp = client.findBook(bookId);
                myBook = temp;
                Message msg = new Message();
                msg.what = 1;
                orderDetailHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
    }

    private void getUser() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                User temp = client.getUserByName(myBook.username);
                myOwner = temp;
                Message msg = new Message();
                msg.what = 4;
                orderDetailHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
    }

    private void updateUI() {
        if(myOrder.orderId == null){
            showToast("订单不存在",getApplicationContext());
            return;
        }
        headerState.setText(myOrder.state);
        headerBuyerName.setText("买家："+myOrder.buyerName);
        headerRequestStatement.setText("订单附言：" + myOrder.requestStatement);
        headerId.setText(myOrder.orderId);
        headerCreateTime.setText("创建时间:" + TimeUtility.getTime(myOrder.createTime.substring(0, 19)));
        if (myOrder.confirmTime != null) {
            headerConfirmTime.setText("确认时间：" + TimeUtility.getTime(myOrder.confirmTime.substring(0, 19)));
        } else {
            headerConfirmTime.setText("尚未确认");
        }

        if (myBook.bookName.charAt(0) == '《') {
            bookBookName.setText(myBook.bookName);
        } else {
            bookBookName.setText("《" + myBook.bookName + "》");
        }
        bookBookId.setText("书籍ID：" + myBook.bookId);
        bookOwnerName.setText(myBook.username);
        bookBookTime.setText(TimeUtility.getTime(myBook.uploadTime.substring(0, 19)));
        bookBookStatement.setText(myBook.bookStatement);
        if (!myBook.bookSaleOrBorrow) {
            bookBookPriceOrDate.setText("¥" + myBook.bookPrice + "元");
        } else {
            bookBookPriceOrDate.setText("出借" + myBook.bookBorrowDate + "天");
        }
        Glide.with(getApplicationContext()).load(myBook.bookPicture).into(bookImage);
        if(myOrder.ownerName.equals(username)){
            deleteButton.setVisibility(View.INVISIBLE);
        }

        if (myOrder.state.equals("待确认") && myBook.username.equals(username)) {
            show2.setText("请您确认此订单");
            deleteButton.setVisibility(View.INVISIBLE);
            confirmedStatement.setVisibility(View.INVISIBLE);
        } else if (myOrder.state.equals("待确认")) {
            show2.setText("订单详情");
            confirmingText.setVisibility(View.INVISIBLE);
            acceptButton.setVisibility(View.INVISIBLE);
            refuseButton.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);
            confirmedStatement.setVisibility(View.VISIBLE);
            confirmedStatement.setText("订单待确认，请耐心等待");
        } else {
            show2.setText("订单详情");
            confirmingText.setVisibility(View.INVISIBLE);
            acceptButton.setVisibility(View.INVISIBLE);
            refuseButton.setVisibility(View.INVISIBLE);
            confirmedStatement.setVisibility(View.VISIBLE);
            confirmedStatement.setText("回复："+myOrder.confirmStatement);
            if (myOrder.state.equals("已完成") && myOrder.buyerName.equals(username)) {
                getUser();
            }
        }


        orderDetailHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                processBar.setVisibility(View.INVISIBLE);
                rootView.setVisibility(View.VISIBLE);
            }
        }, 100);
    }

    private void acceptConfirm() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setPositiveButton("确认接受", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                acceptOrder();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setMessage("将接受此订单，您的联系方式将向对方展示，是否确认");
        dialog.setTitle("提示");
        dialog.show();
    }

    private void acceptOrder() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = client.confirmOrder(orderId, bookId, confirmingText.getText().toString(), true);
                Message msg = new Message();
                msg.what = 2;
                msg.obj = result;
                orderDetailHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
    }

    private void refuseConfirm() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setPositiveButton("确认拒绝", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                refuseOrder();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setMessage("您将拒绝此订单，是否确认");
        dialog.setTitle("提示");
        dialog.show();
    }

    private void refuseOrder() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = client.confirmOrder(orderId, bookId, confirmingText.getText().toString(), false);
                Message msg = new Message();
                msg.what = 2;
                msg.obj = result;
                orderDetailHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
    }

    private void deleteConfirm() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setPositiveButton("确认删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteOrder();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setMessage("订单删除后不可恢复，是否确认");
        dialog.setTitle("提示");
        dialog.show();
    }

    private void deleteOrder() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = client.deleteOrderByID(orderId);
                Message msg = new Message();
                msg.what = 3;
                msg.obj = result;
                orderDetailHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
    }

    private void displayUserInfo() {
        System.out.println("DisplayUserTouched");
        String phoneNumber, qqNumber, wechatNumber;
        phoneNumber = myOwner.phoneNumber;
        qqNumber = myOwner.QQNumber;
        wechatNumber = myOwner.weChatNumber;
        final String ms = "电话号码：" + phoneNumber + "\nQQ：" + qqNumber + "\n微信：" + wechatNumber;
        AlertDialog.Builder dialog = new AlertDialog.Builder(OrderDetailActivity.this);
        dialog.setPositiveButton("复制", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("Label", ms);
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                showToast("联系方式已复制", getApplicationContext());
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setMessage("电话号码：" + phoneNumber + "\nQQ：" + qqNumber + "\n微信：" + wechatNumber);
        dialog.setTitle("联系方式");
        dialog.show();
    }

    private void back() {
        super.onBackPressed();
    }
}
