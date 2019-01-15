package com.ryannm.android.sam;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.olmur.rvtools.BaseRecyclerAdapter;
import com.olmur.rvtools.RvTools;
import com.olmur.rvtools.SwipeContextMenuDrawer;
import com.olmur.rvtools.property.IOnMoveAction;
import com.olmur.rvtools.property.IOnOrderChangedListener;
import com.olmur.rvtools.property.IOnSwipeLeftAction;
import com.olmur.rvtools.property.IOnSwipeRightAction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import static android.app.Activity.RESULT_OK;

public class TaskListFragment extends Fragment implements IOnSwipeLeftAction,IOnSwipeRightAction,IOnMoveAction,IOnOrderChangedListener {
    private static final int REQUEST_TASK_DETAIL = 857;
    static final String TASK_ID_EXTRA = "task_id_EXtrA";
    private static final String NEW_UNDEFINED_TASK_NAME = "newTaskName";
    private List<Task> tasks = new ArrayList<>();
    private final static int[] COLORS = {android.R.color.holo_blue_dark,android.R.color.holo_orange_light,android.R.color.holo_purple, android.R.color.holo_orange_dark};
    private RecyclerView mRecyclerView;
    private int editableTaskPosition = -1;
    private Punishment punishment;

    @CallSuper
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tasks = App.getDaoSession().getTaskDao().loadAll();
        punishment = App.getDaoSession().getPunishmentDao().load((long) Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
        // todo: The above won't work if end of day is 2 am and I'm checking off tasks at 1 am
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        showAppIntro();
        View v = inflater.inflate(R.layout.task_list_fragment, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(new TaskAdapter(tasks));

        FloatingActionButton FAB = (FloatingActionButton) v.findViewById(R.id.fab);
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });

        new RvTools.Builder(mRecyclerView)
                .withSwipeRightAction(this)
                .withSwipeLeftAction(this)
                .withMoveAction(this, this, 0)
                .withSwipeContextMenuDrawer(new SwipeMenuDrawer())
                .buildAndApplyToRecyclerView();

        Toolbar toolbar = (Toolbar) getActivity().getWindow().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        getActivity().setTitle(R.string.app_name);

        if (!tasks.isEmpty()) v.findViewById(R.id.empty_text_view).setVisibility(View.GONE);

       /* SensorManager sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(new ShakeDetector.Listener() {
            @Override
            public void hearShake() {
                List<Task> tasksToRemove = new ArrayList<>();
                for (Task task: tasks) {
                    if (task.getCompleted()) tasksToRemove.add(task);
                }
                if (!tasksToRemove.isEmpty()) {
                    tasks.removeAll(tasksToRemove);
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                }
            } Not for now
        });
        sd.start(sensorManager); */

