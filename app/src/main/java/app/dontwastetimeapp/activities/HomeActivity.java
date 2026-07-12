package app.dontwastetimeapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import app.dontwastetimeapp.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setUpNavigation();
    }
    private void setUpNavigation(){
        LinearLayout navApps = findViewById(R.id.navApps),
                    navFocus = findViewById(R.id.navFocus);
        navApps.setOnClickListener(v-> toNavigate(AppActivity.class));
        navFocus.setOnClickListener(v-> toNavigate(FocusActivity.class));
    }
    private void toNavigate(Class<?> destination){
        Intent intent = new Intent(HomeActivity.this,destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }
}