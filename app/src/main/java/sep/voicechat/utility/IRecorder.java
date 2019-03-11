package sep.voicechat.utility;

public interface IRecorder {
    void stoppedRecording(String fileName);
    void onListenEnd();
}
