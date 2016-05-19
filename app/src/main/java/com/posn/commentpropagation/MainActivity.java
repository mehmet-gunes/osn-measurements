package com.posn.commentpropagation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.posn.commentpropagation.clouds.CloudProvider;
import com.posn.commentpropagation.clouds.Dropbox.DropboxClientUsage;
import com.posn.commentpropagation.utility.DeviceFileManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
   {
      // interface variables
      private TextView dropboxStatusText;
      private EditText numTestsText;

      public CloudProvider cloud = null;
      public int numberOfTests;

      @Override
      protected void onCreate(Bundle savedInstanceState)
         {
            super.onCreate(savedInstanceState);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setContentView(R.layout.activity_main);

            // get buttons from the interface
            Button connectDropboxButton = (Button) findViewById(R.id.connect_button);
            Button startTestsButton = (Button) findViewById(R.id.start_tests_button);

            // set the button onClickListener
            if(connectDropboxButton != null && startTestsButton != null)
               {
                  connectDropboxButton.setOnClickListener(this);
                  startTestsButton.setOnClickListener(this);
               }

            // get textviews from the interface
            dropboxStatusText = (TextView) findViewById(R.id.dropbox_status_text);

            // get edittext from the interface
            numTestsText = (EditText) findViewById(R.id.num_tests_text);

            // create the directories on device
            createDeviceStorageDirectories();
         }


      @Override
      protected void onResume()
         {
            super.onResume();

            if (cloud != null)
               {
                  cloud.onResume();
                  String dropboxStatus = "Connected to Dropbox!";
                  dropboxStatusText.setText(dropboxStatus);
               }
            else
               {
                  cloud = new DropboxClientUsage(this);
               }
         }

      @Override
      public void onClick(View v)
         {
            switch (v.getId())
               {
                  case R.id.connect_button:

                     // Connect with Dropbox account
                     cloud.initializeCloud();

                     if (cloud.isConnected())
                        {
                           // update dropbox status
                           String dropboxStatus = "Connected to Dropbox!";
                           dropboxStatusText.setText(dropboxStatus);
                        }

                     break;

                  case R.id.start_tests_button:

                     if (cloud.isConnected())
                        {
                           // check if the number of tests have been entered
                           if (!isEmpty(numTestsText))
                              {
                                 // get the number of tests
                                 numberOfTests = Integer.parseInt(numTestsText.getText().toString());

                                 // create an async task to perform the measurements
                                 new MeasurementTestsAsyncTask(this).execute();
                              }
                           else
                              {
                                 Toast.makeText(this, "Please enter the number of tests", Toast.LENGTH_SHORT).show();
                              }
                        }
                     else
                        {
                           Toast.makeText(this, "Please connect to a Dropbox account", Toast.LENGTH_SHORT).show();
                        }

                     break;
               }
         }


      void createDeviceStorageDirectories()
         {
            DeviceFileManager.createDirectory(Constants.testFilePath);
         }

      private boolean isEmpty(EditText etText)
         {
            return etText.getText().toString().trim().length() == 0;
         }
   }
