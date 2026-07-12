package app.dontwastetimeapp.activities;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import app.dontwastetimeapp.R;

public class FocusActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.focus_layout);
        setUpNavigation();
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
