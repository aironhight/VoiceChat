package sep.voicechat.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

public class SharedPreferencesController {
    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Gson gson;

    public SharedPreferencesController(Context context) {
        this.context = context;

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();
        gson = new Gson();
    }

    private String saveDBReference(DBManager DBmanager) {
        String DBMjson = gson.toJson(DBmanager);
        editor.putString("DBmanager", DBMjson);
        editor.commit();
        return DBMjson;
    }

    public DBManager getDBManager() {
        String DBMjson = preferences.getString("DBmanager", saveDBReference(new DBManager()));
        DBManager dbmObject = gson.fromJson(DBMjson, DBManager.class);
        return dbmObject;
    }


}
