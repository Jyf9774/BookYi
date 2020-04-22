package com.jyf9774.bookyi;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.google.android.material.snackbar.Snackbar;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileWithBitmapCallback;

import java.io.File;
import java.util.Date;

import static com.jyf9774.bookyi.simpleToast.showToast;

public class UploadActivity extends AppCompatActivity {
    String username = "";
    String imgPath = "";
    boolean sale = true;
    private static int REQUEST_CHOOSE_PHOTO = 2;

    RadioGroup saleOrBorrow;
    EditText inputBookName, inputBookStatement, priceOrDate;
    Button openGallery, uploadButton;
    ImageView img;
    Handler handler;
    TencentCloudCos tos;
    Thread myThread;
    Handler mainhandler;
    SQL_Client client;

    private boolean uploading = false;
    private boolean imgUploaded = false;
    private boolean allUploaded = false;

    //书籍有关信息
    String bookId = "", bookName = "", bookStatement = "", bookImgURL = "", bookPrice = "", bookBorrowDate = "";
    String result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        username = (String) getIntent().getSerializableExtra("username");
        SharedPreferences sp=getSharedPreferences("username_DB",MODE_PRIVATE);
        String localusername = sp.getString("username","");
        if(username == ""||username == null){
            username = localusername;
        }

        //Init View
        openGallery = findViewById(R.id.button_chooseImg);
        uploadButton = findViewById(R.id.button_upload);
        saleOrBorrow = findViewById(R.id.upload_radioGroup);
        inputBookName = findViewById(R.id.ed_bookname);
        inputBookStatement = findViewById(R.id.ed_bookstatement);
        priceOrDate = findViewById(R.id.ed_bookpriceordate);
        findViewById(R.id.img_uploaded_Text).setVisibility(View.INVISIBLE);
        img = findViewById(R.id.upload_bookimg);
        tos = new TencentCloudCos();
        tos.init(UploadActivity.this);


        //Bind Listener
        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        inputBookStatement.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                allUploaded = false;
            }
        });
        inputBookName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                allUploaded = false;
            }
        });

        mainhandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 1:
                        if (msg.obj.toString().equals("书籍信息上传成功")) {
                            showToast("书籍信息上传成功", getApplicationContext());
                            allUploaded = true;
                            UploadActivity.this.finish();
                            break;
                        }
                        showToast(msg.obj.toString(), getApplicationContext());
                    case 2:
                }
            }
        };


        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (inputBookName.getText().toString().equals("")) {
                    Snackbar.make(findViewById(R.id.upload_title), "请输入书名", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (inputBookName.getText().toString().equals("null") || inputBookName.getText().toString().contains("#")) {
                    Snackbar.make(findViewById(R.id.upload_title), "书名包含非法字符", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(inputBookName.getText().toString().length()>=50){
                    Snackbar.make(findViewById(R.id.upload_title), "书名不可超过50个字符，超出部分已自动忽略", Snackbar.LENGTH_SHORT).show();
                    inputBookName.setText(inputBookName.getText().toString().substring(0,49));
                    return;
                }
                if (sale == true) {
                    if (priceOrDate.getText().toString().equals("")) {
                        Snackbar.make(findViewById(R.id.upload_title), "请输入价格", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    if (priceOrDate.getText().toString().equals("")) {
                        Snackbar.make(findViewById(R.id.upload_title), "请输入出借时长", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (inputBookStatement.getText().toString().equals("")) {
                    Snackbar.make(findViewById(R.id.upload_title), "请输入书籍描述", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (inputBookStatement.getText().toString().contains("#")) {
                    Snackbar.make(findViewById(R.id.upload_title), "书籍描述包含非法字符", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (allUploaded) {
                    Snackbar.make(findViewById(R.id.upload_title), "书籍信息已上传", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                bookName = inputBookName.getText().toString();
                bookStatement = inputBookStatement.getText().toString();
                if (sale) {
                    bookPrice = priceOrDate.getText().toString();
                } else {
                    bookBorrowDate = priceOrDate.getText().toString();
                }

                if (!imgPath.equals("")) {
                    uploadImg();
                    mainhandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (imgUploaded) {
                                showToast("图片已上传，书籍信息上传中", getApplicationContext());
                                myThread = new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        client = new SQL_Client();
                                        result = client.uploadBook(bookId, username, bookName, bookStatement, bookImgURL, sale, bookPrice, bookBorrowDate);
                                        Message msg = new Message();
                                        msg.obj = result;
                                        msg.what = 1;
                                        mainhandler.sendMessage(msg);
                                        return;
                                    }

                                });
                                myThread.start();
                            }
                        }
                    }, 2000);
                } else {
                    Snackbar.make(findViewById(R.id.upload_title), "请选择图片", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        saleOrBorrow.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int id = saleOrBorrow.getCheckedRadioButtonId();
                if (id == R.id.rb_sale) {
                    sale = true;
                    priceOrDate.setHint("价格（元）");
                    priceOrDate.setText("");
                    priceOrDate.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    imgUploaded = false;
                    allUploaded = false;
                } else if (id == R.id.rb_borrow) {
                    sale = false;
                    priceOrDate.setHint("天数（天）");
                    priceOrDate.setText("");
                    priceOrDate.setInputType(InputType.TYPE_CLASS_NUMBER);
                    imgUploaded = false;
                    allUploaded = false;
                }
            }
        });
    }

    @Override
    protected void onPause() {
        if (!imgPath.equals("") && uploading) {
            tos.cosxmlUploadTask.cancel();
        }
        super.onPause();
    }

    private void openGallery() {
        if (!PermissionManager.checkPhotoPermission(this, this, REQUEST_CHOOSE_PHOTO)) {
            //打开系统图库
            Intent picture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(picture, 0);
        } else {
            showToast("请检查相册权限", this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Uri selectedImage = data.getData();
            Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
            Tiny.getInstance().source(selectedImage).asFile().withOptions(options).compress(new FileWithBitmapCallback() {
                @Override
                public void callback(boolean isSuccess, Bitmap bitmap, String outfile, Throwable t) {
                    putImage(outfile);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void putImage(final String imagePath) {
        img.setImageURI(Uri.fromFile(new File(imagePath)));
        imgUploaded = false;
        allUploaded = false;
        findViewById(R.id.img_uploaded_Text).setVisibility(View.INVISIBLE);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Uri uri = FileProvider.getUriForFile(UploadActivity.this, BuildConfig.APPLICATION_ID + ".fileprovider", new File(imagePath));
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setDataAndType(uri, "image/jpeg");
                startActivity(intent);
            }
        });
        imgPath = imagePath;
        showToast(imagePath, getApplicationContext());
    }

    //上传图片
    protected void uploadImg() {
        bookId = username + "-" + new Date().getTime();
        final String cosFilename = bookId + ".jpg";
        uploading = true;
        findViewById(R.id.upload_progressBar).setVisibility(View.VISIBLE);
        tos.uploadFile(imgPath, cosFilename);
        tos.cosxmlUploadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                uploading = false;
                imgUploaded = true;
                bookImgURL = "https://jyf-1256919232.cos.ap-chengdu.myqcloud.com/" + cosFilename;
                System.out.println("图片上传成功，地址为：" + bookImgURL);
                mainhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.upload_progressBar).setVisibility(View.INVISIBLE);
                        findViewById(R.id.img_uploaded_Text).setVisibility(View.VISIBLE);
                        showToast("图片上传成功",getApplicationContext());
                    }
                });

            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException e, CosXmlServiceException e1) {
                System.out.println("图片上传失败");
                uploading = false;
                mainhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.upload_progressBar).setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

    }


}
