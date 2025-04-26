package com.faris.drone;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

//import com.faris.drone.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GpsActivity extends FragmentActivity implements OnMapReadyCallback {

    //public static final int FASTEST_INTERVAL = 5;
    //public static final int DEFAULT_INTERVAL = 30;
    //private static final int PERMISSIONS_FINE_LOCATION = 25;
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_updates, tv_address;
    Button btn_back;
    SharedPreferences sharedPreferences;
    Switch sw_gps;

    //Variabel yang memberitahukan apakah lokasi sedang direkam atau tidak
    boolean updateOn = false;
    String ip = "192.168.1.14";

    private GoogleMap mMap;
    //private ActivityMapsBinding binding;
    public static LatLng posisi = null;
    String message;

    //lokasi saat ini
    Location currentLocation;
    //daftar lokasi tersimpan
//    List<Location> markedLocations;

    //Google API untuk lokasi, kebanyakan fitur lokasi di app ini bergantung pada class ini (dependen)
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);

        btn_back = findViewById(R.id.btn_back);

        sw_gps = findViewById(R.id.sw_gps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(GpsActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread myThread = new Thread(new MyClient());
                //Client client = new Client(ipaddress, portnum);
                if (sw_gps.isChecked()) {

                    try {
                        //status = "on";
                        //tv_updates.setText("on");
                        //menggunakan GPS untuk hasil akurat(ganti dengan gps raspberry diakses dengan socket)
                        Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();
                        myThread.start();
                        //tv_address.setText(message);
//                        ipaddress = ip;
//                        if (!checkIP(ipaddress))
//                            throw new UnknownHostException(port + "is not a valid IP address");
//                        portnum = Integer.parseInt(port);
//                        if (portnum > 65535 && portnum < 0)
//                            throw new UnknownHostException(port + "is not a valid port number ");
//
//                        client.start();


//                    } catch (UnknownHostException e) {
//                        showErrorsMessages("Please enter a valid IP !! ");
                    } catch (NumberFormatException e) {
                        showErrorsMessages("Please enter valid port number !! ");
                    }


                } else {
                    //status = "off";
                    Toast.makeText(getApplicationContext(), "Disconnecting...", Toast.LENGTH_SHORT).show();
                    tv_updates.setText("off");
                    tv_lat.setText("N/A");
                    tv_lon.setText("N/A");
                    tv_address.setText("N/A");
                    tv_speed.setText("N/A");
                    tv_accuracy.setText("N/A");
                    tv_altitude.setText("N/A");
                    //myThread.interrupt();
                    //myThread = null;
                    //client.interrupt();
                    //client= null;
                }
            }
        });

    } //akhir method onCreate

    void showErrorsMessages(String error) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(GpsActivity.this);
        dialog.setTitle("Error!! ").setMessage(error).setNeutralButton("OK", null).create().show();
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

    public String[] prosesData(String data){
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
                    message = reader.readLine();
                    buffer = new StringBuilder();
                    buffer.append(reader.readLine());
                    Log.d("Socket", message);
                    Log.d("socket", "Inside thread : buffer == "+buffer);

                    handler.post(() -> {
                        tv_updates.setText("on");
                        String[] data = prosesData(message);
                        tv_lat.setText(data[0]);
                        tv_lon.setText(data[1]);
                        tv_address.setText("N/A");
                        tv_speed.setText("N/A");
                        tv_accuracy.setText("N/A");
                        tv_altitude.setText("N/A");
                        tv_address.setText(message);
                        posisi = new LatLng(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
                        mMap.addMarker(new MarkerOptions().position(posisi).title("Posisi Saya"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(posisi));
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

//        public void run() {
//            try {
//                ss = new InetSocketAddress(ip, 9001);
//                mySocket = new Socket();
//                mySocket.connect(ss);
//                BufferedReader stdIn = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
//                pw = new PrintWriter(mySocket.getOutputStream());
//                while (true) {
//                    /////
////                    DataOutputStream DOS = new DataOutputStream(mySocket.getOutputStream());
////                    DOS.writeUTF("posting data!");
//
//                    pw.write("Client requesting data");
//                    pw.flush();
//                    pw.close();
//                    /////
//
//                    //PrintWriter out = new PrintWriter(mySocket.getOutputStream(), true);
//                    //while(true){
//                    System.out.println("Trying to read...");
//                    Log.d("Socket", stdIn.readLine());
//                    message = stdIn.readLine();
//                    System.out.println("the message is " + message);
//                    // testing code
////                        pw.print("Try"+"\r\n");
////                        pw.flush();
//                    //System.out.println("Message sent");
//                    //}
//                    //dis = new DataInputStream(mySocket.getInputStream());
////                    BufferedInputStream data =
////                            new BufferedInputStream(mySocket.getInputStream());
////                    byte[] contents = new byte[1024];
////                    int bytesRead = 0;
////                    String strFileContents = null;
////                    while((bytesRead = data.read(contents)) != -1) {
////                        strFileContents += new String(contents, 0, bytesRead);
////                    }
////                    message = strFileContents;
//                    //message = dis.readUTF();//nantinya posisi
//                    //if(message != null){
////                    pw = new PrintWriter(mySocket.getOutputStream());
////                    pw.write("Data sended");
////                    pw.flush();
////                    pw.close();
////                        DataOutputStream DOS = new DataOutputStream(mySocket.getOutputStream());
////                        DOS.writeUTF("Data Accepted!");
//                    //}
//                    handler.post(new Runnable() {
//                        public void run() {
//                            tv_updates.setText("on");
//                            tv_address.setText(message);
//                            Toast.makeText(getApplicationContext(), "Data Recieved" + message, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

//        @Override

    //not integrated yet
    //fungsi ini dipisah seandainya mau dipakai lagi di tempat lain, sebenarnya bisa diletakkan di updateUIValues di dalam if di atas.
    private void updateUIValues(Location location) {
        //update semua data textview dengan data nilai lokasi terbaru
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        //beberapa hp memiliki keterbatasan dalam mendapatkan data GPS, menggunakan blok berikut hal tersebut dicek
        if (location.hasAltitude()) {
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        } else {
            tv_altitude.setText("Tidak Tersedia");
        }

        if (location.hasSpeed()) {
            tv_speed.setText(String.valueOf(location.getSpeed()));
        } else {
            tv_speed.setText("Tidak Tersedia");
        }
        //FITUR TAMBAHAN MENGGUNAKAN GEOCODER UNTUK TRANSLASI KOORDINAT KE ALAMAT
        Geocoder geocoder = new Geocoder(GpsActivity.this);
        //seandainya tidak bekerja, digunakan try-catch
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(addresses.get(0).getAddressLine(0)); //Address line berarti jalan, sebenarnya bisa yang lain seperti kode pos atau apa saja yang disediakan Geocoder
        } catch (Exception e) {
            tv_address.setText("Gagal menemukan alamat");
        }

    }
}
// unused variable
//    private String ip = "192.168.1.14";
//    private String port = "9090";
//    private ObjectInputStream in;
//    private ObjectOutputStream out;
//    private Socket socket;
//    private String ipaddress;
//    private int portnum;
//    private TextView link;
//    private Pattern pattern;
//    private Matcher matcher;
//    private Handler handler;
//    private static final String IPADDRESS_PATTERN =
//            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
//                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
//                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
//                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    //considered code to use
//
    /////other function
//    public boolean checkIP(final String ip) {
//        matcher = pattern.matcher(ip);
//        return matcher.matches();
//    }
//
//    private class Client extends Thread {
//        private String ipaddress;
//        private int portnum;
//
//        public Client(String ipaddress, int portnum) {
//            this.ipaddress = ipaddress;
//            this.portnum = portnum;
//        }
//
//        @Override
//        public void run() {
//            super.run();
//            connectToServer(ipaddress, portnum);
//
//        }
////android client socket
//
//        public void connectToServer(String ip, int port) {
//
//            try {
//                socket = new Socket(InetAddress.getByName(ip), port);
//                out = new ObjectOutputStream(socket.getOutputStream());
//                out.flush();
//                in = new ObjectInputStream(socket.getInputStream());
//                handler.post(new Runnable() {
//                    public void run() {
//                        //connect.setText("Close");
//                        //changeSwitchesSatte(true);
//                    }
//                });
//            } catch (IOException e) {
//                e.printStackTrace();
//                handler.post(new Runnable() {
//                    public void run() {
//                        showErrorsMessages("Unkown host!!");
//                    }
//                });
//            }
//
//        }
//
//    }


//    public void run(){
//        try {
//            ss = new InetSocketAddress(ip, 9090);
//            while(true){
//                mySocket = new Socket();
//                mySocket.connect(ss);
//                dis = new DataInputStream(mySocket.getInputStream());
//                message = dis.readUTF();//nantinya alamat
//                handler.post(new Runnable(){
//                    public void run(){
//                        tv_updates.setText("on");
//
//                        Toast.makeText(getApplicationContext(),"Data Recieved" + message,Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
//        public void run(){
//            try {
//                ss = new ServerSocket(9090);
//                while(true){
//                    mySocket = ss.accept();
//                    dis = new DataInputStream(mySocket.getInputStream());
//                    message = dis.readUTF();//nantinya alamat
//                    if(message != null){
//                        DataOutputStream DOS = new DataOutputStream(mySocket.getOutputStream());
//                        DOS.writeUTF("Data Accepted!");
//                    }
//                    handler.post(new Runnable(){
//                        public void run(){
//                            tv_updates.setText("on");
//
//                            Toast.makeText(getApplicationContext(),"Data Recieved" + message,Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
