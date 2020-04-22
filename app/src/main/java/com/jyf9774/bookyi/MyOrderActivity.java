package com.jyf9774.bookyi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import static com.jyf9774.bookyi.simpleToast.showToast;
import static com.tencent.stat.common.StatConstants.LOG_TAG;

public class MyOrderActivity extends AppCompatActivity {
    boolean change = false;
    String username = "";
    SQL_Client client;
    Handler mainHandler;
    Thread mThread;

    ArrayList<Order> OrderList;

    RecyclerView myOrderRecycler;
    LinearLayoutManager layoutManager;
    SwipeRefreshLayout mySwipeRefreshLayout;
    TextView txv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order);
        username = (String) getIntent().getSerializableExtra("username");
        SharedPreferences sp = getSharedPreferences("username_DB", MODE_PRIVATE);
        String localusername = sp.getString("username", "");
        if (username == ""||username == null) {
            username = localusername;
        }
        txv = findViewById(R.id.order_show);
        myOrderRecycler = findViewById(R.id.order_recyclerview);
        myOrderRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        myOrderRecycler.setLayoutManager(layoutManager);
        myOrderRecycler.setVisibility(View.INVISIBLE);

        txv.setText("点击右上角切换查看");
        txv.setVisibility(View.VISIBLE);
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
                ArrayList temp;
                if (change) {
                    temp = client.getBuyerOrder(username);
                } else {
                    temp = client.getOwnerOrder(username);
                }
                OrderList = temp;
                Message msg = new Message();
                msg.what = 1;
                mainHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();

        mySwipeRefreshLayout = findViewById(R.id.order_swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayList temp;
                                if (change) {
                                    temp = client.getBuyerOrder(username);
                                } else {
                                    temp = client.getOwnerOrder(username);
                                }
                                OrderList = temp;
                                Message msg = new Message();
                                msg.what = 1;
                                mainHandler.sendMessage(msg);
                                return;
                            }
                        });
                        mThread.start();
                        myOrderRecycler.setVisibility(View.INVISIBLE);
                        Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

                    }
                }
        );

    }

    @Override
    public void onResume() {
        super.onResume();
        updateDisplay();
    }

    private void updateDisplay() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList temp;
                if (change) {
                    temp = client.getBuyerOrder(username);
                } else {
                    temp = client.getOwnerOrder(username);
                }
                OrderList = temp;
                Message msg = new Message();
                msg.what = 1;
                mainHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
        myOrderRecycler.setVisibility(View.INVISIBLE);
    }


    private void updateUI() {
        final OrderAdapter adapter = new OrderAdapter(getApplicationContext(), OrderList);
        adapter.setOnItemClickListener(new OrderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String orderid = adapter.mArray.get(position).orderId;
                String bookid = adapter.mArray.get(position).bookId;
                jump_Detail(orderid,bookid);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        myOrderRecycler.setAdapter(adapter);

        mySwipeRefreshLayout.setRefreshing(false);
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter.mArray.size() != 0) {
                    if (adapter.mArray.get(0).state == null) {
                        showToast("请检查网络连接", getApplicationContext());
                    } else {
                        txv.setVisibility(View.INVISIBLE);
                        myOrderRecycler.setVisibility(View.VISIBLE);
                        if (change) {
                            showToast("我发出的订单", getApplicationContext());
                        } else {
                            showToast("发给我的订单", getApplicationContext());
                        }
                    }
                } else {
                    //myOrderRecycler.setVisibility(View.VISIBLE);
                    if (change) {
                        showToast("我发出的订单", getApplicationContext());
                    } else {
                        showToast("发给我的订单", getApplicationContext());
                    }
                }
            }
        }, 150);
    }

    private void jump_Detail(String orderId,String bookId) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("orderId", orderId);
        intent.putExtra("bookId",bookId);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.order, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_change) {
            change = !change;
            mySwipeRefreshLayout.setRefreshing(true);
            txv.setVisibility(View.VISIBLE);
            updateDisplay();

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
