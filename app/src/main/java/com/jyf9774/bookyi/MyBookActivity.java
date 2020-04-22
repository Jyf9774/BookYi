package com.jyf9774.bookyi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import static com.jyf9774.bookyi.simpleToast.showToast;
import static com.tencent.stat.common.StatConstants.LOG_TAG;

public class MyBookActivity extends AppCompatActivity {
    SQL_Client client;
    Handler mainHandler;
    Thread mThread;

    boolean searching;
    ArrayList<Book> BookList;

    RecyclerView myBookRecycler;
    LinearLayoutManager layoutManager;
    SwipeRefreshLayout mySwipeRefreshLayout;

    EditText myBookSearch;
    Button SearchButton;

    String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_book);
        username = (String) getIntent().getSerializableExtra("username");
        SharedPreferences sp = getSharedPreferences("username_DB", MODE_PRIVATE);
        String localusername = sp.getString("username", "");
        if (username == ""||username == null) {
            username = localusername;
        }

        myBookRecycler = findViewById(R.id.mybook_recyclerview);
        myBookRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        myBookRecycler.setLayoutManager(layoutManager);
        myBookRecycler.setVisibility(View.INVISIBLE);

        myBookSearch = findViewById(R.id.ed_mybook_search);
        SearchButton = findViewById(R.id.mybook_search_button);
        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = myBookSearch.getText().toString();
                key = key.replace(' ', '%');
                searchUserBook(key);
                searching = true;
            }
        });

        client = new SQL_Client();
        //处理BookList，更新UI
        mainHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 1:
                        updateUI();
                }
            }
        };

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList temp = client.findBookByUser(username);
                BookList = temp;
                Message msg = new Message();
                msg.what = 1;
                mainHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();

        mySwipeRefreshLayout = findViewById(R.id.mybook_swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (searching == false) {
                            mThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    ArrayList temp = client.findBookByUser(username);
                                    BookList = temp;
                                    Message msg = new Message();
                                    msg.what = 1;
                                    mainHandler.sendMessage(msg);
                                    return;
                                }
                            });
                            mThread.start();
                            myBookRecycler.setVisibility(View.INVISIBLE);
                            Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
                        } else {
                            mySwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }
        );


    }

    private void setListener() {

        myBookRecycler.addOnScrollListener(new MyScrollListener(new MyScrollListener.HideAndShowListener() {
            @Override
            public void hide() {
                // 隐藏动画--属性动画
                findViewById(R.id.mybook_search).setVisibility(View.INVISIBLE);
                searching = false;
                myBookSearch.setText("");

            }

            @Override
            public void show() {
                // 显示动画--属性动画
            }
        }));
    }

    private void updateUI() {
        final BookAdapter adapter = new BookAdapter(getApplicationContext(), BookList);
        adapter.setOnItemClickListener(new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String bookid = adapter.mArray.get(position).bookId;
                jump_Detail(bookid);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        myBookRecycler.setAdapter(adapter);
        setListener();
        if (searching == true) {
            if (adapter.getItemCount() == 0) {
                showToast("在您的书中没有找到书名含此关键字的书", getApplicationContext());

            } else {
                showToast("在您的书中找到" + adapter.getItemCount() + "本书", getApplicationContext());
            }
        } else {
            showToast("您共有" + adapter.getItemCount() + "本书", getApplicationContext());
        }

        mySwipeRefreshLayout.setRefreshing(false);
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter.mArray.size() != 0) {
                    if (adapter.mArray.get(0).bookId == null) {
                        showToast("请检查网络连接", getApplicationContext());
                    } else {
                        myBookRecycler.setVisibility(View.VISIBLE);
                    }
                } else {
                    myBookRecycler.setVisibility(View.VISIBLE);
                }
            }
        }, 150);
    }


    private void jump_Detail(String bookid) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("bookid", bookid);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void searchUserBook(final String key) {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList temp = client.searchUserBook(username, key);
                BookList = temp;
                Message msg = new Message();
                msg.what = 1;
                mainHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_search) {
            if (findViewById(R.id.mybook_search).getVisibility() == View.INVISIBLE) {
                findViewById(R.id.mybook_search).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.mybook_search).setVisibility(View.INVISIBLE);
                myBookSearch.setText("");
                searching = false;
            }
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            AlertDialog.Builder bdr = new AlertDialog.Builder(this);
            bdr.setTitle("关于");
            bdr.setMessage("\n   Version 1.0\n\n   Code by Jyf ");
            bdr.setNegativeButton("好", null);
            bdr.setIcon(R.mipmap.ic_launcher);
            bdr.setCancelable(true);
            bdr.show();
        }


        return super.onOptionsItemSelected(item);
    }
}
