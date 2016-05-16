package com.posn.commentpropagation.utility;


import com.posn.commentpropagation.Constants;
import com.posn.commentpropagation.datatypes.Comment;
import com.posn.commentpropagation.datatypes.WallPost;

import java.util.ArrayList;
import java.util.Random;

public class TestValueGenerator
   {
      public static WallPost generateRandomWallPost(int numComments)
         {
            Random rand = new Random();
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
            post.comments = TestValueGenerator.generateComments(post.postID, post.friendID, numComments);

            return post;
         }

      public static ArrayList<Comment> generateComments(String postID, String userID, int numComments)
         {
            ArrayList<Comment> comments = new ArrayList<>();

            // generate a random number of comments
            // int numComments = generateNumberOfComments();
System.out.println("NUM COMMENTS!!!!!!!!!!!!!!!!!!!!!!!: " + numComments);
            for (int i = 0; i < numComments; i++)
               {
                  // generate a random comment string
                  int numCommentChars = generateCommentLength();
                  String commentString = generateRandomString(numCommentChars);

                  comments.add(new Comment(postID, userID, commentString));
               }

            return comments;
         }

      private static int generateNumberOfComments()
         {
            Random random = new Random();

            // generate a random value between 0 and 100
            int propability = random.nextInt(101);

            // check if if less than 2
            if (propability < 1)
               {
                  // create a large amount of comments
                  return random.nextInt((600 - 30) + 1) + 30;
               }
            else
               {
                  if (propability < 50)
                     {
                        // create a number of comments less than the average
                        return random.nextInt((30 - 10) + 1) + 10;
                     }
                  else
                     {
                        // create a number of comments greater than the average
                        return random.nextInt((10 + 1));
                     }
               }
         }

      private static String generateRandomString(int numChars)
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

      private static int generateCommentLength()
         {
            Random random = new Random();

            // generate a random value between 0 and 100
            int propability = random.nextInt(101);

            // check if if less than 2
            if (propability < 2)
               {
                  // create a large amount of comments
                  return random.nextInt((1000 - 200) + 1) + 200;
               }
            else
               {
                  if (propability < 50)
                     {
                        // create a number of comments less than the average
                        return random.nextInt((200 - 50) + 1) + 50;
                     }
                  else
                     {
                        // create a number of comments greater than the average
                        return random.nextInt((50 + 1));
                     }
               }
         }
   }
