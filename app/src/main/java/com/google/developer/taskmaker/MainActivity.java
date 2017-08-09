package com.google.developer.taskmaker;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.developer.taskmaker.data.DatabaseContract;
import com.google.developer.taskmaker.data.Task;
import com.google.developer.taskmaker.data.TaskAdapter;
import com.google.developer.taskmaker.data.TaskUpdateService;

public class MainActivity extends AppCompatActivity implements
        TaskAdapter.OnItemClickListener,
        View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private TaskAdapter mAdapter;
    private static final int ID_TASK_MAKER_LOADER = 44;
    private static final String TAG = "MainActivity";
    String sortOrder;
    SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAdapter = new TaskAdapter(null);
        mAdapter.setOnItemClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
        FloatingActionButton fabButton = (FloatingActionButton) findViewById(R.id.fab);
        fabButton.setOnClickListener(this);
        getSupportLoaderManager().initLoader(ID_TASK_MAKER_LOADER, null, this);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /* Unregister MainActivity as an OnPreferenceChangedListener to avoid any memory leaks. */
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Click events in Floating Action Button */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.fab:Intent intent = new Intent(this, AddTaskActivity.class);
                startActivity(intent);
                break;
            default:
                break;

        }
    }

    /* Click events in RecyclerView items */
    @Override
    public void onItemClick(View v, int position) {
        //complete: Handle list item click event
        Intent startTaskDetailActivity=new Intent(this,TaskDetailActivity.class);
        Uri singleTaskUri= ContentUris.withAppendedId(DatabaseContract.CONTENT_URI,mAdapter.getItemId(position));
        startTaskDetailActivity.setData(singleTaskUri);
        startActivity(startTaskDetailActivity);
    }

    /* Click events on RecyclerView item checkboxes */
    @Override
    public void onItemToggled(boolean active, int position) {
        //complete: Handle task item checkbox event
        Uri singleTaskUri= ContentUris.withAppendedId(DatabaseContract.CONTENT_URI,mAdapter.getItemId(position));
        ContentValues contentValues=new ContentValues();
        if(active){
            contentValues.put(DatabaseContract.TaskColumns.IS_COMPLETE,1);
        }else{
            contentValues.put(DatabaseContract.TaskColumns.IS_COMPLETE,0);
        }
        if(contentValues!=null) {
            // getContentResolver().update(singleTaskUri, contentValues, null, null);
            TaskUpdateService.updateTask(this,singleTaskUri,contentValues);
        }
        //re-queries for all tasks
        getSupportLoaderManager().restartLoader(ID_TASK_MAKER_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        Log.d(TAG, "onCreateLoader: starts");
        SharedPreferences  sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        String key=getString(R.string.pref_sortBy_key);
        String defaultValue=getString(R.string.pref_sortBy_default);
        String sort=sharedPreferences.getString(key,defaultValue);
        if(sort.equals(getString(R.string.pref_sortBy_default))){
            sortOrder=DatabaseContract.DEFAULT_SORT;
        }
        else{
            sortOrder=DatabaseContract.DATE_SORT;
        }
        switch (loaderId) {

            case ID_TASK_MAKER_LOADER:
                Uri taskUri = DatabaseContract.CONTENT_URI;
                return new CursorLoader(this,
                        taskUri,
                        null,
                        null,
                        null,
                        sortOrder);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: called");
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset: called");
        mAdapter.swapCursor(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_sortBy_key))){
            // re-queries for all tasks
            Log.d(TAG, "onSharedPreferenceChanged: called");
            getSupportLoaderManager().restartLoader(ID_TASK_MAKER_LOADER, null, this);
        }
    }
}
