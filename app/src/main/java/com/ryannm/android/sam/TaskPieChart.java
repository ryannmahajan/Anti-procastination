package com.ryannm.android.sam;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class TaskPieChart extends PieChart {
    public TaskPieChart(Context context) {
        super(context);
    }

    public TaskPieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TaskPieChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void refresh() {
        List<PieEntry> entries = new ArrayList<>();
        Float pendingTasksPercent = Task.getPendingTasksPercent(true);
        entries.add(new PieEntry(pendingTasksPercent, "Pending"));
        entries.add(new PieEntry(100f - pendingTasksPercent, "Completed"));
        PieDataSet set = new PieDataSet(entries, "Tasks");
        PieData data = new PieData(set);
        setUsePercentValues(true);
        setData(data);

        String description = getContext().getString(R.string.none);
        Punishment punishment = App.getDaoSession().getPunishmentDao().load((long) new GregorianCalendar().get(Calendar.DAY_OF_YEAR));
        if (punishment!=null && punishment.getJobId()!=null) description = punishment.getTimings();
        description = getContext().getString(R.string.tomorrow_punishment, description);
        Description desc = new Description();
        desc.setText(description);
        setDescription(desc);
    }

}
