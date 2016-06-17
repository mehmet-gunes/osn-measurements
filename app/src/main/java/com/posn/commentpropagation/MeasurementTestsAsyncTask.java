package com.posn.commentpropagation;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.google.common.util.concurrent.AtomicDouble;
import com.posn.commentpropagation.clouds.CloudProvider;
import com.posn.commentpropagation.datatypes.WallPost;
import com.posn.commentpropagation.utility.CloudFileManager;
import com.posn.commentpropagation.utility.DeviceFileManager;
import com.posn.commentpropagation.utility.SymmetricKeyManager;
import com.posn.commentpropagation.utility.TestValueGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MeasurementTestsAsyncTask extends AsyncTask<String, String, String>
   {
      private ProgressDialog pDialog;
      private MainActivity main;
      private CloudProvider cloud;
      private TestValueGenerator testValueGenerator;
      private FileWriter writer;

      private int testNum = 1;
      private int numTests;


      private ArrayList<WallPost> wallPosts = new ArrayList<>();

      String resultString;

      AtomicDouble commentFileTime = new AtomicDouble(0.0);

      public MeasurementTestsAsyncTask(MainActivity activity)
         {
            super();

            // set the main activity to get the context and cloud provider
            main = activity;
            cloud = main.cloud;
            numTests = main.numberOfTests;
         }


      @Override
      protected void onPreExecute()
         {
            super.onPreExecute();

            // show a progress dialog to display the test numbers
            pDialog = new ProgressDialog(main);
            pDialog.setMessage("Measuring Test: " + testNum + " of " + numTests);
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
               {
                  @Override
                  public void onCancel(DialogInterface dialog)
                     {
                        MeasurementTestsAsyncTask.this.cancel(true);
                     }
               });
            pDialog.show();
         }


      protected String doInBackground(String... params)
         {
            // declare variables
            WallPost post;
            String embeddedCommentsWallLink, commentFileWallLink;

            // create cloud storage directories
            cloud.createStorageDirectoriesOnCloud();

            // create a new test value generator to generate new posts
            testValueGenerator = new TestValueGenerator(main, numTests);

            // create random encryption key
            String encryptionKey = SymmetricKeyManager.createRandomKey();

            try
               {
                  // create a new file on the device for the timing results
                  File root = new File(Constants.testFilePath);
                  File resultFile = new File(root, "timings.txt");
                  writer = new FileWriter(resultFile);

                  // loop through the number of posts
                  for (int i = 0; i < numTests && !isCancelled(); i++)
                     {
                        // create a result string for the post number
                        resultString = "Post Num, " + (i + 1) + ", ";

                        // update the progress dialog with the correct test number
                        publishProgress("Testing");

                        // generate a random wall post and add it to the list
                        post = testValueGenerator.generateRandomWallPost();
                        System.out.println("NUM COMMENTS!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + post.comments.size());
                        wallPosts.add(post);

                        // measure the upload performance of group wall with embedded comments
                        writer.append(resultString);
                        embeddedCommentsWallLink = uploadAndMeasureWallWithEmbeddedComments(encryptionKey, post);

                        // measure the upload performance of group wall with comment files
                        writer.append(resultString);
                        commentFileWallLink = uploadAndMeasureWallWithCommentFiles(encryptionKey, post, i);

                        // stop the downloading measurements after 500 tests
                        if (i < 500)
                           {
                              // measure the download performance of group wall with embedded comments
                              writer.append(resultString);
                              downloadAndMeasureWallWithEmbeddedComments(embeddedCommentsWallLink);

                              // measure the download performance of groups wall with comment files
                              writer.append(resultString);
                              downloadAndMeasureWallWithCommentFiles(commentFileWallLink);
                           }

                        // increment the test number and write out the previous results to the file (in case the app crashes)
                        testNum++;
                        writer.flush();
                     }

                  // close the result file and upload the results to the cloud
                  writer.close();
                  cloud.uploadFileToCloud(Constants.resultDirectory, "timings.txt", Constants.testFilePath + "/timings.txt");
               }
            catch (IOException | InterruptedException e)
               {
                  e.printStackTrace();
               }
            catch (Exception e)
               {
                  Log.e("Error", "", e);
               }
            return null;
         }


      protected void onPostExecute(String file_url)
         {
            // dismiss the dialog once done
            pDialog.dismiss();
         }


      @Override
      protected void onProgressUpdate(String... update)
         {
            pDialog.setMessage("Measuring Test: " + testNum + " of " + numTests);
         }


      private String uploadAndMeasureWallWithEmbeddedComments(String encryptionKey, WallPost post) throws IOException
         {
            // create wall file with embedded comments
            CloudFileManager.createGroupWallFile(encryptionKey, wallPosts, Constants.testFilePath, Constants.embedded_comments_wall_file, Constants.TYPE_EMBEDDED_COMMENTS);

            // upload the embedded comments group file into the cloud and time the upload process
            double tStart = System.currentTimeMillis();
            String link = cloud.uploadFileToCloud(Constants.cloudDirectory, Constants.embedded_comments_wall_file, Constants.testFilePath + "/" + Constants.embedded_comments_wall_file);
            double tEnd = System.currentTimeMillis();
            double embeddedGroupFileTime = (tEnd - tStart) / 1000.0;

            // output the time to the result file
            writer.append("Upload, Embedded, " + embeddedGroupFileTime + " sec\n");

            // return the link to the uploaded file
            return link;
         }

      private void downloadAndMeasureWallWithEmbeddedComments(String link) throws IOException
         {
            // download the wall file from the cloud and time the download process
            double tStart = System.currentTimeMillis();
            DeviceFileManager.downloadFileFromURL(link, Constants.testFilePath, Constants.embedded_comments_wall_file);
            double tEnd = System.currentTimeMillis();
            double embeddedGroupFileTime = (tEnd - tStart) / 1000.0;

            // output the time to the result file
            writer.append("Download, Embedded, " + embeddedGroupFileTime + " sec\n");
         }


      private String uploadAndMeasureWallWithCommentFiles(String encryptionKey, WallPost post, int commentFileNum) throws IOException
         {
            // create a new comment file for the post and upload it to the cloud. Measure the upload time
            double commentFileTime = uploadAndMeasureCommentFile(post, commentFileNum);

            // create wall file with comment links
            CloudFileManager.createGroupWallFile(encryptionKey, wallPosts, Constants.testFilePath, Constants.comment_link_wall_file, Constants.TYPE_LINK_COMMENTS);

            // upload the comment link group file into the cloud
            double tStart = System.currentTimeMillis();
            String link = cloud.uploadFileToCloud(Constants.cloudDirectory, Constants.comment_link_wall_file, Constants.testFilePath + "/" + Constants.comment_link_wall_file);
            double tEnd = System.currentTimeMillis();
            double linkGroupFileTime = (tEnd - tStart) / 1000.0;

            // output the time to the result file
            writer.append("Upload, Links, " + (linkGroupFileTime + commentFileTime) + " sec\n");

            // return the link to the uploaded file
            return link;
         }

      private double uploadAndMeasureCommentFile(WallPost post, int commentFileNum)
         {
            // create create a new comment file for the post
            CloudFileManager.createCommentFile(post.multimediaKey, post.comments, Constants.testFilePath, "comment_file.txt");

            // upload the comment file to the cloud and time the upload process
            long tStart = System.currentTimeMillis();
            post.commentFileLink = cloud.uploadFileToCloud(Constants.cloudDirectory, "comment_file_" + commentFileNum + ".txt", Constants.testFilePath + "/comment_file.txt");
            long tEnd = System.currentTimeMillis();

            // remove the old post from the array and add the updated one.
            wallPosts.remove(wallPosts.size() - 1);
            wallPosts.add(post);

            // return the time to upload the comment file
            return (tEnd - tStart) / 1000.0;
         }


      private void downloadAndMeasureWallWithCommentFiles(String link) throws IOException, InterruptedException
         {
            // download and measure the group wall file time and size
            double tStart = System.currentTimeMillis();
            DeviceFileManager.downloadFileFromURL(link, Constants.testFilePath, Constants.embedded_comments_wall_file);
            double tEnd = System.currentTimeMillis();
            double linkGroupFileTime = (tEnd - tStart) / 1000.0;

            // create a pool of threads, 10 downloads will execute in parallel
            ExecutorService threadPool = Executors.newFixedThreadPool(10);

            // set the automic double to 0
            commentFileTime.set(0.0);
            int counter = 0;

            // loop through and fetch all comment files and measure time and file sizes
            for (WallPost post : wallPosts)
               {
                  // add the new url to the download queue
                  threadPool.submit(createDownloadRunnable(post, counter%10));
                  counter++;
               }

            // shutdown the threadpool so no more tasks can be added
            threadPool.shutdown();

            // wait for the threads to finish if necessary
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

            // output the time to the result file
            linkGroupFileTime += commentFileTime.get();
            writer.append("Download, Links, " + linkGroupFileTime + " sec\n");
         }

      private Runnable createDownloadRunnable(final WallPost post, final int num)
         {
            return new Runnable()
               {
                  public void run()
                     {
                        // get the starting time
                        double tStart = System.currentTimeMillis();

                        // download the file
                        DeviceFileManager.downloadFileFromURL(post.commentFileLink, Constants.testFilePath, "comment_file" + Integer.toString(num) + ".txt");

                        // get the ending time
                        double tEnd = System.currentTimeMillis();

                        // add the time to the total comment file time
                        commentFileTime.addAndGet((tEnd - tStart) / 1000.0);
                     }
               };
         }
   }