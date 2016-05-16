package com.posn.commentpropagation.utility;


import com.posn.commentpropagation.datatypes.Comment;
import com.posn.commentpropagation.datatypes.WallPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CloudFileManager
   {
      public static void createCommentFile(String key, ArrayList<Comment> comments, String deviceDirectory, String fileName)
         {
            JSONObject object = new JSONObject();

            try
               {
                  JSONArray commentList = new JSONArray();

                  Comment comment;

                  for (int i = 0; i < comments.size(); i++)
                     {
                        comment = comments.get(i);
                        commentList.put(comment.createJSONObject());
                     }

                  object.put("comments", commentList);

                  // need to encrypt data here
                  String jsonString = object.toString();
                  // String encryptedData = SymmetricKeyManager.encrypt(key, jsonString);

                  DeviceFileManager.writeStringToFile(jsonString, deviceDirectory + "/" + fileName);
               }
            catch (JSONException e)
               {
                  e.printStackTrace();
               }
         }

      public static void createGroupWallFile(String key, ArrayList<WallPost> posts, String deviceDirectory, String fileName, int commentType)
         {
            JSONObject object = new JSONObject();

            try
               {
                  object.put("version", 0);
                  object.put("archive_link", JSONObject.NULL);
                  object.put("archive_key", JSONObject.NULL);

                  JSONArray postList = new JSONArray();


                  for (WallPost wallPost : posts)
                     {
                        postList.put(wallPost.createJSONObject(commentType));
                     }

                  object.put("posts", postList);

                  // need to encrypt data here
                  String jsonString = object.toString();
                 // String encryptedData = SymmetricKeyManager.encrypt(key, jsonString);

                  DeviceFileManager.writeStringToFile(jsonString, deviceDirectory + "/" + fileName);
               }
            catch (JSONException e)
               {
                  e.printStackTrace();
               }
         }

      public static ArrayList<WallPost> fetchAndLoadGroupWallFile(String link, String key,String deviceDirectory, String fileName, int commentType)
         {
            ArrayList<WallPost> wallPostArrayList = new ArrayList<>();

            // download the wall file from the cloud
            DeviceFileManager.downloadFileFromURL(link, deviceDirectory, fileName);

            // read in the encrypted data
            String encryptedString = DeviceFileManager.loadStringFromFile(deviceDirectory + "/" + fileName);

            // decrypt the file data
           // String fileData = SymmetricKeyManager.decrypt(key, encryptedString);

            try
               {
                  JSONObject object = new JSONObject(encryptedString);

                  JSONArray postList = object.getJSONArray("posts");

                  for (int i = 0; i < postList.length(); i++)
                     {
                        WallPost wallPost = new WallPost();
                        wallPost.parseJSONObject(postList.getJSONObject(i), commentType);

                        wallPostArrayList.add(wallPost);
                     }

                  return wallPostArrayList;
               }
            catch (JSONException e)
               {
                  e.printStackTrace();
               }
            return null;
         }






   }
