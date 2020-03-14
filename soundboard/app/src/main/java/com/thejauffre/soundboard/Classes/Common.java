package com.thejauffre.soundboard.Classes;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.thejauffre.soundboard.Activities.MainActivity;
import com.thejauffre.soundboard.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Common {

    public static final String LOGTAG = App.getContext().getString(R.string.app_name);
    static int DEFAULT_BUFFER = 65000;
    static int EOF = -1;
    public static MediaPlayer player;

    public static void playFile(String filename)
    {
        try
        {
            player = new MediaPlayer();
            String filePath = Environment.getExternalStorageDirectory() + "/" + App.getContext().getString(R.string.app_name) + "/" + filename;
            player.setDataSource(filePath);
            player.prepare();
            player.start();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                };
            });
        }
        catch (Exception e)
        {
            Log.d(LOGTAG, e.getMessage());
        }

    }

    public static void shareFile(String filename)
    {
        try
        {
            String filePath = Environment.getExternalStorageDirectory() + "/" + App.getContext().getString(R.string.app_name) + "/" + filename;
            File file = new File(filePath);
            Uri uri = FileProvider.getUriForFile(App.getContext(), App.getContext().getPackageName() + ".fileprovider", file);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("audio/*");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            App.getContext().startActivity(Intent.createChooser(share, App.getContext().getString(R.string.share)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        catch (Exception e)
        {
            Log.d(LOGTAG, e.getMessage());
        }
    }

    /**
     * Unzip a ZIP file, keeping the directory structure.
     *
     * @param zipFile
     *     A valid ZIP file.
     * @param destinationDir
     *     The destination directory. It will be created if it doesn't exist.
     * @return {@code true} if the ZIP file was successfully decompressed.
     */
    public static boolean unzip(File zipFile, File destinationDir) {
        ZipFile zip = null;
        try {
            destinationDir.mkdirs();
            zip = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = zipFileEntries.nextElement();
                String entryName = entry.getName();
                File destFile = new File(destinationDir, entryName);
                File destinationParent = destFile.getParentFile();
                if (destinationParent != null && !destinationParent.exists()) {
                    destinationParent.mkdirs();
                }
                if (!entry.isDirectory()) {
                    BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                    int currentByte;
                    byte data[] = new byte[DEFAULT_BUFFER];
                    FileOutputStream fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos, DEFAULT_BUFFER);
                    while ((currentByte = is.read(data, 0, DEFAULT_BUFFER)) != EOF) {
                        dest.write(data, 0, currentByte);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
        } catch (Exception e) {
            return false;
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException ignored) {
                }
            }
        }
        return true;
    }


    // InputStream -> File
    public static void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {

        try (FileOutputStream outputStream = new FileOutputStream(file)) {

            int read;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            // commons-io
            //IOUtils.copy(inputStream, outputStream);

        }

    }

}
