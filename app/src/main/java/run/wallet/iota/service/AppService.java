package run.wallet.iota.service;

import android.app.Activity;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jota.RunIotaAPI;
import jota.dto.response.ReplayBundleResponse;
import run.wallet.R;
import run.wallet.common.Cal;
import run.wallet.common.delete.SecureDeleteFile;
import run.wallet.iota.api.RefreshEventTask;
import run.wallet.iota.api.TaskManager;
import run.wallet.iota.api.handler.NudgeRequestHandler;
import run.wallet.iota.api.requests.AddressSecurityChangeRequest;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.AuditAddressesRequest;
import run.wallet.iota.api.requests.AutoNudgeRequest;
import run.wallet.iota.api.requests.GetAccountDataRequest;
import run.wallet.iota.api.requests.GetFirstLoadRequest;
import run.wallet.iota.api.requests.GetNewAddressRequest;
import run.wallet.iota.api.requests.MessageFirstLoadRequest;
import run.wallet.iota.api.requests.MessageNewAddressRequest;
import run.wallet.iota.api.requests.NodeInfoRequest;
import run.wallet.iota.api.requests.MessageSendRequest;
import run.wallet.iota.api.requests.NudgeRequest;
import run.wallet.iota.api.requests.RefreshUsedAddressesRequest;
import run.wallet.iota.api.requests.ReplayBundleRequest;
import run.wallet.iota.api.requests.SendTransferRequest;
import run.wallet.iota.api.requests.WebGetExchangeRatesHistoryRequest;
import run.wallet.iota.api.requests.WebGetExchangeRatesRequest;


import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.NodeInfoResponse;
import run.wallet.iota.api.responses.NudgeResponse;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.Sf;
import run.wallet.iota.helper.TorHelper;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.MsgStore;
import run.wallet.iota.model.Nodes;
import run.wallet.iota.model.NudgeTransfer;
import run.wallet.iota.model.PayPacket;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transaction;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.security.Validator;
import run.wallet.iota.ui.activity.MainActivity;


public final class AppService extends Service {
	
	private static AppService SERVICE;//=new AppService();
    private Activity activity;
    private Activity activitySettings;
    private Handler syncDataHandler = new Handler();
    private Handler startSyncsHandler = new Handler();

    private SyncDataThread syncDataThread;

    private final IBinder mBinder = new LocalBinder();
    private static OnAlarmReceiver areceiver;
    //private static OnAlarmReceiver aoreceiver;
    private static OnBootReceiver receiver;
    private static OnBootReceiver breceiver;
    private static OnUserPresentReceiver ubreceiver;


    public static boolean shouldReloadContacts=false;

    private static final long MILLIS_SYNC_DATA = 40000; // every 40 secs
    private static final long MILLIS_SYNC_SLOW = Cal.HOURS_1_IN_MILLIS; // every hour


    private boolean isAppStarted=false;


