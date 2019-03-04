package sep.voicechat.activity.channel;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
import sep.voicechat.activity.LoginActivity;
import sep.voicechat.model.Channel;

public class ChannelActivity extends AppCompatActivity implements View.OnClickListener, Serializable {

    private static DatabaseReference dbr;
    private static String userID;
    private static ListView channelsList;
    private FirebaseAuth firebaseAuth;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        FirebaseApp.initializeApp(this);

        firebaseAuth = FirebaseAuth.getInstance();
        dbr = FirebaseDatabase.getInstance().getReference();

        logoutButton = findViewById(R.id.BtnLogout);
        logoutButton.setOnClickListener(this);
        userID = firebaseAuth.getUid();
        channelsList = findViewById(R.id.channelsList);

        updateChannelList();
    }

    @Override
    public void onClick(View v) {
        if (v == logoutButton) {
            createChannel();
            //logout();
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
                if(dataSnapshot.exists()) {
                    channelsList.setAdapter(null);
                    ArrayList<String> channelNames = new ArrayList<String>();

                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    channelNames.clear();

                    for(DataSnapshot child : children) {
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

}
