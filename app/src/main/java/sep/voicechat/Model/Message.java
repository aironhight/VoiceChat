package sep.voicechat.model;

import java.util.ArrayList;

public class Message {
    private String filePath;
    private ArrayList<String> listenedBy;

    public Message(String filePath, ArrayList<String> listenedBy) {
        this.filePath = filePath;
        this.listenedBy = listenedBy;
    }

    public String getFilePath() {
        return filePath;
    }

    public ArrayList<String> getListenedBy(){
        return listenedBy;
    }

    public static boolean messageIsListened(Message message, String userId) {
        return message.getListenedBy().contains(userId);
    }

}
