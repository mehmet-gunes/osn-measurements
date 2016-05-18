package com.posn.commentpropagation.utility;


import android.content.Context;

import com.posn.commentpropagation.Constants;
import com.posn.commentpropagation.R;
import com.posn.commentpropagation.datatypes.Comment;
import com.posn.commentpropagation.datatypes.WallPost;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class TestValueGenerator
   {
      private final int NUM_COMMENT_CHAR_VALUES = 10000;

      private int[] numCommentsArray;
      private int[] numCommentCharsArray;

      private int postIndex = 0;

      private Context context;
      private Random rand = new Random();


      public TestValueGenerator(Context context, int numTests)
         {
            this.context = context;

            // read the number of comments data from the file
            numCommentsArray = new int[numTests];
            readDataValuesFromFile(R.raw.data_num_comments_limited, numCommentsArray, numTests);

            // read the number of comment chars from the file
            numCommentCharsArray = new int[NUM_COMMENT_CHAR_VALUES];
            readDataValuesFromFile( R.raw.data_num_chars, numCommentCharsArray, NUM_COMMENT_CHAR_VALUES);
         }

      public WallPost generateRandomWallPost()
         {
            WallPost post;

            int postType = rand.nextInt() % 2;

            if (postType == 0)
               {
                  post = new WallPost(Constants.POST_TYPE_STATUS, IDGenerator.generate("test_user@posn.com"));

                  // generate random status text
                  post.textContent = generateRandomString(rand.nextInt(150));
               }
            else
               {
                  post = new WallPost(Constants.POST_TYPE_PHOTO, IDGenerator.generate("test_user@posn.com"));

                  // add in photo link and key
                  post.multimediaLink = "https://www.dropbox.com/s/9ru88jvb88peadc/37f694c6e6c51c04e9527479c8216667e74a701d4b777c09928878054c90de61.jpg?dl=1";
                  post.multimediaKey = SymmetricKeyManager.createRandomKey();
               }

            // generate random comments for the wall post
            int numComments = numCommentsArray[postIndex];
            post.comments = generateComments(post.postID, post.friendID, numComments);
            postIndex ++;

            return post;
         }

      public ArrayList<Comment> generateComments(String postID, String userID, int numComments)
         {
            ArrayList<Comment> comments = new ArrayList<>();

            // generate a random number of comments
            for (int i = 0; i < numComments; i++)
               {
                  // generate a random comment string
                  int numCommentChars = numCommentCharsArray[rand.nextInt(NUM_COMMENT_CHAR_VALUES)];
                  String commentString = generateRandomString(numCommentChars);

                  comments.add(new Comment(postID, userID, commentString));
               }

            return comments;
         }


      private String generateRandomString(int numChars)
         {
            Random generator = new Random();
            StringBuilder randomStringBuilder = new StringBuilder();

            char tempChar;
            for (int i = 0; i < numChars; i++)
               {
                  tempChar = (char) (generator.nextInt(96) + 32);
                  randomStringBuilder.append(tempChar);
               }
            return randomStringBuilder.toString();
         }



      private void readDataValuesFromFile(int res_id, int [] array, int numElements)
         {
            InputStream is = context.getResources().openRawResource(res_id);
            Scanner scanner = new Scanner(is);

            try
               {
                  for (int i = 0; i < numElements; i++)
                     {
                        int value = scanner.nextInt();
                        array[i] = value;
                     }

                  is.close();
               }
            catch (IOException e)
               {
                  e.printStackTrace();
               }

         }
   }
