package huhu.com.demoqiniu;

//1.图片上传失败

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
//import com.qiniu.auth.JSONObjectRet;
//import com.qiniu.io.IO;
//import com.qiniu.io.PutExtra;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

    @SuppressLint("NewApi")
    Button bt_school, bt_category, bt_push, bt_push_back;
    ImageButton bt_camera;
    int schoolnumber, categorynumber;
    private int CAMERA = 0;
    private final String IMAGE_TYPE = "image/*";
    private int IMAGE_CODE = 1;
    String usage[] = new String[]{"拍一张", "选择一张照片"};
    EditText edt_phonenumber, edt_QQnumber, edt_price, edt_name;
    String str_phone, str_QQ;
    private String filename;
    private String time;
    private String IMEI;
    private String str_price;
    private String str_name;
    private ImageView iv_activity_push_photoline, iv_push_back;
    // private String str_describe;
    private Toast toast = null;
    private boolean hasPicture = false;
    private boolean hasContent = false;
    private String campus = "";
    private String type = "";
    private String details = "";
    private EditText describe;
    private String date = "";
    private String bad_times = "";
    private ProgressDialog progress;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case 0:
                    String result0 = msg.obj.toString();
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(result0);
                        bad_times = jsonObject.getString("bad_times");
                        date = jsonObject.getString("time");
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (result0.equalsIgnoreCase("-1") || bad_times.equalsIgnoreCase("0") ) {
                        pushInit();
                    } else {
                        progress.dismiss();

                    }
                    break;
                case 1:
                    String token = msg.obj.toString();
                    System.out.println("=========" + token);
                    UploadPic(filename, IMEI + time, token);
                    break;
                case 2:
                    String result = msg.obj.toString();
                    if (result.equals("1")) {

                        progress.dismiss();
                        finish();
                    } else if (result.equals("0")) {

                        progress.dismiss();
                    }
                    bt_push.setEnabled(true);
                    break;

            }
        }

    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv_push_back = (ImageView) findViewById(R.id.iv_push_back);
        bt_push_back = (Button) findViewById(R.id.bt_push_back);
        bt_push_back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                str_phone = edt_phonenumber.getText().toString().trim();
                str_QQ = edt_QQnumber.getText().toString().trim();
                str_price = edt_price.getText().toString().trim();
                str_name = edt_name.getText().toString().trim();
                campus = bt_school.getText().toString().trim();
                type = bt_category.getText().toString().trim();
                details = describe.getText().toString().trim();
            }
        });
        bt_push_back.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    iv_push_back.setImageResource(R.drawable.activity_push_back_pressed);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    iv_push_back.setImageResource(R.drawable.activity_myoldbook_back);
                }
                return false;
            }
        });
        // 照相的功能在这里实现
        bt_camera = (ImageButton) findViewById(R.id.camera1);
        // 选择拍照还是照片
        bt_camera.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this).setTitle("选择").setItems(usage, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // 拍照
                            Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePhoto, CAMERA);
                        } else if (which == 1) {
                            // 选择一张照片

                            Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
                            getAlbum.setType(IMAGE_TYPE);
                            startActivityForResult(getAlbum, IMAGE_CODE);

                        }
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });


        edt_QQnumber = (EditText) findViewById(R.id.qqnumber1);
        bt_push = (Button) findViewById(R.id.push);
        edt_phonenumber = (EditText) findViewById(R.id.tel1);
        edt_price = (EditText) findViewById(R.id.price1);
        edt_name = (EditText) findViewById(R.id.bookname);
        iv_activity_push_photoline = (ImageView) findViewById(R.id.iv_activity_push_photoline);

        bt_push.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                bt_push = (Button) arg0;
                bt_push.setEnabled(false);
                progress = new ProgressDialog(MainActivity.this);

                time = getTime();
                IMEI = getIMEI();

                Thread t0 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Message msg = new Message();
                        String url = "http://1.myoldbooks.sinaapp.com/bad_record.php?x_number=" + IMEI;
                        try {
                            String result = NetCore.getResultFromNet(url);
                            msg.what = 0;
                            msg.obj = result;
                            handler.sendMessage(msg);

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
                t0.start();

            }
        });

    }


    public void pushInit() {
        // TODO Auto-generated method stub

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Message msg = new Message();
                //test
                String url = "http://1.smartprotecter.sinaapp.com/sp/image_upload.php";
                try {
                    String token = NetCore.getResultFromNet(url);
                    msg.what = 1;
                    msg.obj = token;
                    handler.sendMessage(msg);

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        t1.start();
        time = getTime();
        IMEI = getIMEI();
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Message msg = new Message();
                String picUrl = "http://oldbooks.qiniudn.com/" + IMEI + time;
                String url = "http://1.myoldbooks.sinaapp.com/upload_book.php?picture=" + picUrl + "&price=" + str_price + "&type=" + type + "&campus=" + campus + "&phone=" + str_phone + "&qq=" + str_QQ + "&details=" + details + "&x_number=" + IMEI + "&name=" + str_name + "&time=" + time + "&sale=" + "0";
                url = url.replace(" ", "%20");
                try {
                    String result = NetCore.getResultFromNet(url);
                    msg.what = 2;
                    msg.obj = result;
                    handler.sendMessage(msg);

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        t2.start();
    }

    public void UploadPic(String PicPath, String key, String token) {
        UploadManager uploadManager = new UploadManager();
        uploadManager.put(PicPath, key, token, new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject response) {
                Log.i("qiniu", info + "");
                finish();
            }
        }, null);
    }

    public String getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String time = formatter.format(curDate);
        return time;
    }

    public String getIMEI() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String IMEI = tm.getDeviceId();
        return IMEI;
    }

    // 照相并保存的回调方法
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA) {
            switch (resultCode) {
                case RESULT_OK: // 用户照相

                    Bundle bundle = data.getExtras();
                    // 获取相机返回的数据，并转换为图片格式
                    Bitmap camera_bitmap = (Bitmap) bundle.get("data");
                    // camera_bitmap =
                    // ThumbnailUtils.extractThumbnail(camera_bitmap, 300, 400);
                    iv_activity_push_photoline.setImageBitmap(outputBitmap(camera_bitmap));
                    bt_camera.setBackgroundDrawable(null);
                    hasPicture = true;

                    break;
                case RESULT_CANCELED: // 用户取消照相
                    return;
            }

            // 显示图片
        }
        if (requestCode == IMAGE_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    try {
                        Uri uri = data.getData();
                        if (uri == null) {
                            Log.i("张雯", "huhu");
                            return;
                        }
                        Log.i("张糊糊", uri.toString());
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        // filename = cursor.getString(columnIndex);

                        ContentResolver cr = this.getContentResolver();

                        Bitmap album_bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                        if (album_bitmap == null) {
                            Log.i("张雯", "糊糊");
                        }
                        album_bitmap = ThumbnailUtils.extractThumbnail(album_bitmap, 150, 180);
                        iv_activity_push_photoline.setImageBitmap(outputBitmap(album_bitmap));
                        hasPicture = true;

                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (OutOfMemoryError e) {

                        return;
                    }
                    break;
                case RESULT_CANCELED: // 用户取消照相
                    return;
            }
        }
    }

    /**
     * 把图片压缩并切圆角，保存到本地
     *
     * @param bitmap
     * @return
     */
    public Bitmap outputBitmap(Bitmap bitmap) {
        new DateFormat();
        String name = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
        FileOutputStream fout = null;
        final String SDpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(SDpath + "/sdcard/BookPicture/");
        file.mkdirs();
        filename = file.getPath() + name;
        try {
            fout = new FileOutputStream(filename);

            bitmap = toRoundCorner(bitmap, 10);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                fout.flush();
                fout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 将图片设置为圆角
     */
    public Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

}
