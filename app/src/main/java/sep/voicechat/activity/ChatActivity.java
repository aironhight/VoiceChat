package sep.voicechat.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

import sep.voicechat.R;
import sep.voicechat.utility.IRecorder;
import sep.voicechat.utility.MediaController;

public class ChatActivity extends AppCompatActivity implements RecognitionListener, View.OnClickListener, IRecorder {

    private SpeechRecognizer speechRecognizer;
    private MediaController mediaController;
    private String channelName, userID;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private DatabaseReference dbr;
    private ArrayList<String> filesToListen;
    private Button testButton;
    private boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        channelName = getIntent().getStringExtra("channelName");
        userID = getIntent().getStringExtra("userID");

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        mediaController = new MediaController(channelName, userID, this);
        //mediaController.streamFromURL("https://www.youtube.com/watch?v=QYh6mYIJG2Y");

        dbr = FirebaseDatabase.getInstance().getReference().child("channels").child(channelName).child("messages");
        filesToListen = new ArrayList<>();

        if(firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        verifyStoragePermissions(this);

        testButton = findViewById(R.id.testButton); testButton.setOnClickListener(this);

        getSupportActionBar().setTitle(channelName);

        dbr.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                Message msg = dataSnapshot.getValue(Message.class);
//
//                if(Message.messageIsListened(msg, userID)) {
//                    filesToListen.add(msg.getFilePath());
//                }
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void checkPermissionGranted() {
        if(ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            //...make a request if they're not
            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initialize();
            } else {
                onDestroy();
            }
        }
    }

    private void initialize() {

        if(speechRecognizer == null) {
            checkPermissionGranted();
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        }
    }

    private void uploadFile(String fileUri) {
        final Uri fileToUpload = Uri.fromFile(new File(fileUri));
        StorageReference channelRef = storageReference.child(channelName + fileToUpload.getLastPathSegment());

        UploadTask uploadTask = channelRef.putFile(fileToUpload);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Successful upload
                dbr.push().setValue(fileToUpload.getLastPathSegment());

                deleteFileFromStorage();
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

    private void listen() {
        filesToListen.remove(0);
    }



    private void updateFilesToListen() {


    }

    private void deleteFileFromStorage() {
        //To be implemented
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

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    public void onClick(View v) {
        if(v == testButton) {
//            storageReference.child("recording.3gp").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                @Override
//                public void onSuccess(Uri uri) {
//                    mediaController.streamFromURL(uri.toString());
//                }
//            });
            if(!recording) {
                record();
                recording= true;
            } else {
                stopRecording();
                recording = false;
            }
        }
    }

    @Override
    public void stoppedRecording(String fileName) {
        uploadFile(fileName);
    }

    public void record() {
        mediaController.startRecording();
    }

    private void stopRecording() {
        mediaController.stopRecording();
    }

    public static void verifyStoragePermissions(Activity activity) {

        final int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


}
