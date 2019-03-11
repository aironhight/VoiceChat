package sep.voicechat.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import sep.voicechat.R;
import sep.voicechat.model.Message;
import sep.voicechat.utility.IRecorder;
import sep.voicechat.utility.MediaController;

public class ChatActivity extends AppCompatActivity implements RecognitionListener, View.OnClickListener, IRecorder {
    private final String TAG = "ChatActivity";

    private final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    private final int PERMISSION_REQUEST_RECORD_AUDIO = 3;

    private SpeechRecognizer speechRecognizer;
    private MediaController mediaController;
    private String channelName, userID;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private DatabaseReference dbr;
    private ArrayList<Message> messagesToListen;
    private boolean recording = false, listening = false;
    private TextView backgroundTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        checkPermissions();
        channelName = getIntent().getStringExtra("channelName");
        userID = getIntent().getStringExtra("userID");

        firebaseAuth = FirebaseAuth.getInstance();

        mediaController = new MediaController(channelName, userID, this);

        dbr = FirebaseDatabase.getInstance().getReference().child("channels").child(channelName).child("messages");

        messagesToListen = new ArrayList<>();

        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        initialize();

        backgroundTv = findViewById(R.id.backgroundTv);
        backgroundTv.setOnClickListener(this);

        getSupportActionBar().setTitle(channelName);
        Query q = dbr.orderByKey();
        q.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message msg = dataSnapshot.getValue(Message.class);

                if (!Message.messageIsListened(msg, userID)) {
                    //If the current user did not listenMessages the message it adds it to the list.
                    messagesToListen.add(msg);
                }
                backgroundTv.setText("Click to activate command listener \n There are " + messagesToListen.size() + " messages to listen.");
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Starts the speech recognizer and listens for words.
     */
    private void listenForCommand() {
        speechRecognizer.startListening(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permissions granted
                } else {
                    //Permissions denied
                    moveTaskToBack(true);
                    onDestroy();
                }
                return;
            }
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permissions granted
                } else {
                    //Permissions denied
                    moveTaskToBack(true);
                    onDestroy();
                }
                return;
            }
            case PERMISSION_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permissions granted
                } else {
                    //Permissions denied
                    moveTaskToBack(true);
                    onDestroy();
                }
                return;
            }


        }
    }


    private void initialize() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);

        storageReference = FirebaseStorage.getInstance().getReference();
    }

    /**
     * Uploads a file to the Firebase Storage
     *
     * @param fileUri the file location on the device.
     */
    private void uploadFile(String fileUri) {
        final Uri fileToUpload = Uri.fromFile(new File(fileUri));
        StorageReference channelRef = storageReference.child(fileToUpload.getLastPathSegment());

        UploadTask uploadTask = channelRef.putFile(fileToUpload);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Successful upload
                ArrayList<String> listenedBy = new ArrayList<>();
                listenedBy.add(userID);

                Message tmpMessage = new Message(fileToUpload.getLastPathSegment(), listenedBy);
                dbr.push().setValue(tmpMessage);

                deleteFileFromStorage(fileToUpload.getLastPathSegment());
            }
        });

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //The upload has failed
                Toast.makeText(getApplicationContext(), "The file has failed to upload: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Starts streaming the messages that are not yet listened by the current user.
     */
    private void listenMessages() {
        if (messagesToListen.size() == 0) {
            //There are messages to listenMessages
            backgroundTv.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            Toast.makeText(getApplicationContext(), "There are no messages to listen currently", Toast.LENGTH_SHORT).show();
        } else {
            //There are messages to listenMessages
            backgroundTv.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            backgroundTv.setText("Listening... \n press to play next");
            listening = true;
            final Message messageToListen = messagesToListen.remove(0);

            storageReference.child(messageToListen.getFilePath()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    mediaController.streamFromURL(uri.toString());
                }
            });

            //Put the user in the list of users that have listened the message.
            dbr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Message tmpMsg = child.getValue(Message.class);
                        if (messageToListen.getFilePath().equals(tmpMsg.getFilePath())) {
                            String childKey = child.getKey();
                            messageToListen.addListener(userID);
                            dbr.child(childKey).setValue(messageToListen);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    /**
     * Deletes a file from the default.
     * @param fileName the file to be deleted
     * @return true if the file is delted, false if not.
     */
    private boolean deleteFileFromStorage(String fileName) {
        File toDelete = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);
        return toDelete.delete();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {

    }

    @Override
    public void onResults(Bundle results) {
        Log.i(TAG, "onResults: " + results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));
        String ssd = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
        ArrayList<String> splittedArray = new ArrayList<>(Arrays.asList(ssd.split(" ")));

        if (splittedArray.contains("record") || splittedArray.contains("recording")) {
            record();
        } else if (splittedArray.contains("listen")) {
            listenMessages();
        } else {
            Toast.makeText(getApplicationContext(), "Command not recognized, try again", Toast.LENGTH_SHORT).show();
            listenForCommand();
        }

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    public void onClick(View v) {
        if (v == backgroundTv) {
            checkPermissions();
            if (!recording && !listening) {
                listenForCommand();
            } else if (listening) {
                stopListening();
                listenMessages();
            } else {
                stopRecording();
            }
        }
    }

    @Override
    public void stoppedRecording(String fileName) {
        uploadFile(fileName);
    }

    public void record() {
        checkPermissions();
        mediaController.startRecording();
        recording = true;
        backgroundTv.setText("Recording... \n press to stop");
        backgroundTv.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
    }

    private void stopRecording() {
        mediaController.stopRecording();
        recording = false;
        backgroundTv.setText("");
        backgroundTv.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
    }

    private void checkPermissions() {
        //Checks for reading external storage permissions.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }

        //Checks for writing external storage permissions.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        //Checks for recording audio permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
        }

    }

    public void onListenEnd() {
        backgroundTv.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        backgroundTv.setText("Listening for a command");
        listening = false;
        listenMessages();
    }

    public void stopListening() {
        if(listening) {
            mediaController.stopListening();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopListening();
    }
}
