package com.example.send.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.send.R;
import com.example.send.receiver.ReceivedDataHandler;
import com.example.send.sender.SendingTaskData;
import com.example.send.sender.ServerSender;
import com.example.send.sender.TCPInitiator;
import com.example.send.sender.TCPSender;
import com.example.send.utils.ImageHelper;
import com.example.send.utils.PermissionHandler;
import com.example.send.utils.QRHandler;
import com.example.send.utils.Values;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import pub.devrel.easypermissions.EasyPermissions;

import static android.content.Context.WIFI_SERVICE;

public class SendFragment extends Fragment {
    Context context;
    ViewPager viewPager;
    String sendID;
    private SendingTaskData sendingTaskData;
    View view;
    ImageView imageView;
    Button buttonQR;
    TextView imageViewText;
    String ip;

    public SendFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        this.context = container.getContext();
        view = inflater.inflate(R.layout.fragment_send, container, false);

        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        imageView = view.findViewById(R.id.imageView2);
        imageViewText = view.findViewById(R.id.imageViewText);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewText.setVisibility(View.GONE);
                PermissionHandler.getPermissions(context, SendFragment.this);

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                SendFragment.this.getActivity().startActivityForResult(intent, Values.PICKFILE_REQUEST_CODE);

                Log.w("pick_file", "request send:"+ Values.PICKFILE_REQUEST_CODE);
            }
        });

        buttonQR = view.findViewById(R.id.button_send_qr);
        buttonQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View e) {
                IntentIntegrator integrator = new IntentIntegrator(SendFragment.this.getActivity());
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
                integrator.setOrientationLocked(true);
                integrator.setRequestCode(Values.SCAN_QR_REQUEST_CODE);
                Log.w("scan", "send scan req" );
            }
        });

        Button buttonSendID = view.findViewById(R.id.button_send_id);
        buttonSendID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new InputDialog(context) {
                    @Override
                    public void onResult(String result) {
                        sendID = result;
                        Log.w("send_id", "key:" + sendID);
                        serverSend(sendID);
                    }
                };
            }
        });
        return view;
    }

    void onReceiveQR(IntentResult result){
        if(result.getContents() == null) {
            Log.e("Scan", "Cancelled scan");
        } else {
            String content = result.getContents();
            Log.w("Scan", "Scanned: "+ content);
            Toast.makeText(context, "Scanned: " + content, Toast.LENGTH_LONG).show();

            QRHandler qrHandler = new QRHandler(content);

            if (!ip.equals("0.0.0.0")){
                String message = getString(Values.APP_ID_KEY) +
                        "?" + Values.SEND_REQ_KEY +
                        "?" + ip;
                new TCPInitiator(qrHandler, sendingTaskData, context).execute(message);
                return;
            }
            Log.w("send", "no wifi available");
            serverSend(qrHandler.getServerCommunicationKey());

        }
    }



    void serverSend(String key){
        if (sendingTaskData==null)
            return;
        ServerSender serverSender = new ServerSender(context, key);
        serverSender.execute(sendingTaskData);
        Toast.makeText(context, "sending "+sendingTaskData.getBytes()+" Bytes to Server", Toast.LENGTH_SHORT).show();
    }



    void gotIntentActionSend(Intent intent, final ContentResolver contentResolver){
        imageViewText.setVisibility(View.GONE);

        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        final String type = intent.getType();
        if (uri!=null){
            sendingTaskData = new SendingTaskData(uri, contentResolver);
            Log.w("receive_from_app", "received \""+uri+"\"");
            setIconInImageView(type, contentResolver);
        }else{
            Log.e("receive_from_app", "uri not readable");
        }
    }
    void onReceivePickfileRequest(Intent intent, ContentResolver contentResolver){
        Uri uri = intent.getData();

        sendingTaskData = new SendingTaskData(uri, contentResolver);
        setIconInImageView(sendingTaskData.getMime(), contentResolver);
    }



    void setIconInImageView (String type, ContentResolver contentResolver){
        try {
            Log.w("set_img", "img type is "+type);
            if (type.startsWith("image/")){
                Bitmap bitmap = ReceivedDataHandler.readPictureFromFileUri(sendingTaskData.getSelectedFileUri(),contentResolver);

                int minWhitespace = getResources().getDimensionPixelSize(R.dimen.min_whitespace);
                int availableSpace = ImageHelper.getAvailableSpace(imageView, buttonQR, minWhitespace);
                ImageHelper.setPictureInImageView(bitmap, imageView, availableSpace, getResources());
            }
            else{

                imageView.setBackground(getResources().getDrawable(R.drawable.image_view));
                int imgId;
                if (type.startsWith("audio/"))
                    imgId = Values.AUDIO_IMAGE;
                else if (type.startsWith("video/"))
                    imgId = Values.VIDEO_IMAGE;
                else if (type.startsWith("text/"))
                    imgId = Values.TEXT_IMAGE;
                else
                    imgId = Values.DEFAULT_IMAGE;
                imageView.setImageDrawable(getResources().getDrawable(imgId));
            }
            //if other type..
        }catch (NullPointerException e){
            e.printStackTrace();
            imageView.setImageDrawable(getResources().getDrawable(Values.DEFAULT_IMAGE));
        }
    }
}

