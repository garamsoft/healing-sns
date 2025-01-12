package kr.ac.seoultech.healing;

import android.content.Intent;
import android.os.IBinder;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/* 주기적으로 돌면서  TopActivity 를 얻어서 이를 기록 함 */
public class AppUsageTrackingService extends Service
{
	
  private static List<String> listLauncherApp = null;
  
  // 현재 동작 상태 
  static boolean mIsServiceRunning = false;
  
  private static Timer timerDeleteOldRecords;
  private static Timer timerUsageTracking = null;
  
  private int applicationUsagePeriod = 0;
  private String[] columnsSettings = { "_id", "action", "value" };
  long delayUsageTracking = 0L;
  
  // 현재 사용 응용 이름
  private String lastApplicationName = null;
  private String lastPackageName = null;
  
  // Status Bar & Noti 
  private NotificationManager mNM;
  private Notification mNotification;
  private int NOTIFICATION = R.id.menu_settings; // ??
  
  // 측정 파라메터 
  private String sensitivity = "mid";
  long periodForUsageTracking = 4000L;
  private long startDateTime = 0L;

  static
  {
    timerDeleteOldRecords = null;
  }

  
  /* 측정 간격 파라메터를 컴텐츠-프로바이더에서 읽어서, 세팅함  
   * 
 
   * */
  private void readConfigAndSetSensitivity(String paramString)
  {
	 
	Uri localUri = AppUsage.AppUsageColumns.CONTENT_URI_SETTINGS;
	
    Cursor localCursor = getContentResolver().query(localUri,
    												this.columnsSettings,			// 프로젝션 
    												null,							// Selection 문 
    												new String[] { paramString },   // 찾고자하는 파라메터 
    												null);
    
    // 한개의 record가 발견 �으면, 특히 1개이면, 그 중  3번째 항목인 Value를 읽어옴. 
    if ((localCursor.moveToFirst()) && (localCursor.getCount() == 1))
      sensitivity = localCursor.getString(2);
  
      localCursor.close();
    
      return;
    
    //delayUsageTracking = 0L;
    //periodForUsageTracking = 4000L;    
  }

  
  /* 
   * Status Bar에  Notification 표시. 
   * 
   * 그 것을 눌렀을 때 메인 엑티비티가 동작하도록 함
   * 
   * @todo 죽고 살았음을 알리는 것이 좋을 것 같음. 
   *  
   * */
  private void showNotification()
  {	 	
    // 1. Noti 화면에 나타날 내용 
  	CharSequence localCharSequence = getText(R.string.app_name);
    mNotification = new Notification(R.drawable.ic_launcher, localCharSequence, System.currentTimeMillis());
	
    // 2. Noti 를 누른 경우 Intent 가 발생하도록 세팅 
    //PendingIntent localPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, AppUsageTrackingActivity.class), 0);
    PendingIntent localPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
    mNotification.setLatestEventInfo(this, getText(R.string.app_name), localCharSequence, localPendingIntent);
    
    // 3. 표시 
    mNM.notify(0, mNotification);  // 0 은 ID, 같은 App에서 동일한 번호로 보내면 이전 내용이 update 됨. 
  }
  
  
  /* 서비스  시작  */
  
  private void startService(Context ctx)
  {
    Log.d("AppUsageTracking", "Started tracking");
    
    /*
    // 1. 기록을 지우는 Timer 생성 
    timerDeleteOldRecords = new Timer("AppUsageDeleteOldRecords");
    this.lastApplicationName = null;
    this.applicationUsagePeriod = 0;
    this.startDateTime = 0L;
  
    timerDeleteOldRecords.scheduleAtFixedRate(new AppTimerDeleteTask(null), 500L, 86400000L);  // 24시간 
     
     */
    
    // 2. 서비스 상태를 확인하고, 시작 시킴  
    //getServiceStatusAndExecute(this);
    if(mIsServiceRunning != true){
    
    	
    }
    
    // 3. @todo 결과를 확인해보고 true 로 해야할 것같음. 
    mIsServiceRunning = true;
  }

  
  /* 측정 주기를 세팅 
   * 
   * 현재는 LOW, MID, High의 3단계로 되어 있음.
   * 
   * */
  private void updateTimer(){
	
	// 1. low/mid/high => period in ms   
    if (this.sensitivity.equals("Low"))
        periodForUsageTracking = 8000L;
    else if (this.sensitivity.equals("Mid"))
        periodForUsageTracking = 4000L;
    else if (this.sensitivity.equals("High"))
        periodForUsageTracking = 2000L;
    
    
    Log.d("AppUsageTracking", "Got sensitivty:" + this.sensitivity + 
    		" and setting period for Usage Tracking:" + periodForUsageTracking);
    
    // 2. 타이머 세팅  
    if (timerUsageTracking != null){ // 이전에 세팅된 것이 있으면 없엠
      timerUsageTracking.cancel();
    }
    
    timerUsageTracking = new Timer("AppUsageTracking");
    timerUsageTracking.scheduleAtFixedRate(new AppTimerTask(), delayUsageTracking, periodForUsageTracking);
    
    return;
    
    
  }
  
