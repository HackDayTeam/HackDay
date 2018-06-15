package com.lee.edu.mydemo;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * Created by dmrf on 18-1-25.
 */

public class SaveFile extends AsyncTask<String, Void, Void> {

    private String filename;
    private String filepath;
    private double[] content;
    private int length;

    public SaveFile(String filename, String filepath, double[] content, int length) {
        this.filename = filename;
        this.filepath = filepath;
        this.content = content;
        this.length = length;
    }


    @Override
    protected Void doInBackground(String... strings) {
        File file = new File(filepath + filename);

        FileOutputStream outStream = null;
        try {
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            outStream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(outStream, "gb2312");
            for (int i = 1; i <= length; i++) {
                writer.write(String.valueOf(content[i]));//这边改成浮点型不知道会不会出错
                writer.write("\n");
            }
            writer.close();
            outStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
