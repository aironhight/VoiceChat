package sep.voicechat.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;

import sep.voicechat.R;
import sep.voicechat.model.Channel;

public class ChannelActivity extends AppCompatActivity implements View.OnClickListener, Serializable {

    private static DatabaseReference dbr;
    private static String userID;
    private static ListView channelsList;
    private FirebaseAuth firebaseAuth;
    private FloatingActionButton btnCreateChannel, btnJoinChannel, btnLogoff;
    private boolean doubleBackToExitPressedOnce;

    /**
     * Creates a channel in the firebase, and sets the owner to the current user
     *
     * @param channelName the name of the channel
     */
    private static void createChannelWithName(String channelName) {
        Channel tempch = new Channel(userID, channelName);
        DatabaseReference postRef = dbr.child("channels");
        postRef.child(tempch.getName()).setValue(tempch);
        postRef = dbr.child("users");
        postRef.child(userID).child(tempch.getName()).setValue(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        FirebaseApp.initializeApp(this);

        getSupportActionBar().hide();
        firebaseAuth = FirebaseAuth.getInstance();
        dbr = FirebaseDatabase.getInstance().getReference();


        userID = firebaseAuth.getUid();
        channelsList = findViewById(R.id.channelsList);

        btnCreateChannel = findViewById(R.id.btn_createChannel);
        btnCreateChannel.setOnClickListener(this);
        btnJoinChannel = findViewById(R.id.btn_joinChannel);
        btnJoinChannel.setOnClickListener(this);
        btnLogoff = findViewById(R.id.btn_logoff);
        btnLogoff.setOnClickListener(this);

        updateChannelList();

        channelsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedChannelName = (String) parent.getItemAtPosition(position);
                Intent toChatActivity = new Intent(getApplicationContext(), ChatActivity.class);
                toChatActivity.putExtra("channelName", clickedChannelName);
                toChatActivity.putExtra("userID", userID);
                startActivity(toChatActivity);
            }
        });

        if (ActivityCompat.checkSelfPermission(ChannelActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            //...make a request if they're not
            ActivityCompat.requestPermissions(ChannelActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            return;
        }
    }

    @Override
    public void onClick(View v) {

        if (v == btnLogoff) {
            logout();
        } else if (v == btnCreateChannel) {
            createChannel();
        } else if (v == btnJoinChannel) {
            joinChannel();
        }

    }

    /**
     * Logs off the user and starts the Login activity. The current activity is closed by "finish()" method.
     */
    private void logout() {
        firebaseAuth.signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    /**
     * Gets the channels for which the user is subscribed and inflates the listview with their names
     */
    private void updateChannelList() {
        Query joinedChannels = dbr.child("users").child(userID);
        joinedChannels.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    channelsList.setAdapter(null);
                    ArrayList<String> channelNames = new ArrayList<String>();

                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    channelNames.clear();

                    for (DataSnapshot child : children) {
                        channelNames.add(child.getKey());
                    }

                    ArrayAdapter<String> channels = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, channelNames);
                    channelsList.setAdapter(channels);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "An error occured: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    /**
     * Makes a popup for creating a channel with a field for entering channel name
     */
    private void createChannel() {
        final AlertDialog.Builder createChannelAlert = new AlertDialog.Builder(this);

        createChannelAlert.setTitle("Create a channel");
        createChannelAlert.setMessage("Enter channel name");

        final EditText input = new EditText(this);
        createChannelAlert.setView(input);

        createChannelAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //User has clicked OK
                final String channelName = input.getText().toString();

                //Check if the channel with that name already exists...
                Query channelExistsQuery = dbr.child("channels").orderByChild("name").equalTo(channelName);

                channelExistsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            //If a channel with the same name exists.
                            Toast.makeText(getApplicationContext(), "Channel with this name already exists", Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            createChannelWithName(channelName);
                            updateChannelList();
                            return;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        createChannelAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        createChannelAlert.show();
    }

    private void joinChannel() {
        final AlertDialog.Builder createChannelAlert = new AlertDialog.Builder(this);

        createChannelAlert.setTitle("Join a channel");
        createChannelAlert.setMessage("Enter channel name");

        final EditText input = new EditText(this);
        createChannelAlert.setView(input);

        createChannelAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //User has clicked OK
                final String channelName = input.getText().toString();

                //Check if the channel with that name already exists...
                Query channelExistsQuery = dbr.child("channels").orderByChild("name").equalTo(channelName);

                channelExistsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            //If a channel with this name exists.
                            DatabaseReference postRef = dbr.child("users");
                            postRef.child(userID).child(channelName).setValue(true);
                            Toast.makeText(getApplicationContext(), "Channel joined", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            //Channel with this name does not exist
                            Toast.makeText(getApplicationContext(), "Channel with this name does not exist", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                updateChannelList();
            }
        });

        createChannelAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        createChannelAlert.show();
    }


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            moveTaskToBack(true);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back button again to exit the application...", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

}
