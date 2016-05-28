package com.example.gpstestapp;

import android.Manifest;
import android.app.Service;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements OnClickListener, LocationListener {
    private LocationManager mLocationManager;
    private Button button1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOCATION_UPDATE_MIN_TIME = 0;
    private static final int LOCATION_UPDATE_MIN_DISTANCE = 1;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    int providerflag = 0;
    private boolean isNetworkEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(this);
        // LocationManagerを取得
        mLocationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        requestLocationUpdates();
    }

    // 位置取得から表示までやる関数
    private void requestLocationUpdates() {
        propermissioncheck();
        isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(providerflag != 11){
            providerflag = 1;
        }
        if (isNetworkEnabled != true || providerflag == 11) {
            isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            String message1 = "無効になっています";
            showMessage(message1);
            providerflag = 2;
            if (isNetworkEnabled != true) {
                String message = "Networkが無効になっています";
                showMessage(message);
                return;
            }
        }
        showNetworkEnabled(isNetworkEnabled);
        showProvider(provider(providerflag));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;//パーミッションを確認して許可がない時は、位置を取得せずにreturn
        }
        mLocationManager.requestLocationUpdates(
                provider(providerflag),
                LOCATION_UPDATE_MIN_TIME,
                LOCATION_UPDATE_MIN_DISTANCE,
                this
        );

        Location location = mLocationManager.getLastKnownLocation(provider(providerflag));
        if(location==null){
            providerflag = 11;
            requestLocationUpdates();
        }
        if (location != null) {
            showLocation(location);//　位置情報を表示する
        }
    }

    // パーミッション関係　
    public void propermissioncheck() {// パーミッションチェック
        //androidのバージョンが6.0以上の時
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // 位置情報の取得が許可されているかチェック
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // なければ権限を求めるダイアログを表示
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // GPSの権限を求めるコード
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // 許可されたら
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // テキストを表示してLocationManagerを取得
                Toast.makeText(this, "位置情報取得が許可されました", Toast.LENGTH_SHORT).show();
                // 許可されなかったら
            } else {
                // 何もしない
                Toast.makeText(this, "位置情報取得が拒否されました", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String provider(int check) {// GPSとNETWORKの切り替え
        if (check == 1)
            return LocationManager.GPS_PROVIDER;

        return LocationManager.NETWORK_PROVIDER;

    }
// パーミッション関係ここまで
// プロバイダ関係
    @Override
    public void onLocationChanged(Location location) {// 位置が変わったら
        Log.e(TAG, "onLocationChanged");
        showLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {// プロバイダが変わったら
        Log.e(TAG, "onstatusChanged");
        showProvider(provider);
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                String outOfServiceMessage = provider + "が圏外になっているので取得できません";
                showMessage(outOfServiceMessage);
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                String temporarilyUnacaliableMessage = "一時的に" + provider + "が利用できません";
                showMessage(temporarilyUnacaliableMessage);
                break;
            case LocationProvider.AVAILABLE:
                if (provider.equals(provider(providerflag))) {
                    String avalialeMessage = provider + "が利用できます";
                    showMessage(String.valueOf(providerflag));
                    requestLocationUpdates();
                }
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {// プロバイダが有効になったら
        Log.e(TAG, "onProviderEnabled");
        String message = provider + "有効になりました";
        showMessage(message);
        showProvider(provider);
        if (provider.equals(provider(providerflag))) {
            requestLocationUpdates();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {// プロバイダが無効になったら
        Log.e(TAG, "onProviderDIsbled");
        showProvider(provider);
        if (provider.equals(provider(providerflag))) {
            String message = provider + "無効になりました";
            showMessage(message);
        }
    }

    // プロバイダ関係　ここまで
// showメソッド ここから
    private void showMessage(String message) {// エラーメッセージなどを表示
        TextView textView = (TextView) findViewById(R.id.message);
        textView.setText(message);
    }

    private void showProvider(String networkProvider) {// 利用しているプロバイダを表示
        TextView textView = (TextView) findViewById(R.id.provider);
        textView.setText(networkProvider);
    }

    private void showNetworkEnabled(boolean isNetworkEnabled) {// プロバイダが使えるか表示
        TextView textView = (TextView) findViewById(R.id.enable);
        textView.setText("GPSEnabled:" + String.valueOf(isNetworkEnabled));
    }

    private void showLocation(Location location) {// 位置情報を表示
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        long time = location.getTime();
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("yyyy/mm/dd HH:mm:ss:SS");
        String dateFomatted = formatter.format(date);
        TextView longitudeTextView = (TextView) findViewById(R.id.longitude);
        longitudeTextView.setText("緯度" + String.valueOf(longitude));
        TextView latitudeTextView = (TextView) findViewById(R.id.latitude);
        latitudeTextView.setText("経度" + String.valueOf(latitude));
        TextView gettimeTextView = (TextView) findViewById(R.id.gettime);
        gettimeTextView.setText("取得時間：" + dateFomatted);
    }

    // showメソッド ここまで
    //　クリック処理
    public void onClick(View v) {
        if (v == button1) {
            requestLocationUpdates();
        }
    }
}