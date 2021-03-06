package com.example.send.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.send.R;
import com.example.send.receiver.ServerReceiver;
import com.example.send.receiver.TCPReceiver;
import com.example.send.utils.ImageHelper;
import com.example.send.utils.PermissionHandler;
import com.example.send.utils.QRHandler;
import com.example.send.utils.Values;

import net.glxn.qrgen.android.QRCode;

import static android.content.Context.WIFI_SERVICE;

public class ReceiveFragment extends Fragment {
    ViewPager viewPager;
    Dialog qrDialog;
    Context context;
    private TCPReceiver tcpReceiver;
    private ServerReceiver serverReceiver;

    boolean tcpIsRunning = false;

    Thread TCPReceiverThread;

    QRHandler qrHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        context = container.getContext();
        View view = inflater.inflate(R.layout.fragment_receive, container, false);

        viewPager = getActivity().findViewById(R.id.pager);
        //req for permission if needed
        PermissionHandler.getPermissions(context, this);

        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        final String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        final ImageView arrow1 =  view.findViewById(R.id.arrow1);
        final ImageView imageView =  view.findViewById(R.id.imageView);
        final Button qrButton = view.findViewById(R.id.button_qr_generate);

        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_data_placeholder));



        final ArrowIndicatorAnimator arrowIndicatorAnimator = new ArrowIndicatorAnimator(arrow1) {
            @Override
            void onClickArrow() {
                viewPager.setCurrentItem(1,true);
            }
        };

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffset > 0.5){
                    arrowIndicatorAnimator.cancel();
                    arrow1.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        qrDialog = new Dialog(container.getContext());

        qrHandler = new QRHandler(getResources());

        int minWhitespace = getResources().getDimensionPixelSize(R.dimen.min_whitespace);
        final int availableSpace = ImageHelper.getAvailableSpace(imageView, qrButton, minWhitespace);

        qrButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                qrDialog.setContentView(R.layout.dialog_qr);
                qrDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                qrDialog.show();
                ImageView qrImageView = qrDialog.findViewById(R.id.qr_image_view);
                qrImageView.setImageBitmap(qrHandler.getQRCode(ip));

                final Handler handler = new Handler();
                final int delay = ServerReceiver.CHECK_RESULT_TIMEOUT;

                Log.w("server_receiver", "start");
                handler.postDelayed(new Runnable(){
                    public void run(){
                        serverReceiver = new ServerReceiver(context, imageView, availableSpace, qrHandler.getServerCommunicationKey(), qrDialog);
                        serverReceiver.execute();

                        //workaround for checking if got result
                        if (qrDialog.isShowing()){
                            handler.postDelayed(this, delay);
                        }
                    }
                }, delay);

                if (!tcpIsRunning && qrHandler.isIncludedTCP()){
                    tcpIsRunning = true;
                    tcpReceiver = new TCPReceiver(context, imageView, availableSpace) {
                        @Override
                        public void runOnUiThread(Runnable runnable) {
                            getActivity().runOnUiThread(runnable);
                        }

                        @Override
                        public void onReceiving() {
                            serverReceiver.cancel(false);
                            qrDialog.dismiss();
                            tcpIsRunning = false;
                        }
                    };

                    TCPReceiverThread = new Thread(tcpReceiver);
                    TCPReceiverThread.start();
                }
            }
        });

        Button buttonReceiveID = view.findViewById(R.id.button_receive);
        buttonReceiveID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new InputDialog(context) {
                    @Override
                    public void onResult(String receiveID) {
                        Log.w("send_id", "key:" + receiveID);

                        serverReceiver = new ServerReceiver(context, imageView, availableSpace, receiveID, true);
                        serverReceiver.execute();
                    }
                };
            }
        });
        return view;
    }
}
