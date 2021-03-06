package sep.voicechat.utility;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class MediaController {
    private final String LOG_TAG = "MediaController";
    private MediaRecorder recorder;
    private MediaPlayer mediaPlayer;
    private String fileName, channelName, userId;
    private IRecorder recorderCallback;

    public MediaController(String channelName, String userId, IRecorder recorderCallback) {
        this.channelName = channelName;
        this.userId = userId;

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.recorderCallback = recorderCallback;
    }

    /**
     * Starts the recording of sound through the mic
     */
    public void startRecording() {
        Date currentTime = Calendar.getInstance().getTime();
        fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileName += "/" + channelName + "_" + userId + "_" + currentTime + ".3gp";

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed :" + e.getMessage());
        }

        recorder.start();
    }

    /**
     * Stops the recording of sound through the mic.
     */
    public void stopRecording() {
        recorder.stop();
        recorder.release();
        recorderCallback.stoppedRecording(fileName);
        recorder = null;
        fileName = "";
    }

    /**
     * Stops and resets the media player
     */
    public void stopListening() {
        mediaPlayer.stop();
        mediaPlayer.reset();
    }

    /**
     * Streams the audio from a given URL
     *
     * @param URL for aduio streaming
     */
    public void streamFromURL(String URL) {
        new Player().execute(URL);
    }

    /**
     * Class for streaming audio files from URL.
     */
    class Player extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            Boolean prepared;

            try {
                mediaPlayer.setDataSource(strings[0]);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {

                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                });

                mediaPlayer.prepare();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        recorderCallback.onListenEnd();
                    }
                });
                prepared = true;

            } catch (Exception e) {
                e.printStackTrace();
                prepared = false;
            }

            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);

            mediaPlayer.start();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}
