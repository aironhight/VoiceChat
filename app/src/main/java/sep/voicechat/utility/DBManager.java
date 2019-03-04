package sep.voicechat.utility;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class DBManager {
    private StorageReference databaseReference;

    public DBManager() {
        databaseReference = FirebaseStorage.getInstance().getReference();
    }

    public void uploadFile(File file, String channelName) {

    }

    public void downloadFile(String channelName, String filename) {
//        File localFile = File.createTempFile("images", "jpg");
//        databaseReference.getFile(localFile)
    }




}
