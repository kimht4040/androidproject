package com.example.myapplication;

public class AlarmData {
    private int id;
    private int hour;
    private int minute;
    private boolean[] days;
    private int repeatCount;

    public AlarmData(int id, int hour, int minute, boolean[] days, int repeatCount) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.days = days;
        this.repeatCount = repeatCount;
    }

    public int getId() { return id; }
    public int getHour() { return hour; }
    public int getMinute() { return minute; }
    public boolean[] getDays() { return days; }
    public int getRepeatCount() { return repeatCount; }

    public String getTimeString() {
        return String.format("%02d:%02d", hour, minute);
    }

    public String getDaysString() {
        StringBuilder sb = new StringBuilder();
        String[] dayNames = {"일", "월", "화", "수", "목", "금", "토"};
        for (int i = 0; i < days.length; i++) {
            if (days[i]) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(dayNames[i]);
            }
        }
        return sb.toString();
    }
}