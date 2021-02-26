package com.example.tp2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("JFL", "Hello");

        Button authenticate = (Button)findViewById(R.id.authenticate);
        authenticate.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String login = ((EditText)findViewById(R.id.login)).getText().toString();
                String password = ((EditText)findViewById(R.id.password)).getText().toString();
                MyConnection connection = new MyConnection(login + ":" + password);
                connection.start();
            }
        });
    }

    class MyConnection extends Thread{
        private String credential;
        public MyConnection(String credential){
            this.credential = credential;
        }
        public void run(){
            handleConnection();
        }

        public void handleConnection(){
            URL url = null;
            try {
                //url = new URL("https://www.android.com/");
                url = new URL("https://httpbin.org/basic-auth/bob/sympa");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                String basicAuth = "Basic " + Base64.encodeToString(credential.getBytes(),
                        Base64.NO_WRAP);
                urlConnection.setRequestProperty("Authorization", basicAuth);
                int code = -1;
                try {
                    code = urlConnection.getResponseCode();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    String s = readStream(in);
                    JSONObject json = new JSONObject(s);
                    Object res = json.get("authenticated");
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            TextView result = (TextView)findViewById(R.id.result);
                            if((boolean)res){
                                result.setText(s);
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                    Log.i("JFL", "Handle done - "+code);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String readStream(InputStream is) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            try {
                for (String line = reader.readLine(); line != null; line =reader.readLine()){
                    sb.append(line);
                }
            } catch (IOException e) {
                Log.i("JFL", "IOException", e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.i("JFL", "IOException", e);
                }
            }
            return sb.toString();
        }
    }
}