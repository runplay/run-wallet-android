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

package run.wallet.iota.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import run.wallet.common.json.JSONArray;
import run.wallet.common.json.JSONObject;

public class Transfer implements Parcelable, Comparable<Transfer> {

    public static final Creator<Transfer> CREATOR = new Creator<Transfer>() {
        @Override
        public Transfer createFromParcel(Parcel in) {
            return new Transfer(in);
        }

        @Override
        public Transfer[] newArray(int size) {
            return new Transfer[size];
        }
    };


    private long timestamp;
    private String address;
    private String hash;
    private Boolean persistence;
    private long value;
    private String message;
    private String tag;
    private boolean markDoubleSpend=false;
    private boolean markDoubleAddress=false;
    private long lastDoubleCheck;
    private long milestone;
    private int nudgeCount;
    private long timestampConfirmed;
    private List<TransferTransaction> transactions=new ArrayList<>();
    private List<TransferTransaction> othertransactions=new ArrayList<>();

    public boolean isCompleted() {
        return getPersistence()!=null?getPersistence():false;
    }
    public JSONObject toJson() {
        JSONObject job = new JSONObject();
        job.put("ts",timestamp);
        job.put("add",address);
        job.put("hash",hash);
        job.put("ps",persistence);
        job.put("val",value);
        job.put("msg",message);
        job.put("tag",tag);
        job.put("mil", getMilestone());
        job.put("dbl", isMarkDoubleSpend());
        job.put("dba", markDoubleAddress);
        job.put("ldb",lastDoubleCheck);
        job.put("nc",nudgeCount);
        job.put("tsc", getTimestampConfirmed());
        JSONArray jar = new JSONArray();
        for(TransferTransaction t: getTransactions()) {
            jar.put(t.toJson());
        }
        job.put("tra",jar);
        JSONArray ojar = new JSONArray();
        for(TransferTransaction t: othertransactions) {
            ojar.put(t.toJson());
        }
        job.put("tro",ojar);
        return job;
    }
    public Transfer(JSONObject job) {

        timestamp=job.optLong("ts");
        address=job.optString("add");
        hash=job.optString("hash");
        persistence=job.optBoolean("ps");
        value=job.optLong("val");
        nudgeCount=job.optInt("nc");
        message=job.optString("msg");
        setMilestone(job.optInt("mil"));
        tag=job.optString("tag");
        setMarkDoubleSpend(job.optBoolean("dbl"));
        markDoubleAddress=job.optBoolean("dba");
        lastDoubleCheck=job.optLong("ldb");
        setTimestampConfirmed(job.optLong("tsc"));
        JSONArray jar = job.optJSONArray("tra");
        for(int i=0; i<jar.length(); i++) {
            TransferTransaction t= new TransferTransaction(jar.getJSONObject(i));
            getTransactions().add(t);
        }
        JSONArray ojar = job.optJSONArray("tro");
        for(int i=0; i<ojar.length(); i++) {
            TransferTransaction t= new TransferTransaction(ojar.getJSONObject(i));
            othertransactions.add(t);
        }
    }
    public Transfer(String address, long value, String message, String tag) {
        this.address = address;
        this.value = value;
        this.message = message;
        this.tag = tag;
    }

    public Transfer(long timestamp, String address, String hash, Boolean persistence,
                    long value, String message, String tag) {
        this.timestamp = timestamp;
        this.address = address;
        this.hash = hash;
        this.persistence = persistence;
        this.value = value;
        this.message = message;
        this.tag = tag;

    }

    public Transfer(Parcel in) {
        timestamp = in.readLong();
        address = in.readString();
        hash = in.readString();
        persistence = in.readInt() != 0;
        value = in.readLong();
        message = in.readString();
        tag = in.readString();
    }

    public long getInternalTotal() {
        long internalTotal=0L;
        for(TransferTransaction tran: getTransactions()) {
            internalTotal+=tran.getValue();
        }
        return internalTotal;
    }
    public String getHashShort() {
        return hash.substring(0,16);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timestamp);
        dest.writeString(address);
        dest.writeString(hash);
        dest.writeLong(value);
        dest.writeInt((persistence != null ? persistence : false) ? 1 : 0);
        dest.writeString(message);
        dest.writeString(tag);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Boolean getPersistence() {
        return persistence;
    }

    public void setPersistence(Boolean persistence) {
        this.persistence = persistence;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public int compareTo(@NonNull Transfer transfer) {
        return Long.compare(transfer.getTimestamp(), getTimestamp());
    }

    public boolean isMarkDoubleSpend() {
        return markDoubleSpend;
    }

    public void setMarkDoubleSpend(boolean markDoubleSpend) {
        this.markDoubleSpend = markDoubleSpend;
    }

    public List<TransferTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransferTransaction> transactions) {
        this.transactions = transactions;
    }
    public List<TransferTransaction> getOtherTransactions() {
        return othertransactions;
    }

    public void setOtherTransactions(List<TransferTransaction> othertransactions) {
        this.othertransactions = othertransactions;
    }

    public long getMilestone() {
        return milestone;
    }

    public void setMilestone(long milestone) {
        this.milestone = milestone;
    }

    public boolean isMarkDoubleAddress() {
        return markDoubleAddress;
    }

    public void setMarkDoubleAddress(boolean markDoubleAddress) {
        this.markDoubleAddress = markDoubleAddress;
    }

    public long getLastDoubleCheck() {
        return lastDoubleCheck;
    }

    public void setLastDoubleCheck(long lastDoubleCheck) {
        this.lastDoubleCheck = lastDoubleCheck;
    }

    public int getNudgeCount() {
        return nudgeCount;
    }

    public void setNudgeCount(int nudgeCount) {
        this.nudgeCount = nudgeCount;
    }

    public long getTimestampConfirmed() {
        return timestampConfirmed;
    }

    public void setTimestampConfirmed(long timestampConfirmed) {
        this.timestampConfirmed = timestampConfirmed;
    }
}
