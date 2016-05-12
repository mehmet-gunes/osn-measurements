package com.posn.commentpropagation.clouds.Dropbox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;
import com.posn.commentpropagation.Constants;
import com.posn.commentpropagation.clouds.CloudProvider;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class DropboxClientUsage extends CloudProvider
   {
      private Context context;
      private boolean authenticated = false;

      // variable declarations
      public DropboxAPI<AndroidAuthSession> dropboxSession;

      public DropboxClientUsage(Context context)
         {
            this.context = context;
         }

      final static private String APP_KEY = "btt1coh7iijfczm";
      final static private String APP_SECRET = "tifgdmddm7g0hqd";

      @Override
      public void initializeCloud()
         {
            // create the application key token
            AppKeyPair appKeyToken = new AppKeyPair(APP_KEY, APP_SECRET);

            // try to get the session token from the shared preferences
            String sessionToken = getDropboxToken();

            // check if the session token was retrieved
            if (sessionToken == null)
               {
                  // if no token, then have the user sign in
                  dropboxSession = new DropboxAPI<>(new AndroidAuthSession(appKeyToken));
                  dropboxSession.getSession().startOAuth2Authentication(context);
               }
            else
               {
                  // session token already available
                  dropboxSession = new DropboxAPI<>(new AndroidAuthSession(appKeyToken, sessionToken));

                  // authenticate Dropbox login
                  authenticateDropboxLogin();

               }
         }

      @Override
      public void onResume()
         {
            if(!authenticated)
               {
                  authenticateDropboxLogin();

               }
         }


      private void authenticateDropboxLogin()
         {
            // check if the dropbox session has been created
            if (dropboxSession != null)
               {
                  // check if the authentication was successful
                  if (dropboxSession.getSession().authenticationSuccessful())
                     {
                        // finish the authentication
                        dropboxSession.getSession().finishAuthentication();

                        //saveDropboxToken(dropboxSession.getSession().getAccessTokenPair());
                        saveDropboxToken(dropboxSession.getSession().getOAuth2AccessToken());
                        System.out.println("TOKEN SAVED!!!!");

                        showToast("Dropbox Connected!");
                        authenticated = true;
                        isConnected = true;
                     }
                  else
                     {
                        System.out.println("WOOPS2");

                     }
               }
            else
               {
                  System.out.println("WOOPS1");
               }
         }

      private void showToast(String message)
         {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
         }

      @Override
      public void createStorageDirectoriesOnCloudAsyncTask()
         {
            new AsyncTask<Void, Void, Void>()
            {
               protected Void doInBackground(Void... params)
                  {
                     createStorageDirectoriesOnCloud();
                     return null;
                  }
            }.execute();
         }

      @Override
      public void downloadFileFromCloudAsyncTask(final String folderName, final String fileName, final String devicePath)
         {
            new AsyncTask<Void, Void, Void>()
            {
               protected Void doInBackground(Void... params)
                  {
                     downloadFileFromCloud(folderName, fileName, devicePath);
                     return null;
                  }
            }.execute();
         }

      @Override
      public void uploadFileToCloudAsyncTask(final String folderName, final String fileName, final String devicePath)
         {
            new AsyncTask<Void, Void, Void>()
            {
               protected Void doInBackground(Void... params)
                  {
                     uploadFileToCloud(folderName, fileName, devicePath);
                     return null;
                  }
            }.execute();
         }

      // check to see if the token has already been obtained
      //private AccessTokenPair getDropboxToken()
      private String getDropboxToken()
         {
            // get the shared preferences area for the token
            SharedPreferences sessionTokenRecord = context.getSharedPreferences("comment_propagation", Context.MODE_PRIVATE);

            // get the token key
            String sessionToken = sessionTokenRecord.getString("accessToken", null);

            if (!(sessionToken == null))
               {
                  System.out.println("TOKEN FOUND!");
                  return sessionToken;
               }
            else
               {
                  System.out.println("TOKEN FAILED!");

                  // return null
                  return null;
               }

         }


      //private void saveDropboxToken(AccessTokenPair accessToken)
      private void saveDropboxToken(String accessToken)
         {
            // create a new shared preferences area for the token
            SharedPreferences.Editor tokenRecordEditor = context.getSharedPreferences("comment_propagation", Context.MODE_PRIVATE).edit();

            tokenRecordEditor.putString("accessToken", accessToken);

            // write the preferences to the device
            tokenRecordEditor.apply();
         }

      @Override
      public void activityResult(int requestCode, int resultCode, Intent data)
         {

         }

      @Override
      public void createStorageDirectoriesOnCloud()
         {
            for(int i = 0; i < Constants.NUM_DIRECTORIES; i++)
               {
                  // check if a direct exists, if it does not then exception will be thrown
                  try
                     {
                        System.out.println("Creating!");
                        // attempt to create dropbox folder
                        dropboxSession.createFolder(Constants.directoryNames[i]);
                        System.out.println("Created!");

                     }
                  catch (DropboxException e)
                     {
                        // folder already exists, so ignore
                     }
               }
         }

      @Override
      public void downloadFileFromCloud(String folderName, String fileName, String devicePath)
         {
            // declare variables
            int tries = 3;
            FileOutputStream outputStream;

            String dropboxPath = "/" + folderName + "/" + fileName;

            // get the location of the file to be downloaded
            java.io.File f = new java.io.File(devicePath);

            // try up to three times to download the file
            while (tries > 0)
               {
                  try
                     {
                        // create a stream to put the file onto the device
                        outputStream = new FileOutputStream(f);

                        // download the file from dropbox
                        DropboxAPI.DropboxFileInfo info = dropboxSession.getFile(dropboxPath, null, outputStream, null);

                        // check if the download was successful
                        if (info != null)
                           {
                              tries = 0;
                              Log.i("Dropbox Download", "Download Complete");
                           }
                        else
                           {
                              tries--;
                              Log.i("Dropbox Download", "Download Failed");
                           }

                        // close the stream
                        outputStream.close();
                     }
                  catch (Exception e)
                     {
                        e.printStackTrace();
                        Log.i("Dropbox Download", "Exception");
                        tries--;
                     }
               }
         }

      @Override
      public String uploadFileToCloud(String folderName, String fileName, String devicePath)
         {
            // declare variables
            DropboxAPI.Entry response = null;
            int tries = 3;
            FileInputStream inputStream;

            String dropboxPath = "/" + folderName + "/" + fileName;
            String directLink = null;

            // get the location of the file to be uploaded
            java.io.File file = new java.io.File(devicePath);

            // try up to three times to upload the file
            while (tries > 0)
               {
                  try
                     {
                        // get the file into memory
                        inputStream = new FileInputStream(file);

                        // upload the file to dropbox
                        response = dropboxSession.putFileOverwrite(dropboxPath, inputStream, file.length(), null);

                        // check if the upload was successful
                        if(response != null)
                           {
                              // share the file to the public
                              DropboxAPI.DropboxLink link = dropboxSession.share(dropboxPath);

                              // returned link is a short-link, so get long link
                              // create a new URL
                              URL url = new URL(link.url);

                              // open the connection to redirect
                              HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
                              ucon.setInstanceFollowRedirects(false);

                              // get the long link string in the Location part of the header
                              String test = ucon.getHeaderField("Location");

                              // replace end 0 with a 1 to make it become a direct link
                              directLink = test.substring(0,test.length()-1) + "1";


                              Log.i("Dropbox Upload", "Upload Complete");
                              tries = 0;
                           }
                        else
                           {
                              Log.i("Dropbox Upload", "Upload Failed");
                              tries --;
                           }

                        // close the stream
                        inputStream.close();
                     }
                  catch (IOException | DropboxException e)
                     {
                        e.printStackTrace();
                        Log.i("Dropbox Upload", "IO Exception");
                        tries--;
                     }
               }

            return directLink;
         }
   }
