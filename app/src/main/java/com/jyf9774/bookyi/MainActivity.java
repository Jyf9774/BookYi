package com.jyf9774.bookyi;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import android.view.MenuItem;

import android.view.Menu;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

import static com.jyf9774.bookyi.simpleToast.showToast;
import static com.tencent.stat.common.StatConstants.LOG_TAG;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private long mExitTime;
    RecyclerView mainRecycler;
    LinearLayoutManager layoutManager;
    SwipeRefreshLayout mySwipeRefreshLayout;
    Toolbar toolbar;
    FloatingActionButton fab;
    EditText mainSearch;
    Button SearchButton;

    String username = "";
    Thread mThread;
    Handler mainHandler;
    SQL_Client client;
    ArrayList<Book> BookList;
    boolean searching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = (String) getIntent().getSerializableExtra("username");
        //init view
        SharedPreferences sp = getSharedPreferences("username_DB", MODE_PRIVATE);
        String localusername = sp.getString("username", "");
        if(username == ""||username == null){
            username = localusername;
        }

        mainRecycler = findViewById(R.id.main_recyclerview);
        mainRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        mainRecycler.setLayoutManager(layoutManager);
        mainRecycler.setVisibility(View.INVISIBLE);

        mainSearch = findViewById(R.id.ed_main_search);
        SearchButton = findViewById(R.id.main_search_button);
        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = mainSearch.getText().toString();
                key = key.replace(' ', '%');
                searchBook(key);
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
                ArrayList temp = client.getAllBook();
                BookList = temp;
                Message msg = new Message();
                msg.what = 1;
                mainHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
        mySwipeRefreshLayout = findViewById(R.id.main_swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (searching == false) {
                            mThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    ArrayList temp = client.getAllBook();
                                    BookList = temp;
                                    Message msg = new Message();
                                    msg.what = 1;
                                    mainHandler.sendMessage(msg);
                                    return;
                                }
                            });
                            mThread.start();
                            mainRecycler.setVisibility(View.INVISIBLE);
                            Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
                        } else {
                            mySwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }
        );

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jump_upload();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {

            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                TextView nav_Title = findViewById(R.id.nav_Title);
                nav_Title.setText(username);
                nav_Title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jump_UserCenter();
                    }
                });
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {

            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setListener() {

        mainRecycler.addOnScrollListener(new MyScrollListener(new MyScrollListener.HideAndShowListener() {
            @Override
            public void hide() {
                // 隐藏动画--属性动画
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                findViewById(R.id.main_search).setVisibility(View.INVISIBLE);
                searching = false;
                mainSearch.setText("");
                fab.animate().translationY(fab.getHeight() + layoutParams.bottomMargin).setInterpolator(new AccelerateInterpolator(3));
            }

            @Override
            public void show() {
                // 显示动画--属性动画
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                fab.animate().translationY(0).setInterpolator(new DecelerateInterpolator(3));
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
        mainRecycler.setAdapter(adapter);

        setListener();
        if (searching == true) {
            if (adapter.getItemCount() == 0) {
                showToast("没有找到书名含此关键字的书", getApplicationContext());
                return;
            } else {
                showToast("找到" + adapter.getItemCount() + "本书", getApplicationContext());
            }
        } else {
            if (adapter.getItemCount() != 0 && adapter.mArray.get(0).bookId != null) {
                showToast("共有" + adapter.getItemCount() + "本书", getApplicationContext());
            }
        }

        mySwipeRefreshLayout.setRefreshing(false);
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter.getItemCount() <= 1) {
                    if (adapter.mArray.get(0).bookId == null) {
                        showToast("请检查网络连接", getApplicationContext());
                    } else {
                        mainRecycler.setVisibility(View.VISIBLE);
                    }
                } else {
                    mainRecycler.setVisibility(View.VISIBLE);
                }

            }
        }, 150);
    }

    private void jump_upload() {
        Intent intent = new Intent(this, UploadActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void jump_Detail(String bookid) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("bookid", bookid);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void jump_UserCenter() {
        Intent intent = new Intent(this, UserCenterActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void searchBook(final String key) {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList temp = client.searchBook(key);
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
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Object mHelperUtils;
            showToast("再按一次退出登录", getApplicationContext());
            //System.currentTimeMillis()系统当前时间
            mExitTime = System.currentTimeMillis();
            return;
        } else {
            super.onBackPressed();
            MainActivity.this.finish();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList temp = client.getAllBook();
                BookList = temp;
                Message msg = new Message();
                msg.what = 1;
                mainHandler.sendMessage(msg);
                return;
            }
        });
        mThread.start();
        mainRecycler.setVisibility(View.INVISIBLE);

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
            if (findViewById(R.id.main_search).getVisibility() == View.INVISIBLE) {
                findViewById(R.id.main_search).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.main_search).setVisibility(View.INVISIBLE);
                mainSearch.setText("");
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_myBooks) {
            Intent intent = new Intent(this, MyBookActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            item.setChecked(false);

        } else if (id == R.id.nav_myOrders) {
            Intent intent = new Intent(this, MyOrderActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            //获取剪贴板管理器：
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", "欢迎使用书易，请点击https://apk-1256919232.cos.ap-beijing.myqcloud.com/BookYi.apk下载最新安装包~");
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
            showToast("分享链接已复制",getApplicationContext());
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