   /* 
   * 현재 서비스 가 시작중인지를 확인해서, 시작되지 않은 경우만 실행하도록 함? 
   * */
  public void getServiceStatusAndExecute(Service targetSvc)
  {
  	  
	// 1. 이미 수행중인지 확인
	int i = -1;
/*	  
	Uri localUri1 = AppUsage.AppUsageColumns.CONTENT_URI_SETTINGS_COUNT;
    Cursor localCursor1 = getContentResolver().query(localUri1,
     												new String[] { "row_count" },
     												null,
     												new String[] { "tracking_started" }, 
     												null);
    
    if (localCursor1.moveToFirst()){
      i = localCursor1.getInt(0);
      Log.d("AppUsageTracking", "Got row_count for tracking_started action: " + i);
    }
    localCursor1.close();
  
*/  
	  
	// 2. 아직 수행 중이 아니면, 자신의  서비스를 시작함?????
    if (i == 0){
    	
      Log.i("AppUsageTracking", "Started tracking: " );
      targetSvc.startService(new Intent(targetSvc, AppUsageTrackingService.class));
      
/*  시작 한것을 기록       
      ContentValues localContentValues = new ContentValues();
      localContentValues.put("action", "tracking_started");
      localContentValues.put("value", "true");
      targetSvc.getContentResolver().insert(AppUsage.AppUsageColumns.CONTENT_URI_SETTINGS, localContentValues);
*/      
    }
 
    
    /*
    
    Uri localUri2 = AppUsage.AppUsageColumns.CONTENT_URI_SETTINGS;
    String[] arrayOfString = { "_id", "action", "value" };
    Cursor localCursor2 = targetSvc.getContentResolver().query(localUri2,
    															arrayOfString,
    															null, 
    															new String[] { "tracking_started" },
    															null);
    if ((localCursor2.moveToFirst()) && (localCursor2.getCount() == 1) && (localCursor2.getString(2).equals("true")))
        Log.i("AppUsageTracking", "Started tracking: " + targetSvc.startService(new Intent(targetSvc, AppUsageTrackingService.class)));
    
      localCursor2.close();
   */
 }

  // Binder RPC 지원 안함.
  public IBinder onBind(Intent paramIntent)
  {
    return null;
  }

  /* 
   *  서비스가 처음 사용될 때 호출됨
   *     
   *  
   */
  public void onCreate(){
	  
    super.onCreate();
    
    // @todo 미리 준비해두는 것이 도움이 될찌?
    //listLauncherApp = MiscUtils.getHomeActivity(getApplicationContext());
    mNM = ((NotificationManager)getSystemService("notification"));
   
    // 2. 서비스 시작 
    startService(this);
    
    // 3. 상태바에 시작되었음을 알림.
    showNotification();
  }

  public void onDestroy()
  {
    super.onDestroy();
    ContentValues localContentValues = new ContentValues();
    if (this.lastApplicationName.indexOf(".") == -1)
      localContentValues.put("app", this.lastApplicationName);
    while (true)
    {
      localContentValues.put("app_pkg", this.lastPackageName);
      localContentValues.put("date_time", Long.valueOf(this.startDateTime));
      localContentValues.put("duration", Integer.valueOf(1 + this.applicationUsagePeriod));
      try
      {
        Log.d("AppUsageTracking", "Stored the App Usage info into DB:" + this.lastApplicationName + " " + this.startDateTime + " " + this.applicationUsagePeriod + " Received URI:" + getContentResolver().insert(AppUsage.AppUsageColumns.CONTENT_URI, localContentValues));
        this.mNM.cancel(2130968579);
        timerUsageTracking.cancel();
        timerDeleteOldRecords.cancel();
        mIsServiceRunning = false;
        Log.i("AppUsageTracking", "Stopped the tracking...");
        return;
        //localContentValues.put("app", MiscUtils.convertPkgNameToAppName(this.lastApplicationName));
      }
      catch (SQLException localSQLException)
      {
        while (true)
          Log.w("AppUsageTracking", "Sql Exception: " + localSQLException.getMessage());
      }
    }
  }
  
  
  /* 시스템에 메모리가 모자라는 상태 */
  public void onLowMemory()
  {
    super.onLowMemory();
    Log.i("AppUsageTracking", "Service running in low memory!!! Stopping the service!!!");
    onDestroy();
  }

