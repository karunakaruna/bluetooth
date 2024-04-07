package edu.cs4730.controllersimpledemo;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import edu.cs4730.controllersimpledemo.databinding.ActivityMainBinding;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import android.util.Log;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;



/**
 * A simple demo to show how to get input from a bluetooth controller
 * See https://developer.android.com/training/game-controllers/controller-input.html
 * for a lot more info
 */
@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    Boolean isJoyStick = false, isGamePad = false;
    ArrayList<ArrayList<String>> gridData = new ArrayList<>();
    int x = 0, y = 0; // Current coordinates for navigating the grid
    private TextToSpeech textToSpeech;
    private static final int PERMISSIONS_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // getGameControllerIds();

        // Initialize the grid data structure with example data
        initializeGridData();

        // Display the initial grid item
        displayCurrentItem();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WAKE_LOCK}, PERMISSIONS_REQUEST_CODE);
        }
        startYourForegroundService();

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Set language for text-to-speech
                    // You can adjust this based on your needs or app settings
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                } else {
                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, you can start your foreground service now
                startYourForegroundService();
            } else {
                // Permission was denied. Handle the failure to obtain permission here
            }
        }
    }

    private void startYourForegroundService() {
        // Make sure that this method is not called before the user has granted the permission.
        Intent serviceIntent = new Intent(this, YourForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
    private void initializeGridData() {
        // Arrays of numbers 1 to 5 in five different languages
        String[] english = {"One", "Two", "Three", "Four", "Five"};
        String[] spanish = {"Uno", "Dos", "Tres", "Cuatro", "Cinco"};
        String[] french = {"Un", "Deux", "Trois", "Quatre", "Cinq"};
        String[] german = {"Eins", "Zwei", "Drei", "Vier", "Fünf"};
        String[] italian = {"Uno", "Due", "Tre", "Quattro", "Cinque"};

        // List of the language arrays for easier iteration
        String[][] languages = {english, spanish, french, german, italian};

        // Clear the existing grid data
        gridData.clear();

        // Populate the grid
        for (int i = 0; i < 5; i++) { // For each language
            ArrayList<String> row = new ArrayList<>();
            for (int j = 0; j < 5; j++) { // For each number
                row.add(languages[i][j]); // Add the number in the current language
            }
            gridData.add(row); // Add the row to the grid
        }
    }


    private void displayCurrentItem() {
        // Fetch the current item
        String currentItem = gridData.get(y).get(x);

        // Display the current item in the TextView
        binding.lastBtn.setText(currentItem);

        // Speak the current item out loud
        if (textToSpeech != null) {
            textToSpeech.speak(currentItem, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        getGameControllerIds();
    }

    //getting the "joystick" or dpad motion.

    @Override
    public boolean onGenericMotionEvent(android.view.MotionEvent motionEvent) {
        float xaxis = 0.0f, yaxis = 0.0f;
        boolean handled = false;

        //if both are true, this code will show both JoyStick and dpad.  Which one you want to use is
        // up to you
        if (isJoyStick) {
            xaxis = motionEvent.getAxisValue(MotionEvent.AXIS_X);
            yaxis = motionEvent.getAxisValue(MotionEvent.AXIS_Y);

            binding.lastBtn.setText("JoyStick");
            binding.logger.append("JoyStick: X " + xaxis + " Y " + yaxis + "\n");
            handled = true;
        }

        if (isGamePad) {
            xaxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_X);
            yaxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_Y);

            // Check if the AXIS_HAT_X value is -1 or 1, and set the D-pad
            // LEFT and RIGHT direction accordingly.
            if (Float.compare(xaxis, -1.0f) == 0) {
                // Dpad.LEFT;
                binding.lastBtn.setText("Dpad Left");
                y = Math.min(gridData.size() - 1, y + 1);
                handled = true;
            } else if (Float.compare(xaxis, 1.0f) == 0) {
                // Dpad.RIGHT;
                binding.lastBtn.setText("Dpad Right");
                y = Math.max(0, y - 1);

                handled = true;
            }
            // Check if the AXIS_HAT_Y value is -1 or 1, and set the D-pad
            // UP and DOWN direction accordingly.
            else if (Float.compare(yaxis, -1.0f) == 0) {
                // Dpad.UP;
                binding.lastBtn.setText("Dpad Up");
                x = Math.min(gridData.get(0).size() - 1, x + 1);
                handled = true;
            } else if (Float.compare(yaxis, 1.0f) == 0) {
                // Dpad.DOWN;
                binding.lastBtn.setText("Dpad Down");
                x = Math.max(0, x - 1);
                handled = true;
            } else if ((Float.compare(xaxis, 0.0f) == 0) && (Float.compare(yaxis, 0.0f) == 0)) {
                //Dpad.center
                binding.lastBtn.setText("Dpad centered");
                handled = true;
            }
            if (!handled) {
                binding.lastBtn.setText("Unknown");
                binding.logger.append("unhandled: X " + xaxis + " Y " + yaxis + "\n");
            }

        }
        displayCurrentItem(); // Update display after navigation
        return handled;
    }

    //getting the buttons.  note, there is down and up action.  this only
    //looks for down actions.
    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        boolean handled = false;
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_BUTTON_X:
                        binding.lastBtn.setText("X Button");
                        handled = true;
                        break;
                    case KeyEvent.KEYCODE_BUTTON_A:
                        binding.lastBtn.setText("A Button");
                        handled = true;
                        break;
                    case KeyEvent.KEYCODE_BUTTON_Y:
                        binding.lastBtn.setText("Y Button");
                        handled = true;
                        break;
                    case KeyEvent.KEYCODE_BUTTON_B:
                        binding.lastBtn.setText("B Button");
                        handled = true;
                        break;
                }
                if (!handled) binding.logger.append("code is " + event.getKeyCode() + "\n");
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                //don't care, but need to handle it.
                handled = true;
            } else {
                binding.logger.append("unknown action " + event.getAction());
            }
        }
        return handled;
    }

    @Override
    protected void onDestroy() {
        // Shut down the TextToSpeech engine when the activity is destroyed
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }


    //From Google's page on controller-input
    public ArrayList<Integer> getGameControllerIds() {
        ArrayList<Integer> gameControllerDeviceIds = new ArrayList<Integer>();
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int deviceId : deviceIds) {
            InputDevice dev = InputDevice.getDevice(deviceId);
            int sources = dev.getSources();

            // Verify that the device has gamepad buttons, control sticks, or both.
            if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) || ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
                // This device is a game controller. Store its device ID.
                binding.Name.setText(dev.getName());
                if (!gameControllerDeviceIds.contains(deviceId)) {
                    gameControllerDeviceIds.add(deviceId);
                }
                //possible both maybe true.
                if ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    isGamePad = true;
                if ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)
                    isJoyStick = true;
                binding.logger.append("GamePad: " + isGamePad + "\n");
                binding.logger.append("JoyStick: " + isJoyStick + "\n");
            }

        }
        return gameControllerDeviceIds;
    }

}
