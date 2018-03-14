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

package run.wallet.iota.api.requests;

import java.util.ArrayList;
import java.util.List;

import jota.model.Transfer;
import run.wallet.common.Sf;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.PayPacket;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;

public class SendTransferRequest extends SeedApiRequest {

    private int security = 2;
    List<PayPacket.PayTo> payTos=new ArrayList<>();
    //private List<String> addresses;
    //private String value = "";
    private String message = "";
    private String tag = "";
    private int minWeightMagnitude = Constants.PREF_MIN_WEIGHT_DEFAULT;
    private int depth = 9;
    private List<Address> fromAddresses;
    private Address remainder;

    public SendTransferRequest(Seeds.Seed seed, String address, String value
            , List<Address> fromAddress, Address remainder
            , String message, String tag) {
        super(seed);

        PayPacket.PayTo pt = new PayPacket.PayTo(Sf.toLong(value),address);
        payTos.add(pt);
        //this.value = value;
        this.message = message;
        this.tag = tag;
        this.setFromAddresses(fromAddress);
        this.setRemainder(remainder);
        minWeightMagnitude= Store.getMinWeightDefault();

    }
    public SendTransferRequest(Seeds.Seed seed, String address, String value, String message, String tag) {
        super(seed);
        PayPacket.PayTo pt = new PayPacket.PayTo(Sf.toLong(value),address);
        payTos.add(pt);
        //this.value = value;
        this.message = message;
        this.tag = tag;
        minWeightMagnitude= Store.getMinWeightDefault();
    }

    public SendTransferRequest(Seeds.Seed seed, List<PayPacket.PayTo> payTos, List<Address> fromAddress, Address remainder
            , String message, String tag) {
        super(seed);

        this.payTos = payTos;
        //this.value = value;
        this.message = message;
        this.tag = tag;
        this.setFromAddresses(fromAddress);
        this.setRemainder(remainder);
        minWeightMagnitude= Store.getMinWeightDefault();
    }
/*
    public Transfer prepareTransfer() {
        return new Transfer(address, Long.valueOf(value), message, tag);
        //return transfers;
    }
*/
    public List<Transfer> prepareTransfers() {
        List<Transfer> transfers = new ArrayList<>();
        for (PayPacket.PayTo pt: payTos) {
            transfers.add(new Transfer(pt.address, pt.value, message, tag));
        }
        return transfers;
    }



    //public void setSeed(Seeds.Seed seed) {
    //    this.seed = seed;
    //}

    public int getSecurity() {
        return security;
    }

    public void setSecurity(int security) {
        this.security = security;
    }


    public List<PayPacket.PayTo> getPayTos() {
        return payTos;
    }


    public long getValue() {
        long value=0L;
        for (PayPacket.PayTo pt: payTos) {
            value+=pt.value;
        }
        return value;
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

    public int getMinWeightMagnitude() {
        return minWeightMagnitude;
    }

    public void setMinWeightMagnitude(int minWeightMagnitude) {
        this.minWeightMagnitude = minWeightMagnitude;
    }


    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public List<Address> getFromAddresses() {
        return fromAddresses;
    }

    public void setFromAddresses(List<Address> fromAddresses) {
        this.fromAddresses = fromAddresses;
    }

    public Address getRemainder() {
        return remainder;
    }

    public void setRemainder(Address remainder) {
        this.remainder = remainder;
    }
}
