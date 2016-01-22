package android.steveleec.reacher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity {

    public final class Constants {
        public final static String EXTRA_MESSAGE = "android.steveleec.reacher.MESSAGE";
        public static final String EXTENDED_DATA_STATUS = "android.steveleec.reacher.STATUS";
        public static final String BROADCAST_ACTION = "android.steveleec.reacher.BROADCAST";
    }

    Intent intent;
    ImageView indicatorImage;
    ConnectivityManager connectivityManager;
    NetworkInfo networkInfo;

    private ProgressBar spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        indicatorImage= (ImageView) findViewById(R.id.imageViewIndicator);
        indicatorImage.setImageResource(R.drawable.disabled);

        final EditText editTextHost = (EditText) findViewById(R.id.editTextHost);

        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        Button btnTest = (Button) findViewById(R.id.buttonTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextHost.getText().toString();
                networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    spinner.setVisibility(View.VISIBLE);
                    intent = new Intent(getBaseContext(), VerifyHostService.class);
                    intent.putExtra(Constants.EXTRA_MESSAGE, message);
                    startService(intent);
                } else {
                    Toast.makeText(MainActivity.this, "No internet connection.", Toast.LENGTH_LONG).show();
                    indicatorImage.setImageResource(R.drawable.disabled);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(VerifyHostService.Constants.BROADCAST_ACTION));
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean response = intent.getBooleanExtra(Constants.EXTENDED_DATA_STATUS, false);
            if (response) {
                indicatorImage.setImageResource(R.drawable.green);
            } else {
                indicatorImage.setImageResource(R.drawable.red);
            }
            spinner.setVisibility(View.GONE);
        }
    };

}
