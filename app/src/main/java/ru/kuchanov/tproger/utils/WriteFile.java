package ru.kuchanov.tproger.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import ru.kuchanov.tproger.robospice.request.RoboSpiceRequestCategoriesArts;

/**
 * Created by Юрий on 28.01.2016 21:36.
 * For TProger.
 */
public class WriteFile extends AsyncTask<String, Void, String>
{
    public static final String LOG = WriteFile.class.getSimpleName();

    String pathToFile;
    Context ctx;
    String data;
    String dirToWrite;
    String fileName;

    public WriteFile(String data, String dirToWrite, String fileName, Context ctx)
    {
        this.data = data;
        this.dirToWrite = dirToWrite;
        this.fileName = fileName;
        this.ctx = ctx;
    }

    protected String doInBackground(String... str)
    {
        String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
//        storagePath = ctx.getFilesDir().getAbsolutePath();
        File dirToWriteFile = new File(storagePath + "/" + dirToWrite);
        if (!dirToWriteFile.exists())
        {
            dirToWriteFile.mkdirs();
        }
        pathToFile = dirToWriteFile + "/" + fileName;
        File writenedFile = new File(pathToFile);
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writenedFile, false), "UTF8"));
            bw.write(data);
            bw.close();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return writenedFile.getAbsolutePath();
    }

    protected void onPostExecute(String storagePath)
    {
        Log.d(LOG, "Saved " + storagePath);
//        System.out.println("Saved " + fileName);
    }
}