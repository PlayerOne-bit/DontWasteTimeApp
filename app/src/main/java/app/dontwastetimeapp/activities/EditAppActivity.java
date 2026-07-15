package app.dontwastetimeapp.activities;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import app.dontwastetimeapp.R;

public class EditAppActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.edit_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edit_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setUpButtons();
    }
    private void setUpButtons(){
        LinearLayout saveEditButton = findViewById(R.id.saveEditButton);
        LinearLayout editBackButton = findViewById(R.id.editBackButton);
        LinearLayout deleteAppButton = findViewById(R.id.deleteAppButton);
        editBackButton.setOnClickListener(v->finish());
    }
}
