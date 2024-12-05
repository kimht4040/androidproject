package com.example.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity implements SensorEventListener {
    private TextView textViewSquatCount;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private boolean isSquatDown = false;
    private int squatCount = 0;
    private static final int REQUIRED_SQUATS = 3;
    private static final float SQUAT_UP_THRESHOLD = 9.0f;
    private static final float SQUAT_DOWN_THRESHOLD = 7.0f;
    private float currentZ = 0;
    private long lastUpdateTime;
    private static final long MIN_MOVEMENT_TIME = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textViewSquatCount = findViewById(R.id.textViewSquatCount);
        updateSquatCountText();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer == null) {
                Toast.makeText(this, "가속도 센서를 찾을 수 없습니다.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AlarmReceiver.cancelNotification(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();

            currentZ = event.values[2];
            Log.d("SquatDetector", "Z acceleration: " + currentZ);

            if (currentTime - lastUpdateTime > MIN_MOVEMENT_TIME) {
                if (!isSquatDown && currentZ > SQUAT_UP_THRESHOLD) {
                    Log.d("SquatDetector", "Standing detected: " + currentZ);
                    isSquatDown = true;
                    lastUpdateTime = currentTime;
                }
                else if (isSquatDown && currentZ < SQUAT_DOWN_THRESHOLD) {
                    Log.d("SquatDetector", "Squatting detected: " + currentZ);
                    isSquatDown = false;
                    squatCount++;
                    updateSquatCountText();
                    lastUpdateTime = currentTime;

                    if (squatCount >= REQUIRED_SQUATS) {
                        stopAlarmAndFinish();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void updateSquatCountText() {
        runOnUiThread(() -> {
            textViewSquatCount.setText(String.format("스쿼트: %d / %d\n가속도: %.2f",
                    squatCount, REQUIRED_SQUATS, currentZ));
        });
    }

    private void stopAlarmAndFinish() {
        try {
            AlarmReceiver.stopAlarm(this);
            Toast.makeText(this, "알람이 해제되었습니다!", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Log.e("AlarmActivity", "Error stopping alarm: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button
    }
}