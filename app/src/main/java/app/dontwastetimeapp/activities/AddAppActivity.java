package app.dontwastetimeapp.activities;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import app.dontwastetimeapp.R;
import app.dontwastetimeapp.classes.AppInfo;
import app.dontwastetimeapp.database.AppPreferences;

public class AddAppActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.add_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setUpNavigation();
        setUpAddAppButton();
    }
    private void setUpAddAppButton(){
        CardView cardViewPicker = findViewById(R.id.cardViewPicker);
        cardViewPicker.setOnClickListener(v->{
            findViewById(R.id.appPickerScroll).setVisibility(View.VISIBLE);
        });

    }

    private void setUpNavigation(){
        ImageView backButton = findViewById(R.id.backButton);
        CardView saveAppButton = findViewById(R.id.saveAppButton);
        backButton.setOnClickListener(v->finish());
        saveAppButton.setOnClickListener(v->saveAppButton());
    }
    private void saveAppButton(){
        //TODO: selectedApp
        if (selectedApp == null) {
            Toast.makeText(this, "Please select an app first", Toast.LENGTH_SHORT).show();
            return;
        }

        PackageManager pm = getPackageManager();
        String packageName = selectedApp.packageName;
        String appName = pm.getApplicationLabel(selectedApp).toString();

        SeekBar timeLimitSeekBar = findViewById(R.id.timeLimitSeekBar);
        int dailyLimitMinutes = timeLimitSeekBar.getProgress();

        SwitchCompat blockOverrideSwitch = findViewById(R.id.blockOverrideSwitch);
        boolean blocked = blockOverrideSwitch.isChecked();

        AppInfo newApp = new AppInfo(packageName, appName, dailyLimitMinutes);
        newApp.setBlocked(blocked);

        AppPreferences db = new AppPreferences(this);
        boolean success = db.addApp(newApp);

        if (success) {
            finish();
        } else {
            Toast.makeText(this, "Failed to save app", Toast.LENGTH_SHORT).show();
        }
    }

}
