package sep.voicechat.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;

import sep.voicechat.R;
import sep.voicechat.utility.DBManager;
import sep.voicechat.utility.SharedPreferencesController;

public class ChatActivity extends AppCompatActivity implements RecognitionListener {

    private SpeechRecognizer speechRecognizer;
    private MediaRecorder recorder;
    private String channelName;
    private DBManager dbm;
    private SharedPreferencesController spController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        spController = new SharedPreferencesController(this);
        //dbm = spController.getDBManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(permissionGranted() && speechRecognizer != null) {
            initialize();
        } else {
            //Request permissions if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    private boolean permissionGranted() {
        //Check for location services permissions
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    //permission denied - close the app
                    moveTaskToBack(true);
                    return;
                }
                return;
            }
        }
    }

    private void initialize() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
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

    public void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        recorder.start();
    }

    public void stopRecording() {
        recorder.stop();
    }

}
