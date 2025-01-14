package com.JazzDevStudio.LacunaExpress.JavaLeWrapper;

/**
 * Created by Alma on 11/26/2014.
 */


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;

//package JavaLEWrapper;

public class Empire extends LESuperClass{
    static final public String url = "empire";
    private Gson gson = new Gson();
    public String Login(String Username, String Password, int requestID ){
        LoginObject login = new LoginObject(requestID, Username, Password);
        return gson.toJson(login);
    }
    String FetchCaptcha(String SessionID){
        StartOfObject(1, "fetch_captcha");
        String i = "0";
        try{
            JsonWriter writer = new JsonWriter(new StringWriter());
            writer.value(SessionID);
            //writer.value(BodyID);
            writer.endArray();
            writer.endObject();
            writer.close();
            i = gson.toJson(writer);
            //writer.flush();
            i = CleanJsonObject(i);
        }catch(IOException e){
            System.out.println("ioexception");
        }catch(NullPointerException e){
            Log.e("Empire.FecthCaptcha", "object null, null pointer exception");
            System.out.println("null pointer exception");
        }finally{
        }
        return i;
    }
    public String Find(int requestID, String sessionID, String nameToFind){
    	return Request("find", sessionID, String.valueOf(requestID), nameToFind );
    }
    private static void StrikesBack(){}
    //{"id":6,"method":"find","jsonrpc":"2.0","params":["7190e43d-8722-4e5a-ad72-3aacdacfe0df","Norway"]}
    private class LoginObject{

        LoginObject(int i, String u, String p) {
            params[0] = u;
            params[1] = p;
            params[2] = "6266769d-1f73-4325-a40f-6660c4c6440d";  //API Key
            id = i;
        }
        String jsonrpc = "2.0";
        int id;
        String method = "login";
        String[] params = new String[3];
    }

}
