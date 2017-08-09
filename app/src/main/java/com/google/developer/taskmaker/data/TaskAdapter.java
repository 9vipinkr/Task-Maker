package com.google.developer.taskmaker.data;

import android.content.Context;
import android.database.Cursor;
import android.os.Trace;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.developer.taskmaker.R;
import com.google.developer.taskmaker.views.TaskTitleView;

import java.util.Calendar;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {
    /* Callback for list item click events */
    private static final String TAG = "TaskAdapter";
    public interface OnItemClickListener {
        void onItemClick(View v, int position);

        void onItemToggled(boolean active, int position);
    }

    /* ViewHolder for each task item */
    public class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TaskTitleView nameView;
        public TextView dateView;
        public ImageView priorityView;
        public CheckBox checkBox;

        public TaskHolder(View itemView) {
            super(itemView);

            nameView = (TaskTitleView) itemView.findViewById(R.id.text_description);
            dateView = (TextView) itemView.findViewById(R.id.text_date);
            priorityView = (ImageView) itemView.findViewById(R.id.priority);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);

            itemView.setOnClickListener(this);
            checkBox.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == checkBox) {
                completionToggled(this);
            } else {
                postItemClick(this);
            }
        }
    }

    private Cursor mCursor;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;

    public TaskAdapter(Cursor cursor) {
        mCursor = cursor;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    private void completionToggled(TaskHolder holder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemToggled(holder.checkBox.isChecked(), holder.getAdapterPosition());
        }
    }

    private void postItemClick(TaskHolder holder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
        }
    }

    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.list_item_task, parent, false);

        return new TaskHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TaskHolder holder, int position) {
        long start=System.currentTimeMillis();
        boolean isComplete=false;
        //completed: Bind the task data to the views
        mCursor.moveToPosition(position);
        Task task=new Task(mCursor);
        TaskTitleView taskTitleView=new TaskTitleView(holder.nameView.getContext());
        //setting states for the tasks

        Calendar dueDate=Calendar.getInstance();
        dueDate.setTimeInMillis(DatabaseContract.getColumnLong(mCursor, DatabaseContract.TaskColumns.DUE_DATE));
        Calendar currentDate=Calendar.getInstance();
        if(DatabaseContract.getColumnInt(mCursor,DatabaseContract.TaskColumns.IS_COMPLETE)==1)
        {
            taskTitleView.setState(TaskTitleView.DONE);
            isComplete=true;
        }
        else if(currentDate.after(dueDate)){
            taskTitleView.setState(TaskTitleView.OVERDUE);
        }
        else {
            taskTitleView.setState(TaskTitleView.NORMAL);
        }
        // binding data //
        //binding task description
        holder.nameView.setText(DatabaseContract.getColumnString(mCursor,DatabaseContract.TaskColumns.DESCRIPTION));
        holder.nameView.setState(taskTitleView.getState());
        holder.checkBox.setChecked(isComplete);
        //binding due date
        if(!task.hasDueDate()){
            holder.dateView.setVisibility(View.GONE);
        }
        else {
            holder.dateView.setVisibility(View.VISIBLE);
            CharSequence formatted=DateUtils.getRelativeTimeSpanString(mContext,DatabaseContract.getColumnLong(mCursor, DatabaseContract.TaskColumns.DUE_DATE));
            holder.dateView.setText(formatted);
        }
        //binding priority
        if(DatabaseContract.getColumnInt(mCursor,DatabaseContract.TaskColumns.IS_PRIORITY)==1) {
            holder.priorityView.setImageResource(R.drawable.ic_priority);
        }
        else{
            holder.priorityView.setImageResource(R.drawable.ic_not_priority);
        }

        Log.d(TAG, "onBindViewHolder: time taken: "+(System.currentTimeMillis()-start)+" ms");
    }

    @Override
    public int getItemCount() {
        return (mCursor != null) ? mCursor.getCount() : 0;
    }

    /**
     * Retrieve a {@link Task} for the data at the given position.
     *
     * @param position Adapter item position.
     *
     * @return A new {@link Task} filled with the position's attributes.
     */
    public Task getItem(int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Invalid item position requested");
        }

        return new Task(mCursor);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }
}
