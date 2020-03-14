package com.thejauffre.soundboard.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.thejauffre.soundboard.Classes.CustomAdapter;
import com.thejauffre.soundboard.R;
import com.thejauffre.soundboard.Classes.Common;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thejauffre.soundboard.Classes.Common.LOGTAG;
import static com.thejauffre.soundboard.Classes.Common.copyInputStreamToFile;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 10;

    private ArrayAdapter<String> arrayAdapter;
    private ListView listView;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<CustomAdapter.ViewHolder> mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add(getString(R.string.write_perm));
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add(getString(R.string.read_perm));

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = getString(R.string.permission) + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }
        mainCode();
    }

    public ProgressDialog loadingDialog;
    public Thread loadingThread;

    private void mainCode()
    {
        try
        {
            File f = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
            if (f.exists())
            {
                displayAndExecute(f);
            }
            else
            {
                loadingThread = new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            File f = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
                            f.mkdirs();
                            InputStream ins = getResources().openRawResource(R.raw.sample);
                            File temp_file = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "_tmp");
                            copyInputStreamToFile(ins, temp_file);
                            Common.unzip(temp_file , f);
                            temp_file.delete();
                        }
                        catch (Exception e)
                        {
                            Log.d(LOGTAG, e.getMessage());
                        }
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run() {
                                loadingDialog.dismiss();
                            }
                        });
                    }
                };
                loadingDialog = ProgressDialog.show(this,
                        "", getResources().getString(R.string.unpacking_file),true);
                loadingThread.start();
                loadingThread.join();
                displayAndExecute(f);
            }
        }
        catch (Exception e)
        {
            Log.d(LOGTAG, e.getMessage());
        }
    }

    private void displayAndExecute(File directory)
    {
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        File[] files = directory.listFiles();
        if (files != null && files.length > 1) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File object1, File object2) {
                    return object1.getName().compareTo(object2.getName());
                }
            });
        }
        List<String> allFiles = new ArrayList<>();

        for (int i = 0; i < files.length; i++)
        {
            String fname = files[i].getName();
            Log.d("Files", "FileName:" + fname);
            if (!fname.equals(getString(R.string.nomedia)))
                allFiles.add(fname);
        }

        // specify an adapter (see also next example)
        mAdapter = new CustomAdapter(this, allFiles);
        recyclerView.setAdapter(mAdapter);
    }

    /***
     * Ask for permissions
     * ***/
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), okListener)
                .setNegativeButton(getString(R.string.cancel), null)
                .create()
                .show();
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            return shouldShowRequestPermissionRationale(permission);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
            {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                {
                    // All Permissions Granted
                    mainCode();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, getString(R.string.permission_denied), Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
