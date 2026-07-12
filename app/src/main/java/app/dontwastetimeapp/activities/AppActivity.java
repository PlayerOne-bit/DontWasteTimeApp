package app.dontwastetimeapp.activities;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import app.dontwastetimeapp.R;

public class AppActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_layout);
        setUpNavigation();
    }
    private void setUpNavigation(){
        LinearLayout navHome = findViewById(R.id.navHome),
                    navFocus = findViewById(R.id.navFocus);
        navHome.setOnClickListener(v->toNavigate(HomeActivity.class));
        navFocus.setOnClickListener(v->toNavigate(FocusActivity.class));
    }
    private void toNavigate(Class<?> destination){
        Intent intent = new Intent(AppActivity.this, destination);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP|FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }
}
