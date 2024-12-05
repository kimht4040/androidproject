package com.example.myapplication;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String TAG = "AlarmReceiver";
    public static MediaPlayer mediaPlayer;
    public static int currentRepeatCount = 0;
    public static int maxRepeatCount = 1;
    public static int currentDayOfWeek = -1;
    public static final String CHANNEL_ID = "ALARM_CHANNEL";
    public static final int NOTIFICATION_ID = 1;
    private static final String ACTION_STOP_ALARM = "com.example.myapplication.STOP_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_STOP_ALARM.equals(intent.getAction())) {
            stopAlarm(context);
            return;
        }

        try {
            maxRepeatCount = intent.getIntExtra("repeatCount", 1);
            int dayOfWeek = intent.getIntExtra("dayOfWeek", -1);

            if (currentDayOfWeek != dayOfWeek) {
                currentDayOfWeek = dayOfWeek;
                currentRepeatCount = 0;
            }

            if (currentRepeatCount < maxRepeatCount) {
                playAlarmAndShowNotification(context);
                currentRepeatCount++;

                if (currentRepeatCount < maxRepeatCount) {
                    setNextAlarm(context, intent);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in AlarmReceiver: ", e);
        }
    }

    private void playAlarmAndShowNotification(Context context) {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            }

            mediaPlayer = MediaPlayer.create(context, R.raw.basketcase);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();

            createNotificationChannel(context);

            Intent stopIntent = new Intent(context, AlarmReceiver.class);
            stopIntent.setAction(ACTION_STOP_ALARM);
            PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    stopIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            Intent alarmActivityIntent = new Intent(context, AlarmActivity.class);
            alarmActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(
                    context,
                    1,
                    alarmActivityIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("알람")
                    .setContentText("알람이 울리고 있습니다")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setContentIntent(contentIntent)
                    .addAction(android.R.drawable.ic_menu_close_clear_cancel, "알람 끄기", stopPendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, builder.build());

        } catch (Exception e) {
            Log.e(TAG, "Error playing alarm: ", e);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "알람",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("알람 앱의 알림 채널입니다");
            channel.setSound(null, null);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setNextAlarm(Context context, Intent originalIntent) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 7);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("repeatCount", maxRepeatCount);
        intent.putExtra("dayOfWeek", currentDayOfWeek);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                currentDayOfWeek * 100,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }

    public static void cancelNotification(Context context) {
        try {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID);
        } catch (Exception e) {
            Log.e(TAG, "Error canceling notification: " + e.getMessage());
        }
    }

    public static void stopAlarm(Context context) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        cancelNotification(context);
    }
}