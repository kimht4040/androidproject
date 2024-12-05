package com.example.myapplication;
// MainActivity.java
import androidx.recyclerview.widget.LinearLayoutManager;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TimePicker timePicker;
    private Button setAlarmButton;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private CheckBox[] dayCheckBoxes = new CheckBox[7];
    private int repeatCount = 1;
    private TextView repeatCountText;

    private AlarmDBHelper dbHelper;
    private AlarmAdapter alarmAdapter;
    private RecyclerView recyclerView;

    @RequiresApi(api = Build.VERSION_CODES.S)
    private boolean hasAlarmPermission() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        return alarmManager.canScheduleExactAlarms();
    }

    private void requestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasAlarmPermission()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }
    private void deleteAlarm(AlarmData alarm) {
        // 알람 매니저에서 알람 취소
        Intent intent = new Intent(this, AlarmReceiver.class);
        for (int i = 0; i < 7; i++) {
            if (alarm.getDays()[i]) {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        i * 100,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                );
                alarmManager.cancel(pendingIntent);
            }
        }

        // 데이터베이스에서 알람 삭제
        dbHelper.deleteAlarm(alarm.getId());

        // 목록 업데이트
        alarmAdapter.updateAlarms(dbHelper.getAllAlarms());
        Toast.makeText(this, "알람이 삭제되었습니다", Toast.LENGTH_SHORT).show();
    }
    private void requestPermissions() {
        // Android 13 이상에서 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1);
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // RecyclerView 초기화
            recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // 데이터베이스 헬퍼 초기화
            dbHelper = new AlarmDBHelper(this);

            // TimePicker 초기화
            timePicker = findViewById(R.id.timePicker);

            // 버튼 초기화
            setAlarmButton = findViewById(R.id.setAlarmButton);
            setAlarmButton.setOnClickListener(v -> setAlarm());

            // 요일 체크박스 초기화
            initializeDayCheckboxes();

            // 반복 횟수 관련 뷰 초기화
            initializeRepeatViews();

            // 알람 매니저 초기화
            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            // 알람 어댑터 초기화
            alarmAdapter = new AlarmAdapter(dbHelper.getAllAlarms(), alarm -> deleteAlarm(alarm));
            recyclerView.setAdapter(alarmAdapter);

        } catch (Exception e) {
            Log.e("MainActivity", "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "앱 초기화 중 오류가 발생했습니다", Toast.LENGTH_LONG).show();
        }
    }
    private void initializeViews() {
        timePicker = findViewById(R.id.timePicker);
        setAlarmButton = findViewById(R.id.setAlarmButton);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        repeatCountText = findViewById(R.id.text_repeat_count);

        // Initialize day checkboxes
        dayCheckBoxes[0] = findViewById(R.id.checkBox_sun);
        dayCheckBoxes[1] = findViewById(R.id.checkBox_mon);
        dayCheckBoxes[2] = findViewById(R.id.checkBox_tue);
        dayCheckBoxes[3] = findViewById(R.id.checkBox_wed);
        dayCheckBoxes[4] = findViewById(R.id.checkBox_thu);
        dayCheckBoxes[5] = findViewById(R.id.checkBox_fri);
        dayCheckBoxes[6] = findViewById(R.id.checkBox_sat);
    }
    private void initializeDayCheckboxes() {
        dayCheckBoxes = new CheckBox[7];
        dayCheckBoxes[0] = findViewById(R.id.checkBox_sun);
        dayCheckBoxes[1] = findViewById(R.id.checkBox_mon);
        dayCheckBoxes[2] = findViewById(R.id.checkBox_tue);
        dayCheckBoxes[3] = findViewById(R.id.checkBox_wed);
        dayCheckBoxes[4] = findViewById(R.id.checkBox_thu);
        dayCheckBoxes[5] = findViewById(R.id.checkBox_fri);
        dayCheckBoxes[6] = findViewById(R.id.checkBox_sat);
    }
    private void initializeRepeatViews() {
        TextView repeatCountText = findViewById(R.id.text_repeat_count);
        Button btnMinus = findViewById(R.id.btn_minus);
        Button btnPlus = findViewById(R.id.btn_plus);

        btnMinus.setOnClickListener(v -> {
            if (repeatCount > 1) {
                repeatCount--;
                repeatCountText.setText(String.valueOf(repeatCount));
            }
        });

        btnPlus.setOnClickListener(v -> {
            repeatCount++;
            repeatCountText.setText(String.valueOf(repeatCount));
        });
    }
    private void setupClickListeners() {
        findViewById(R.id.btn_minus).setOnClickListener(v -> {
            if (repeatCount > 1) {
                repeatCount--;
                repeatCountText.setText(String.valueOf(repeatCount));
            }
        });

        findViewById(R.id.btn_plus).setOnClickListener(v -> {
            repeatCount++;
            repeatCountText.setText(String.valueOf(repeatCount));
        });

        setAlarmButton.setOnClickListener(v -> setAlarm());
    }

    private void setAlarm() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!hasAlarmPermission()) {
                    requestAlarmPermission();
                    return;
                }
            }

            boolean[] selectedDays = new boolean[7];
            boolean anyDaySelected = false;
            for (int i = 0; i < 7; i++) {
                selectedDays[i] = dayCheckBoxes[i].isChecked();
                if (selectedDays[i]) anyDaySelected = true;
            }

            if (!anyDaySelected) {
                Toast.makeText(this, "요일을 선택해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            // 각 선택된 요일마다 알람 설정
            for (int i = 0; i < 7; i++) {
                if (selectedDays[i]) {
                    setAlarmForDay(i, hour, minute);
                }
            }

            String message = String.format("알람이 %02d:%02d으로 설정되었습니다 (반복: %d회)",
                    hour, minute, repeatCount);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            long alarmId = dbHelper.addAlarm(hour, minute, selectedDays, repeatCount);

            // 알람 설정 후
            // 목록 업데이트
            alarmAdapter.updateAlarms(dbHelper.getAllAlarms());

        } catch (Exception e) {
            Log.e(TAG, "Error in setAlarm: ", e);
            Toast.makeText(this,
                    "알람 설정 중 오류가 발생했습니다: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setAlarmForDay(int dayOfWeek, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek + 1); // Calendar.SUNDAY is 1

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("repeatCount", repeatCount);
        intent.putExtra("dayOfWeek", dayOfWeek);

        // 각 요일별로 다른 requestCode 사용
        int requestCode = dayOfWeek * 100;
        pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }
}
