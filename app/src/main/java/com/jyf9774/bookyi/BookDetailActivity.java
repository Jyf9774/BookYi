package com.jyf9774.bookyi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.Date;

import static com.jyf9774.bookyi.simpleToast.showToast;

public class BookDetailActivity extends AppCompatActivity {
    TextView txv_bookName, txv_BookId, txv_BookUserName, txv_bookUploadTime, txv_bookStatement, txv_bookPriceOrDate;
    ImageView bookImg;
    EditText ed_want_say;
    Button want, delete;
    ScrollView rootView;
    View processBar;

    String bookId, username;
    boolean showMode = false;

    Book book;

    Thread mThread;
    Handler bookHandler;
    SQL_Client client;
    TencentCloudCos tos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        bookId = (String) getIntent().getSerializableExtra("bookid");
        username = (String) getIntent().getSerializableExtra("username");
        SharedPreferences sp = getSharedPreferences("username_DB", MODE_PRIVATE);
        String localusername = sp.getString("username", "");
        if(username.equals("")||username == null){
            username = localusername;
        }
        txv_BookId = findViewById(R.id.order_detail_book_id);
        txv_bookName = findViewById(R.id.order_detail_book_name);
        txv_BookUserName = findViewById(R.id.order_detail_owner_name);
        txv_bookUploadTime = findViewById(R.id.order_detail_book_time);
        txv_bookStatement = findViewById(R.id.order_detail_book_statement);
        txv_bookPriceOrDate = findViewById(R.id.order_detail_book_price_or_date);
        bookImg = findViewById(R.id.order_detail_book_img);
        ed_want_say = findViewById(R.id.order_detail_confirming_statement);
        want = findViewById(R.id.order_detail_accept);
        delete = findViewById(R.id.order_detail_delete_button);
        client = new SQL_Client();
        rootView = findViewById(R.id.order_detail_root);
        rootView.setVisibility(View.INVISIBLE);
        processBar = findViewById(R.id.order_detail_progress);
        processBar.setVisibility(View.VISIBLE);

        delete.setVisibility(View.INVISIBLE);
        if (showMode == true) {
            ed_want_say.setVisibility(View.INVISIBLE);
            want.setVisibility(View.INVISIBLE);
        }

        want.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wantBook();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteConfirm();
            }
        });

        bookHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 1:
                        updateUI();
                        break;
                    case 2:
                        if (msg.obj.toString().equals("删除成功")) {
                            deleteBook();
                            break;
                        } else {
                            showToast(msg.obj.toString(), getApplicationContext());
                        }
                        break;
                    case 3:
                        showToast(msg.obj.toString(), getApplicationContext());
                        bookHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                back();
                            }
                        }, 200);
                        BookDetailActivity.this.finish();
                        break;
                    case 4:
                        showToast(msg.obj.toString(), getApplicationContext());
                        if (msg.obj.toString().equals("书籍已售出") || msg.obj.toString().equals("书籍不存在")) {
                            bookHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    back();
                                }
                            }, 200);
                            BookDetailActivity.this.finish();
                        }
                        break;
                }
            }
        };
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Book temp = client.findBook(bookId);
                book = temp;
                Message msg = new Message();
                msg.what = 1;
                bookHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();

    }

    public void updateUI() {
        if (book.bookId == null) {
            showToast("书籍不存在", getApplicationContext());
            processBar.setVisibility(View.INVISIBLE);
            return;
        }
        txv_BookId.setText("书籍ID：" + book.bookId);
        if (book.bookName.charAt(0) == '《') {
            txv_bookName.setText(book.bookName);
        } else {
            txv_bookName.setText("《" + book.bookName + "》");
        }
        txv_BookUserName.setText(book.username);
        if (book.username.equals(username) && !showMode) {
            txv_BookUserName.setText(book.username);
            delete.setVisibility(View.VISIBLE);
            ed_want_say.setEnabled(false);
            ed_want_say.setHint("这是自己的书哦");
            want.setEnabled(false);
            want.setText("不能点");
        } else {
            txv_BookUserName.setText(book.username);
            ed_want_say.setText("我想要这本书，谢谢您~");
        }
        txv_bookStatement.setText(book.bookStatement);
        String showTime = TimeUtility.getTime(book.uploadTime.substring(0, 19));
        txv_bookUploadTime.setText(showTime);
        if (book.bookSaled) {
            txv_bookPriceOrDate.setText("交易已完成");
            ed_want_say.setVisibility(View.INVISIBLE);
            want.setVisibility(View.INVISIBLE);
        } else {
            if (!book.bookSaleOrBorrow) {
                txv_bookPriceOrDate.setText("¥" + book.bookPrice + "元");
            } else {
                txv_bookPriceOrDate.setText("出借" + book.bookBorrowDate + "天");
            }
        }
        Glide.with(getApplicationContext()).load(book.bookPicture).into(bookImg);
        bookHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                processBar.setVisibility(View.INVISIBLE);
                rootView.setVisibility(View.VISIBLE);
            }
        }, 100);
    }

    private void deleteConfirm() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setPositiveButton("确认删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteBookImg();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setMessage("书籍信息删除后不可恢复，是否确认");
        dialog.setTitle("提示");
        dialog.show();
    }

    private void deleteBookImg() {
        tos = new TencentCloudCos();
        tos.init(getApplicationContext());
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = tos.deleteFile(bookId + ".jpg");
                Message msg = new Message();
                msg.what = 2;
                msg.obj = result;
                bookHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
    }

    private void deleteBook() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = client.deleteBookByID(bookId);
                Message msg = new Message();
                msg.what = 3;
                msg.obj = result;
                bookHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
    }

    private void wantBook() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String OrderID = book.username + "-" + username + "-" + new Date().getTime();
                String result = client.createOrder(OrderID, username, book.username, book.bookId, book.bookName, ed_want_say.getText().toString());
                Message msg = new Message();
                msg.what = 4;
                msg.obj = result;
                bookHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
    }

    private void back() {
        super.onBackPressed();
    }


}
