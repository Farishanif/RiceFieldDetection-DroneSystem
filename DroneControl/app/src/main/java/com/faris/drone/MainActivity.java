package com.faris.drone;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText addrField;
    Button btnConnect,btnLocate,btnControl, btnPict;
    VideoView streamView;
    MediaController mediaController;
    String s="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        addrField = (EditText)findViewById(R.id.addr);
        btnConnect = (Button)findViewById(R.id.connect);
        btnLocate = (Button)findViewById(R.id.btn_location);
        btnPict = (Button)findViewById(R.id.btn_photo);
        btnControl = (Button)findViewById(R.id.btn_control);
        streamView = (VideoView)findViewById(R.id.streamview);

        btnConnect.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                s = addrField.getEditableText().toString();
                playStream(s);
            }});

        btnLocate.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,GpsActivity.class);
                startActivity(i);
            }
        });
        btnControl.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View view) {
                streamView.stopPlayback();
                Intent i = new Intent(MainActivity.this,ControlActivity.class);
                i.putExtra("field",s);
                startActivity(i);
            }
        });
        btnPict.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,PhotoActivity.class);
                i.putExtra("pict",true);
                startActivity(i);
            }
        });

    }

    private void playStream(String src){

        Uri UriSrc = Uri.parse(src);
        if(UriSrc == null){
            Toast.makeText(MainActivity.this,
                    "UriSrc == null", Toast.LENGTH_LONG).show();
        }else{

            streamView.setVideoURI(UriSrc);
            mediaController = new MediaController(this);
            streamView.setMediaController(mediaController);
            streamView.start();

            Toast.makeText(MainActivity.this,
                    "Connect: " + src,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        streamView.stopPlayback();
    }

    //considered code to use
//    Thread Streamthread = new Thread(new Runnable(){
//        @Override
//        public void run(){
//            //code to do the HTTP request
//            Uri UriSrc = Uri.parse(s);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (UriSrc == null) {
//                        Toast.makeText(MainActivity.this,
//                                "UriSrc == null", Toast.LENGTH_LONG).show();
//                    } else {
//
//                        streamView.setVideoURI(UriSrc);
//                        mediaController = new MediaController(MainActivity.this);
//                        streamView.setMediaController(mediaController);
//                        streamView.start();
//
//                        Toast.makeText(MainActivity.this,
//                                "Connect: " + s,
//                                Toast.LENGTH_LONG).show();
//                    }
//                }
//            });
//        }
//    });

//    class sendPhoto implements Runnable{
//        Handler handler = new Handler(Looper.getMainLooper());
//        public void run() {
//
//            //Background work here
//
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    //UI Thread work here
//                }
//            });
//        }
//    }


}



