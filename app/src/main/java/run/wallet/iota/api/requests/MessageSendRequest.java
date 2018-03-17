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
import jota.utils.SeedRandomGenerator;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.Utils;
import run.wallet.iota.model.Seeds;

public class MessageSendRequest extends ApiRequest {

    private Seeds.Seed seed;
    private int security = Constants.PREF_ADDRESS_SECURITY_DEFAULT;
    private String address = "";
    private List<String> addresses;
    private String value = "0";
    private String message = "";
    private String tag = "";
    private int minWeightMagnitude = Constants.PREF_MIN_WEIGHT_DEFAULT;
    private int depth = 9;

    private static final String keyid="KEY";

    public MessageSendRequest(Seeds.Seed seed, String address, String message, String tag) {
        this.seed = seed;
        this.address = address;
        this.message = message;
        this.tag = tag;
    }


    public List<Transfer> prepareTransfer() {
        List<Transfer> transfers = new ArrayList<>();
        String usetag = keyid+SeedRandomGenerator.generateNewSeed().substring(0,24);
        String encMessage=Utils.encryptMessageForTransfer(message,usetag,"");
        Transfer transfer=new Transfer(address, Long.valueOf(value), encMessage, usetag);
        transfers.add(transfer);
        return transfers;
    }
/*
    public String getSeedValue() {
        return String.valueOf(seed.value);
    }
*/
    public Seeds.Seed getSeed() {
        return seed;
    }


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
}
