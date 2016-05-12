package com.posn.commentpropagation;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.posn.commentpropagation.clouds.CloudProvider;
import com.posn.commentpropagation.clouds.Dropbox.DropboxClientUsage;
import com.posn.commentpropagation.clouds.utility.DeviceFileManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
   {
      // interface variables
      Button connectDropboxButton;
      Button startTestsButton;
      TextView dropboxStatusText;
      TextView testStatusText;

      private CloudProvider cloud = null;

      @Override
      protected void onCreate(Bundle savedInstanceState)
         {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // get buttons from interface
            connectDropboxButton = (Button) findViewById(R.id.connect_button);
            startTestsButton = (Button) findViewById(R.id.start_tests_button);
            connectDropboxButton.setOnClickListener(this);
            startTestsButton.setOnClickListener(this);

            // get textviews from interface
            dropboxStatusText = (TextView) findViewById(R.id.dropbox_status_text);
            testStatusText = (TextView) findViewById(R.id.test_status_text);

            // create directories on device
            createDeviceStorageDirectories();
         }

      @Override
      public void onResume()
         {
            super.onResume();

            if(cloud != null)
               {
                  cloud.onResume();
               }
         }

      @Override
      public void onClick(View v)
         {
            switch (v.getId())
               {
                  case R.id.connect_button:
System.out.println("here!!!");
                     // Connect with Dropbox account
                     cloud = new DropboxClientUsage(this);
                     cloud.initializeCloud();

                     break;

                  case R.id.start_tests_button:

                     if(cloud.isConnected())
                        {
                           new AsyncTask<Void, Void, Void>() {
                              protected void onPreExecute() {
                                 // Pre Code
                              }
                              protected Void doInBackground(Void... unused) {
                                 cloud.createStorageDirectoriesOnCloud();
                                 return null;
                              }
                              protected void onPostExecute(Void unused) {
                                 // Post Code
                              }
                           }.execute();
                        }
                     break;
               }
         }


      void createDeviceStorageDirectories()
         {
            DeviceFileManager.createDirectory(Constants.testFilePath);
         }
   }
