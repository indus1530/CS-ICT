package edu.aku.hassannaqvi.cs_ict.ui.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.aku.hassannaqvi.cs_ict.CONSTANTS;
import edu.aku.hassannaqvi.cs_ict.R;
import edu.aku.hassannaqvi.cs_ict.adapter.SyncListAdapter;
import edu.aku.hassannaqvi.cs_ict.adapter.UploadListAdapter;
import edu.aku.hassannaqvi.cs_ict.contracts.ChildContract;
import edu.aku.hassannaqvi.cs_ict.contracts.FormsContract;
import edu.aku.hassannaqvi.cs_ict.core.DatabaseHelper;
import edu.aku.hassannaqvi.cs_ict.core.MainApp;
import edu.aku.hassannaqvi.cs_ict.databinding.ActivitySyncBinding;
import edu.aku.hassannaqvi.cs_ict.get.GetAllData;
import edu.aku.hassannaqvi.cs_ict.otherClasses.SyncModel;
import edu.aku.hassannaqvi.cs_ict.sync.SyncAllData;
import edu.aku.hassannaqvi.cs_ict.sync.SyncAllPhotos;
import edu.aku.hassannaqvi.cs_ict.sync.SyncDevice;
import edu.aku.hassannaqvi.cs_ict.utils.AndroidUtilityKt;

import static edu.aku.hassannaqvi.cs_ict.utils.CreateTable.DATABASE_NAME;
import static edu.aku.hassannaqvi.cs_ict.utils.CreateTable.DB_NAME;
import static edu.aku.hassannaqvi.cs_ict.utils.CreateTable.PROJECT_NAME;

public class SyncActivity extends AppCompatActivity implements SyncDevice.SyncDevicInterface {
    SharedPreferences.Editor editor;
    SharedPreferences sharedPref;
    String DirectoryName;
    DatabaseHelper db;
    SyncListAdapter syncListAdapter;
    UploadListAdapter uploadListAdapter;
    ActivitySyncBinding bi;
    SyncModel model;
    SyncModel uploadmodel;
    List<SyncModel> list;
    List<SyncModel> uploadlist;
    Boolean listActivityCreated;
    Boolean uploadlistActivityCreated;
    private boolean sync_flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bi = DataBindingUtil.setContentView(this, R.layout.activity_sync);
        bi.setCallback(this);
        list = new ArrayList<>();
        uploadlist = new ArrayList<>();
        bi.noItem.setVisibility(View.VISIBLE);
        bi.noDataItem.setVisibility(View.VISIBLE);
        listActivityCreated = true;
        uploadlistActivityCreated = true;
        sharedPref = getSharedPreferences("src", MODE_PRIVATE);
        editor = sharedPref.edit();
        db = new DatabaseHelper(this);
        dbBackup();
        sync_flag = getIntent().getBooleanExtra(CONSTANTS.SYNC_LOGIN, false);

