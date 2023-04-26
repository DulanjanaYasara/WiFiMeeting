package com.example.wifimeeting.card;

import android.content.res.Resources;
import android.util.Log;

import com.example.wifimeeting.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A Member entry in the list of members.
 */
public class MemberEntry {
    private static final String TAG = MemberEntry.class.getSimpleName();

    public final String memberName;
    public final Boolean isMute;


    public MemberEntry(String memberName, Boolean isMute) {

        this.memberName = memberName;
        this.isMute = isMute;
    }

    /**
     * Loads a raw JSON at R.raw.products and converts it into a list of ProductEntry objects
     */
    public static List<MemberEntry> initProductEntryList(Resources resources) {
        InputStream inputStream = resources.openRawResource(R.raw.members);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            int pointer;
            while ((pointer = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, pointer);
            }
        } catch (IOException exception) {
            Log.e(TAG, "Error writing/reading from the JSON file.", exception);
        } finally {
            try {
                inputStream.close();
            } catch (IOException exception) {
                Log.e(TAG, "Error closing the input stream.", exception);
            }
        }
        String jsonProductsString = writer.toString();
        Gson gson = new Gson();
        Type productListType = new TypeToken<ArrayList<MemberEntry>>() {
        }.getType();
        return gson.fromJson(jsonProductsString, productListType);
    }
}