  public void onStart(Intent paramIntent, int paramInt)
  {
    super.onStart(paramIntent, paramInt);
    Log.i("AppUsageTracking", "Service onStart getting called!!!");
  }
  
  /* 서비스 시작 명령을 받은 경우 호출  */
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    try
    {
      super.onStartCommand(intent, flags, startId);
      Log.d("AppUsageTracking", "Received Intent to start sevice or change sensitivity!!!");
      
      if (intent != null){
        
    	  // 주기 정보 
    	  Bundle localBundle = intent.getExtras();
    	  if (localBundle != null)
    		  this.sensitivity = ((String)localBundle.getCharSequence("sensitivity"));
    	  else
    		  this.sensitivity = "Low"; 
        
    	  // 타이머 세팅 
          updateTimer();
          
          // 주기 저장 
          //readConfigAndSetSensitivity("sensitivity");
        }
     } catch (Exception localException) {
        Log.d("AppUsageTracking", "Some problem occured! Handle it gracefully!!!");
        readConfigAndSetSensitivity("sensitivity");
      }
     
     //	We want this service to continue running until it is explicitly
     // stopped, so return sticky.
     return  START_STICKY; //START_FLAG_REDELIVERY; ??
  }

  /*----------------------------------------------------------------------------------------------
   *  기간이 지난 데이터를 지우는 Task  
   * 
   *  -------------------------------------------------------------------------------------------
   */
   private class AppTimerDeleteTask extends TimerTask
  {
    private AppTimerDeleteTask(){ 
    	
    }
    
    public void run(){
    
    	Uri localUri1 = AppUsage.AppUsageColumns.CONTENT_URI_SETTINGS;
      
    	Cursor localCursor = AppUsageTrackingService.this.getContentResolver().query(localUri1,
    		  AppUsageTrackingService.this.columnsSettings,
    		  null,
    		  new String[] { "delete" },
    		  null);
     
    	Uri localUri2;
    	long now, time_of_record;
    	String str;
      
    	if ((localCursor.moveToFirst()) && (localCursor.getCount() == 1)){
    	
    		localUri2 = AppUsage.AppUsageColumns.CONTENT_URI;
    		now = System.currentTimeMillis() / 1000L;
    		time_of_record = 0L;
    		str = localCursor.getString(2);
    		
      		if (str.equals("Never delete usage data"))
    			return;
    
      		if (!str.equals("Clear All Data"));
      			//break;
      		
      		AppUsageTrackingService.this.getContentResolver().delete(localUri2, null, null);
        
    	
    		// 지울 기간을 계산 
    		if (!str.equals("More than 7 Days"))
      			//break label178;
    			time_of_record = now - 604800L;
      		else if (str.equals("More than 30 Days"))
      			time_of_record = now - 2592000L;
            else if (str.equals("More than 60 Days"))
            	time_of_record = now - 5184000L;
            else if (str.equals("More than 6 Months"))
            	time_of_record = now - 15724800L;
            else if (str.equals("More than 1 Year"))
            	time_of_record = now - 31536000L;
          
    	
    	ContentResolver localContentResolver = AppUsageTrackingService.this.getContentResolver();
    	String[] arrayOfString = new String[1];
    	arrayOfString[0] = String.valueOf(time_of_record);
        localContentResolver.delete(localUri2, "date_time<?", arrayOfString);
        localCursor.close();
    	}
     
    }
  }
  
  /* 주기적으로 측정을 하는 핵심 태스크 */  
  private class AppTimerTask extends TimerTask
  {
	  
	private final static String  TAG ="AppTimerTask";
	private  int count = 0;
    private AppTimerTask()
    {
    }

    
    public void run(){
    	
     Log.d(TAG, "Running Count = " + ++count + "time" + System.currentTimeMillis());	
         
     
      // 1.1. get the current package name  	
      ActivityManager activityMgr = (ActivityManager)AppUsageTrackingService.this.getSystemService("activity");
      ActivityManager.RunningTaskInfo taskInfo = (ActivityManager.RunningTaskInfo)activityMgr.getRunningTasks(1).get(0);
      String topPackageName = taskInfo.topActivity.getPackageName();
    
      // 1.2. Get the corresponding (current) App. name
      PackageManager localPackageManager = AppUsageTrackingService.this.getPackageManager();
      String topAppName = "";
      try{
          ApplicationInfo localApplicationInfo = localPackageManager.getApplicationInfo(topPackageName, 0);
          CharSequence appLabel;
          
          if (localApplicationInfo != null){
        	
        	// 라벨은 왜 필요?
        	appLabel = localPackageManager.getApplicationLabel(localApplicationInfo);
            topAppName = (String)appLabel;
            
            Log.d(TAG, "Current Top: appname = " + topAppName + ", package = " +  topPackageName + ", time =" + System.currentTimeMillis() / 1000L);
            
            if (lastApplicationName == null){ // 이전 app이 없음 (맨처음 호출시)
              lastApplicationName = topAppName;
              lastPackageName = topPackageName;
              startDateTime = (System.currentTimeMillis() / 1000L);
            }
          } else {
        	appLabel = "App Null";
            //continue;
          }
        }catch (PackageManager.NameNotFoundException localNameNotFoundException){
        	
          Log.d(TAG, "Name Not Found Exception: " + localNameNotFoundException.getMessage());
          
          if (lastApplicationName.equalsIgnoreCase(topAppName))
          {
            AppUsageTrackingService localAppUsageTrackingService = AppUsageTrackingService.this;
            localAppUsageTrackingService.applicationUsagePeriod += (int)(AppUsageTrackingService.this.periodForUsageTracking / 1000L);
            Log.d("AppUsageTracking", "Currently Running App: " + topAppName + 
            		" and Last ApplicationName: " + lastApplicationName + " packageName: " + topPackageName + " and Usage Period: " + AppUsageTrackingService.this.applicationUsagePeriod);
          }
          if (lastApplicationName.equals(""))
            ;//break label562;
        }
        
        
        ContentValues localContentValues = null;
        //label373: 
       	int i = 0;
        Iterator localIterator = null;
       
       //  if (!lastPackageName.equals("")) {
        // 2. APP이 빠뀐 경우에 기록 함.
        Log.d(TAG, "lastPackageName=" + lastPackageName +" topPackageName=" + topPackageName);
        Log.d(TAG, "lastApplicationName=" + lastApplicationName +" topAppName=" + topAppName);
        if (!lastPackageName.equals(topPackageName)) {         	    
          
        	localContentValues = new ContentValues();
          
        	if (lastApplicationName.lastIndexOf(":") == -1 + lastApplicationName.length())  // 맨뒤에 ":"가 붙은 경우?
        		lastApplicationName = lastApplicationName.replace(":", "");                 // 제거함.
          
        	if (lastApplicationName.indexOf(".") != -1)
        		;// .이 없는 경우 
                //break label614;
        	
        	 // 2.2 INSERT할 데이터 준비            
        	localContentValues.put("app", lastApplicationName);
          
        	//if (AppUsageTrackingService.listLauncherApp == null)
        		//AppUsageTrackingService.listLauncherApp = MiscUtils.getHomeActivity(AppUsageTrackingService.this.getApplicationContext());
          
        	//	localIterator = AppUsageTrackingService.listLauncherApp.iterator();
        	//	if (localIterator.hasNext())
            //    if (i == 0) {
            localContentValues.put("app_pkg",   lastPackageName);
            localContentValues.put("date_time", Long.valueOf(startDateTime));
            localContentValues.put("duration",  Integer.valueOf(applicationUsagePeriod + (int)(periodForUsageTracking / 2000L)));
            //    }
           
            //  	
            // 2.3 새로운 app으로 현재 app 변수 갱신
            //      오류가 나더라도 현재  app을  갱신해야함. 그렇지 않으면 지속하여 기록을 시도하는  문제가 있음. 	
            lastApplicationName = topAppName;
            lastPackageName = topPackageName;
            applicationUsagePeriod = (int)(periodForUsageTracking / 2000L);
            startDateTime = (System.currentTimeMillis() / 1000L);
        
           	try	{
           		
           		Log.d("AppUsageTracking", "Before Insert: " + localContentValues.toString());  
           		
           		// 2.2 INSERT      
           		Uri record = getContentResolver().insert(AppUsage.AppUsageColumns.CONTENT_URI, localContentValues);	
           	
           		Log.d("AppUsageTracking", "Stored the App Usage info into DB:" + lastApplicationName + " " + startDateTime + " " + applicationUsagePeriod 
           				+ " Received URI:" +  record);        
          
          //localContentValues.put("app", MiscUtils.convertPkgNameToAppName(lastApplicationName));
          //break label373;
          //String str3 = (String)localIterator.next();
         // if (!AppUsageTrackingService.this.lastPackageName.equals(str3)){
            //break label405;
          //i = 1;
         // }
           	}catch (SQLException sqlException){
           		Log.w("AppUsageTracking", "Sql Exception: " + sqlException.getMessage());
           	}catch (Exception e){
           		Log.w("AppUsageTracking", "Sql Exception: " + e.getMessage());
           	}
           	
         
        }
    } // run
    
  } // inner class 

  }//  class 
  


