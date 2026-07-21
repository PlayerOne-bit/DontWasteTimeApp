package app.dontwastetimeapp.activities;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

import app.dontwastetimeapp.R;
import app.dontwastetimeapp.classes.AppInfo;
import app.dontwastetimeapp.database.AppPreferences;
import app.dontwastetimeapp.database.TimerPreferences;

public class FocusActivity extends AppCompatActivity {
    private int focusSeconds;
    private int restSeconds;
    private static final String CHANNEL_ID = "timer_channel";
    private static final int NOTIF_ID = 1;
    private CountDownTimer focusCountDownTimer, restCountDownTimer;
    private long focusMillisLeft, restMillisLeft;
    private static boolean isFocusRunning = false;
    private CardView
            focusCardView,
            restCardView,
            focusButton,
            restButton;
    private CircularProgressIndicator
            focusCircularProgress,
            restCircularProgress;
    private TextView
            lightText,
            steadyText,
            deepText,
            contextDepth,
            focusTimeRemaining,
            restState,
            focusState;
    private LinearLayout
            depthLinearLayout;
    TimerPreferences timerPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        timerPrefs = new TimerPreferences(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.focus_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.focus_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        createNotificationChannel();
        requestNotificationPermissionIfNeeded();
        setUpIDs();
        setUpNavigation();
        initialize();
    }
    private void setUpIDs(){
        focusCardView = findViewById(R.id.focusCardView);
        restCardView = findViewById(R.id.restCardView);
        focusButton = findViewById(R.id.focusButton);
        restButton = findViewById(R.id.restButton);

        focusCircularProgress = findViewById(R.id.focusCircularProgress);
        restCircularProgress = findViewById(R.id.restCircularProgress);

        lightText = findViewById(R.id.lightText);
        steadyText = findViewById(R.id.steadyText);
        deepText = findViewById(R.id.deepText);
        contextDepth = findViewById(R.id.contextDepth);
        focusTimeRemaining = findViewById(R.id.focusTimeRemaining);
        restState = findViewById(R.id.restState);
        focusState = findViewById(R.id.focusState);

        depthLinearLayout = findViewById(R.id.depthLinearLayout);
    }
    @Override
    protected void onResume(){
        super.onResume();
        loadAllApps();
        syncFocusUiWithPrefs();
    }
    private void syncFocusUiWithPrefs() {
        if (timerPrefs.isFocusActive()) {
            long left = timerPrefs.getMillisLeft();
            focusTimeRemaining.setText(timeConvert((int) (left / 1000)));
            focusState.setText("STARTED");
            // restart the visible CountDownTimer from the real remaining time
            focusCountDownTimer = new CountDownTimer(left, 1000) {
                @Override public void onTick(long millisUntilFinished) {
                    focusTimeRemaining.setText(timeConvert((int) (millisUntilFinished / 1000)));
                }
                @Override public void onFinish() {
                    timerPrefs.stop();
                    focusState.setText("DONE");
                }
            }.start();
        }
    }
    private void initialize(){
        depthLinearLayout.setVisibility(View.VISIBLE);
        focusCardView.setVisibility(View.VISIBLE);
        restCardView.setVisibility(View.GONE);
        focusButton.setVisibility(View.GONE);
        restButton.setVisibility(View.VISIBLE);

        focusCircularProgress.setProgress(0);
        restCircularProgress.setProgress(0);

        String initialState = "NOT STARTED";
        focusState.setText(initialState);
        restState.setText(initialState);

        buttonSelected(lightText,false);
        buttonSelected(steadyText,false);
        buttonSelected(deepText,false);
        String depth = "DEPTH";
        contextDepth.setText(depth);
    }
    private void buttonSelected(TextView textView, boolean isSelected){
        if(isSelected){
            textView.setBackgroundColor(Color.parseColor("#3B82F6"));
            textView.setTextColor(Color.parseColor("#000000"));
        }else{
            textView.setBackgroundColor(Color.parseColor("#1F1F1F"));
            textView.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }
    private void setDepth(String depth){
        String time;
        String context;
        switch(depth){
            case "LIGHT":
                context = "30 minutes - FOCUS : 5 minutes - REST";
                time = "30m";
                focusSeconds=30*60;
                restSeconds=5*60;
                contextDepth.setText(context);
                focusTimeRemaining.setText(time);
                buttonSelected(lightText,true);
                buttonSelected(steadyText,false);
                buttonSelected(deepText,false);
                break;
            case "STEADY":
                context = "1 hour - FOCUS : 10 minutes - REST";
                time = "1h";
                focusSeconds=60*60;
                restSeconds=10*60;
                contextDepth.setText(context);
                focusTimeRemaining.setText(time);
                buttonSelected(lightText,false);
                buttonSelected(steadyText,true);
                buttonSelected(deepText,false);
                break;
            case "DEEP":
                context = "2 hour - FOCUS : 20 minutes - REST";
                time = "2h";
                focusSeconds=120*60;
                restSeconds=20*60;
                contextDepth.setText(context);
                focusTimeRemaining.setText(time);
                buttonSelected(lightText,false);
                buttonSelected(steadyText,false);
                buttonSelected(deepText,true);
                break;
        }
        focusButton.setVisibility(View.VISIBLE);
    }
    private String timeConvert(int totalSeconds){
        int totalMinutes= totalSeconds/60,
                minutes=totalMinutes%60,
                hours= totalMinutes/60,
                seconds = totalSeconds %60;
        String hoursText=(hours>0)?hours+"h ":"",
                minutesText=(minutes>0)?minutes+"m ":"",
                secondsText=(seconds>0)?seconds+"s":"";
        return (hoursText+minutesText+secondsText).trim();
    }
    private void focusButton(){
        depthLinearLayout.setVisibility(View.GONE);
        contextDepth.setVisibility(View.GONE);
        focusButton.setVisibility(View.GONE);
        if (focusMillisLeft <= 0) {
            focusMillisLeft = focusSeconds * 1000L;
            timerPrefs.start("FOCUS", focusSeconds * 1000L);
            focusCircularProgress.setMax(focusSeconds * 1000);
        }

        focusCountDownTimer = new CountDownTimer(focusMillisLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                focusMillisLeft = millisUntilFinished;
                focusTimeRemaining.setText(timeConvert((int) (millisUntilFinished / 1000)));
                focusCircularProgress.setProgress((int) (focusSeconds * 1000L - millisUntilFinished));
            }

            @Override
            public void onFinish() {
                timerPrefs.stop();
                focusMillisLeft = 0;
                focusState.setText("DONE");
                sendNotification("Focus complete", "Time for a rest.");

                focusCardView.setVisibility(View.GONE);
                restCardView.setVisibility(View.VISIBLE);
            }
        }.start();

        isFocusRunning = true;
        focusState.setText("STARTED");
    }
    private void restButton(){
        restButton.setVisibility(View.GONE);

        if (restMillisLeft <= 0) {
            restMillisLeft = restSeconds * 1000L;
            restCircularProgress.setMax(restSeconds * 1000);
        }

        restCountDownTimer = new CountDownTimer(restMillisLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                restMillisLeft = millisUntilFinished;
                restCircularProgress.setProgress((int) (restSeconds * 1000L - millisUntilFinished));
            }

            @Override
            public void onFinish() {
                restMillisLeft = 0;
                restState.setText("DONE");
                sendNotification("Rest complete", "Back to focus when ready.");
                initialize();
            }
        }.start();

