package com.app.download;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;


public class MainActivity extends AppCompatActivity {
    private Snackbar snackBarConnection;
    String url="https://fileLink";
    String fileName="the file name";
    LinearLayout main;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main=findViewById(R.id.main);
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



        Button download=findViewById(R.id.download);

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                        downloadFile (fileName,url);
                    }else{
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},/*WRITE_PERMISSION*/1001);
                    }
                }else{
                    downloadFile (fileName,url);
                }
            }
        });


    }
    void downloadFile(String fileName,String url){
        Uri downloadUri=Uri.parse(url);
        DownloadManager manager=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        try{
            if (manager!=null){
                DownloadManager.Request request=new DownloadManager.Request(downloadUri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI| DownloadManager.Request.NETWORK_MOBILE)
                        .setTitle(fileName)
                        .setDescription("Downloading "+fileName)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName)
                        .setMimeType(getMimeType(downloadUri));
                manager.enqueue(request);
                Toast.makeText(this,"Download started",Toast.LENGTH_SHORT).show();
            }else{
                Intent intent=new Intent(Intent.ACTION_VIEW,downloadUri);
                startActivity(intent);
            }
        }catch (Exception e){
            Toast.makeText(this,"Something wrong",Toast.LENGTH_SHORT).show();
            Log.d("error",""+e.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1001){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                downloadFile (fileName,url);
            }else{
                Toast.makeText(getApplicationContext(),"Permission denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getMimeType(Uri uri){
        ContentResolver resolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
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
