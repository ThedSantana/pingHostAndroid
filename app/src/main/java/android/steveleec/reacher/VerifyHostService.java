package android.steveleec.reacher;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by steveleec on 1/21/16.
 */
public class VerifyHostService extends Service {
    public final class Constants {
        public static final String BROADCAST_ACTION = "android.steveleec.reacher.BROADCAST";
        public static final String EXTENDED_DATA_STATUS = "android.steveleec.reacher.STATUS";
        public final static String EXTRA_MESSAGE = "android.steveleec.reacher.MESSAGE";
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    boolean reachable = false;
    Intent intent;
    String typeOfInput;

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(Constants.BROADCAST_ACTION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = (String) intent.getExtras().get(Constants.EXTRA_MESSAGE);

        typeOfInput = getTypeOfInput(input);
        if (typeOfInput == "invalid") {
            super.onDestroy();
            return START_STICKY;
        }

        DoBackGroundTask doBackGroundTask = new DoBackGroundTask();
        doBackGroundTask.execute(input);

        super.onDestroy();
        return START_STICKY;
    }

    private String getTypeOfInput(String input) {
        boolean isIP = false;
        boolean isValidIP = false;

        String[] array = input.split("\\.");
        if (array.length == 4 && !input.contains("www") && !input.contains("http")) {
            for (String ipNum: array) {
                isIP = TextUtils.isDigitsOnly(ipNum);
                if (!isIP) {
                    break;
                } else {
                    if (Integer.parseInt(ipNum) >= 0 && Integer.parseInt(ipNum) <= 255) {
                        isValidIP = true;
                    } else {
                        Toast.makeText(this, "The IP address is out of range.", Toast.LENGTH_LONG).show();
                        isValidIP = false;
                        break;
                    }
                }
            }
        }
        if (isIP) {
            return "IP";
        } else if (isIP && !isValidIP) {
            intent.putExtra(Constants.EXTENDED_DATA_STATUS, false);
            sendBroadcast(intent);
            return "invalid";
        }

        try {
            new URL(input);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            if (e.getMessage().contains("Unknown protocol")) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            } else if (e.getMessage().contains("Protocol not found")) {
                Toast.makeText(this, "Provide a protocol (http:// or https://)", Toast.LENGTH_LONG).show();
            }
            intent.putExtra(Constants.EXTENDED_DATA_STATUS, false);
            sendBroadcast(intent);
            return "invalid";
        }
        return "URL";
    }

    public class DoBackGroundTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String...inputs) {
            // Checks when input is URL: https://www.google.com
            if (typeOfInput == "URL") {
                try {
                    URL input = new URL(inputs[0]);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) input.openConnection();
                    httpURLConnection.setRequestProperty("User-Agent", "Android Application");
                    httpURLConnection.setRequestProperty("Connection", "close");
                    httpURLConnection.setConnectTimeout(2000);
                    httpURLConnection.connect();
                    reachable = (httpURLConnection.getResponseCode() == 200);
                } catch (IOException e) {
                    reachable = false;
                }
            } else {
                // Checks when input is IP: 216.58.213.196
                try {
                    Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -w 1 " + inputs[0]);
                    int mExitValue = process.waitFor();
                    reachable = mExitValue == 0;
                }
                catch (InterruptedException ignore) {
                    ignore.printStackTrace();
                    System.out.println(" Exception:"+ignore);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.out.println(" Exception:" + e);
                }
            }
            intent.putExtra(Constants.EXTENDED_DATA_STATUS, reachable);
            sendBroadcast(intent);
            return reachable;
        }
    }
}