package com.example.nikos.ocrapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAINACTIVITY";
    String serverAddress;
    Bitmap bitmap;
    String line;
    Button maps, google, messages, editText;
    private final int requestCode = 20;
    private boolean mapPressed = false, gSearchPressed = false, messagesPressed = false, editTextPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        serverAddress = getIntent().getExtras().getString("serverAddress");

        maps = findViewById(R.id.mapsButton);
        google = findViewById(R.id.googleSearchButton);
        messages = findViewById(R.id.messagesButton);
        editText = findViewById(R.id.editTextButton);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextPressed = true;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, requestCode);
            }
        });

        maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapPressed = true;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, requestCode);
            }
        });

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gSearchPressed = true;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, requestCode);
            }
        });

        messages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messagesPressed = true;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, requestCode);
            }
        });
    }

    private void googleSearch(){

        if(mapPressed == true){
            String url = "http://maps.google.co.in/maps?q=" + line;
            Intent gSearchIntent = new Intent(Intent.ACTION_VIEW);
            gSearchIntent.setData(Uri.parse(url));
            startActivity(gSearchIntent);
            mapPressed = false;
        }

        if(gSearchPressed == true){
            String url = "http://www.google.com/search?q=" + line;
            Intent gSearchIntent = new Intent(Intent.ACTION_VIEW);
            gSearchIntent.setData(Uri.parse(url));
            startActivity(gSearchIntent);
            gSearchPressed = false;
        }

        if(messagesPressed == true){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.putExtra("sms_body", line);
            intent.setData(Uri.parse("sms:"));
            startActivity(intent);
            messagesPressed = false;
        }

        if(editTextPressed == true){
            Intent intent = new Intent(MainActivity.this, EditTextActivity.class);
            intent.putExtra("text", line);
            startActivity(intent);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestCode && resultCode == RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");
            new SendPostRequest().execute();

        }
    }

    public class SendPostRequest extends AsyncTask<String, Void, String> {

        URL url;
        String encodedImage;
        JSONObject postDataParams;
        HttpURLConnection conn;
        OutputStream os;
        BufferedWriter writer;


        //protected void onPreExecute() {}

        protected String doInBackground(String... arg0) {

            try {

                url = new URL("http://" + serverAddress + ":5000/upload"); // here is your URL path
                encodedImage = encodeImage(bitmap);
                //JSONObject postDataParams = new JSONObject("{\"image\":\"" + encodedImage + "\"}");
                postDataParams = new JSONObject();
                postDataParams.put("image", encodedImage);
                Log.e("params", postDataParams.toString());

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 );
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                os = conn.getOutputStream();
                writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(postDataParams.toString());

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    line = "";

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return line;

                } else {
                    return new String("false : " + responseCode );
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), line,
                    Toast.LENGTH_LONG).show();
            googleSearch();
        }

        private String encodeImage(Bitmap bitmap){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            return encodedImage;
        }
    }
}
