package app.dontwastetimeapp.activities;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.util.List;

import app.dontwastetimeapp.R;
import app.dontwastetimeapp.classes.AppInfo;
import app.dontwastetimeapp.database.AppPreferences;

public class FocusActivity extends AppCompatActivity {
    List<AppInfo> savedAllApps;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.focus_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.focus_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setUpNavigation();
        setUpFocus();
    }
    @Override
    protected void onResume(){
        super.onResume();
        loadAllApps();
    }
    private void loadAllApps(){
        try(AppPreferences db = new AppPreferences(this)){
            savedAllApps=db.getAllApps();
            savedAllApps.sort((a,b)->a.getAppName().compareToIgnoreCase(b.getAppName()));
            for(AppInfo app:savedAllApps){

            }
        }
    }


    private void setUpFocus(){

    }
    private void setUpNavigation(){
        LinearLayout navHome = findViewById(R.id.navHome),
                    navApps = findViewById(R.id.navApps);
        navHome.setOnClickListener(v->toNavigate(HomeActivity.class));
        navApps.setOnClickListener(v->toNavigate(AppActivity.class));
    }
    private void toNavigate(Class<?> destination){
        Intent intent = new Intent(FocusActivity.this, destination);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }
}
