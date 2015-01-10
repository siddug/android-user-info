package com.example.hike;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.TreeMap;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.Person.AgeRange;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.provider.CallLog;
import android.text.format.Time;
import android.text.method.ScrollingMovementMethod;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView.FindListener;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("NewApi") public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener{

    public Context context;
    public HashMap calls 	= new HashMap< String, List<Double> >();
    public HashMap names 	= new HashMap<String, String>();
    
    public List steps       = new ArrayList<Long> ();
    public int steps_int    = 0;
    public int steps_time   = 20000; 
    public List loc			= new ArrayList<Map.Entry<Double, Double>> ();
    public int loc_time		= 10000;

    GPSTracker gps;
    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    private boolean mIntentInProgress;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();
        
        get_email_list();
        get_bf_contacts();
        get_activity_info();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // only for gingerbread and newer versions
        	get_loc_info();
        }
        analyzeApps();
        analyseBrowserHistory();
        
        
        mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(Plus.API)
        .addScope(Plus.SCOPE_PLUS_LOGIN)
        .build();
        mGoogleApiClient.connect();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Listener that handles step sensor events for step detector and step counter sensors.
     */
    private final SensorEventListener mListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                // A step detector event is received for each step.
                // This means we need to count steps ourselves
                steps.add(System.currentTimeMillis());
                steps_int++;
            }
		}
    };
    
    public void get_email_list(){
    	//email id
    	Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
    	Account[] accounts = AccountManager.get(context).getAccounts();
    	
    	HashMap<String, Integer> emails = new HashMap<String, Integer>();
    	
    	for (Account account : accounts) {
    	    if (emailPattern.matcher(account.name).matches()) {
    	        String possibleEmail = account.name;
    	        if(!emails.containsKey(possibleEmail)){
    	        	emails.put(possibleEmail, 1);
    	        }
    	    }
    	}
    	
    	TextView tv = (TextView) findViewById(R.id.email);
    	tv.setText("Below are the list of email id's used to link this device:");
    	int i=1;
    	
    	for( String email : emails.keySet()){
    		tv.setText(tv.getText()+"\n"+String.valueOf(i)+") "+email);
    	}

    }
    
    @TargetApi(Build.VERSION_CODES.KITKAT) public void get_activity_info(){
        // Get the default sensor for the sensor type from the SenorManager
        SensorManager sensorManager =
                (SensorManager) this.getSystemService(Activity.SENSOR_SERVICE);
        // sensorType is either Sensor.TYPE_STEP_COUNTER or Sensor.TYPE_STEP_DETECTOR
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        // Register the listener for this sensor in batch mode.
        // If the max delay is 0, events will be delivered in continuous mode without batching.
        final boolean batchMode = sensorManager.registerListener(
                mListener, sensor, SensorManager.SENSOR_DELAY_NORMAL, 0);
        
        //wait for some time
        // Execute some code after 2 seconds have passed
        final Handler handler = new Handler(){ 
             public void handleMessage(Message msg) { 
            	 super.handleMessage(msg);
            	 TextView tv = (TextView) findViewById(R.id.motion);
            	 
                 tv.setText("You are "+ getActivity(steps));
             } 
        }; 

        new Thread(new Runnable(){
            public void run() {
            // TODO Auto-generated method stub
            while(true)
            {
               try {
                Thread.sleep(steps_time);
                handler.sendEmptyMessage(0);

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 

            }

                            }
        }).start();
    }


    public void get_bf_contacts(){
        //call history
        Uri allCalls = Uri.parse("content://call_log/calls");
        Cursor c = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                String num= c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));// for  number
                String name= c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME));// for name
                String duration = c.getString(c.getColumnIndex(CallLog.Calls.DURATION));// for duration
                int type = Integer.parseInt(c.getString(c.getColumnIndex(CallLog.Calls.TYPE)));// for call type,

                names.put(num, name);
                if(calls.containsKey(num)){
                    List l = new ArrayList<Long>();
                    l.addAll((java.util.Collection) calls.get(num));

                    l.add(Long.valueOf(duration).longValue());
                    calls.put(num,l);
                }
                else{
                    List l = new ArrayList<Long>();
                    l.add(Double.valueOf(duration).longValue());
                    calls.put(num,l);
                }
            }
        }
        ArrayList<Map.Entry<String,Double>> sorted_dials = sortByStatus(calls);
        TextView tv = (TextView) findViewById(R.id.friends);
        tv.setText("You love to talk to ");
        for(int i=0;i<3;i++){
        	String number = sorted_dials.get(i).getKey();
            String name = (String) names.get(number);
            tv.setText(tv.getText()+"\n"+String.valueOf(i+1)+ ") "+name);
        }
        
    }

    ArrayList<Map.Entry<String,Double>> sortByStatus(HashMap<String,ArrayList<Long>> Input) {
        HashMap<String, Double> rankedContacts = new HashMap<String, Double>();

        Set<String> Contacts = Input.keySet();
        for (String Contact : Contacts) {
            ArrayList<Long> calls = Input.get(Contact);
            double weight = 0;
            ArrayList<Long> numCalls = new ArrayList<Long>(3);
            ArrayList<Long> durationCalls = new ArrayList<Long>(3);
            for (int i = 0; i < 3; i++) {
                numCalls.add((long) 0);
                durationCalls.add((long) 0);
            }
            for (long call : calls) {
                if (call < 300) {
                    numCalls.set(0, numCalls.get(0) + 1);
                    durationCalls.set(0, numCalls.get(0) + call);
                } else if (call < 600) {
                    numCalls.set(1, numCalls.get(1) + 1);
                    durationCalls.set(1, numCalls.get(1) + call);
                } else {
                    numCalls.set(2, numCalls.get(2) + 1);
                    durationCalls.set(2, numCalls.get(2) + call);
                }
            }

            weight = 2 * (numCalls.get(0)) + 6 * (numCalls.get(1)) + 15 * (numCalls.get(2));
            rankedContacts.put(Contact, weight);
        }
        ArrayList<Map.Entry<String,Double>> scoredContacts = new ArrayList<Map.Entry<String, Double>>(rankedContacts.entrySet());
        Collections.sort(scoredContacts, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        return scoredContacts;
    }

    public String getActivity(List<Long> timeSteps) {
    	
    	if(timeSteps.size()==0){
    		if(Time.HOUR>21){
    			return " mostly sleeping";
    		}
    		return "idle";
    	}
    	//use steps_time for now
        float speed = ((timeSteps.size())*1000)/steps_time;
        String result = "";

         System.out.println(speed);
        if (speed < 0.3)
            result = "moving very slowly";
        else if (speed >= 0.3 && speed < 1.0)
            result = "slow walk";
        else if (speed >=1.0 && speed < 1.8)
            result = "normal walk";
        else if (speed >=1.8 && speed < 2.4)
            result = "race walking/jogging";
        else if (speed >= 2.4)
            result = "running";

        steps.clear();
        return result;
    }
    
    public void get_loc_info(){
    	gps = new GPSTracker(MainActivity.this);
        
        final Handler handler = new Handler(){ 
            public void handleMessage(Message msg) { 
           	 super.handleMessage(msg);
           	 	TextView tv = (TextView) findViewById(R.id.transit);
           	 	tv.setText("You are "+getTransit() + " now");
            } 
       }; 

       new Thread(new Runnable(){
           public void run() {
           // TODO Auto-generated method stub
           while(true)
           {
              try {
               Thread.sleep(loc_time);
               handler.sendEmptyMessage(0);

           } catch (InterruptedException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           } 

           }

                           }
       }).start();
       
    }

	public static double haversine(
	        double lat1, double lng1, double lat2, double lng2) {
	    int r = 6371; // average radius of the earth in km
	    double dLat = Math.toRadians(lat2 - lat1);
	    double dLon = Math.toRadians(lng2 - lng1);
	    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
	       Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) 
	      * Math.sin(dLon / 2) * Math.sin(dLon / 2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    double d = r * c;
	    return d;
	}
	
	public String findSpeed( List<entry> Positions ){
		double speed = 0;
		
		if(Positions.size()<=1){
			return "not in transit";
		}
		
		double startTime = Positions.get(0).time;
		double endTime = startTime;
		entry prevEntry = Positions.get(0);
		
		entry startPos = Positions.get(0);
		entry endPos   = Positions.get(Positions.size()-1);
		
		double totalDistance = 0;
		
		for(int i=1;i<Positions.size();i++){
			double curTime = Positions.get(i).time;
			entry curPos = Positions.get(i);
			double distanceCovered = haversine(prevEntry.latitude,prevEntry.longitude,curPos.latitude,curPos.longitude);
			totalDistance += distanceCovered;
			endTime = curTime;
			prevEntry = curPos;
		}
		
		double totalDisplacement = haversine(startPos.latitude, startPos.longitude, endPos.latitude, endPos.longitude);
		
		
		
		speed = (totalDistance*1000000/(endTime-startTime));		
		Double velocity = totalDisplacement/(endPos.time-startPos.time);
		
		String s = "speed: " + speed + " dist: " + totalDistance + " disp: " + totalDisplacement + " vel: " + velocity;
		String status = "";
		if(speed>5){
			status = "in transit";
		}
		else{
			status += "not in transit";
		}
		//return s // for debugging
		return status;
	}
	
	public String getTransit(){
		List<entry> loc = gps.getList(); 
		String mesg = findSpeed(loc);
		return mesg;
	}
	
	public void analyzeApps(){
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    	List<ApplicationInfo> packages = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
    	
    	List<String> pkgApps = new ArrayList<String>();
    	TreeMap<Long, String> sentData = new TreeMap<Long, String>();
    	HashMap<String, Long> recData = new HashMap<String, Long>();    	
    	
    	for(ApplicationInfo packageInfo : packages){
    		String name = packageInfo.loadLabel(getPackageManager()).toString();
    		pkgApps.add(name);
    		long sent = TrafficStats.getUidTxBytes(packageInfo.uid);
    		long rec  = TrafficStats.getUidRxBytes(packageInfo.uid);
    		sentData.put(sent, name);
    		recData.put(name, rec);
    		
    	}
    
    	/*
    	for( Long key : sentData.keySet()){
    		add(String.valueOf(key) + " : " + sentData.get(key));
    	}
    	for( Long key : recData.keySet()){
    		add(String.valueOf(key) + " : " + recData.get(key));
    	}
    	*/
    	
    	List<String> ls = top3Apps(sentData, recData);
    	
    	TextView tv = (TextView) findViewById(R.id.apps);
    	tv.setText("You like to open these apps much! - ");
    	for(int i=0;i<3;i++){
    		tv.setText(tv.getText()+"\n" + String.valueOf(i+1)+ ") "+ls.get(i));
    	}
    	//add(sentData.toString());
    	//add(recData.toString());
    	
	}
	
	public void analyseBrowserHistory(){
		String[] proj = new String[] { Browser.BookmarkColumns.TITLE,Browser.BookmarkColumns.URL };
		Uri uriCustom = Uri.parse("content://com.android.chrome.browser/bookmarks");
		String sel = Browser.BookmarkColumns.BOOKMARK + " = 0"; // 0 = history, 1 = bookmark
		Cursor mCur = getContentResolver().query(uriCustom, proj, sel, null, null);
		mCur.moveToFirst();
		@SuppressWarnings("unused")
		String title = "";
		@SuppressWarnings("unused")
		String url = "";

		List<String> sites = new ArrayList<String>();
		List<String> words = new ArrayList<String>();
		if (mCur.moveToFirst() && mCur.getCount() > 0) {
		    boolean cont = true;
		    while (mCur.isAfterLast() == false && cont) {
		        title = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
		        url = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.URL));
		    	
		    	sites.add(url);
		    	words.add(title);
		    	
		        mCur.moveToNext();
		    }
		}
		
		top10Sites(sites);
		top10Words(words);
	}

	
	/* received map is hashmap with key appname and value its data usage */
	@SuppressLint("NewApi") private List<String> top3Apps(TreeMap<Long,String> sentMap,HashMap<String,Long> receivedMap) {
		Long sum= (long) 0;
		List<String> topapps = new ArrayList<String>();
		int count=0;
		for(Entry<String, Long> entry : receivedMap.entrySet()) {
			sum += entry.getValue();
			count++;
		}
		Long avg = sum/count;
		int apps = 0;
		for(Entry<Long, String> entry : sentMap.descendingMap().entrySet()) {
		  String appName = entry.getValue();
		  if(apps >= 3)
		  	break;
		  if(!appName.toLowerCase().contains("google")) {
		  	if(receivedMap.get(appName) > avg) {
		  		apps++;
		  		//System.out.println(appName);
		  		topapps.add(appName);
		  	}
		  }
		}
		return topapps;
	}

	 protected void onStart() {
		    super.onStart();
		    
		  }

		  protected void onStop() {
		    super.onStop();

		    if (mGoogleApiClient.isConnected()) {
		      mGoogleApiClient.disconnect();
		    }
		  }
		  
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		if (!mIntentInProgress && result.hasResolution()) {
		    try {
		      mIntentInProgress = true;
		      startIntentSenderForResult(result.getResolution().getIntentSender(),
		          RC_SIGN_IN, null, 0, 0, 0);
		    } catch (SendIntentException e) {
		      // The intent was canceled before it was sent.  Return to the default
		      // state and attempt to connect to get an updated ConnectionResult.
		      mIntentInProgress = false;
		      mGoogleApiClient.connect();
		    }
		  }
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
			Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
			String personName = currentPerson.getDisplayName();
			AgeRange Age = currentPerson.getAgeRange();
			int Gender = currentPerson.getGender();
	    	
	    	TextView tv = (TextView) findViewById(R.id.age);
	    	tv.setText("");
	    	if(Age.hasMin()){
	    		tv.setText(tv.getText()+"You are min "+ Age.getMin() + " old");
	    	}
	    	if(Age.hasMax()){
	    		String s ="";
	    		if(Age.hasMin()){
	    			s="\n";
	    		}
	    		tv.setText(tv.getText()+s+"You are max "+ Age.getMax() + " old");
	    	}
	    	tv = (TextView) findViewById(R.id.gender);
	    	if(Gender==0){
	    		tv.setText("You are a male");
	    	}
	    	else{
	    		tv.setText("You are a female");
	    	}
	    	tv = (TextView) findViewById(R.id.hi);
	    	tv.setText("Hi "+ personName);
	    	
	    	tv = (TextView) findViewById(R.id.profile);
	    	tv.setText("");
	    	if(currentPerson.hasBirthday()){
	    		tv.setText(tv.getText()+ "\nYour birthday is on: "+ currentPerson.getBirthday());
	    	}
	    	if(currentPerson.hasCurrentLocation()){
	    		tv.setText(tv.getText()+"\nYour currently at: "+currentPerson.getCurrentLocation());
	    	}
	    	if(currentPerson.hasLanguage()){
	    		tv.setText(tv.getText()+"\nYou speak "+ currentPerson.getLanguage());
	    	}
	    	if(currentPerson.hasRelationshipStatus()){
	    		String s = "single";
	    		int i = currentPerson.getRelationshipStatus();
	    		if(i==0){
	    			s = "single";
	    		}
	    		if(i==1){
	    			s="in relationship";
	    		}
	    		tv.setText(tv.getText()+"\nYou are "+ s);
	    	}
	    	
		}
	}

	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub
		mGoogleApiClient.connect();
	}
		
	
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		  if (requestCode == RC_SIGN_IN) {
		    mIntentInProgress = false;

		    if (!mGoogleApiClient.isConnecting()) {
		      mGoogleApiClient.connect();
		    }
		  }
	}
	static class ValueComparator implements Comparator<String> {

	    Map<String, Integer> base;
	    public ValueComparator(HashMap<String, Integer> map) {
	        this.base = map;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}

	private void top10Words(List<String> lines) {
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		ValueComparator bvc =  new ValueComparator(map);
	    TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
		for(String line : lines) {
			String[] words = line.split(" ");
			for(String word : words) {
				if(word.length() > 3 && !word.toLowerCase().contains("google") && !word.toLowerCase().contains("search")) {

					if(map.containsKey(word)) {

					map.put(word, map.get(word) + 1);

					}

					else {

					map.put(word, 1);

					}
					}
			}
		}
		sorted_map.putAll(map);
		int count=0;
		
		TextView tv = (TextView) findViewById(R.id.interests);
		tv.setText("top ten interests from your search history: ");
		
		for(Entry<String, Integer> entry : sorted_map.entrySet()) {
			if(count >= 10)
				break;
			tv.setText(tv.getText()+"\n"+String.valueOf(count+1)+") "+ entry.getKey());
			count++;
		}
	}
	public void top10Sites(List<String> sites) {
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		ValueComparator bvc =  new ValueComparator(map);
	    TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
	    for(String site : sites) {
	    	URL url;
			try {
				url = new URL(site);
				site = url.getHost();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	if(!site.contains("google")){
		    	if(map.containsKey(site)) {
					map.put(site, map.get(site) + 1);
				}
				else {
					map.put(site, 1);
				}
	    	}
	    }
	    sorted_map.putAll(map);
		int count=0;
		
		TextView tv = (TextView) findViewById(R.id.sites);
		tv.setText("top ten sites from your search history: ");
		
		for(Entry<String, Integer> entry : sorted_map.entrySet()) {
			if(count >= 10)
				break;
			tv.setText(tv.getText()+"\n"+String.valueOf(count+1)+") "+ entry.getKey());
			count++;
		}
	}
	
}



