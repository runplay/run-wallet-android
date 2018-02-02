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

import com.google.gson.Gson;

import run.wallet.common.json.JSONObject;


public class Address implements Parcelable {

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
    private String address;
    private Boolean used;
    private boolean attached;
    private int lastMilestone;
    private long value=0;
    private long pendingValue=0;
    private int index;
    private int indexName;
    private int security=2;
    private boolean pendingAttach=false;
    private int pig=0;

    public boolean taskAttaching=false;

    public void setValue(long value) {
        this.value=value;
    }
    public long getValue() {
        return value;
    }
    public void setIndex(int index) {
        this.index=index;
    }
    public int getIndex() {
        return index;
    }
    public boolean isAttached() {
        return attached;
    }
    public void setAttached(boolean attached) {
        this.attached=attached;
    }
    public JSONObject toJson() {
        JSONObject job = new JSONObject();
        job.put("add",address);
        job.put("used",used);
        job.put("att",attached);
        job.put("val",value);
        job.put("pval",pendingValue);
        job.put("ind",index);
        job.put("inn", indexName);
        job.put("mil", lastMilestone);
        job.put("sec",security);
        job.put("pig",pig);
        return job;
    }
    public Address(JSONObject job) {
        address=job.optString("add",address);
        used=job.optBoolean("used");
        attached=job.optBoolean("att");
        value=job.optLong("val");
        index=job.optInt("ind");
        pendingValue=job.optLong("pval");
        pig=job.optInt("pig");
        indexName=job.optInt("inn");
        security=job.optInt("sec");
        lastMilestone=job.optInt("mil");
        if(security==0)
            security=2;
    }
    public String getShortAddress() {
        return address.substring(0,16);
    }
    public String getDisplayShortAddress() {
        return address.substring(0,8);
    }
    public static String toShortAddress(String address) {
        if(address.length()>16)
            return address.substring(0,16);
        return address;
    }
    public Address(String address, boolean used) {

        this.address = address;
        this.used = used;
        security=Store.getAddressSecurityDefault();
    }
    public Address(String address, boolean used, boolean attached) {
        this.address = address;
        this.used = used;
        this.attached=attached;
    }
    public Address(Parcel in) {

        address = in.readString();
        used = in.readInt() != 0;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeInt((used != null ? used : false) ? 1 : 0);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    @Override
    public boolean equals(Object object) {
        return this.getAddress().equals(((Address) object).getAddress());
    }



    public long getPendingValue() {
        return pendingValue;
    }

    public void setPendingValue(long pendingValue) {
        this.pendingValue = pendingValue;
    }

    public boolean isPendingAttach() {
        return pendingAttach;
    }

    public void setPendingAttach(boolean pendingAttach) {
        this.pendingAttach = pendingAttach;
    }

    public boolean isPig() {
        return pig>0;
    }

    public int getPigInt() {
        return pig;
    }
    public void setPigInt(int val) {
        if(val<0)
            val=0;
        else if(val>2)
            val=2;
        this.pig=val;
    }
    public boolean isPigUser() {
        return pig==2;
    }
    public void setPigUser(boolean pig) {
        this.pig = pig?2:0;
    }
    public boolean isPigLock() {
        return pig==1;
    }

    public void setPigLock(boolean pig) {
        this.pig = pig?1:0;
    }

    public int getIndexName() {
        return indexName;
    }

    public void setIndexName(int indexName) {
        this.indexName = indexName;
    }

    public int getSecurity() {
        return security;
    }

    public void setSecurity(int security) {
        this.security = security;
    }

    public int getLastMilestone() {
        return lastMilestone;
    }

    public void setLastMilestone(int lastMilestone) {
        this.lastMilestone = lastMilestone;
    }
}