    private Handler appStopTimeout=new Handler();
    private Runnable appStopCheck = new Runnable() {
        @Override
        public void run() {
            Store.logout();
            TorHelper.destroy();
            if(SERVICE.activity!=null) {
                SERVICE.activity.finish();
            }
            if(SERVICE.activitySettings!=null) {
                SERVICE.activitySettings.finish();
            }
        }
    };
    public static void setIsAppStarted(Activity activity, boolean started) {
        if(SERVICE!=null) {
            //Log.e("APP-START","val is: "+started);
            if(activity!=null)
                SERVICE.activity=activity;
            SERVICE.isAppStarted=started;
            if (SERVICE.isAppStarted) {
                SERVICE.appStopTimeout.removeCallbacks(SERVICE.appStopCheck);
            } else {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SERVICE);
                int logoutMinutes=Sf.toInt(prefs.getString(Constants.PREF_AUTO_LOGOUT,Constants.PREF_AUTO_LOGUT_DEFAULT+""));
                if(logoutMinutes==0)
                    SERVICE.appStopTimeout.postDelayed(SERVICE.appStopCheck, 100);
                else
                    SERVICE.appStopTimeout.postDelayed(SERVICE.appStopCheck, Cal.MINUTES_1_IN_MILLIS*logoutMinutes);
            }
        }
    }
    public static void setIsSettingsAppStarted(Activity activity, boolean started) {
        if(SERVICE!=null) {
            //Log.e("APP-START","val is: "+started);
            if(activity!=null)
                SERVICE.activitySettings=activity;
            SERVICE.isAppStarted=started;
            if (SERVICE.isAppStarted) {
                SERVICE.appStopTimeout.removeCallbacks(SERVICE.appStopCheck);
            } else {
                SERVICE.appStopTimeout.postDelayed(SERVICE.appStopCheck, 10000);
            }
        }
    }
    public static boolean isAppStarted() {

        return SERVICE.isAppStarted;
    }



    private class NotifyObject {
        public String text;
        public int Ricon;
        public String head;
    }

    public class LocalBinder extends Binder {
        public AppService getService() {
            return AppService.this;
        }
    }

    @Override
    public void onCreate() {
    	super.onCreate();
    }


    public static void ensureStartups(Context context) {

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
    	if(SERVICE==null) {
    		SERVICE=this;

        ensureStartups(getBaseContext());

        TorHelper.init(this,null);

    	Intent service = new Intent(this, OnAlarmReceiver.class);
    	PendingIntent pintent = PendingIntent.getService(this, 0, service, 0);

    	AlarmManager alarm = (AlarmManager) this.getSystemService(Service.ALARM_SERVICE);
    	// Start every 30 seconds
    	alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 30*1000, pintent);

		IntentFilter afilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		areceiver = new OnAlarmReceiver();
		
		//IntentFilter aofilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		//aoreceiver = new OnAlarmReceiver();

		IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
		receiver = new OnBootReceiver();
		
		IntentFilter bfilter = new IntentFilter("android.intent.action.QUICKBOOT_POWERON");
		breceiver = new OnBootReceiver();

		IntentFilter ubfilter = new IntentFilter(Intent.ACTION_USER_PRESENT);
		ubreceiver=new OnUserPresentReceiver();
		

		registerReceiver(areceiver, afilter);
		//registerReceiver(aoreceiver, aofilter);
		registerReceiver(receiver, filter);
		registerReceiver(breceiver, bfilter);
		registerReceiver(ubreceiver, ubfilter);

		// removed for now, will be coming back with future Friend transfers feature
		//ContactsObserver contentObserver = new ContactsObserver();
		//this.getContentResolver().registerContentObserver (ContactsContract.Contacts.CONTENT_URI, true, contentObserver);

        startSyncsHandler.postDelayed(new Runnable() {
            public void run() {

                AppService.startRegularRefresh();
                syncDataHandler.postDelayed(syncDataThread, 10000);
            }
        },2000);

    	}
        //ensureForegroundService();
    	return START_STICKY;
    }
    private static final int FOREGROUND_ID=1767849;


    private void ensureForegroundService() {
        Intent intent = new Intent(SERVICE, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setTicker("TICKER").setContentTitle("Play Lock").setContentText("")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis()).setAutoCancel(false)
                .setOngoing(true).setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendIntent);
        Notification notification = builder.build();

        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(FOREGROUND_ID, notification);
    }
    private class ContactsObserver extends ContentObserver {

        public ContactsObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            shouldReloadContacts=true;
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
        	super.onChange(selfChange,uri);
            shouldReloadContacts=true;
        } 
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(areceiver);
            //unregisterReceiver(aoreceiver);
            unregisterReceiver(receiver);
            unregisterReceiver(breceiver);
            unregisterReceiver(ubreceiver);

            Intent pservice = new Intent(this, OnAlarmReceiver.class);
            stopService(pservice);
        } catch(Exception e){}
		syncDataHandler=null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

	
    public static boolean isAppServiceRunning(Context context) {
        if(context!=null) {
            android.app.ActivityManager manager = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (AppService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void checkStartAppService(Context context) {
        if(context!=null && !isAppServiceRunning(context)) {
            Intent service = new Intent(context, AppService.class);
            startService(service);
        }
    }



    private class OnBootReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {

            synchronized(this) {
                checkStartAppService(context);
                AppService.startRegularRefresh();
            }
    	}
    }
    

    private class OnAlarmReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {

    	    synchronized(this) {
                checkStartAppService(context);
                AppService.startRegularRefresh();
    		}
    	}
    }
    private class OnUserPresentReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {

            synchronized(this) {
                checkStartAppService(context);
                AppService.startRegularRefresh();
            }
    	}
    }

    public static void startRegularRefresh() {
        if(SERVICE!=null) {
            if (SERVICE.syncDataHandler != null)
                SERVICE.syncDataHandler.removeCallbacks(SERVICE.syncDataThread);
            if (SERVICE.syncDataThread == null) {
                SERVICE.syncDataThread = SERVICE.new SyncDataThread();
            }
            if (!SERVICE.isrefreshing) {
                SERVICE.syncDataHandler.postDelayed(SERVICE.syncDataThread, 30000);
            }
        }
    }
    
    private boolean isrefreshing=false;
    
    private class SyncDataThread  implements Runnable {
    	@Override
    	public void run() {
            synchronized (this) {
                if (!isrefreshing) {
                    isrefreshing = true;
                    new SyncDataTask().execute(false);
                }
            }
    	}

    }
	private class SyncDataTask extends AsyncTask<Boolean, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Boolean... params) {
            ensureStartups(getBaseContext());
            AutoNudgerGo();
            checkUsedAddresses();
			return true;
		}      
	
		@Override
		protected void onPostExecute(Boolean result) {
			isrefreshing=false;
			try {
                SERVICE.syncDataHandler.removeCallbacks(SERVICE.syncDataThread);
                Store.init(SERVICE,false);
                if(!Store.getNudgeTransfers().isEmpty()) {
                    SERVICE.syncDataHandler.postDelayed(SERVICE.syncDataThread, MILLIS_SYNC_DATA);
                } else {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SERVICE);
                    int nudgeAttempts= Sf.toInt(prefs.getString(Constants.PREF_TRANSFER_NUDGE_ATTEMPTS, ""+Constants.PREF_TRANSFER_NUDGE_ATTEMPTS_VALUE));
                    boolean keepfast=false;
                    for(NudgeTransfer nt: Store.getNudgeTransfers()) {
                        if(nt.transfer.getNudgeCount()<nudgeAttempts) {
                            keepfast=true;
                        }
                    }
                    if(keepfast) {
                        SERVICE.syncDataHandler.postDelayed(SERVICE.syncDataThread, MILLIS_SYNC_DATA);
                    } else {
                        SERVICE.syncDataHandler.postDelayed(SERVICE.syncDataThread, MILLIS_SYNC_SLOW);
                    }

                }
            } catch(Exception e) {}
		}
	}

	public static void setFastMode() {
        SERVICE.syncDataHandler.removeCallbacks(SERVICE.syncDataThread);
        SERVICE.syncDataHandler.postDelayed(SERVICE.syncDataThread, MILLIS_SYNC_DATA);
    }
    private static void checkUsedAddresses() {
        Store.init(SERVICE,false);
        if(!Store.getSeedList().isEmpty() && Store.getLastUsedCheck()<System.currentTimeMillis()-Cal.HOURS_24_IN_MILLIS) {
            TaskManager rt = new TaskManager(SERVICE);
            RefreshUsedAddressesRequest tir = new RefreshUsedAddressesRequest(null);
            runTask(rt,tir);
        }
    }

    /* IOTA */
    private Map<Long,ApiRequest> tasks = new ConcurrentHashMap<Long,ApiRequest>();

    public static void dropAllTasks() {
        if(SERVICE!=null) {
            SERVICE.tasks.clear();
        }
    }
    public static boolean isAutoNudgerRunning() {
        try {
            Collection<ApiRequest> reqs=SERVICE.tasks.values();
            for(ApiRequest request:reqs) {
                if(request.getClass().getCanonicalName().equals(AutoNudgeRequest.class.getCanonicalName())) {
                    return true;
                }
            }
        } catch(Exception e) {
            Log.e("service","error001: "+e.getMessage());
        }
        return false;
    }
    public static boolean isGetAccountDataRunning(Seeds.Seed seed) {
        try {
            Collection<ApiRequest> reqs=SERVICE.tasks.values();
            for(ApiRequest request:reqs) {

                if(request.getClass().getCanonicalName().equals(GetAccountDataRequest.class.getCanonicalName())) {
                    GetAccountDataRequest req = (GetAccountDataRequest) request;
                    if(req.getSeed().getShortValue().equals(seed.getShortValue()))
                        return true;
                }
            }
        } catch(Exception e) {
            Log.e("service","error001: "+e.getMessage());
        }
        return false;
    }
    public static int countAuditRunningTasks(Seeds.Seed seed) {
        int count=0;
        try {
            Collection<ApiRequest> reqs=SERVICE.tasks.values();
            for(ApiRequest request:reqs) {
                if(request.getClass().getCanonicalName().equals(AuditAddressesRequest.class.getCanonicalName())) {
                    AuditAddressesRequest req = (AuditAddressesRequest) request;
                    if(req.getSeed().getShortValue().equals(seed.getShortValue())) {

                        count++;
                    }
                }
            }
        } catch(Exception e) {
            Log.e("service","error002: "+e.getMessage());
        }
        return count;
    }
    public static int countSeedRunningTasks(Seeds.Seed seed) {
        int count=0;
        try {
            Collection<ApiRequest> reqs=SERVICE.tasks.values();
            for(ApiRequest request:reqs) {
                if(request.getClass().getCanonicalName().equals(AuditAddressesRequest.class.getCanonicalName())) {
                    AuditAddressesRequest req = (AuditAddressesRequest) request;
                    if(req.getSeed().getShortValue().equals(seed.getShortValue())) {
                        count++;
                    }
                } else if(request.getClass().getCanonicalName().equals(SendTransferRequest.class.getCanonicalName())) {
                    SendTransferRequest req = (SendTransferRequest) request;
                    if(req.getSeed().getShortValue().equals(seed.getShortValue())) {
                        count++;
                    }
                } else if(request.getClass().getCanonicalName().equals(GetNewAddressRequest.class.getCanonicalName())) {
                    GetNewAddressRequest req = (GetNewAddressRequest) request;
                    if(req.getSeed().getShortValue().equals(seed.getShortValue())) {
                        count++;
                    }
                }
            }
        } catch(Exception e) {
            Log.e("service","error003: "+e.getMessage());
        }
        return count;
    }
    public static int countTransferRunningTasks(Seeds.Seed seed) {
        int count=0;
        try {
            Collection<ApiRequest> reqs=SERVICE.tasks.values();
            for(ApiRequest request:reqs) {
                if(request.getClass().getCanonicalName().equals(SendTransferRequest.class.getCanonicalName())) {
                    SendTransferRequest req = (SendTransferRequest) request;
                    if(req.getSeed().getShortValue().equals(seed.getShortValue())) {
                        count++;
                    }
                }
            }
        } catch(Exception e) {
            Log.e("service","error004: "+e.getMessage());
        }
        return count;
    }
    public static int countAddressRunningTasks(Seeds.Seed seed) {
        int count=0;
        try {
            Collection<ApiRequest> reqs=SERVICE.tasks.values();
            for(ApiRequest request:reqs) {
                if(request.getClass().getCanonicalName().equals(SendTransferRequest.class.getCanonicalName())) {
                    SendTransferRequest req = (SendTransferRequest) request;
                    if(req.getSeed().getShortValue().equals(seed.getShortValue())) {
                        count++;
                    }
                } else if(request.getClass().getCanonicalName().equals(GetNewAddressRequest.class.getCanonicalName())) {
                    GetNewAddressRequest req = (GetNewAddressRequest) request;
                    if(req.getSeed().getShortValue().equals(seed.getShortValue())) {
                        count++;
                    }
                }
            }
        } catch(Exception e) {
            Log.e("service","error005: "+e.getMessage());
        }
        return count;
    }
    public static List<ApiRequest> getRunningTasks() {
        List<ApiRequest> rtasks=new ArrayList<>();
        if(SERVICE!=null && !SERVICE.tasks.isEmpty()) {
            for(ApiRequest request: SERVICE.tasks.values()) {
                rtasks.add(request);
            }
        }
        return rtasks;
    }

    private static void runTask(TaskManager taskManager, ApiRequest request) {
        if(request!=null && taskManager !=null) {
            taskManager.startNewRequestTask(request);
            if(SERVICE!=null) {
                SERVICE.tasks.put(taskManager.getTaskId(), request);
            }
        }
    }
    private static void runBasicTask(TaskManager taskManager, ApiRequest request) {
        if(request!=null && taskManager !=null) {
            taskManager.startNewBasicRequestTask(request);
            if(SERVICE!=null) {
                SERVICE.tasks.put(taskManager.getTaskId(), request);
            }
        }
    }
    private static void runMessageTask(TaskManager taskManager, ApiRequest request) {
        if(request!=null && taskManager !=null) {
            taskManager.startNewMessageTask(request);
            if(SERVICE!=null) {
                SERVICE.tasks.put(taskManager.getTaskId(), request);
            }
        }
    }
    public static void markTaskFinished(long taskId) {
        if(SERVICE!=null) {
            SERVICE.tasks.remove(taskId);
        }
    }

    public static boolean isFirstTimeLoadRunning(Context context) {
        Collection<ApiRequest> intasks=SERVICE.tasks.values();
        for(ApiRequest tm: intasks) {
            if(tm.getClass().getCanonicalName().equals(GetFirstLoadRequest.class.getCanonicalName())) {
                return true;
            }
        }
        return false;
    }

    public static void runMessageFirstLoad(Context context) {
        if(Validator.isValidCaller() && Store.getCurrentSeed()!=null && MsgStore.getSeed()==null) {
            TaskManager rt = new TaskManager(SERVICE);
            MsgStore.createSeed(SERVICE);
            MessageFirstLoadRequest gtr = new MessageFirstLoadRequest();
            runMessageTask(rt,gtr);
        }
    }
    public static void generateMessageNewAddress(Context context) {
        if(Validator.isValidCaller() && MsgStore.getSeed()!=null) {
            TaskManager rt = new TaskManager(SERVICE);
            MessageNewAddressRequest gtr = new MessageNewAddressRequest(MsgStore.getSeed());
            runTask(rt,gtr);
        }
    }
    public static void generateNewAddress(Context context,Seeds.Seed seed) {
        if(Validator.isValidCaller() && seed!=null) {
            TaskManager rt = new TaskManager(SERVICE);
            GetNewAddressRequest gtr = new GetNewAddressRequest(seed);
            runTask(rt,gtr);
        }
    }
    public static void auditAddresses(Context context, Seeds.Seed seed) {
        if(Validator.isValidCaller() && seed!=null) {
            if(AppService.countSeedRunningTasks(seed)==0) {
                TaskManager rt = new TaskManager(SERVICE);
                AuditAddressesRequest gtr = new AuditAddressesRequest(seed);
                runTask(rt, gtr);
            }
        }
    }
    public static void auditAddressesWithDelay(Context context,Seeds.Seed seed) {
        if(Validator.isValidCaller() && seed!=null) {
            if(AppService.countSeedRunningTasks(seed)==0) {
                TaskManager rt = new TaskManager(SERVICE);
                AuditAddressesRequest gtr = new AuditAddressesRequest(seed);
                runTask(rt, gtr);
            }
        }
    }
    public static void getFirstTimeLoad(Context context) {
        if(Validator.isValidCaller() && Store.getCurrentSeed()!=null) {
            TaskManager rt = new TaskManager(SERVICE);
            GetFirstLoadRequest gna = new GetFirstLoadRequest(Store.getCurrentSeed());
            runTask(rt,gna);
        }
    }
    private long lastAccountCall=0;
    public static void getAccountData(Context context, Seeds.Seed seed) {
        getAccountData(SERVICE, seed,false);
    }
    public static void getAccountData(Context context, Seeds.Seed seed,boolean force) {
        if(Validator.isValidCaller() && Store.getCurrentSeed()!=null) {
            if(SERVICE !=null && countSeedRunningTasks(seed)==0 && !isGetAccountDataRunning(seed)) {
                if (force || SERVICE.lastAccountCall < System.currentTimeMillis() - 10000) {
                    SERVICE.lastAccountCall=System.currentTimeMillis();
                    TaskManager rt = new TaskManager(SERVICE);
                    GetAccountDataRequest gna = new GetAccountDataRequest(seed);
                    gna.setForce(force);
                    runTask(rt, gna);
                }
            }
        }
    }
    public static boolean canRunGetAccountData(Seeds.Seed seed) {
        if(Validator.isValidCaller() && Store.getCurrentSeed()!=null) {
            if(SERVICE !=null && countSeedRunningTasks(seed)==0 && !isGetAccountDataRunning(seed)) {
                if (SERVICE.lastAccountCall < System.currentTimeMillis() - 10000) {
                    return true;
                }
            }
        }
        return false;
    }
    public static void getAccountDataSingleAddress(Context context, Seeds.Seed seed, String address) {
        if(Validator.isValidCaller() && Store.getCurrentSeed()!=null) {
            if(SERVICE !=null && !isGetAccountDataRunning(Store.getCurrentSeed())) {
                TaskManager rt = new TaskManager(SERVICE);
                GetAccountDataRequest gna = new GetAccountDataRequest(seed);
                gna.setSingleAddressRefresh(address);
                runTask(rt, gna);
            }
        }
    }
    Handler refresher = new Handler();
    RefreshEventRunnable refresherRunnable;
    private static class RefreshEventRunnable implements Runnable {
        @Override
        public void run() {
            RefreshEventTask task=new RefreshEventTask();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
        }
    }
    public static void refreshEvent() {
        if(SERVICE!=null) {
            if(SERVICE.refresherRunnable!=null)
                SERVICE.refresher.removeCallbacks(SERVICE.refresherRunnable);
            SERVICE.refresherRunnable=new RefreshEventRunnable();
            SERVICE.refresher.postDelayed(SERVICE.refresherRunnable,100);
        }


    }
    public static void nudgeTransaction(Context context, Seeds.Seed seed, Transfer transfer) {
        if(Validator.isValidCaller() && Store.getCurrentSeed()!=null) {
            NudgeRequest rtr = new NudgeRequest(seed,transfer);
            TaskManager rt = new TaskManager(SERVICE);
            runTask(rt,rtr);
        }
    }
    public static void replayBundleTransaction(Context context, Seeds.Seed seed, String hash, String refreshCallAddress) {
        if(Validator.isValidCaller() && Store.getCurrentSeed()!=null) {
            ReplayBundleRequest rtr = new ReplayBundleRequest(seed,hash,refreshCallAddress);
            TaskManager rt = new TaskManager(SERVICE);
            runTask(rt,rtr);
        }
    }
    public static void addressSecurityChange(Context context, Seeds.Seed seed, Address address, int security) {
        if(Validator.isValidCaller() && Store.getCurrentSeed()!=null) {
            if(security<1)
                security=1;
            if(security>3)
                security=3;
            AddressSecurityChangeRequest rtr = new AddressSecurityChangeRequest(seed,address,security);
            TaskManager rt = new TaskManager(SERVICE);
            runTask(rt,rtr);
        }
    }
    public static void attachNewAddress(Context context, Seeds.Seed seed, String address) {
        if(Validator.isValidCaller() && Store.getCurrentSeed()!=null) {
            TaskManager rt = new TaskManager(SERVICE);
            SendTransferRequest tir = new SendTransferRequest(seed,address, "0", "", Constants.NEW_ADDRESS_TAG);
            runTask(rt,tir);
        }
    }
    public static void checkUsedAddress(Seeds.Seed seed) {
        if(Validator.isValidCaller() && Store.getCurrentSeed()!=null) {
            TaskManager rt = new TaskManager(SERVICE);
            RefreshUsedAddressesRequest tir = new RefreshUsedAddressesRequest(seed);
            runTask(rt,tir);
        }
    }
    public static void sendMessageToAddress(Context context, Seeds.Seed seed, String toAddress, String amountIOTA, String message, String tag) {
        if(Validator.isValidCaller() && Store.getCurrentSeed()!=null) {
            TaskManager rt = new TaskManager(SERVICE);
            MessageSendRequest tir = new MessageSendRequest(seed,toAddress, message, tag);
            runMessageTask(rt,tir);
        }
    }
    public static void sendNewTransfer(Context context, Seeds.Seed seed, List<PayPacket.PayTo> payTos, List<Address> fromAddress, Address remainder, String message, String tag) {
        if(Validator.isValidCaller() && Store.getCurrentSeed()!=null) {
            TaskManager rt = new TaskManager(SERVICE);
            SendTransferRequest tir = new SendTransferRequest(seed, payTos,fromAddress,remainder, message, tag);
            runTask(rt,tir);
        }
    }
    public static void sendNewTransfer(Context context, Seeds.Seed seed, String toAddress, String amountIOTA, List<Address> fromAddress, Address remainder, String message, String tag) {
        if(Validator.isValidCaller() && Store.getCurrentSeed()!=null) {
            TaskManager rt = new TaskManager(SERVICE);
            SendTransferRequest tir = new SendTransferRequest(seed, toAddress, amountIOTA,fromAddress,remainder, message, tag);
            runTask(rt,tir);
        }
    }
    @Deprecated
    public static void sendNewTransfer(Context context, Seeds.Seed seed, String toAddress, String amountIOTA, String message, String tag) {
        if(Validator.isValidCaller() && Store.getCurrentSeed()!=null) {
            TaskManager rt = new TaskManager(SERVICE);
            SendTransferRequest tir = new SendTransferRequest(seed, toAddress, amountIOTA, message, tag);
            runTask(rt,tir);
        }
    }

    private TaskManager taskManagerBinded;
    public static void bindTaskManager(TaskManager tm) {
        if(SERVICE!=null && tm!=null) {
            SERVICE.taskManagerBinded=tm;
        }
    }
    private boolean isTaskManagerBinded() {
        if(SERVICE.taskManagerBinded!=null) {
            return true;
        }
        return false;
    }
    private long lastNodeInfo=0;
    public static void getNodeInfo(Context context) {
        if(SERVICE !=null) {
            if (SERVICE.lastNodeInfo < System.currentTimeMillis() - 3000) {
                SERVICE.lastNodeInfo = System.currentTimeMillis();
                TaskManager rt = new TaskManager(SERVICE);
                NodeInfoRequest nir = new NodeInfoRequest();
                runTask(rt, nir);
            }
        }
    }
    public static void getNodeInfo(Context context, boolean force) {
        if(SERVICE !=null) {
            if (force || (SERVICE.lastNodeInfo < System.currentTimeMillis() - 3000)) {
                SERVICE.lastNodeInfo = System.currentTimeMillis();
                TaskManager rt = new TaskManager(SERVICE);
                NodeInfoRequest nir = new NodeInfoRequest();
                runTask(rt, nir);
            }
        }
    }
    private static long lastExchangeRateCall;
    private static final int timeoutExchangeRates=120000;
    public static void updateExchangeRates(Context context) {
        long now=System.currentTimeMillis();
        if(lastExchangeRateCall==0 || now>lastExchangeRateCall+timeoutExchangeRates) {
            lastExchangeRateCall=now;
            TaskManager rt = new TaskManager(SERVICE);
            WebGetExchangeRatesRequest nir = new WebGetExchangeRatesRequest();
            runBasicTask(rt, nir);
        }

    }
    public static void updateExchangeRatesHistory(Context context, String currencyPair, int step) {
        TaskManager rt = new TaskManager(SERVICE);
        WebGetExchangeRatesHistoryRequest nir = new WebGetExchangeRatesHistoryRequest(currencyPair,step);
        runBasicTask(rt, nir);
    }

    private static final long nudgeEvery=60000;
    private static long lastNudgeRun=System.currentTimeMillis()-(40000);

    private static void AutoNudgerGo() {
        long now=System.currentTimeMillis();
        if(SERVICE!=null && lastNudgeRun<now-nudgeEvery) {
            Store.loadNudgeTransfers(SERVICE);
            if(!Store.getNudgeTransfers().isEmpty() && !isAutoNudgerRunning()) {
                lastNudgeRun=now;
                TaskManager rt = new TaskManager(SERVICE);
                AutoNudgeRequest nir = new AutoNudgeRequest();
                runTask(rt, nir);
            }

        }
    }




}
