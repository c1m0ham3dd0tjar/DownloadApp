package com.app.download;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    private Snackbar snackBarConnection;

    LinearLayout main;
    Button clipboard;
    EditText urlEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clipboard = findViewById(R.id.clipboard);
        main = findViewById(R.id.main);
        urlEditText = findViewById(R.id.urlEditText);
        //catching download complete events from android download manager which broadcast message
        showSnackBarConnection();
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                    if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {

                        main.setVisibility(View.VISIBLE);


                        if (snackBarConnection.isShown())
                            snackBarConnection.dismiss();


                    } else if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {

                        snackBarConnection.show();
                        main.setVisibility(View.INVISIBLE);
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(broadcastReceiver, filter);


        clipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard.hasPrimaryClip()) {
                    android.content.ClipDescription description = clipboard.getPrimaryClipDescription();
                    android.content.ClipData data = clipboard.getPrimaryClip();
                    if (data != null && description != null && description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
                        urlEditText.setText(data.getItemAt(0).getText());
//                        Toast.makeText(getApplicationContext(), data.getItemAt(0).getText(), Toast.LENGTH_SHORT).show();
                }

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                    final android.content.ClipboardManager clipboardManager =
//                            (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                    final android.content.ClipData clipData = android.content.ClipData
//                            .newPlainText("text label", "text to clip");
//                    clipboardManager.setPrimaryClip(clipData);
//                } else {
//                    final android.text.ClipboardManager clipboardManager =  (ClipboardManager)
//                             getSystemService(Context.CLIPBOARD_SERVICE);
//                    clipboardManager.setText("text to clip");
//                }
            }
        });
        Button download = findViewById(R.id.download);

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!urlEditText.getText().toString().isEmpty() |
                        urlEditText.getText().toString().contains("https://")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
                    String currentDateandTime = sdf.format(new Date());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            downloadFile(currentDateandTime, urlEditText.getText().toString());

                        } else {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},/*WRITE_PERMISSION*/1001);
                        }
                    } else {
                        downloadFile(currentDateandTime, urlEditText.getText().toString());

                    }
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                            downloadFile(fileName, url);
//                        } else {
//                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},/*WRITE_PERMISSION*/1001);
//                        }
//                    } else {
//                        downloadFile(fileName, url);
//                    }
                }
            }
        });


    }

    //download
    void downloadFile(String fileName, String url) {
        Uri downloadUri = Uri.parse(url);
        DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        try {
            if (manager != null) {
                DownloadManager.Request request = new DownloadManager.Request(downloadUri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                        .setTitle(getString(R.string.download_intitle) + " " + fileName)
//                        .setDescription(" تحميل "+fileName)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName + ".mp4")
                        .setMimeType(getMimeType(downloadUri));
                manager.enqueue(request);
                Toast.makeText(this, getString(R.string.start_download), Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW, downloadUri);
                startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.not_downloaded), Toast.LENGTH_SHORT).show();
            Log.d("error", "" + e.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), getString(R.string.accept_permission), Toast.LENGTH_SHORT).show();
//                downloadFile (fileName,url);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.deny_permission), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getMimeType(Uri uri) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(uri));

    }

    void showSnackBarConnection() {
        snackBarConnection = Snackbar.make(main, "You are not connected", Snackbar.LENGTH_INDEFINITE);
        View mView = snackBarConnection.getView();
        TextView mTextView = mView.findViewById(com.google.android.material.R.id.snackbar_text);
        mTextView.setTextColor(getResources().getColor(R.color.colorAccent));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        } else {
            mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        }

    }
}
