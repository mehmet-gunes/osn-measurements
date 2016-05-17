package com.posn.commentpropagation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MeasurementTestsAsyncTask extends AsyncTask<String, String, String>
   {
      private ProgressDialog pDialog;
      private MainActivity main;
      private CloudProvider cloud;

      private int testNum = 1;
      private int numTests;

      private FileWriter writer;

      private ArrayList<WallPost> wallPosts = new ArrayList<>();
      private int[] numCommentsArray;

      String resultString;

      AtomicDouble commentFileTime = new AtomicDouble(0.0);

      public MeasurementTestsAsyncTask(MainActivity activity)
         {
            super();
            main = activity;
            cloud = main.cloud;
            numTests = main.numberOfTests;
            numCommentsArray = new int[numTests];
         }


      @Override
      protected void onPreExecute()
         {
            super.onPreExecute();
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
            // create cloud storage directories
            cloud.createStorageDirectoriesOnCloud();

            // create random encryption key
            String encryptionKey = SymmetricKeyManager.createRandomKey();

            // read the number of comments data from the file
            readNumberOfCommentsFromFile(main, R.raw.data_num_comments);

            WallPost post;

            int numComments;
            String embeddedCommentsWallLink, commentFileWallLink;

            try
               {
                  File root = new File(Constants.testFilePath);
                  File resultFile = new File(root, "timings.txt");
                  writer = new FileWriter(resultFile);

                  for (int i = 0; i < numTests && !isCancelled(); i++)
                     {
                        resultString = "Post Num, " + (i + 1) + ", ";

                        // update the progress dialog with the correct test number
                        publishProgress("Testing");

                        // generate a random wall post and add it to the list
                        numComments = numCommentsArray[i];
                        post = TestValueGenerator.generateRandomWallPost(numComments);
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


      private void readNumberOfCommentsFromFile(Context ctx, int res_id)
         {
            InputStream is = ctx.getResources().openRawResource(res_id);
            Scanner scanner = new Scanner(is);
            Random rand = new Random();

            try
               {
                  for (int i = 0; i < numTests; i++)
                     {
                        int value = scanner.nextInt();

                        if (value > 30)
                           {

                              numCommentsArray[i] = rand.nextInt(21);
                           }
                        else
                           {
                              numCommentsArray[i] = value;
                           }
                     }

                  is.close();
               }
            catch (IOException e)
               {
                  e.printStackTrace();
               }

         }


      private String uploadAndMeasureWallWithEmbeddedComments(String encryptionKey, WallPost post) throws IOException
         {
            // create wall file with embedded comments
            CloudFileManager.createGroupWallFile(encryptionKey, wallPosts, Constants.testFilePath, Constants.embedded_comments_wall_file, Constants.TYPE_EMBEDDED_COMMENTS);

            //File file = new File(Constants.testFilePath + "/" + Constants.embedded_comments_wall_file);
            //long embeddedFileSize = file.length();


            // upload the embedded comments group file into the cloud
            double tStart = System.currentTimeMillis();
            String link = cloud.uploadFileToCloud(Constants.cloudDirectory, Constants.embedded_comments_wall_file, Constants.testFilePath + "/" + Constants.embedded_comments_wall_file);
            double tEnd = System.currentTimeMillis();
            double embeddedGroupFileTime = (tEnd - tStart) / 1000.0;

            //writer.append("Upload, Embedded, " + embeddedFileSize + " bytes, " + embeddedGroupFileTime + " sec\n");
            writer.append("Upload, Embedded, " + embeddedGroupFileTime + " sec\n");

            return link;
         }

      private void downloadAndMeasureWallWithEmbeddedComments(String link) throws IOException
         {
            double tStart = System.currentTimeMillis();
            DeviceFileManager.downloadFileFromURL(link, Constants.testFilePath, Constants.embedded_comments_wall_file);

            //File file = new File(Constants.testFilePath + "/" + Constants.embedded_comments_wall_file);
            // long embeddedFileSize = file.length();

            double tEnd = System.currentTimeMillis();
            double embeddedGroupFileTime = (tEnd - tStart) / 1000.0;

            //writer.append("Download, Embedded, " + embeddedFileSize + " bytes, " + embeddedGroupFileTime + " sec\n");
            writer.append("Download, Embedded, " + embeddedGroupFileTime + " sec\n");

         }


      private String uploadAndMeasureWallWithCommentFiles(String encryptionKey, WallPost post, int commentFileNum) throws IOException
         {
            double commentFileTime = uploadAndMeasureCommentFile(post, commentFileNum);

            // create wall file with comment links
            CloudFileManager.createGroupWallFile(encryptionKey, wallPosts, Constants.testFilePath, Constants.comment_link_wall_file, Constants.TYPE_LINK_COMMENTS);

            // File file = new File(Constants.testFilePath + "/" + Constants.comment_link_wall_file);
            // long linkFileSize = file.length();


            // upload the comment link group file into the cloud
            double tStart = System.currentTimeMillis();
            String link = cloud.uploadFileToCloud(Constants.cloudDirectory, Constants.comment_link_wall_file, Constants.testFilePath + "/" + Constants.comment_link_wall_file);
            double tEnd = System.currentTimeMillis();
            double linkGroupFileTime = (tEnd - tStart) / 1000.0;

            // writer.append("Upload, Links, " + linkFileSize + " bytes, " + (linkGroupFileTime + commentFileTime) + " sec\n");
            writer.append("Upload, Links, " + (linkGroupFileTime + commentFileTime) + " sec\n");

            return link;
         }

      private double uploadAndMeasureCommentFile(WallPost post, int commentFileNum)
         {
            // create and upload comment file
            CloudFileManager.createCommentFile(post.multimediaKey, post.comments, Constants.testFilePath, "comment_file.txt");
            long tStart = System.currentTimeMillis();
            post.commentFileLink = cloud.uploadFileToCloud(Constants.cloudDirectory, "comment_file_" + commentFileNum + ".txt", Constants.testFilePath + "/comment_file.txt");
            wallPosts.remove(wallPosts.size() - 1);
            wallPosts.add(post);

            long tEnd = System.currentTimeMillis();
            return (tEnd - tStart) / 1000.0;
         }


      private void downloadAndMeasureWallWithCommentFiles(String link) throws IOException, InterruptedException
         {
            // download and measure the group wall file time and size
            double tStart = System.currentTimeMillis();
            DeviceFileManager.downloadFileFromURL(link, Constants.testFilePath, Constants.embedded_comments_wall_file);
            double tEnd = System.currentTimeMillis();
            double linkGroupFileTime = (tEnd - tStart) / 1000.0;

            // File file = new File(Constants.testFilePath + "/" + Constants.comment_link_wall_file);
            // long linkFileSize = file.length();

            // create a pool of threads, 10 max jobs will execute in parallel
            ExecutorService threadPool = Executors.newFixedThreadPool(10);
            commentFileTime.set(0.0);

            // loop through and fetch all comment files and measure time and file sizes
            for (WallPost post : wallPosts)
               {
                  // add the new url to the download queue
                  threadPool.submit(createDownloadRunnable(post));
               }

            // shutdown the threadpool so no
            threadPool.shutdown();
            // wait for the threads to finish if necessary
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

            // write the results to the file
            // writer.append("Download, Links, " + linkFileSize + " bytes, " + linkGroupFileTime + " sec\n");
            System.out.println(linkGroupFileTime + " | " + commentFileTime.get());
            linkGroupFileTime += commentFileTime.get();
            writer.append("Download, Links, " + linkGroupFileTime + " sec\n");


         }

      private Runnable createDownloadRunnable(final WallPost post)
         {
            Runnable aRunnable = new Runnable()
               {
                  public void run()
                     {
                        // get the starting time
                        double tStart = System.currentTimeMillis();

                        // download the file
                        DeviceFileManager.downloadFileFromURL(post.commentFileLink, Constants.testFilePath, "comment_file.txt");

                        // get the ending time
                        double tEnd = System.currentTimeMillis();

                        // add the time to the total comment file time
                        commentFileTime.addAndGet((tEnd - tStart) / 1000.0);
                     }
               };

            return aRunnable;

         }
   }