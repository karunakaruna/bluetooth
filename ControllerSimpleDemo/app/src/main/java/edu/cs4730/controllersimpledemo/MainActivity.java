package edu.cs4730.controllersimpledemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import edu.cs4730.controllersimpledemo.databinding.ActivityMainBinding;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    Boolean isJoyStick = false, isGamePad = false;
    ArrayList<ArrayList<String>> gridData = new ArrayList<>();
    int x = 0, y = 0; // Current coordinates for navigating the grid

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the grid data structure with example data
        initializeGridData();

        // Display the initial grid item
        displayCurrentItem();
    }

    private void initializeGridData() {
        // Assuming a 5x5 grid
        for (int i = 0; i < 5; i++) {
            ArrayList<String> row = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                row.add("Item " + i + "," + j); // Placeholder data
            }
            gridData.add(row);
        }
    }

    private void displayCurrentItem() {
        // Update the text of a TextView to display the current item
        binding.lastBtn.setText(gridData.get(y).get(x)); // Using lastBtn as an example
    }

    // Update your dispatchKeyEvent method to handle D-pad navigation
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Your existing code for handling key events
        // Add logic here for D-pad navigation using x and y to update displayCurrentItem()

        // Example:
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    y = Math.max(0, y - 1); break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    y = Math.min(gridData.size() - 1, y + 1); break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    x = Math.max(0, x - 1); break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    x = Math.min(gridData.get(0).size() - 1, x + 1); break;
            }
            displayCurrentItem(); // Update display after navigation
        }

        return super.dispatchKeyEvent(event); // Ensure to call super if not handled
    }

    // Include your existing methods without modification
}
