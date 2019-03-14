package sep.voicechat.model;

import java.util.ArrayList;

public class Message {
    private String filePath;
    private ArrayList<String> listenedBy;

    public Message(String filePath, ArrayList<String> listenedBy) {
        this.filePath = filePath;
        this.listenedBy = listenedBy;
    }

    public Message() {
        //Firebase requires an empty constructor...
    }

    public String getFilePath() {
        return filePath;
    }

    public ArrayList<String> getListenedBy(){
        return listenedBy;
    }

    public void addListener(String userID) {
        if(listenedBy == null ) {
            listenedBy = new ArrayList<>();
        }
        listenedBy.add(userID);
    }
    public static boolean messageIsListened(Message message, String userID) {
        if(message.getListenedBy() == null) {
            return false;
        }
        return message.getListenedBy().contains(userID);
    }

}
