package app.dontwastetimeapp.activities;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import app.dontwastetimeapp.R;

public class AppActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.app_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.app_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setUpNavigation();
    }
    private void setUpNavigation(){
        LinearLayout navHome = findViewById(R.id.navHome),
                    navFocus = findViewById(R.id.navFocus);
        CardView addAppButton = findViewById(R.id.addAppButton);
        navHome.setOnClickListener(v->toNavigate(HomeActivity.class));
        navFocus.setOnClickListener(v->toNavigate(FocusActivity.class));
        addAppButton.setOnClickListener(v->toAddAppActivity());
    }
    private void toAddAppActivity(){
        Intent intent = new Intent(AppActivity.this, AddAppActivity.class);
        startActivity(intent);
    }
    private void toNavigate(Class<?> destination){
        Intent intent = new Intent(AppActivity.this, destination);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP|FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }
}
