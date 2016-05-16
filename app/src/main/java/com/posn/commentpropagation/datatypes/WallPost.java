package com.posn.commentpropagation.datatypes;


import com.posn.commentpropagation.Constants;
import com.posn.commentpropagation.utility.IDGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class WallPost
   {
      // data variables
      public int type;
      public String postID;
      public String friendID;
      public String date;
      public String textContent = null;

      public String multimediaLink = null;
      public String multimediaKey = null;

      public ArrayList<Comment> comments = new ArrayList<>();

      public String commentFileLink = null;
      public String commentFileKey = null;


      public WallPost()
         {
         }


      public WallPost(int type, String friendID, String textContent)
         {
            // create POST ID
            postID = IDGenerator.generate(this.friendID);

            // create post date
            Date currentDate = new Date();
            SimpleDateFormat dateformatDay = new SimpleDateFormat("MMM dd 'at' h:mmaa", Locale.US);
            this.date = dateformatDay.format(currentDate);

            this.type = type;
            this.friendID = friendID;
            this.textContent = textContent;
         }

      public WallPost(int type, String friendID)
         {
            // create POST ID
            postID = IDGenerator.generate(this.friendID);

            // create post date
            Date currentDate = new Date();
            SimpleDateFormat dateformatDay = new SimpleDateFormat("MMM dd 'at' h:mmaa", Locale.US);
            this.date = dateformatDay.format(currentDate);

            this.type = type;
            this.friendID = friendID;
         }

      public JSONObject createJSONObject(int commentType)
         {
            JSONObject obj = new JSONObject();

            try
               {
                  obj.put("type", type);
                  obj.put("postID", postID);
                  obj.put("friendID", friendID);
                  obj.put("date", date);

                  // store the content based on the content type
                  if (type == Constants.POST_TYPE_STATUS)
                     {
                        obj.put("textContent", textContent);
                     }
                  else
                     {
                        obj.put("multimediaKey", multimediaKey);
                        obj.put("multimediaLink", multimediaLink);
                     }

                  if (commentType == 0)
                     {
                        // store the comments
                        JSONArray commentList = new JSONArray();
                        for (int i = 0; i < comments.size(); i++)
                           {
                              Comment comment = comments.get(i);
                              commentList.put(comment.createJSONObject());
                           }
                        obj.put("comments", commentList);
                     }
                  else
                     {
                        obj.put("commentFileLink", commentFileLink);
                        obj.put("commentFileKey", commentFileKey);
                     }

               }
            catch (JSONException e)
               {
                  e.printStackTrace();
               }

            return obj;
         }

      public void parseJSONObject(JSONObject obj, int commentType)
         {
            try
               {
                  type = obj.getInt("type");
                  postID = obj.getString("postID");
                  friendID = obj.getString("friendID");
                  date = obj.getString("date");

                  if (type == Constants.POST_TYPE_STATUS)
                     {
                        textContent = obj.getString("textContent");
                     }
                  else
                     {
                        multimediaKey = obj.getString("multimediaKey");
                        multimediaLink = obj.getString("multimediaLink");
                     }

                  if (commentType == 0)
                     {
                        // get the comments
                        JSONArray commentList = obj.getJSONArray("comments");
                        for (int i = 0; i < commentList.length(); i++)
                           {
                              Comment comment = new Comment();
                              comment.parseJSONObject(commentList.getJSONObject(i));
                              comments.add(comment);
                           }
                     }
                  else
                     {
                        commentFileLink = obj.getString("commentFileLink");
                        commentFileKey = obj.getString("commentFileKey");
                     }

               }
            catch (JSONException e)
               {
                  e.printStackTrace();
               }
         }
   }