package ru.kuchanov.tproger.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Юрий on 22.10.2015 17:56.
 * For ExpListTest.
 */
public class DataBaseFileSaver
{
    // Copy to sdcard for debug use

    /**
     * @return path to saved DB or error msg
     */
    public static String copyDatabase(Context c, String DATABASE_NAME)
    {
        String result;

        String databasePath = c.getDatabasePath(DATABASE_NAME).getPath();
        File f = new File(databasePath);
        OutputStream myOutput = null;
        InputStream myInput = null;
        Log.i("testing", " testing db path " + databasePath);
        Log.i("testing", " testing db exist " + f.exists());

        if (f.exists())
        {
            try
            {

//                File directory = new File("/mnt/sdcard/DB_DEBUG");
                File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DB_DEBUG");
                result = directory.getAbsolutePath();
                Log.i("testing", " new db path " + directory.getAbsolutePath());
                if (!directory.exists())
                {
                    directory.mkdirs();
                }

                myOutput = new FileOutputStream(directory.getAbsolutePath()
                        + "/" + DATABASE_NAME + ".db");
                myInput = new FileInputStream(databasePath);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0)
                {
                    myOutput.write(buffer, 0, length);
                }

                myOutput.flush();
            }
            catch (Exception e)
            {
                result = e.getLocalizedMessage();
            }
            finally
            {
                try
                {
                    if (myOutput != null)
                    {
                        myOutput.close();
                        myOutput = null;
                    }
                    if (myInput != null)
                    {
                        myInput.close();
                        myInput = null;
                    }
                }
                catch (Exception e)
                {
                }
            }
        }
        else
        {
            result = "DB is not exists";
        }

        return result;
    }
}