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
import androidx.core.app.ActivityCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import android.util.Log;
import android.content.pm.PackageManager;
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
    HashMap<String, ArrayList<ArrayList<GridCell>>> allGridsData = new HashMap<>();
    ArrayList<ArrayList<GridCell>> mainGridData = new ArrayList<>();
    ArrayList<ArrayList<GridCell>> currentGrid;
    String currentGridName = "main";
    int x = 0, y = 0; // Current coordinates for navigating the grid
    int prevX = 0, prevY = 0; // Previous coordinates for navigating back

    private boolean isAButtonHeld = false;
    private boolean isInLiminalSpace = false;

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

    public class GridCell {
        private String description;
        private String item;
        private List<String> subItems; // List to store sub-items

        // Constructor
        public GridCell(String description, String item) {
            this.description = description;
            this.item = item;
            this.subItems = new ArrayList<>(4); // Initialize the list with a capacity of 4
        }

        // Getters and Setters
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public List<String> getSubItems() {
            return subItems;
        }

        public void setSubItems(List<String> subItems) {
            this.subItems = subItems;
        }

        // Method to add a sub-item
        public void addSubItem(String subItem) {
            if (subItems.size() < 4) {
                subItems.add(subItem);
            } else {
                // Handle the case where more than 4 sub-items are being added
                // This could be an exception or a replacement of an existing item
            }
        }

        // Optionally, override toString() for easy printing
        @Override
        public String toString() {
            return "Description: " + description + ", Item: " + item + ", Sub-Items: " + subItems;
        }
    }


    private void initializeGridData() {
        // Example item names corresponding to the languages


        // Arrays of numbers 1 to 5 in five different languages
        String[][] descriptions = {
                {"Big Tree", "Roundabout", "Road", "Pothole", "Far Road"},
                {"North Forest", "Big Rock", "Front Yard", "Maple Tree", "Fence"},
                {"Warm Clearing", "Quiet Forest", "Home", "Field", "Shed"},
                {"Path", "Fallen Log", "Back Yard", "Dirt Lot", "Tall Grass"},
                {"Mush Circle", "Grassy Knoll", "Shallow Pool", "Flower Patch", "Sunken Statue"}
        };

        String[][] items = {
                {"Tree Item", "Roundabout Item", "Road Item", "Pothole Item", "Far Road Item"},
                {"Forest Item", "Rock Item", "Yard Item", "Tree Item", "Fence Item"},
                {"Clearing Item", "Forest Item", "Home Item", "Field Item", "Shed Item"},
                {"Path Item", "Log Item", "Yard Item", "Lot Item", "Grass Item"},
                {"Circle Item", "Knoll Item", "Pool Item", "Patch Item", "Statue Item"}
        };

        // Clear the existing grid data
        mainGridData.clear();

        // Populate the grid with GridCell instances
        for (int i = 0; i < descriptions.length; i++) {
            ArrayList<GridCell> row = new ArrayList<>();
            for (int j = 0; j < descriptions[i].length; j++) {
                row.add(new GridCell(descriptions[i][j], items[i][j]));
            }
            mainGridData.add(row);
        }

        // Convert the Big Rock grid to use GridCell instances as well
        ArrayList<ArrayList<GridCell>> bigRockGrid = createDummyGrid();
        allGridsData.put("Big Rock", bigRockGrid);

        ArrayList<ArrayList<GridCell>> betaGrid = createBetaGrid();
        allGridsData.put("Field", betaGrid);

        // Set currentGrid to the main grid
        currentGrid = mainGridData;
    }

    private ArrayList<ArrayList<GridCell>> createDummyGrid() {
        ArrayList<ArrayList<GridCell>> grid = new ArrayList<>();

        // Example data
        String[][] descriptions = {
                {"Echoing Cave", "Luminous Moss", "Whispering Wind", "Crimson Sunset", "Forgotten Path"},
                {"Glimmering Pond", "Shrouded Valley", "Eternal Rain", "Misty Cliffs", "Twilight Grove"},
                {"Starlit Field", "Ancient Ruins", "Moonlit Stones", "Silver Clouds", "Quiet Oasis"},
                {"Winding River", "Singing Leaves", "Dancing Shadows", "Golden Dawn", "Sleeping Giants"},
                {"Hidden Spring", "Secret Meadow", "Lost Horizon", "Fading Light", "Silent Peak"}
        };

        String[][] items = {
                {"Flashlight, Black and White Chess Pieces, Dark Mask, Empty Book", "Heraldic Shield, Hat with Huge Feather, Glove with Very Sticky Fingers, Fishing Rod with Lure", "Sword With a Mouth Guard, A Wet Bandaid, Fish Bone, Basket Full of Flowers", "Item4", "Item5"},
                {"Item6", "Item7", "Item8", "Item9", "Item10"},
                {"Item11", "Item12", "Item13", "Item14", "Item15"},
                {"Item16", "Item17", "Item18", "Item19", "Item20"},
                {"Item21", "Item22", "Item23", "Item24", "Item25"},
        };

        String[][][] subItems = {
                // Sub-items for row 1...
                {       {"Law 1. Never Outshine the Master", "Law 2. Never Put Too Much Trust in Friends, Learn How to Use Enemies", "Law 3. Conceal Your Intentions", "Law 4. Always Say Less Than Necessary"},
                        {"Law 5. So Much Depends on Reputation--Guard It with Your Life", "Law 6. Court Attention at All Cost", "Law 7. Get Others to Do the Work for You, but Always Take the Credit", "Law 8. Make Other People Come to You--Use Bait If Necessary"},
                        {"Law 9. Win Through Your Actions, Never through Argument", "Law 10. Infection: Avoid the Unhappy and Unlucky", "Law 11. Learn to Keep People Dependent on You", "Law 12. Use Selective Honesty and Generosity to Disarm Your Victim"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                },
                // Sub-items for row 2...
                {       {"Big Tree 1", "Big Tree 2", "Big Tree 3", "Big Tree 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                },
                // Sub-items for row 3...
                {       {"Big Tree 1", "Big Tree 2", "Big Tree 3", "Big Tree 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                },
                // Sub-items for row 4...
                {       {"Big Tree 1", "Big Tree 2", "Big Tree 3", "Big Tree 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                },
                // Sub-items for row 5...
                {       {"Big Tree 1", "Big Tree 2", "Big Tree 3", "Big Tree 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                },
        };

        for (int i = 0; i < descriptions.length; i++) {
            ArrayList<GridCell> row = new ArrayList<>();
            for (int j = 0; j < descriptions[i].length; j++) {
                GridCell cell = new GridCell(descriptions[i][j], items[i][j]);
                // Add sub-items to the cell
                for (String subItem : subItems[i][j]) {
                    cell.addSubItem(subItem);
                }
                row.add(cell);
            }
            grid.add(row);
        }

        return grid;
    }

    private ArrayList<ArrayList<GridCell>> createBetaGrid() {
        ArrayList<ArrayList<GridCell>> grid = new ArrayList<>();

        // Example data
        String[][] descriptions = {
                {"Tall Chamber", "Ornate Foyer", "North Junction", "Golden Railing", "North East Tower"},
                {"Chamber Gate", "Gallery", "Northern Path", "Training Ground", "Deep Craig Bridge"},
                {"Sunset Mall", "Grand Path", "Central Plaza", "Lucid Garden", "East Gate"},
                {"Telescope Point", "Royal Meadow", "Royal Throne Room", "Deep Chambers", "Tall Wall"},
                {"South West Tower", "Dock Wall", "Southern Dock", "Lighthouse", "South East Pool"}
        };

        String[][] items = {
                {"Item1", "Item2", "Item3", "Item4", "Item5"},
                {"Item6", "Item7", "Item8", "Item9", "Item10"},
                {"Item11", "Item12", "Item13", "Item14", "Item15"},
                {"Item16", "Item17", "Item18", "Item19", "Item20"},
                {"Item21", "Item22", "Item23", "Item24", "Item25"},
        };

        String[][][] subItems = {
                // Sub-items for row 1...
                {       {"one", "two", "three", "four"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                },
                // Sub-items for row 2...
                {       {"Big Tree 1", "Big Tree 2", "Big Tree 3", "Big Tree 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                },
                // Sub-items for row 3...
                {       {"Big Tree 1", "Big Tree 2", "Big Tree 3", "Big Tree 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                },
                // Sub-items for row 4...
                {       {"Big Tree 1", "Big Tree 2", "Big Tree 3", "Big Tree 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                },
                // Sub-items for row 5...
                {       {"Big Tree 1", "Big Tree 2", "Big Tree 3", "Big Tree 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                        {"Roundabout 1", "Roundabout 2", "Roundabout 3", "Roundabout 4"},
                },
        };


        for (int i = 0; i < descriptions.length; i++) {
            ArrayList<GridCell> row = new ArrayList<>();
            for (int j = 0; j < descriptions[i].length; j++) {
                GridCell cell = new GridCell(descriptions[i][j], items[i][j]);
                // Add sub-items to the cell
                for (String subItem : subItems[i][j]) {
                    cell.addSubItem(subItem);
                }
                row.add(cell);
            }
            grid.add(row);
        }

        return grid;
    }

    private void displayCurrentItem() {
        // Assuming currentGrid is now an ArrayList<ArrayList<GridCell>>
        GridCell currentCell = currentGrid.get(y).get(x);
        String currentDescription = currentCell.getDescription();

        // Update the UI to show the description
        binding.lastBtn.setText(currentDescription);

        // Speak the description out loud
        if (!isInLiminalSpace) { // Only speak the description if not in liminal space
            if (textToSpeech != null) {
                textToSpeech.speak(currentDescription, TextToSpeech.QUEUE_FLUSH, null, null);
            }
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

        // Joystick handling remains unchanged.
        if (isJoyStick) {
            xaxis = motionEvent.getAxisValue(MotionEvent.AXIS_X);
            yaxis = motionEvent.getAxisValue(MotionEvent.AXIS_Y);

            binding.lastBtn.setText("JoyStick");
            binding.logger.append("JoyStick: X " + xaxis + " Y " + yaxis + "\n");
            handled = true;
        }

        // Handling for D-pad navigation.
        xaxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_X);
        yaxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_Y);

        if (!isInLiminalSpace) {
            // D-pad navigation logic for moving around the grid.
            if (Float.compare(xaxis, -1.0f) == 0) {
                y = Math.max(0, y - 1);
                binding.lastBtn.setText("Dpad Left");
                handled = true;
            } else if (Float.compare(xaxis, 1.0f) == 0) {
                y = Math.min(mainGridData.size() - 1, y + 1);
                binding.lastBtn.setText("Dpad Right");
                handled = true;
            } else if (Float.compare(yaxis, -1.0f) == 0) {
                x = Math.min(mainGridData.get(0).size() - 1, x + 1);
                binding.lastBtn.setText("Dpad Up");
                handled = true;
            } else if (Float.compare(yaxis, 1.0f) == 0) {
                x = Math.max(0, x - 1);
                binding.lastBtn.setText("Dpad Down");
                handled = true;
            }
        } else {
            // Logic for selecting sub-items in liminal space.
            GridCell currentCell = currentGrid.get(y).get(x);
            List<String> subItems = currentCell.getSubItems();

            // Make sure the list of sub-items is not empty and has at least 4 elements to avoid IndexOutOfBoundsException.
            if (subItems.size() >= 4) {
                if (Float.compare(yaxis, -1.0f) == 0) { // Up
                    selectSubItem(subItems.get(0));
                    handled = true;
                } else if (Float.compare(xaxis, 1.0f) == 0) { // Right
                    selectSubItem(subItems.get(1));
                    handled = true;
                } else if (Float.compare(yaxis, 1.0f) == 0) { // Down
                    selectSubItem(subItems.get(2));
                    handled = true;
                } else if (Float.compare(xaxis, -1.0f) == 0) { // Left
                    selectSubItem(subItems.get(3));
                    handled = true;
                }
            }
        }

        if (handled) {
            displayCurrentItem(); // Call to update the display if needed, but consider whether you want to repeat the description.
        }
        return handled;
    }


    //getting the buttons.  note, there is down and up action.  this only
    //looks for down actions.
    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        boolean handled = false;
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_BUTTON_A:
                        binding.lastBtn.setText("A Button");
                        if (isInLiminalSpace) {
                            isInLiminalSpace = false;
                            Log.d("GameController", "Exiting Liminal Space");
                            if (textToSpeech != null) {
                                textToSpeech.speak("Exiting subspace", TextToSpeech.QUEUE_FLUSH, null, null);
                            }
                            // Possibly reset liminal space state or do additional cleanup here
                        } else {
                        if (!"main".equals(currentGridName)) {
                            currentGrid = mainGridData; // Switch back to main grid
                            currentGridName = "main";
                            x = prevX; // Restore previous coordinates
                            y = prevY;
                            displayCurrentItem();
                        }}

                        handled = true;
                        break;
                    case KeyEvent.KEYCODE_BUTTON_X:
                        binding.lastBtn.setText("X Button");

                        Log.d("GameController", "A Button pressed");
                        GridCell currentCell = currentGrid.get(y).get(x);
                        String currentItem = currentCell.getItem();
                        isAButtonHeld = true;
                        Log.d("GameController", "A Button held ");
                        // Use TextToSpeech to speak the item
                        if (textToSpeech != null) {
                            textToSpeech.speak(currentItem, TextToSpeech.QUEUE_FLUSH, null, null);
                        }

                        Log.d("GameController", "A Button pressed - Item: " + currentItem);


                        handled = true;
                        break;
                    case KeyEvent.KEYCODE_BUTTON_Y:
                        binding.lastBtn.setText("Y Button");
                        currentCell = currentGrid.get(y).get(x);
                        String description = currentCell.getDescription();
                        if (allGridsData.get(currentCell.getDescription()) == null) {
                            // No nested grid exists, enter liminal space
                            Log.d("GameController", "Entering Liminal Space");
                            if (!isInLiminalSpace) {
                                if (textToSpeech != null) {
                                    textToSpeech.speak("Entering subspace", TextToSpeech.QUEUE_FLUSH, null, null);
                                }
                            }
                            String currentItemlist = currentCell.getItem();

                            // Use TextToSpeech to speak the item
                            if (textToSpeech != null) {
                                textToSpeech.speak(currentItemlist, TextToSpeech.QUEUE_FLUSH, null, null);
                            }
                            isInLiminalSpace = true;
                        } else {
                            // Load nested grid as before
                            loadNestedGrid(currentCell.getDescription());
                        }
                        handled = true;
                        break;
                    case KeyEvent.KEYCODE_BUTTON_B:
                        binding.lastBtn.setText("B Button");
                        handled = true;
                        break;
                }
                if (!handled) binding.logger.append("code is " + event.getKeyCode() + "\n");
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A) {
                    isAButtonHeld = false;
                    Log.d("GameController", "A Button released ");
                    handled = true;
                }
                // ... Add additional handling for other button releases if needed
                if (!handled) {
                    binding.logger.append("Unhandled code on up: " + event.getKeyCode() + "\n");
                }
            }
            else {
                binding.logger.append("unknown action " + event.getAction());
            }
        }
        return handled;
    }


    private void selectSubItem(String subItem) {
        Log.d("GameController", "Selecting SubItem: " + subItem); // Add this log
        if (textToSpeech != null && subItem != null) {
            textToSpeech.speak(subItem, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }


    private void loadNestedGrid(String itemName) {
        ArrayList<ArrayList<GridCell>> nestedGrid = allGridsData.get(itemName);
        if (nestedGrid != null) {
            // Save current location before moving to the nested grid
            prevX = x;
            prevY = y;
            currentGrid = nestedGrid;
            currentGridName = itemName;
            x = 0; // Reset coordinates for nested grid navigation
            y = 0;
            displayCurrentItem();
        }
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
