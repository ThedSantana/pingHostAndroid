# Verify reachability of a host

TECH STACK: Android Studio 1.5
-------------------------------
It verifies wheter a certain host is reachable or not.
The input is the host name (http://www.google.com) or host ip address (216.58.213.196).

DEVELOPMENT:
------------
The core operation of this app is made in a separate thread created by using Services and Intents.
From the Main Thread, the input is retrieved from the Field Text and passed to the Service. Depending on the type of the input (URL or IP), it will use a HttpURLConnection or Process function to verify its availabillity.

*Input as URL:
```java
//...
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
```

*Input as IP ADDRESS:
```java
//...
try {
      Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -w 1 " + inputs[0]);
      int mExitValue = process.waitFor();
      reachable = mExitValue == 0;
  } catch (InterruptedException ignore) {
      ignore.printStackTrace();
      System.out.println(" Exception:"+ignore);
  } catch (IOException e) {
      e.printStackTrace();
      System.out.println(" Exception:" + e);
  }
```
Once it is verified, the response is broadcasted from the background to the main thread in order to change the UI.
In Main, there is a Broadcaster Receiver waiting for that response. Once the Local broadcaster is called, it updtes the UI element changing its color from Green (success) to Red (Failed).

DEMO:
-----

<p align='center'>

  <img src='https://s3-us-west-1.amazonaws.com/portfoliostevem/androidGifHost.gif' width='375' height='590'/>
  
</p>

