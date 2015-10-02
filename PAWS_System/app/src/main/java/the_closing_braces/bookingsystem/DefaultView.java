/*
 * @file DefaultView.java
 * @author  Roger Tavares <rog.tava@gmail.com>
 * @author Godfrey Ndumiso Mathe <u13103394@tuks.co.za>
 *
 * @section DESCRIPTION
 *
 * - This class provides functionality for work activity display.
 * - Allows a user to add a new activity locally on the device if the person is free.
 * - Shows the current status of the persons schedule
 */

package the_closing_braces.bookingsystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.SimpleAdapter;

import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DefaultView extends Activity {
    String currentActivityEndTime = ""; //Used for status change
    List<Map<String, String>> activities = new ArrayList<Map<String, String>>(); //Stores the activities for the day in a Mapped list
    ListView todaysActivities = null; //List view of todays activities
    SimpleAdapter populateActivities = null; //Adapter to set the items on the list view
    private int maxActivityDuration; //Used to set the Quick Book activity max time
    Intent getRoomDetails; // Get room details from previous Activity
    static String responseText = ""; // Response json from the server
    Thread statusThread; //Used for status
    Thread connectionThread; // Used for connection to server

    /**
     * The default on create function for the activity where everything gets initialized as well as thread creation for checking status changes and to retrieve activities from server
     *
     * @param savedInstanceState - This is used to save the state of the activity should it be interrupted
     * @return This function does not return any value
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set flags for lock screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_default_view);

        //Initialize the name of the room
        final TextView roomName = (TextView) findViewById(R.id.labelRoomName);
        roomName.setText("Today's Schedule!");

        //Set the adapter for the list view
        todaysActivities = (ListView) findViewById(R.id.lstActivities);
        populateActivities = new SimpleAdapter(getApplicationContext(), activities, R.layout.list_view_activities_layout, new String[]{"activitySubject", "activityOrganizer", "activityStartTime", "activityEndTime"}, new int[]{R.id.subject, R.id.Organizer, R.id.startTime, R.id.endTime});
        todaysActivities.setAdapter(populateActivities);
    }

    /**
     * This is a representation of a activity by the use of a hash map
     *
     * @param subjectKey   - The key in the map associated with the activity Subject
     * @param Subject      - The value for the subject
     * @param timeStartKey - The key in the map associated with the activity start time
     * @param startTime-   The value for the start time
     * @param timeEndKey   - The key in the map associated with the activity end time
     * @param endTime      - The value for the end time
     * @return This function returns a HashMap with all the activities for the day
     */
    private HashMap<String, String> createActivity(String subjectKey, String Subject, String venueKey, String Venue, String timeStartKey, String startTime, String timeEndKey, String endTime) {
        HashMap<String, String> activity = new HashMap<String, String>();
        activity.put(subjectKey, Subject);
        activity.put(venueKey, Venue);
        activity.put(timeStartKey, startTime);
        activity.put(timeEndKey, endTime);
        return activity;
    }

    /**
     * This function is executed when the user clicks the quick book button
     *
     * @param v - Default view of the activity used to link it to the button
     * @return This function does not return any value
     */
    public void newActivityFunction(View v) {
                showNewActivityInputDialog(false, null, 0);
    }

    /**
     * Displays the layout for the entry of the new activity details this is done in the form of a dialog
     *
     * @param required - Boolean sent to dialog to display the the this field is required text
     * @param Subject  - Send the subject to the dialog should the Duration field be the only empty one
     * @param Duration - Send the duration to the dialog should the Subject field be the only empty one
     * @return This function does not return any value
     */
    private void showNewActivityInputDialog(Boolean required, final String Subject, final int Duration) {

        //Create a alert builder
        LayoutInflater layInflater = LayoutInflater.from(DefaultView.this);
        View promptView = layInflater.inflate(R.layout.new_activity, null);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(DefaultView.this);
        alertBuilder.setView(promptView);

        //initialize all the elements from the layout
        final EditText activitySubject = (EditText) promptView.findViewById(R.id.editSubject);
        final EditText activityVenue = (EditText) promptView.findViewById(R.id.editActivityVenue);
        final TimePicker activityStartTime = (TimePicker) promptView.findViewById(R.id.startTime);
        final NumberPicker activityDuration = (NumberPicker) promptView.findViewById(R.id.duration);
        final TextView labelRequiredDuration = (TextView) promptView.findViewById(R.id.requiredDuration);
        final TextView labelRequiredSubject = (TextView) promptView.findViewById(R.id.requiredSubject);

        //set activity duration limits
        activityDuration.setMinValue(0);
        activityDuration.setMaxValue(720);

        //Build the Alert dialog
        alertBuilder.setCancelable(false)
                .setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {




                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                                String startTime = activityStartTime.getCurrentHour().toString()+":"+activityStartTime.getCurrentMinute().toString();

                            DateFormat timeFormatInput = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
                            DateFormat timeFormatOutput = new SimpleDateFormat("HH:mm");

                            try {
                                Date date = null;

                                date = timeFormatInput.parse(startTime);

                                startTime = timeFormatOutput.format(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }



                                calendar.add(Calendar.MINUTE, activityDuration.getValue());
                                String endTime = timeFormat.format(calendar.getTime());



                                            //Let user know activity is being booked
                                            Toast toastAdding= Toast.makeText(getApplicationContext(), "Adding activity: " + activitySubject.getText().toString(), Toast.LENGTH_LONG);
                                            toastAdding.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                                            toastAdding.show();

                                            activities.add(createActivity("activitySubject", activitySubject.getText().toString(), "activityOrganizer", activityVenue.getText().toString(), "activityStartTime", startTime, "activityEndTime", endTime));

                                            currentActivityEndTime = endTime;



                                        todaysActivities = (ListView) findViewById(R.id.lstActivities);
                                        todaysActivities.setAdapter(populateActivities);








                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            //Cancel the dialog
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        //Create the dialog
        AlertDialog alert = alertBuilder.create();
        alert.setTitle("Add new activity Details!");

        //Check for required fields
        if (required) {
            if (Subject == null) {
                labelRequiredSubject.setVisibility(View.VISIBLE);
            } else {
                activitySubject.setText(Subject);
                activityDuration.requestFocusFromTouch();
            }

            if (Duration == 0) {
                labelRequiredDuration.setVisibility(View.VISIBLE);
            } else {
                activityDuration.setValue(Duration);
                activitySubject.requestFocusFromTouch();
            }

        }

        //Position the dialog
        alert.getWindow().getAttributes().gravity = Gravity.LEFT | Gravity.TOP;
        alert.getWindow().getAttributes().x = 550;
        alert.getWindow().getAttributes().y = 10;

        //Show the dialog and set its dimensions
        alert.show();
        alert.getWindow().setLayout(800, 900);
    }
}