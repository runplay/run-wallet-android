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
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;

public class SendTransferRequest extends SeedApiRequest {

    private int security = 2;
    private String address = "";
    private List<String> addresses;
    private String value = "";
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

        this.address = address;
        this.value = value;
        this.message = message;
        this.tag = tag;
        this.setFromAddresses(fromAddress);
        this.setRemainder(remainder);
        minWeightMagnitude= Store.getMinWeightDefaultDefault();

    }
    public SendTransferRequest(Seeds.Seed seed, String address, String value, String message, String tag, int minWeight) {
        super(seed);
        this.minWeightMagnitude=minWeight;
        this.address = address;
        this.value = value;
        this.message = message;
        this.tag = tag;
    }
    public SendTransferRequest(Seeds.Seed seed, String address, String value, String message, String tag) {
        super(seed);

        this.address = address;
        this.value = value;
        this.message = message;
        this.tag = tag;
        minWeightMagnitude= Store.getMinWeightDefaultDefault();
    }

    public SendTransferRequest(Seeds.Seed seed,List<String> addresses, String value, String message, String tag) {
        super(seed);

        this.addresses = addresses;
        this.value = value;
        this.message = message;
        this.tag = tag;
        minWeightMagnitude= Store.getMinWeightDefaultDefault();
    }

    public Transfer prepareTransfer() {
        return new Transfer(address, Long.valueOf(value), message, tag);
        //return transfers;
    }

    public List<Transfer> prepareTransfers() {
        List<Transfer> transfers = new ArrayList<>();
        for (int i = 0; i < addresses.size(); i++) {
            transfers.add(new Transfer(addresses.get(i), Long.valueOf(value), message, tag));
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
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
