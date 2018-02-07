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

package run.wallet.iota.helper;

public class Constants {

    public static final String PREFERENCE_NODE_PROTOCOL = "preference_node_protocol";

    public static final String PREFERENCE_ENC_PASS = "preference_enc_seed";
    public static final String PREFERENCE_WALLET_VALUE_CURRENCY = "preference_wallet_currency";
    public static final String PREFERENCE_RUN_WITH_ROOT = "preference_run_with_root";
    public static final String PREFERENCES_LOCAL_POW = "preference_local_pow";
    public static final String PREFERENCES_SHOW_CANCELLED = "preference_show_cancelled";
    public static final String PREFERENCES_SHOW_ATTACH = "preference_show_attach";
    public static final String PREFERENCES_SHOW_USED = "preference_show_used";
    public static final String PREFERENCES_MIN_ADDRESSES = "preference_min_addresses";
    public static final int PREFERENCES_MIN_ADDRESSES_DEFAULT = 2;
    public static final String PREFERENCE_ISSUE_REPORTER = "preference_issue_reporter";
    public static final String PRICE_STORAGE_PREFIX = "exchange_rate_storage";

    public static final int PREF_TRANSFER_DEPTH_DEFAULT =9;

    public static final String PREF_BALANCE_DISPLAY= "pref_baland_display";

    public static final String PREF_FIRST_LOAD_ATTEMPTS="preference_fl_attempts";
    public static final int PREF_FIRST_LOAD_ATTEMPTS_DEFAULT =50;
    public static final String PREF_FIRST_LOAD_RANGE="preference_fl_range";
    public static final int PREF_FIRST_LOAD_RANGE_DEFAULT =5;

    public static final String PREF_TRANSFER_NUDGE_ATTEMPTS="preference_nudge_attempts";
    public static final int PREF_TRANSFER_NUDGE_ATTEMPTS_VALUE =2;
    public static final int PREF_TRANSFER_NUDGE_MILESTONES_VALUE =5;

    public static final int PREF_AUTO_LOGUT_DEFAULT=2;
    public static final String PREF_AUTO_LOGOUT="preference_auto_logout";

    public static final int PREF_MIN_WEIGHT_DEFAULT =14;
    public static final String PREF_MIN_WEIGHT="preference_min_weight";
    public static final String PREF_ADDRESS_SECURITY="preference_address_security";
    public static final int PREF_ADDRESS_SECURITY_DEFAULT=2;
    //Preferences defaults
    public static final String PREF_MSG_MESSY="preference_address_security";
    public static final String PREF_MSG_TAP_BALANCE="preference_tap_balance";


    public static final String DUMMY_PASSWORD = "preference_password_stored";

    public static final String PREFERENCE_NODE_DEFAULT_PROTOCOL = "http";

    public static final String PREFERENCE_PASS_LENGTH = "plvalue";

    public static final String DISPLAY_RUNIOTA_NODE_NAME="wallet";
    public static final String DISPLAY_RUNIOTA_NODE_NAME_END=".iota.runplay.com";

    public static final int REQUEST_CODE_LOGIN = 101;
    public static final int REQUEST_RESTART_KILL_APP =85;

    public static final int REQUEST_STORAGE_PERMISSION = 1;
    public static final int REQUEST_CAMERA_PERMISSION = 12;

    public static final String QRCODE = "qrcode";
    public static final String UDP = "udp://";
    public static final String NEW_ADDRESS_TAG =  "99999999RUN9WALLET9ADDRESS9";
    public static final String NEW_TRANSFER_TAG = "9999999RUN9WALLET9TRANSFER9";
    public static final String DONATION_TAG =     "9999999RUN9WALLET9DONATE999";


    public static final String DONATION_ADDRESS = "9PPMLVNEGQEZLCKTPDSCMKNKNDPMHUTC9PMOAKHGNGOVVTXOTRA99JFPVAAXHPUM99DGLUHOYLMWOL9YCSGRZJIYSW";

    public static final int DONATE_NOW=68732;


    //Intent actions
    public static final String ACTION_MAIN = "ACTION_MAIN";
    public static final String ACTION_SEND_TRANSFER = "sendTransfer";
    public static final String ACTION_GENERATE_QR_CODE = "generateQrCode";

    public static final String WWW_RUN_IOTA="http://iota.runplay.com";
    public static final String WWW_RUN_PLAY="http://www.runplay.com";

}
