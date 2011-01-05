package cri.sanity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class MainService extends Service
{
	private static boolean running = false;

	private PhoneListener phoneListener;

	//---- static methods

	public static boolean isRunning() { return running; }

	public static void start() { A.app().startService(new Intent(A.app(), MainService.class)); }
	public static void stop () { A.app(). stopService(new Intent(A.app(), MainService.class)); }

	//---- methods

	@Override
	public IBinder onBind(Intent intent) { return null; }

	//@Override
	//public void onCreate() { super.onCreate(); A.logd("MainService created"); }

	@Override
	public int onStartCommand(Intent intent, int flags, int id)
	{
		running = true;
		super.onStartCommand(intent, flags, id);
		if(A.activity == null) MainActivity.notifyRun();
		A.logd("MainService started");
		if(phoneListener == null) phoneListener = new PhoneListener();
		phoneListener.startup();
		A.telMan().listen(phoneListener, PhoneListener.LISTEN);
		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		A.telMan().listen(phoneListener, PhoneListener.LISTEN_NONE);
		if(A.activity == null) A.notifyCanc();
		else MainActivity.notifyRun();
		running = false;
		A.logd("MainService destroyed");
		super.onDestroy();
	}

}
