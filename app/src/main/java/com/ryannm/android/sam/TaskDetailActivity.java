package com.ryannm.android.sam;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class TaskDetailActivity extends SinglePaneActivity implements TaskDetailFragment.Callback {

    private static final String TASK_ID_EXTRA = "taskIdExtra";

    @Override
    Fragment getFragment() {
        return TaskDetailFragment.getFragment(getIntent().getLongExtra(TASK_ID_EXTRA, Long.MAX_VALUE));
    }

    public static Intent newIntent (Context context, Long taskId) {
        Intent i = new Intent(context, TaskDetailActivity.class);
        i.putExtra(TASK_ID_EXTRA, taskId);
        return i;
    }

    @Override
    public void onConfirmTask(Task task) {
        Intent i = new Intent();
        i.putExtra(TaskListFragment.TASK_ID_EXTRA, task.getId());
        setResult(RESULT_OK, i);
        finish();
    }
}