        restState.setText("STARTED");
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Timer", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifies when the timer finishes");
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }
    private void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(this).notify(NOTIF_ID, builder.build());
    }

    private void sendTimeUpNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // swap for your own icon
                .setContentTitle("Time's up!")
                .setContentText("Your timer has finished.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat nmCompat = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return; // permission not granted, skip (should already be requested in onCreate)
        }
        nmCompat.notify(NOTIF_ID, builder.build());
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (focusCountDownTimer != null) focusCountDownTimer.cancel();
        if (restCountDownTimer != null) restCountDownTimer.cancel();
    }

    private void loadAllApps(){
        try(AppPreferences db = new AppPreferences(this)){
            List<AppInfo> savedAllApps = db.getAllApps();
            savedAllApps.sort((a, b)->a.getAppName().compareToIgnoreCase(b.getAppName()));
            LinearLayout focusAppsContainer = findViewById(R.id.focusAppContainer);
            focusAppsContainer.removeAllViews();
            for(AppInfo app: savedAllApps) {
                View row = getLayoutInflater().inflate(R.layout.installed_app_card, focusAppsContainer, false);
                ImageView appIcon = row.findViewById(R.id.installedAppIcon);
                TextView appName = row.findViewById(R.id.installedAppName);
                PackageManager pm = getPackageManager();
                try {
                    Drawable drawable = pm.getApplicationIcon(app.getPackageName());
                    appIcon.setImageDrawable(drawable);
                } catch (PackageManager.NameNotFoundException e) {
                    appIcon.setImageResource(R.drawable.app_icon);
                }
                appName.setText(app.getAppName());
                focusAppsContainer.addView(row);
            }
        }
    }


    private void setUpNavigation(){
        LinearLayout navHome = findViewById(R.id.navHome),
                    navApps = findViewById(R.id.navApps);
        lightText.setOnClickListener(v->setDepth("LIGHT"));
        steadyText.setOnClickListener(v->setDepth("STEADY"));
        deepText.setOnClickListener(v->setDepth("DEEP"));

        navHome.setOnClickListener(v->toNavigate(HomeActivity.class));
        navApps.setOnClickListener(v->toNavigate(AppActivity.class));
        focusButton.setOnClickListener(v->focusButton());
        restButton.setOnClickListener(v->restButton());


    }
    private void toNavigate(Class<?> destination){
        Intent intent = new Intent(FocusActivity.this, destination);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }
}
