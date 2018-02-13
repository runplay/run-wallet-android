/*
 * Copyright (C) 2017 IOTA Foundation
 *
 * Authors: pinpong, adrianziser, saschan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package run.wallet.iota.api;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import run.wallet.iota.IOTA;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.service.AppService;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TaskManager {

    private static final TaskManager TM=new TaskManager();
    private final HashMap<String, AsyncTask> runningTasks = new HashMap<>();
    private final HashMap<String, AsyncTask> runningMessageTasks = new HashMap<>();
    private final Context context;
    private long taskId;
    private IotaRequestTask iotaRequestTask;
    private BasicRequestTask basicRequestTask;
    private MessageRequestTask messageRequestTask;
    private ApiRequest apiRequest;
    private String tag;

    public static final int STATUS_QUEUED=0;
    public static final int STATUS_RUNNING=1;
    public static final int STATUS_COMPLETE=2;

    protected int status;

    public void setStatus(int STATUS_) {
        this.status=status;
    }
    public int getStatus() {
        return status;
    }

    private TaskManager(){
        this.context=null;
        AppService.bindTaskManager(TM);
    }
    public TaskManager(Context context) {
        this.context = context;
        this.taskId=System.currentTimeMillis();

    }

    public long getTaskId() {
        return taskId;
    }
    public String getTag() {
        return apiRequest.getClass().getCanonicalName();
    }
    public static final AsyncTask getRunningTask(String tag) {
        return TM.runningTasks.get(tag);
    }
    public static Set<String> getRunningTasksTags() {
        return TM.runningTasks.keySet();
    }

    private static synchronized void addTask(IotaRequestTask iotaRequestTask, ApiRequest ir) {
        String tag = ir.getClass().getCanonicalName();

        if (!TM.runningTasks.containsKey(tag)) {
            TM.runningTasks.put(tag, iotaRequestTask);
            //if (IOTA.DEBUG)
            //    Log.e("Added Task ", tag);
            iotaRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
        }
    }

    public static synchronized void removeTask(String tag) {
        if (TM.runningTasks.containsKey(tag)) {
            //if (IOTA.DEBUG)
            //    Log.i("Removed Task ", tag);
            TM.runningTasks.remove(tag);
        }
    }

    public static void stopAndDestroyAllTasks(Context context) {
        Iterator<Map.Entry<String, AsyncTask>> it = TM.runningTasks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, AsyncTask> entry = it.next();
            try {
                entry.getValue().cancel(true);
            } catch (IllegalStateException e) {
                e.getStackTrace();
            }
            it.remove();
        }
        Iterator<Map.Entry<String, AsyncTask>> mit = TM.runningMessageTasks.entrySet().iterator();
        while (mit.hasNext()) {
            Map.Entry<String, AsyncTask> entry = mit.next();
            try {
                entry.getValue().cancel(true);
            } catch (IllegalStateException e) {
                e.getStackTrace();
            }
            mit.remove();
        }
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        AppService.dropAllTasks();
    }

    public void startNewRequestTask(ApiRequest ir) {
        iotaRequestTask = new IotaRequestTask(context);
        iotaRequestTask.setTaskId(taskId);
        apiRequest=ir;
        TaskManager.addTask(iotaRequestTask, apiRequest);
    }
    public void startNewBasicRequestTask(ApiRequest ir) {
        basicRequestTask = new BasicRequestTask(context);
        basicRequestTask.setTaskId(taskId);
        apiRequest=ir;
        TaskManager.addBasicTask(basicRequestTask, apiRequest);
    }
    private static synchronized void addBasicTask(BasicRequestTask basicRequestTask, ApiRequest ir) {
        String tag = ir.getClass().getCanonicalName();

        if (!TM.runningTasks.containsKey(tag)) {
            TM.runningTasks.put(tag, basicRequestTask);
            //if (IOTA.DEBUG)
            //    Log.e("Added Task ", tag);
            basicRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
        }
    }


    public void startNewMessageTask(ApiRequest ir) {
        messageRequestTask = new MessageRequestTask(context);
        messageRequestTask.setTaskId(taskId);
        apiRequest=ir;
        TaskManager.addMessageTask(messageRequestTask, apiRequest);
    }
    private static synchronized void addMessageTask(MessageRequestTask requestTask, ApiRequest ir) {
        String tag = ir.getClass().getCanonicalName();

        if (!TM.runningTasks.containsKey(tag)) {
            TM.runningTasks.put(tag, requestTask);
            //if (IOTA.DEBUG)
            //    Log.e("Added message Task ", tag);
            requestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
        }
    }

}
