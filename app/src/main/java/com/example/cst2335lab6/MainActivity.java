package com.example.cst2335lab6;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends Activity {
    private ImageView catImage;
    private ProgressBar progressBar;
    private CatImages catImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_main);

        catImage = findViewById(R.id.catImage);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(100);

        catImages = new CatImages();
        catImages.execute();
    }

    @Override
    protected void onDestroy() {
        if (catImages != null) {
            catImages.cancel(true);
        }
        super.onDestroy();
    }

    private class CatImages extends AsyncTask<String, Integer, String> {
        private Bitmap currentCat;
        private boolean newPicture;
        private String errorMessage;

        @Override
        protected String doInBackground(String... strings) {
            while (!isCancelled()) {
                HttpURLConnection connection = null;
                try {
                    URL jsonUrl = new URL("https://cataas.com/cat?json=true");
                    connection = (HttpURLConnection) jsonUrl.openConnection();
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    connection.setRequestMethod("GET");

                    String jsonText;
                    try (InputStream input = connection.getInputStream();
                         Scanner scanner = new Scanner(input, "UTF-8")) {
                        scanner.useDelimiter("\\A");
                        jsonText = scanner.hasNext() ? scanner.next() : "";
                    }

                    JSONObject json = new JSONObject(jsonText);
                    String id = json.getString("id");
                    File imageFile = new File(getFilesDir(), id + ".jpg");

                    if (imageFile.exists() && imageFile.length() > 0) {
                        currentCat = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    } else {
                        String imageUrl = json.optString("url");
                        if (imageUrl == null || imageUrl.isEmpty()) {
                            imageUrl = "/cat/" + id;
                        }
                        if (imageUrl.startsWith("/")) {
                            imageUrl = "https://cataas.com" + imageUrl;
                        } else if (!imageUrl.startsWith("http")) {
                            imageUrl = "https://cataas.com/cat/" + id;
                        }

                        HttpURLConnection imageConnection = (HttpURLConnection)
                                new URL(imageUrl).openConnection();
                        imageConnection.setConnectTimeout(15000);
                        imageConnection.setReadTimeout(20000);
                        try (InputStream imageInput = imageConnection.getInputStream()) {
                            currentCat = BitmapFactory.decodeStream(imageInput);
                        } finally {
                            imageConnection.disconnect();
                        }

                        if (currentCat != null) {
                            try (FileOutputStream output = new FileOutputStream(imageFile)) {
                                currentCat.compress(Bitmap.CompressFormat.JPEG, 90, output);
                            }
                        }
                    }

                    if (currentCat != null) {
                        newPicture = true;
                        publishProgress(0);
                        newPicture = false;
                        for (int i = 0; i < 100 && !isCancelled(); i++) {
                            publishProgress(i);
                            Thread.sleep(30);
                        }
                    }
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (currentCat != null && values[0] == 0) {
                catImage.setImageBitmap(currentCat);
            }
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            if (errorMessage != null) {
                Toast.makeText(MainActivity.this, "Unable to load cat pictures. Check your connection.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
