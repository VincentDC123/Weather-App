package com.example.webservices;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RemoteDataReader {
    private String urlString;

    public RemoteDataReader(String baseURL, String cityString, String keyName, String key) {
        urlString = baseURL + cityString + keyName + key;
    }

    public String getData() {
        try {
            // Establish the connection
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();
            int code = con.getResponseCode();
            if(code != 200) {
                Log.w("MainActivity", String.valueOf(code));
                throw new IOException("Invalid response from server:" + code);
            }

            // Get the input stream and prepare to read
            InputStream is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            // Read the data
            String dataRead = new String( );
            String line = br.readLine( );
            while ( line != null ) {
                dataRead += line;
                line = br.readLine( );
            }

            is.close();
            con.disconnect();
            return dataRead;
        } catch (Exception e) {
            return "" + e;
        }
    }
}