        return v;
    }

    private void showAppIntro() {
        if (QueryPreferences.isFirstRun(getActivity())) {
            startActivity(new Intent(getActivity(), AppIntroActivity.class));
        }
    }

    @CallSuper
    @Override
    public void onResume() {
        super.onResume();
        if (!tasks.isEmpty()) getView().findViewById(R.id.empty_text_view).setVisibility(View.GONE);

    }

    private void addTask() {
        Boolean addNewToBottom = QueryPreferences.addNewToBottom(getActivity());

        int position = 0;
        if (addNewToBottom!=null) {
            if (addNewToBottom) position = tasks.size();
        } else {
            //Look out for drags up n down
        }

        ((TaskAdapter) mRecyclerView.getAdapter()).addTask(position);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==RESULT_OK) {
            if (requestCode==REQUEST_TASK_DETAIL) {
                long taskId = data.getLongExtra(TASK_ID_EXTRA, Long.MAX_VALUE);
                if (taskId!=Long.MAX_VALUE) refreshRecyclerView(App.getDaoSession().getTaskDao().load(taskId));
            }
        }
    }

    // Notifies the recyclerView when a task is added or edited
    public void refreshRecyclerView(Task task) {
        if (task==null) return;
        boolean taskAlreadyExists = false;
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(task.getId())) {
                tasks.set(i, task);
                taskAlreadyExists = true;
                mRecyclerView.getAdapter().notifyItemChanged(i);
                break;
            }
        }
        if (!taskAlreadyExists) {
            tasks.add(task);
            ((TaskAdapter) mRecyclerView.getAdapter()).notifyItemInserted(tasks.size()-1);
        }
    }

    @Override
    public void onSwipeLeft(int position) {
        destructTask(position);
        // todo: SnackBar task deleted
    }

    private void destructTask(int position) {
        ((TaskAdapter) mRecyclerView.getAdapter()).removeTask(position);
    }

    @Override
    public void onSwipeRight(int position) {
        destructTask(position);
        // todo: Snackbar task done
    }


    private class TaskAdapter extends BaseRecyclerAdapter<Task, TaskHolder> {
        private static final int SIMPLE_TASK_COLLAPSED = 7;
        private static final int EDITABLE_TASK_COLLAPSED = 6;

        TaskAdapter(List<Task> tasks) {
            super(TaskListFragment.this.getActivity());
            mAdapterContent = tasks;
        }

        @Override
        public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType==EDITABLE_TASK_COLLAPSED) return new TaskHolder(LayoutInflater.from(getActivity()).inflate(R.layout.task_collapsed_editable, parent, false), viewType);
            else return new TaskHolder(LayoutInflater.from(getActivity()).inflate(R.layout.task_collapsed, parent, false), viewType);

            // todo: Maybe show an icon for reminder (preferred) or for earlier deadline

        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        void removeTask(int position) {
            Task t = tasks.remove(position);
            App.getDaoSession().getTaskDao().delete(t);
            notifyItemRemoved(position);
        }

        void addTask(int position) {
            Task task = new Task();
            task.setName(NEW_UNDEFINED_TASK_NAME);
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.set(Calendar.HOUR_OF_DAY, calendar.getMaximum(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendar.getMaximum(Calendar.MINUTE));
            task.setDeadline(calendar.getTime());
            getView().findViewById(R.id.empty_text_view).setVisibility(View.GONE);
            editableTaskPosition = position;
            // tasks.addTask(position, task);
            App.getDaoSession().getTaskDao().insert(task);
            tasks.add(position, task);
            notifyItemInserted(position);
            if (tasks.size()==1 && (punishment==null || punishment.getJobId()==null)) {
                createNewPunishment();
            }
        }

        public int getItemViewType(int position) {
            if (position==editableTaskPosition)
                return EDITABLE_TASK_COLLAPSED;
            else
                return SIMPLE_TASK_COLLAPSED;
        }
    }

    class TaskHolder extends BaseRecyclerAdapter.BaseViewHolder<Task> {
        private TextView mTaskName;
        private EditText mEditableTaskName;
        private CheckBox mTaskCheck;

        TaskHolder(View itemView, int viewType) {
            super(itemView);
            //noinspection deprecation
         //   itemView.setBackgroundColor(getResources().getColor(COLORS[((int)(Math.random()*10)%COLORS.length)]));
            if (viewType==TaskAdapter.SIMPLE_TASK_COLLAPSED) mTaskName = (TextView) itemView.findViewById(R.id.taskName);
            else if (viewType==TaskAdapter.EDITABLE_TASK_COLLAPSED) mEditableTaskName = (EditText) itemView.findViewById(R.id.taskName);

            mTaskCheck = (CheckBox) itemView.findViewById(R.id.taskCheck);
           // mTaskDeadline = (TextView) itemView.findViewById(R.id.taskDeadline);
        }

        @Override
        public void bindView(final Task task) {
            if (getItemViewType()==TaskAdapter.SIMPLE_TASK_COLLAPSED) {
                mTaskName.setText(task.getName());
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult(TaskDetailActivity.newIntent(getActivity(), task.getId()), REQUEST_TASK_DETAIL);
                    }
                });
            }

            else if (getItemViewType()==TaskAdapter.EDITABLE_TASK_COLLAPSED) {
                mEditableTaskName.setText(task.getName());
                if (task.getName().equals(NEW_UNDEFINED_TASK_NAME)) mEditableTaskName.setText("");
                mEditableTaskName.setImeOptions(EditorInfo.IME_ACTION_DONE);

                mEditableTaskName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            //mRecyclerView.getAdapter().notifyItemChanged(getAdapterPosition());
                            task.setName(v.getText().toString());
                            App.getDaoSession().getTaskDao().insertOrReplace(task);
                            editableTaskPosition = -1;
                            mRecyclerView.getAdapter().notifyItemChanged(getAdapterPosition());
                            if (!TrackAppServiceVersion9.isAccessibilityEnabled()) TrackAppServiceVersion9.showAccessibilityDialog(TaskListFragment.this);
                            // todo: Can probably place above line better
                        }
                        return false; // So that it closes automatically
                    }
                });
            }

            mTaskCheck.setChecked(task.getCompleted());
            mTaskCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // todo: Update punishment object and punishment fragment
                    task.setCompleted(isChecked);
                    App.getDaoSession().getTaskDao().insertOrReplace(task);
                    punishment.changeTimingsBy(isChecked? -1.0/(float)tasks.size(): 1.0/(float)tasks.size());

                    if (isChecked) {
                        boolean allTasksDone = true;
                        for (Task task:tasks) {
                            if (!task.getCompleted()) {
                                allTasksDone = false;
                                break;
                            }
                        }
                        if (allTasksDone) {
                            punishment.deleteEndOfDayJob();
                        }
                        if (mTaskName != null)
                            mTaskName.setPaintFlags(mTaskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        else if (mEditableTaskName != null)
                            mEditableTaskName.setPaintFlags(mEditableTaskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
                        int incompleteTasks = 0;
                        for (Task task: tasks) {
                            if (!task.getCompleted()) incompleteTasks++;
                        }
                        if (incompleteTasks==1) { // So this is the only unchecked task, and it's just been unchecked
                            punishment.initEndOfDayJob();
                        }
                        if (mTaskName != null)
                            mTaskName.setPaintFlags(mTaskName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                        else if (mEditableTaskName != null)
                            mEditableTaskName.setPaintFlags(mEditableTaskName.getPaintFlags() &~ Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                }
            });
        }

    }

    private void createNewPunishment() {
        String timings = QueryPreferences.getPunishmentTime(getActivity());
        String[] times = timings.split("-");
        int durationMins = Punishment.convertToMins(times[1]) - Punishment.convertToMins(times[0]);
        if (durationMins<0) durationMins+=Punishment.convertToMins(24);
        punishment = new Punishment(null, timings, null, durationMins);
        punishment.setId((long) new GregorianCalendar().get(Calendar.DAY_OF_YEAR));
        punishment.initEndOfDayJob();
        App.getDaoSession().getPunishmentDao().insertOrReplace(punishment);
    }

    private class SwipeMenuDrawer extends SwipeContextMenuDrawer {
        Drawable mTick;
        Drawable mDelete;

        @SuppressWarnings("deprecation")
        SwipeMenuDrawer() {
            mTick = getResources().getDrawable(R.drawable.ic_tick);
            mDelete = getResources().getDrawable(R.drawable.ic_delete_white);
        }

        @Override
        public void drawRight(@NonNull Canvas canvas, @NonNull View view) { // View that comes up when swiped right
            Paint rightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            rightPaint.setColor(Color.GREEN);
            canvas.drawRect(view.getLeft(), view.getTop(), view.getRight(),view.getBottom(), rightPaint);

            float left = getPixelsFromDp(view.getLeft() + 16);
            float right = left + mTick.getIntrinsicWidth();
            float centreY = ( view.getTop() + view.getBottom() )/2;
            mTick.setBounds((int)left, (int) centreY - mTick.getIntrinsicHeight()/2,(int)right, (int) centreY + mTick.getIntrinsicHeight()/2);
            mTick.draw(canvas);
        }

        private float getPixelsFromDp(int dp) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            return dp/displayMetrics.density; // cos pixels = density-independent pixels*160/dpi & density = dpi/160
        }

        @Override
        public void drawLeft(@NonNull Canvas canvas, @NonNull View view) { // View that comes up when swiped left
            Paint leftPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            leftPaint.setColor(Color.RED);
            canvas.drawRect(view.getLeft(), view.getTop(), view.getRight(),view.getBottom(), leftPaint);

            float right = getPixelsFromDp(view.getRight() - 8);
            float left = right - mDelete.getIntrinsicWidth();
            float centreY = ( view.getTop() + view.getBottom() )/2;
            mDelete.setBounds((int)left, (int) centreY - mDelete.getIntrinsicHeight()/2,(int)right, (int) centreY + mDelete.getIntrinsicHeight()/2);
            mDelete.draw(canvas);
        }
    }

    @Override
    public void onMove(int fromPosition, int toPosition) {
        // Blank
    }

    @Override
    public void onOrderChanged() {
        // Blank
    }

    // Created to workaround error on deleting/ticking task. Error: java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter
    private class WrapContentLinearLayoutManager extends LinearLayoutManager {

        private WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("TaskListFragment", "meet a IOOBE in RecyclerView");
            }
        }
    }

}
