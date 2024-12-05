package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {
    private List<AlarmData> alarmList;
    private OnAlarmDeleteListener deleteListener;

    public interface OnAlarmDeleteListener {
        void onDelete(AlarmData alarm);
    }

    public AlarmAdapter(List<AlarmData> alarmList, OnAlarmDeleteListener listener) {
        this.alarmList = alarmList;
        this.deleteListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AlarmData alarm = alarmList.get(position);
        holder.textTime.setText(alarm.getTimeString());
        holder.textDays.setText(alarm.getDaysString());
        holder.textRepeat.setText("반복: " + alarm.getRepeatCount() + "회");

        holder.buttonDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(alarm);
            }
        });
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public void updateAlarms(List<AlarmData> newAlarmList) {
        this.alarmList = newAlarmList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTime;
        TextView textDays;
        TextView textRepeat;
        ImageButton buttonDelete;

        ViewHolder(View view) {
            super(view);
            textTime = view.findViewById(R.id.textTime);
            textDays = view.findViewById(R.id.textDays);
            textRepeat = view.findViewById(R.id.textRepeat);
            buttonDelete = view.findViewById(R.id.buttonDelete);
        }
    }
}