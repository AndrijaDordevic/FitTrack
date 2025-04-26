package com.myapp.fitnessapp.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.myapp.fitnessapp.R;

public class TimerFragment extends Fragment {
    private static final String CHANNEL_ID = "timer_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final int REQ_POST_NOTIF = 2001;

    private TextView textCountdown;
    private Button btnStart, btnPause, btnReset;

    private CountDownTimer timer;
    private long timeLeftMillis = 0;
    private boolean isRunning = false;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_timer, container, false);

        textCountdown  = root.findViewById(R.id.text_countdown);
        btnStart       = root.findViewById(R.id.btn_start);
        btnPause       = root.findViewById(R.id.btn_pause);
        btnReset       = root.findViewById(R.id.btn_reset);

        createNotificationChannel();

        textCountdown.setOnClickListener(v -> showTimePickerDialog());
        btnStart.setOnClickListener(v -> startTimer());
        btnPause.setOnClickListener(v -> pauseTimer());
        btnReset.setOnClickListener(v -> resetTimer());

        updateCountdownText();
        updateButtons();
        return root;
    }

    private void showTimePickerDialog() {
        View dlg = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_time_picker, null);
        NumberPicker minP = dlg.findViewById(R.id.picker_minutes);
        NumberPicker secP = dlg.findViewById(R.id.picker_seconds);

        minP.setMinValue(0);
        minP.setMaxValue(59);
        secP.setMinValue(0);
        secP.setMaxValue(59);

        int totSec = (int)(timeLeftMillis/1000);
        minP.setValue(totSec/60);
        secP.setValue(totSec%60);

        new AlertDialog.Builder(requireContext())
                .setTitle("Set Timer")
                .setView(dlg)
                .setPositiveButton("OK", (d, w) -> {
                    int mins = minP.getValue(), secs = secP.getValue();
                    timeLeftMillis = (mins*60L + secs)*1000L;
                    updateCountdownText();
                    updateButtons();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startTimer() {
        if (isRunning || timeLeftMillis <= 0) return;
        timer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long ms) {
                timeLeftMillis = ms;
                updateCountdownText();
            }
            @Override
            public void onFinish() {
                onTimerFinished();
            }
        }.start();
        isRunning = true;
        updateButtons();
    }

    private void pauseTimer() {
        if (!isRunning) return;
        timer.cancel();
        isRunning = false;
        updateButtons();
    }

    private void resetTimer() {
        if (timer != null) timer.cancel();
        isRunning = false;
        timeLeftMillis = 0;
        updateCountdownText();
        updateButtons();
    }

    private void updateCountdownText() {
        int tot = (int)(timeLeftMillis/1000);
        textCountdown.setText(
                String.format("%02d:%02d", tot/60, tot%60)
        );
    }

    private void updateButtons() {
        btnStart.setEnabled(!isRunning && timeLeftMillis>0);
        btnPause.setEnabled(isRunning);
        btnReset.setEnabled(!isRunning && timeLeftMillis>0);
    }

    private void onTimerFinished() {
        isRunning = false;
        timeLeftMillis = 0;
        updateCountdownText();
        updateButtons();
        maybeSendNotification();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(
                    CHANNEL_ID, "Timer Alerts", NotificationManager.IMPORTANCE_DEFAULT
            );
            chan.setDescription("Notifies when a countdown ends");
            NotificationManager nm =
                    requireContext().getSystemService(NotificationManager.class);
            nm.createNotificationChannel(chan);
        }
    }

    private void maybeSendNotification() {
        Context ctx = requireContext();
        // On Android 13+, check/request permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQ_POST_NOTIF
            );
            return;
        }
        sendNotification();
    }

    private void sendNotification() {
        Context ctx = requireContext();

        // Explicitly check POST_NOTIFICATIONS before trying to notify
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // permission not granted → skip sending
            return;
        }

        // launch intent for your app
        Intent launch = ctx.getPackageManager()
                .getLaunchIntentForPackage(ctx.getPackageName());
        PendingIntent pi = PendingIntent.getActivity(
                ctx, 0, launch,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder nb = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.notif_timer)
                .setContentTitle("Timer complete!")
                .setContentText("Your countdown has finished.")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // now it's safe to call
        NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, nb.build());
    }

}
