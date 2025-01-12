package com.example.msns_taskshowtestproject_ver01.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.msns_taskshowtestproject_ver01.Log_write_Service;
import com.example.msns_taskshowtestproject_ver01.StaticValue.StaticValue;

public class ScreenReciver extends BroadcastReceiver {

	public static boolean wasScreenOn = true;
	private Intent alarmIntent;
	private PendingIntent pendingIntent;
	private Context mContext;
	private AlarmManager alarmManager;
	@Override
	public void onReceive(Context context, Intent intent) {
		
		mContext  = context;
		alarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);

		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			// do whatever you need to do here
			/*알람을 종료한다.*/
			alarmIntent = new Intent(mContext, Log_write_Service.class);
			pendingIntent = PendingIntent.getService(mContext, 8109329, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager.cancel(pendingIntent);
			Toast.makeText(context, "Off", 0).show();
			wasScreenOn = false;
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			// and do whatever you need to do here
			Toast.makeText(context, "On", 0).show();
			/*알람을 등록한다*/
			alarmIntent = new Intent(mContext, Log_write_Service.class);
			pendingIntent = PendingIntent.getService(mContext, 8109329, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, StaticValue.NUMBEROFALARMTERM, pendingIntent);
			wasScreenOn = true;
		}
		/*부팅이 완료되었을때.*/
	}

}
