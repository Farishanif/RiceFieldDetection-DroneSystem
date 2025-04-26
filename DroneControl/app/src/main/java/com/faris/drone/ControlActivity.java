package com.faris.drone;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ControlActivity extends AppCompatActivity implements OnMapReadyCallback {
    VideoView streamView;
    MediaController mediaController;
    String s="";
    String ip = "192.168.1.14";
    GoogleMap mMap;
    LatLng posisi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        streamView = (VideoView) findViewById(R.id.streamview2);
        //Menetapkan posisi orientasi menjadi Portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            s = extras.getString("field");
            Thread myThread = new Thread(new ControlActivity.MyClient());
            //The key argument here must match that used in the other activity
            //Toast.makeText(this,
            //        s, Toast.LENGTH_LONG).show();
            if (s != ""){
            playStream(s);}
            myThread.start();

        }

    }

    private void playStream(String src) {
        Uri UriSrc = Uri.parse(src);
        if (UriSrc == null) {
            Toast.makeText(ControlActivity.this,
                    "UriSrc == null", Toast.LENGTH_LONG).show();
        } else {
            streamView.setVideoURI(UriSrc);
            mediaController = new MediaController(this);
            streamView.setMediaController(mediaController);
            streamView.start();
            if(src!= null || src!= ""){
            Toast.makeText(ControlActivity.this,
                    "Connect: " + src,
                    Toast.LENGTH_LONG).show();}
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        streamView.stopPlayback();
    }

    public String[] prosesData(String data) {
        String[] datas = data.split(",");
        return datas;
    }

    class MyClient implements Runnable {
        StringBuilder buffer = null;

        Handler handler = new Handler(Looper.getMainLooper());

        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(ip);
                Socket socket = new Socket(serverAddr, 9000);
                DataOutputStream dataOutput = null;
                DataInputStream dataInput = null;
                try {
                    dataOutput = new DataOutputStream(socket.getOutputStream());
                    dataInput = new DataInputStream(socket.getInputStream());
//
//                    byte[] buf = new byte[BUF_SIZE];
//                    int dataLen;
//                    while ((dataLen = dataInput.read(buf)) != -1) {
                    dataOutput.write("Take Data Position".getBytes());
                    dataOutput.flush();
//                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = reader.readLine();
                    buffer = new StringBuilder();
                    buffer.append(reader.readLine());
                    Log.d("Socket", message);
                    Log.d("socket", "Inside thread : buffer == " + buffer);

                    handler.post(() -> {
                        String[] data = prosesData(message);
                        posisi = new LatLng(Integer.parseInt(data[0]), Integer.parseInt(data[1]));

//                        mMap.addMarker(new MarkerOptions().position(posisi).title("Posisi Saya"));//null?
//                        mMap.moveCamera(CameraUpdateFactory.newLatLng(posisi));
                        //Toast.makeText(getApplicationContext(), "Data Recieved" + message, Toast.LENGTH_SHORT).show();
                    });

                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsString = sw.toString();
                    Log.e("StackTrace", exceptionAsString);
                } finally {
                    try {
                        if (dataInput != null)
                            dataInput.close();
                        if (dataOutput != null)
                            dataOutput.close();
                        if (socket != null)
                            socket.close();
                    } catch (IOException e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        String exceptionAsString = sw.toString();
                        Log.e("StackTrace", exceptionAsString);
                    }
                }
            } catch (IOException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                Log.e("StackTrace", exceptionAsString);
            }

        }
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(posisi).title("Marker in Sydney"));
        if (posisi != null) {
            mMap.addMarker(new MarkerOptions().position(posisi).title("Posisi Saya"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(posisi));
        } else {
            //Toast.makeText(GpsActivity.this, "Posisi null, set up default", Toast.LENGTH_SHORT).show();
            posisi = new LatLng(-6.368473, 106.824644);
            //Toast.makeText(this, "PNJ", Toast.LENGTH_SHORT).show();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(posisi));
    }
}
