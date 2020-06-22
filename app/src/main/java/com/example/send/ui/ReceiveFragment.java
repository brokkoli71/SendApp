package com.example.send.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
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

import net.glxn.qrgen.android.QRCode;

import static android.content.Context.WIFI_SERVICE;

public class ReceiveFragment extends Fragment {
    ViewPager viewPager;
    Dialog qrDialog;
    Context context;
    String receiveID;
    private TCPReceiver tcpReceiver;
    private ServerReceiver serverReceiver;

    Thread TCPReceiverThread;

    public ReceiveFragment(ViewPager viewPager) {
        this.viewPager = viewPager;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        context = container.getContext();
        View view = inflater.inflate(R.layout.fragment_receive, container, false);

        //req for permission if needed
        PermissionHandler.getPermissions(context, this);

        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        final String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        final ImageView arrow1 =  view.findViewById(R.id.arrow1);
        final ImageView imageView =  view.findViewById(R.id.imageView);
        final Button qrButton = view.findViewById(R.id.button_qr_generate);

        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_data_placeholder));

        int minWhitespace = getResources().getDimensionPixelSize(R.dimen.min_whitespace);
        int availableSpace = ImageHelper.getAvailableSpace(imageView, qrButton, minWhitespace);
        tcpReceiver = new TCPReceiver(context, imageView, availableSpace) {
            @Override
            public void runOnUiThread(Runnable runnable) {
                getActivity().runOnUiThread(runnable);
            }
        };

        TCPReceiverThread = new Thread(tcpReceiver);
        TCPReceiverThread.start();

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

        //temp todo
        final String qrContent = ip;
        qrButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                qrDialog.setContentView(R.layout.dialog_qr);
                qrDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                qrDialog.show();
                ImageView qrImageView = qrDialog.findViewById(R.id.qr_image_view);
                qrImageView.setImageBitmap(QRCode.from(qrContent).withSize(500, 500).bitmap());
            }
        });

        Button buttonReceiveID = view.findViewById(R.id.button_receive);
        buttonReceiveID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new InputDialog(context) {
                    @Override
                    public void onResult(String result) {
                        //todo: extract receiveID ("key") from QRCode result
                        receiveID = result;
                        Log.w("send_id", "key:" + receiveID);

                        int minWhitespace = getResources().getDimensionPixelSize(R.dimen.min_whitespace);
                        int availableSpace = ImageHelper.getAvailableSpace(imageView, qrButton, minWhitespace);
                        serverReceiver = new ServerReceiver(context, imageView, availableSpace);
                        serverReceiver.execute(receiveID);
                    }
                };
            }
        });

        return view;
    }



}
