package com.posn.commentpropagation;


import android.os.Environment;

public class Constants
   {

      // wall post types
      public static final int POST_TYPE_STATUS = 0;
      public static final int POST_TYPE_PHOTO = 1;

      // wall file types
      public static final int TYPE_EMBEDDED_COMMENTS = 0;
      public static final int TYPE_LINK_COMMENTS = 1;

      // device file paths
      public static final String testFilePath = Environment.getExternalStorageDirectory() + "/Android/data/com.posn.commentpropagation/data/test_files";


      // file names
      public static final String embedded_comments_wall_file = "embedded_wall_file.txt";
      public static final String comment_link_wall_file = "link_wall_file.txt";

      // cloud directory names
      public static final String cloudDirectory = "test_files";
      public static final String resultDirectory = "results";


      public static final String[] directoryNames = {
          "test_files",
      };

      public static final int NUM_DIRECTORIES = 1;

   }
