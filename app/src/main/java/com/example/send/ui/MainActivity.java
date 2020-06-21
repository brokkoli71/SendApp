package com.example.send.ui;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.send.utils.ImageHelper;
import com.example.send.R;
import com.example.send.receiver.ReceivedDataHandler;
import com.example.send.sender.SendingTaskData;
import com.example.send.receiver.ServerReceiver;
import com.example.send.sender.ServerSender;
import com.example.send.utils.Toaster;
import com.example.send.utils.Values;
import com.example.send.receiver.TCPReceiver;
import com.example.send.sender.TCPSender;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import pub.devrel.easypermissions.EasyPermissions;

//new ui
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import me.relex.circleindicator.CircleIndicator;

public class MainActivity extends AppCompatActivity {



    private ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init Toaster to make all toasts on MainActivity
        Toaster.init(this);

        //new UI
        viewPager = findViewById(R.id.pager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), viewPager);
        viewPager.setAdapter(viewPagerAdapter);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        CircleIndicator indicator = findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);



        Intent intent = getIntent();
        String action = intent.getAction();

        // send multiple Data might get implemented later
        if(Intent.ACTION_SEND.equals(action)){
            //todo: NullPointerException -> add delay until ui building finished
            viewPager.setCurrentItem(1,true);
            viewPagerAdapter.sendFragment.gotIntentActionSend(intent, getContentResolver());
        }
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.w("activity_result", "request handling:"+", "
                +(resultCode==RESULT_CANCELED?"canceled":(resultCode==RESULT_OK?"result ok":resultCode)));
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == Values.PICKFILE_REQUEST_CODE) {
                if (resultCode == RESULT_OK && intent != null) {
                    viewPagerAdapter.sendFragment.onReceivePickfileRequest(intent, getContentResolver());
                }
            }else  if (requestCode == Values.SCAN_QR_REQUEST_CODE) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
                if(result != null) {
                    viewPagerAdapter.sendFragment.onReceiveQR(result);
                }else { //necessary?
                    // "This is important, otherwise the result will not be passed to the fragment"
                    super.onActivityResult(requestCode, resultCode, intent);
                }
            }
        }
    }






}
