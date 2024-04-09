package edu.cs4730.controllersimpledemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.media.MediaPlayer;


public class YourForegroundService extends Service {
    private PowerManager.WakeLock wakeLock;
    private static final String CHANNEL_ID = "ControllerInputServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private MediaPlayer mediaPlayer;

    public static final String ACTION_PLAY_SOUND_A = "edu.cs4730.controllersimpledemo.ACTION_PLAY_SOUND_A";
    public static final String ACTION_PLAY_SOUND_X = "edu.cs4730.controllersimpledemo.ACTION_PLAY_SOUND_X";


    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakeLockTag");
        wakeLock.acquire();

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Controller Input Service")
                .setContentText("Listening for controller commands...")
                .setSmallIcon(android.R.drawable.arrow_up_float) // Ensure this is a valid icon.
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        CharSequence name = getString(R.string.channel_name); // defined in strings.xml
        String description = getString(R.string.channel_description); // defined in strings.xml
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Your code to listen for controller input and handle text-to-speech
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
