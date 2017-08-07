package sdk.smssend;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "MainActivity";

    private ListView listView;
    private TextView sendNumText;
    private Button selectBtn;
    private Button sendBtn;
    private List<String> phoneList;
    private ArrayAdapter<String> adapter;

    private OkHttpClient client = new OkHttpClient();

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String result= (String) msg.obj;
            phoneList.add(0,result);
            adapter.notifyDataSetChanged();
        }
    };

    private Callback callback=new Callback(){
        @Override
        public void onFailure(Call call, IOException e) {
            Log.i("MainActivity","onFailure");
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            //从response从获取服务器返回的数据，转成字符串处理
            String str = new String(response.body().bytes(),"utf-8");
            Log.i("MainActivity","onResponse:"+str);

            //通过handler更新UI
            Message message=handler.obtainMessage();
            message.obj=str;
            message.sendToTarget();
        }
    };

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getApplicationContext().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 0);
        listView = (ListView) findViewById(R.id.listView);
        sendNumText = (TextView) findViewById(R.id.sendNum);
        selectBtn = (Button) findViewById(R.id.select);
        sendBtn = (Button) findViewById(R.id.sendButton);
        selectBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);

        phoneList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, phoneList);
        listView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select:
                getPhone();
                break;
            case R.id.sendButton:
                sendSms();
                break;
            default:
                break;
        }


    }

    private void getPhone(){
        Request.Builder builder = new Request.Builder().url("http://106.14.76.93:8008/GetRes.ashx?username=1&type=0&checkNum=0");
        Call call = client.newCall(builder.build());
        call.enqueue(callback);
    }

    public void sendSms() {
        // 一个一个去发送短信
        int i=0;
        for (String number : phoneList) {
            PendingIntent pi = PendingIntent.getActivity(
                    MainActivity.this, 0, new Intent(), 0);
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, "aaaaaaa", pi, null);
            i++;
        }
        Log.e(TAG, "send sms");
        sendNumText.setText(""+i);
        Toast.makeText(MainActivity.this, "短信群发完成", Toast.LENGTH_SHORT).show();
    }
}
