package com.posn.commentpropagation;


import android.os.Environment;

public class Constants
   {

      // device file paths
      public static final String testFilePath = Environment.getExternalStorageDirectory() + "/Android/data/com.posn.commentpropagation/data/test_files";


      // device file names
      public static final String test_files = "/user_comments.txt";


      // cloud directory names
      public static final String wallDirectory = "wall";


      public static final String[] directoryNames = {
          "test_files",
      };

      public static final int NUM_DIRECTORIES = 1;

   }
