package com.administrator.projecte;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
// com.google.firebase.messaging.FirebaseMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.MediaType;

import static com.administrator.projecte.R.layout.activity_contacts;

/**
 * Created by bluekey630 on 5/31/2017.
 */

public class UsersActivity extends AppCompatActivity {

    ListView userList;
    EditText groupName;
    EditText groupPassword;
    Button inviteButton;
    LinearLayout user_item;
    ProgressDialog pd;
    int totalUser = 0;
    ArrayList<String> user = new ArrayList<>();
    ArrayList<String> checked = new ArrayList<>();
    ArrayList<String> tokens = new ArrayList<>();
    private static final String TAG = "UsersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_contacts);

        setUI();

        pd = new ProgressDialog(UsersActivity.this);
        pd.setMessage("Loading...");
        pd.show();
        String url = "https://projecte-e0a1f.firebaseio.com/users.json";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                doOnSuccess(s);
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError);
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(UsersActivity.this);
        rQueue.add(request);
    }

    private void setUI() {

        userList = (ListView) findViewById(R.id.contact_list);
        groupName = (EditText) findViewById(R.id.group_name);
        groupPassword = (EditText) findViewById(R.id.group_password);
        inviteButton = (Button) findViewById(R.id.btn_invite);
        user_item = (LinearLayout)findViewById(R.id.list_user);

        groupName.requestFocus();

        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = String.valueOf(R.drawable.table_cell_uncheck);
                if (checked.get(position).equals(str)) {
                    checked.set(position, String.valueOf(R.drawable.table_cell_check));
                }
                else {
                    checked.set(position, String.valueOf(R.drawable.table_cell_uncheck));
                }

                userList.invalidateViews();
            }
        });

        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int selected = 0;
                for (int i = 0; i < checked.size(); i++) {
                    String str = String.valueOf(R.drawable.table_cell_check);
                    if (checked.get(i).equals(str)) {
                        selected++;
                    }
                }

                if (selected == 0) {
                    Toast.makeText(UsersActivity.this, "Please select users.",
                            Toast.LENGTH_SHORT).show();
                }
                else if (groupName.getText().toString().length()<1) {
                    groupName.setError("Can't be blank");
                }
                else if (groupPassword.getText().toString().length() < 1) {
                    groupPassword.setError("Can't be blank");
                }
                else {

                    for (int i = 0; i < checked.size(); i++) {
                        String str = String.valueOf(R.drawable.table_cell_check);
                        if (checked.get(i).equals(str)) {
                            String token = tokens.get(i);
                            Log.d(TAG, token);
                            try {
                                String msg = "Group Name: " + groupName.getText().toString() + " Password: " + groupPassword.getText().toString();
                                sendNotification(msg, "fd", token);
                                startActivity(new Intent(UsersActivity.this, MainActivity.class));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }
        });
    }

    public void doOnSuccess(String s){
        try {
            JSONObject obj = new JSONObject(s);

            Iterator i = obj.keys();

            String key = "";
            JSONObject pswObj = null;
            while(i.hasNext()){
                key = i.next().toString();
                pswObj = obj.getJSONObject(key);
                String token = pswObj.getString("token");

                if (UserDetails.username.equals(key)) {
                    continue;
                }
                user.add(key);
                checked.add(String.valueOf(R.drawable.table_cell_uncheck));
                tokens.add(token);
                totalUser++;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(totalUser <1){
            userList.setVisibility(View.GONE);
        }
        else{
            userList.setVisibility(View.VISIBLE);

            CustomAdapter adapter = new CustomAdapter(getApplicationContext(), user, checked);
            userList.setAdapter(adapter);
        }
        pd.dismiss();
    }

    private void sendNotification(String text, String senderId, String receiverId) throws JSONException {
        String url = "https://fcm.googleapis.com/fcm/send";
        final String API_KEY = "AIzaSyCbNOxzF-zoCp7yuFBAwCPyGOuqDw8HPas";
        String token = receiverId;

        final JSONObject json = new JSONObject();
        JSONObject notification = new JSONObject();
        notification.put("title", "Hi");
        notification.put("body", text);
        json.put("to",token);
        json.put("notification", notification);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("" + error);
            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return json.toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization", "key="+API_KEY);
                return headers;
            }
        };

        RequestQueue rQueue = Volley.newRequestQueue(UsersActivity.this);
        rQueue.add(request);
    }

}
