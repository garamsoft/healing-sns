package kr.ac.seoultech.healing;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import kr.ac.seoultech.healing.manager.StoreAppManager;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;


public class Log_write_Service extends Service{

	
	List<RunningTaskInfo> info;

	ActivityManager activityManager;
	
	Context mContext;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		mContext = getApplicationContext();
		activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	@Override
	public void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		super.onRebind(intent);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		/**
		 * 구현 할 기능
		 * 1. 우선 돌고 있는 것을 알아온다.
		 * 2. 알아온 것 중에 최상위를 가져온다.
		 * 3. 가져온 것에 돌고 있는 지 확인한다.
		 * 4. 확인하고 1개 이상이라면 저장한다.
		 * 5. 종료한다.*/
		info = activityManager.getRunningTasks(1);
		
		String log = getRunningTaskInfo(info);
		
		if(!log.equals("") )
		{
			StoreAppManager storeAppManager = StoreAppManager.getInstance(getApplicationContext());
			storeAppManager.writeLog(log);	
		}
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}
	
	private String getRunningTaskInfo(List<RunningTaskInfo> recentTaskInfos) {
		
		String string ="";
		Iterator<RunningTaskInfo> iterator = recentTaskInfos.iterator();
		
		 while(iterator.hasNext()){
	        	
			 RunningTaskInfo runningTaskInfo = iterator.next();
	        	
//	        	textView.append("baseIntent :"+RecentTaskInfo.baseIntent+"\n");
//	        	textView.append("description :"+RecentTaskInfo.describeContents()+"\n");
//	        	textView.append("id "+RecentTaskInfo.id+"\n");
//	        	textView.append("origActivity :"+RecentTaskInfo.origActivity+"\n\n");
	        	//textView.append("persistentId :"+RecentTaskInfo.persistentId+"\n");
	        	//LEVEL 11
	        	//bitmap = runningTaskInfo.thumbnail;
	        
	        	GregorianCalendar gregorianCalendar = new GregorianCalendar();
	        	
	        	if(runningTaskInfo.numRunning>0)
	        	string = gregorianCalendar.getTime().toString() + runningTaskInfo.baseActivity.getPackageName();
	        	
			}
		 
		 return string;
	}
	

}
