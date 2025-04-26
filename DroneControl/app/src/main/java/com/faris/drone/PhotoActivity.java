package com.faris.drone;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static com.google.android.gms.common.util.Base64Utils.decode;

public class PhotoActivity extends Activity {

    private String photo;
    private boolean pict;
    String ip = "192.168.1.14";
    Button btnBack;
    ImageView img;
    TextView label;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        btnBack = (Button)findViewById(R.id.btn_back2);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            pict = extras.getBoolean("pict");
            if(pict){
                Toast.makeText(getApplicationContext(), "Processing Image...", Toast.LENGTH_LONG).show();
                Thread myThread = new Thread(new PhotoActivity.getPict());
                myThread.start();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                myThread.interrupt();
            }

        }
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PhotoActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
//        img.setImageResource(R.drawable.my_image);
//        ImageView img = new ImageView(this);
//        img.setImageResource(photo);
    }


    public String[] prosesData(String data){
        String[] datas = data.split(",");
        return datas;
    }

    public Bitmap StringToBitMap(String image){
        try{
            byte [] encodeByte=Base64.decode(image, Base64.DEFAULT);

            InputStream inputStream  = new ByteArrayInputStream(encodeByte);
            Bitmap bitmap  = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }

    //////////under construct////////////////
    class getPict implements Runnable {

        StringBuilder buffer = null;
        InputStream data;
        Bitmap bitmap;
        Handler handler = new Handler(Looper.getMainLooper());

        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(ip);
                Socket socket = new Socket(serverAddr, 9001);
                DataOutputStream dataOutput = null;
                DataInputStream dataInput = null;
                try {
                    dataOutput = new DataOutputStream(socket.getOutputStream());
                    dataInput = new DataInputStream(socket.getInputStream());
//
//                  byte[] buf = new byte[BUF_SIZE];
//                  int dataLen;
//                  while ((dataLen = dataInput.read(buf)) != -1) {
                    dataOutput.write("Take a Pict".getBytes());
                    dataOutput.flush();
//                  }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String massage = reader.readLine();
                    String[] data = prosesData(massage);
                    Log.d("img",data[0]);
                    Log.d("label",data[1]);
                    Charset charset = Charset.forName("ASCII");
                    byte[] encStringImg = android.util.Base64.decode(data[0].getBytes(charset), Base64.DEFAULT);
                    //Log.d((byte[]))
                    //String image = encStringImg;
                    String labels = data[1];
                    bitmap = StringToBitMap(data[0]);

                    //bitmap = BitmapFactory.decodeByteArray(encStringImg, 0, encStringImg.length);//StringToBitMap(image);
                    //String labels = "Ricefield";
                    //bitmap = BitmapFactory.decodeStream(socket.getInputStream());
                    //byte[] bimg = ;
                    //Bitmap bmp = BitmapFactory.decodeByteArray(bimg, 0, byteArray.length);

                    handler.post(() -> {
                        //Toast.makeText(getApplicationContext(), "Data Recieved" + message, Toast.LENGTH_SHORT).show();
                        img= (ImageView) findViewById(R.id.image);
                        label= (TextView) findViewById(R.id.label);
                        label.setText(labels);
                        img.setImageBitmap(bitmap);
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

//    public Image getImageFromArray(int[] pixels, int width, int height) {
//        MemoryImageSource mis = new MemoryImageSource(width, height, pixels, 0, width);
//        Toolkit tk = Toolkit.getDefaultToolkit();
//        return tk.createImage(mis);
//    }
//import java.io.BufferedReader;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.util.EntityUtils;
//
//import android.app.Activity;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.util.Base64;
//import android.util.Log;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//public class MainActivity extends Activity implements OnClickListener {
//
//    TextView tvIsConnected;
//    EditText etName,etCountry,etTwitter;
//    Button btnPost;
//    static TextView tvp;
//    String disp= "Zooooooooooo";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // get reference to the views
//        tvIsConnected = (TextView) findViewById(R.id.tvIsConnected);
//        tvp = (TextView) findViewById(R.id.tv);
//        btnPost = (Button) findViewById(R.id.btnPost);
//
//        // check if you are connected or not
//        if(isConnected()){
//            tvIsConnected.setBackgroundColor(0xFF00CC00);
//            tvIsConnected.setText("You are conncted");
//        }
//        else{
//            tvIsConnected.setText("You are NOT conncted");
//        }
//
//        // add click listener to Button "POST"
//        btnPost.setOnClickListener(this);
//
//    }
//
//    public static String POST(String url)
//    {
//        Log.i("MINION", "inside POST()");
//
//        InputStream inputStream = null;
//        String result = "";
//        byte[] buffer;
//        int bufferSize = 1 * 1024 * 1024;
//        Bitmap bm;
//        String imagePath = "/mnt/sdcard/BusinessCard/image.jpg";//diubah ke penyimpanan gambar
//        String encodedImage = null;
//        File file = new File(imagePath);
//        String temp = "Vipul Sharma";
//        int responseCode=0;
//        String responseMessage = "";
//        try {
//
//            Log.i("MINION", "inside try block");
//
//            // 1. create HttpClient
//            HttpClient httpclient = new DefaultHttpClient();
//
//            // 2. make POST request to the given URL
//            HttpPost httpPost = new HttpPost(url);
//
//
//            //Check if file exists
//            if (!file.isFile()) {
//                Log.e("uploadFile", "Source File not exist :"+imagePath);
//            }
//
//            else
//            {
//                Log.i("MINION", "image file found");
//                FileInputStream fileInputStream = new FileInputStream(file);
//                buffer = new byte[bufferSize];
//                bm = BitmapFactory.decodeStream(fileInputStream);
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                buffer = baos.toByteArray();	//Image->Byte Array
//
//
//                //Byte Array to base64 image string
//                encodedImage = Base64.encodeToString(buffer, Base64.DEFAULT);
//                Log.i("MINION", "image conversted to Base 64 string");
//
//                //Converting encodedImage to String Entity
//                //StringEntity se = new StringEntity(encodedImage);
//                StringEntity se = new StringEntity(temp);
//                Log.i("MINION", "encodedImage to StringEntity");
//
//                //httpPost Entity
//                httpPost.setEntity(se);
//                // 7. Set some headers to inform server about the type of the content
//                httpPost.setHeader("Accept", "application/json");
//                httpPost.setHeader("Content-type", "application/json");
//
//                // 8. Execute POST request to the given URL
//                HttpResponse httpResponse = httpclient.execute(httpPost);
//                Log.i("MINION", "Post request successful");
//
//                // 9. receive response as inputStream
//
//
//
//
//                //inputStream = httpResponse.getEntity().getContent();
//
//
//                HttpEntity entity = httpResponse.getEntity();
//
//                String responseText = EntityUtils.toString(entity);
//
//                responseCode = httpResponse.getStatusLine().getStatusCode();
//                System.out.println("Response Code: " + responseCode);
//
//                //responseMessage = EntityUtils.toString(httpResponse.getEntity());
//                System.out.println("Response Message: " + responseText);
//
//
//	           /* // 10. convert inputstream to string
//	            if(inputStream != null)
//	            {
//	            	Log.i("MINION", "Converting Response to String");
//	            	result = convertInputStreamToString(inputStream);
//	            	Log.i("MINION", "Response to String Sucess");
//	            }
//	            else
//	            	result = "Did not work!";
//
//	            */
//            }
//
//
//        } catch (Exception e) {
//            Log.d("InputStream", e.toString());
//        }
//
//        // 11. return result
//        result = "Response Code: " + responseCode + "Response Message: " + responseMessage + result;
//        System.out.println("Result: " + result);
//        return result;
//    }
//
//    public boolean isConnected(){
//        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        if (networkInfo != null && networkInfo.isConnected())
//            return true;
//        else
//            return false;
//    }
//    @Override
//    public void onClick(View view) {
//
//        switch(view.getId()){
//            case R.id.btnPost:
//                // call AsynTask to perform network operation on separate thread
//                new HttpAsyncTask().execute("http://businesscardreader.cloudapp.net");
//                break;
//        }
//
//    }
//    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
//        @Override
//        protected String doInBackground(String... urls) {
//
//
//            return POST(urls[0]);
//        }
//        // onPostExecute displays the results of the AsyncTask.
//        @Override
//        protected void onPostExecute(String result) {
//            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
//        }
//    }
//
//
//    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
//        Log.i("MINION", "Inside convertINputStreamToString");
//        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
//        String line = "";
//        String result = "";
//        while((line = bufferedReader.readLine()) != null)
//            result += line;
//
//        inputStream.close();
//        return result;
//
//    }
//}


}