        setAdapter();
        setUploadAdapter();
    }

    public void btnOnDownloadData(View v) {
        if (AndroidUtilityKt.isNetworkConnected(this)) {
            bi.actionLbl.setText("Downloading Data...");
            if (sync_flag) new SyncData(SyncActivity.this, MainApp.DIST_ID).execute(true);
            else new SyncDevice(SyncActivity.this, true).execute();
        } else {
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_SHORT).show();
        }
    }

    public void btnOnUploadData(View v) {
        if (AndroidUtilityKt.isNetworkConnected(this)) {
            bi.actionLbl.setText("Uploading Data...");
            DatabaseHelper db = new DatabaseHelper(this);

            new SyncDevice(this, false).execute();
//  *******************************************************Forms*********************************
            Toast.makeText(getApplicationContext(), "Syncing Forms", Toast.LENGTH_SHORT).show();
            if (uploadlistActivityCreated) {
                uploadmodel = new SyncModel();
                uploadmodel.setstatusID(0);
                uploadlist.add(uploadmodel);
            }
            new SyncAllData(
                    this,
                    "Forms",
                    "updateSyncedForms",
                    FormsContract.class,
                    MainApp._HOST_URL + MainApp._SERVER_URL,
                    FormsContract.FormsTable.TABLE_NAME,
                    db.getUnsyncedForms(), 0, uploadListAdapter, uploadlist
            ).execute();


            if (uploadlistActivityCreated) {
                uploadmodel = new SyncModel();
                uploadmodel.setstatusID(0);
                uploadlist.add(uploadmodel);
            }
            new SyncAllData(
                    this,
                    "Child",
                    "updateSyncedChildForms",
                    ChildContract.class,
                    MainApp._HOST_URL + MainApp._SERVER_URL,
                    ChildContract.ChildTable.TABLE_NAME,
                    db.getUnsyncedChildForms(), 1, uploadListAdapter, uploadlist
            ).execute();

            bi.noDataItem.setVisibility(View.GONE);

            uploadlistActivityCreated = false;

            SharedPreferences syncPref = getSharedPreferences("SyncInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = syncPref.edit();

            editor.putString("LastDataUpload", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));
            editor.apply();

        } else {
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_SHORT).show();
        }
    }

    public void btnUploadPhotos(View view) {

        File directory = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), PROJECT_NAME);
        Log.d("Directory", "uploadPhotos: " + directory);
        if (directory.exists()) {
            File[] files = directory.listFiles(file -> (file.getPath().endsWith(".jpg") || file.getPath().endsWith(".jpeg")));
            Log.d("Files", "Count: " + files.length);
            if (files.length > 0) {
                for (File file : files) {
                    Log.d("Files", "FileName:" + file.getName());
                    SyncAllPhotos syncAllPhotos = new SyncAllPhotos(file.getName(), this);
                    syncAllPhotos.execute();
                    try {
                        //3000 ms delay to process upload of next file.
                        Thread.sleep(3000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                editor.putString("LastPhotoUpload", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));
                editor.apply();
            } else {
                Toast.makeText(this, "No photos to upload.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No photos were taken", Toast.LENGTH_SHORT).show();
        }
    }

    void setAdapter() {
        syncListAdapter = new SyncListAdapter(list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        bi.rvSyncList.setLayoutManager(mLayoutManager);
        bi.rvSyncList.setItemAnimator(new DefaultItemAnimator());
        bi.rvSyncList.setAdapter(syncListAdapter);
        syncListAdapter.notifyDataSetChanged();
        if (syncListAdapter.getItemCount() > 0) {
            bi.noItem.setVisibility(View.GONE);
        } else {
            bi.noItem.setVisibility(View.VISIBLE);
        }
    }

    void setUploadAdapter() {
        uploadListAdapter = new UploadListAdapter(uploadlist);
        RecyclerView.LayoutManager mLayoutManager2 = new LinearLayoutManager(getApplicationContext());
        bi.rvUploadList.setLayoutManager(mLayoutManager2);
        bi.rvUploadList.setItemAnimator(new DefaultItemAnimator());
        bi.rvUploadList.setAdapter(uploadListAdapter);
        uploadListAdapter.notifyDataSetChanged();
        if (uploadListAdapter.getItemCount() > 0) {
            bi.noDataItem.setVisibility(View.GONE);
        } else {
            bi.noDataItem.setVisibility(View.VISIBLE);
        }
    }

    private void dbBackup() {

        if (sharedPref.getBoolean("flag", false)) {

            String dt = sharedPref.getString("dt", "");

            if (!dt.equals(new SimpleDateFormat("dd-MM-yy").format(new Date()))) {
                editor.putString("dt", new SimpleDateFormat("dd-MM-yy").format(new Date()));
                editor.apply();
            }

            File folder = new File(Environment.getExternalStorageDirectory() + File.separator + PROJECT_NAME);
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }
            if (success) {
                DirectoryName = folder.getPath() + File.separator + sharedPref.getString("dt", "");
                folder = new File(DirectoryName);
                if (!folder.exists()) {
                    success = folder.mkdirs();
                }
                if (success) {

                    try {
                        File dbFile = new File(this.getDatabasePath(DATABASE_NAME).getPath());
                        FileInputStream fis = new FileInputStream(dbFile);

                        String outFileName = DirectoryName + File.separator + DB_NAME;

                        // Open the empty db as the output stream
                        OutputStream output = new FileOutputStream(outFileName);

                        // Transfer bytes from the inputfile to the outputfile
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            output.write(buffer, 0, length);
                        }
                        // Close the streams
                        output.flush();
                        output.close();
                        fis.close();
                    } catch (IOException e) {
                        Log.e("dbBackup:", e.getMessage());
                    }

                }

            } else {
                Toast.makeText(this, "Not create folder", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void processFinish(boolean flag) {
        if (flag) {
            MainApp.appInfo.updateTagName(SyncActivity.this);
            new SyncData(SyncActivity.this, MainApp.DIST_ID).execute(sync_flag);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_OK);
        finish();
    }

    private class SyncData extends AsyncTask<Boolean, String, String> {

        private final Context mContext;
        private final String distID;

        private SyncData(Context mContext, String districtId) {
            this.mContext = mContext;
            this.distID = districtId;
        }

        @Override
        protected String doInBackground(Boolean... booleans) {
            runOnUiThread(() -> {

                if (booleans[0]) {
//                  getting Users!!
                    if (listActivityCreated) {
                        model = new SyncModel();
                        model.setstatusID(0);
                        list.add(model);
                    }
                    new GetAllData(mContext, "User", syncListAdapter, list).execute();

//                    Getting App Version
                    if (listActivityCreated) {
                        model = new SyncModel();
                        model.setstatusID(0);
                        list.add(model);
                    }
                    new GetAllData(mContext, "VersionApp", syncListAdapter, list).execute();

//                  getting Districts!!
                    if (listActivityCreated) {
                        model = new SyncModel();
                        model.setstatusID(0);
                        list.add(model);
                    }
                    new GetAllData(mContext, "District", syncListAdapter, list).execute();
                    bi.noItem.setVisibility(View.GONE);

                } else {

//                   getting BL Random
                    if (listActivityCreated) {
                        model = new SyncModel();
                        model.setstatusID(0);
                        list.add(model);
                    }
                    new GetAllData(mContext, "BLRandom", syncListAdapter, list).execute(distID);

//                    Getting Enumblocks
                    if (listActivityCreated) {
                        model = new SyncModel();
                        model.setstatusID(0);
                        list.add(model);
                    }
                    new GetAllData(mContext, "EnumBlock", syncListAdapter, list).execute(distID);
                    bi.noItem.setVisibility(View.GONE);

                }

                listActivityCreated = false;
            });

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            new Handler().postDelayed(() -> {
                editor.putString("LastDataDownload", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));
                editor.apply();
                editor.putBoolean("flag", true);
                editor.commit();

                dbBackup();

            }, 1200);
        }
    }
}
