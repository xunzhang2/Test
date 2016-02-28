package helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anna on 2/27/16.
 */
public class PreferenceService {
    private Context context;

    public PreferenceService(Context context) {
        this.context=context;
    }
    /*
    save username
     */
    public void save(String username){
        SharedPreferences sharedPreferences=context.getSharedPreferences("user",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("username",username);
        editor.commit();
    }

    public Map<String,String> getPreferences(String filename){
        Map<String,String> map=new HashMap<String,String>();
        SharedPreferences sharedPreferences=context.getSharedPreferences(filename,Context.MODE_PRIVATE);
        map.put("username",sharedPreferences.getString("username","default"));
        return map;
    }
}
