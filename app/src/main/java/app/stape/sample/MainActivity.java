package app.stape.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Calendar;

import app.stape.Stape;
import app.stape.StapeBroadcast;
import app.stape.StapePermissions;

public class MainActivity extends AppCompatActivity {

    Button identifyNowBtn;
    TextView responseTxt;

    MyBroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        identifyNowBtn = findViewById(R.id.identify_now);
        responseTxt = findViewById(R.id.response_text);

        StapePermissions.init(this);
        broadcastReceiver = new MyBroadcastReceiver();
        registerReceiver(broadcastReceiver, new IntentFilter(StapeBroadcast.BROADCAST_ACTION_RESULT));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String permissions[], @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        StapePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    public void identifyNow(View view){
        Stape.identifyNow(this);
        responseTxt.setText(R.string.response_is_recording);
        identifyNowBtn.setEnabled(false);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null || intent.getAction() == null) {
                return;
            }
            if(intent.getAction().equals(StapeBroadcast.BROADCAST_ACTION_RESULT)){
                finishedProcessingListener(context, intent);
            }
        }

        private void finishedProcessingListener(@NotNull Context context, @NotNull Intent intent){
            String reason = intent.getStringExtra(StapeBroadcast.BROADCAST_EXTRA_RESULT_CODE);
            identifyNowBtn.setEnabled(true);


            // FINGERPRINT GENERATED
            if(reason.equals(StapeBroadcast.FINGERPRINT_CREATED)) {
                responseTxt.setText(R.string.response_is_identifying);
            }

            // SUCCESS!!! The music was identified
            else if(reason.equals(StapeBroadcast.SUCCESS)) {
                String result = intent.getStringExtra(StapeBroadcast.BROADCAST_EXTRA_RESULT_VALUE);

                String time = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                StringBuilder response = new StringBuilder(time + "\r\n");
                try {
                    JSONObject recognition = new JSONObject(result);
                    String title = recognition.getString("title");
                    response.append(title).append("\r\n");
                    JSONArray artists = recognition.getJSONArray("artists");
                    for(int i = 0; i < artists.length(); i++){
                        String artist = artists.getJSONObject(i).getString("name");
                        response.append(artist).append(";");
                    }
                    response.append("\r\n");
                } catch (JSONException ignore) { }

                responseTxt.setText(response.toString());
            }

            // UNSUCCESS. The audio didn't match any song
            else if(reason.equals(StapeBroadcast.UNSUCCESS)){
                responseTxt.setText(R.string.response_no_result);
            }

            // OTHER SIGNALS
            else{
                responseTxt.setText("");
            }
        }
    }

}
