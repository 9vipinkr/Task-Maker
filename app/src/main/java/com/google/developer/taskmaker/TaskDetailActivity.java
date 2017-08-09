package com.google.developer.taskmaker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.developer.taskmaker.data.DatabaseContract;
import com.google.developer.taskmaker.data.Task;
import com.google.developer.taskmaker.data.TaskUpdateService;
import com.google.developer.taskmaker.reminders.AlarmScheduler;
import com.google.developer.taskmaker.views.DatePickerFragment;

import java.util.Calendar;

public class TaskDetailActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener {
    TextView mDescription;
    TextView mDuedate;
    ImageView mPriorityView;
    Cursor mCursor=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_task);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Task must be passed to this activity as a valid provider Uri
        final Uri taskUri = getIntent().getData();
        //completed: Display attributes of the provided task in the UI
        mDescription=(TextView)findViewById(R.id.detail_task_description);
        mDuedate=(TextView)findViewById(R.id.detail_task_due_date);
        mPriorityView=(ImageView) findViewById(R.id.detail_task_priority);
        mCursor=getContentResolver().query(taskUri,null,null,null,null);
        if(mCursor!=null&&mCursor.getCount()>0){
            mCursor.moveToFirst();
        }
        Task task=new Task(mCursor);
        mDescription.setText(DatabaseContract.getColumnString(mCursor,DatabaseContract.TaskColumns.DESCRIPTION));
        if(task.hasDueDate()) {
            CharSequence formatted=DateUtils.getRelativeTimeSpanString(this,DatabaseContract.getColumnLong(mCursor, DatabaseContract.TaskColumns.DUE_DATE));
            mDuedate.setText(getString(R.string.task_date)+"  "+formatted);
        }
        else{
            mDuedate.setText(getString(R.string.task_date)+"  "+getString(R.string.date_empty));
        }
        if(DatabaseContract.getColumnInt(mCursor,DatabaseContract.TaskColumns.IS_PRIORITY)==1) {
            mPriorityView.setImageResource(R.drawable.ic_priority);
        }
        else{
            mPriorityView.setImageResource(R.drawable.ic_not_priority);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_detail, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            // getContentResolver().delete(getIntent().getData(),null,null);
            TaskUpdateService.deleteTask(this,getIntent().getData());
            finish();
            return true;
        }
        else if(id==R.id.action_reminder){
            DatePickerFragment dialogFragment = new DatePickerFragment();
            dialogFragment.show(getSupportFragmentManager(), "datePicker");
            return true;
        }
        else if(id==R.id.home){
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        //completed: Handle date selection from a DatePickerFragment
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY,12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        if(Calendar.getInstance().before(c)) {
            AlarmScheduler.scheduleAlarm(this, c.getTimeInMillis(), getIntent().getData());
        }
    }
}
