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
        // Inflate layout and bind UI elements
        View root = inflater.inflate(R.layout.fragment_timer, container, false);
        textCountdown  = root.findViewById(R.id.text_countdown);
        btnStart       = root.findViewById(R.id.btn_start);
        btnPause       = root.findViewById(R.id.btn_pause);
        btnReset       = root.findViewById(R.id.btn_reset);

        // Prepare notification channel for timer alerts
        createNotificationChannel();

        // Set click handlers: tapping time opens picker, buttons control timer
        textCountdown.setOnClickListener(v -> showTimePickerDialog());
        btnStart.setOnClickListener(v -> startTimer());
        btnPause.setOnClickListener(v -> pauseTimer());
        btnReset.setOnClickListener(v -> resetTimer());

        // Initialize display and buttons based on current state
        updateCountdownText();
        updateButtons();
        return root;
    }

    // Dialog to pick minutes and seconds for the countdown
    private void showTimePickerDialog() {
        View dlg = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_time_picker, null);
        NumberPicker minP = dlg.findViewById(R.id.picker_minutes);
        NumberPicker secP = dlg.findViewById(R.id.picker_seconds);

        // Configure pickers range
        minP.setMinValue(0);
        minP.setMaxValue(59);
        secP.setMinValue(0);
        secP.setMaxValue(59);

        // Pre-set to current time left
        int totSec = (int)(timeLeftMillis/1000);
        minP.setValue(totSec/60);
        secP.setValue(totSec%60);

        new AlertDialog.Builder(requireContext())
                .setTitle("Set Timer")
                .setView(dlg)
                .setPositiveButton("OK", (d, w) -> {
                    // Update countdown duration
                    int mins = minP.getValue(), secs = secP.getValue();
                    timeLeftMillis = (mins*60L + secs)*1000L;
                    updateCountdownText();
                    updateButtons();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Start the countdown if not already running
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

    // Pause the countdown
    private void pauseTimer() {
        if (!isRunning) return;
        timer.cancel();
        isRunning = false;
        updateButtons();
    }

    // Reset countdown to zero
    private void resetTimer() {
        if (timer != null) timer.cancel();
        isRunning = false;
        timeLeftMillis = 0;
        updateCountdownText();
        updateButtons();
    }

    // Update the displayed time in MM:SS format
    private void updateCountdownText() {
        int tot = (int)(timeLeftMillis/1000);
        textCountdown.setText(
                String.format("%02d:%02d", tot/60, tot%60)
        );
    }

    // Enable/disable buttons based on state
    private void updateButtons() {
        btnStart.setEnabled(!isRunning && timeLeftMillis>0);
        btnPause.setEnabled(isRunning);
        btnReset.setEnabled(!isRunning && timeLeftMillis>0);
    }

    // Handle timer completion: reset and notify
    private void onTimerFinished() {
        isRunning = false;
        timeLeftMillis = 0;
        updateCountdownText();
        updateButtons();
        maybeSendNotification();
    }

    // Create a notification channel for API >= 26
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(
                    CHANNEL_ID, "Timer Alerts", NotificationManager.IMPORTANCE_DEFAULT
            );
            chan.setDescription("Notifies when a countdown ends");
            NotificationManager nm = requireContext().getSystemService(NotificationManager.class);
            nm.createNotificationChannel(chan);
        }
    }

    // Check notification permission and send if granted
    private void maybeSendNotification() {
        Context ctx = requireContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission on Android 13+
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIF);
            return;
        }
        sendNotification();
    }

    // Build and dispatch the completion notification
    private void sendNotification() {
        Context ctx = requireContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Intent to reopen app when notification tapped
        Intent launch = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        PendingIntent pi = PendingIntent.getActivity(
                ctx, 0, launch,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder nb = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.notif_timer)
                .setContentTitle("Timer complete!")
                .setContentText("Your countdown has finished.")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, nb.build());
    }

}
