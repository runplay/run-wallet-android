package run.wallet.iota.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import run.wallet.common.Currency;
import run.wallet.common.Sf;
import run.wallet.common.json.JSONArray;
import run.wallet.common.json.JSONObject;
import run.wallet.iota.api.requests.AddressSecurityChangeRequest;
import run.wallet.iota.api.requests.GetNewAddressRequest;
import run.wallet.iota.api.responses.GetNeighborsResponse;
import run.wallet.iota.api.responses.GetNewAddressResponse;
import run.wallet.iota.api.responses.NodeInfoResponse;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.Utils;
import run.wallet.iota.security.Validator;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.SpManager;
import run.wallet.iota.ui.adapter.WalletAddressCardAdapter;
import run.wallet.iota.ui.adapter.WalletTransfersCardAdapter;

/**
 * Created by coops on 18/12/17.
 */

public class Store {

    private static Store store=new Store();

    private static final String SP_STORE="tickerstore";
    private static final String NT_STORE="ntstore";
    private static final String PREF_TICKHIST="pref_ticker_hist";
    private static final String PREF_EXCHANGERATES="excr";
    private static final String PREF_ADDRESSES="add";
    private static final String PREF_NUDGE_TRANSFERS="nudgetran";
    private static final String PREF_TRANSFERS="traf";
    private static final String PREF_WALLET= "wallet";


    private Seeds seeds;
    private Seeds.Seed currentSeed;
    private Wallet currentWallet;
    private Nodes nodes;
    private Nodes.Node currentNode;
    private boolean isLoggedin=false;
    private int balanceDisplayType;

    private int addressSecurity=Constants.PREF_ADDRESS_SECURITY_DEFAULT;
    private int minWeight=Constants.PREF_MIN_WEIGHT_DEFAULT;
    private int autoAttach=Constants.PREFERENCES_MIN_ADDRESSES_DEFAULT;


    List<SystemMessage> sysMessages=new ArrayList<>();
    private final List<Wallet> allwallets=new ArrayList<>();

    private Map<String,Ticker> tickers=new HashMap();
    private Map<String,TickerHist> tickerHist=new HashMap();


    private final List<Address> addresses = new ArrayList();
    private final List<NudgeTransfer> nudgeTransfers = new ArrayList();
    //private List<Transaction> transactions = new ArrayList();
    private final List<Transfer> transfers = new ArrayList();

    private NodeInfoResponse nodeInfo;
    private List<Neighbor> neighbours=new ArrayList<>();

    private int failedNodeAttempts=0;

    public static final int getFailedNodeAttempt() {
        return store.failedNodeAttempts;
    }
    public static final void addFailedNodeAttempt() {
        store.failedNodeAttempts++;
    }
    private Address tmpCacheAddress;
    private Transfer tmpCacheTransfer;

    private Store() {}

    public static void init(Context context) {
        if(store.currentSeed==null) {
            //Log.e("INIT","Store init()");
            store.seeds = new Seeds(context);
            store.seeds = new Seeds(context);
            store.currentSeed = Store.getDefaultSeed();
            loadDefaults(context);
            jointInit(context);
        } else {
            //Log.e("INIT","Store ALREADY init()");
        }
    }

    public static void reinit(Context context) {
        jointInit(context);
    }
    public static Currency getDefaultCurrency(Context context) {
        return Utils.getConfiguredAlternateCurrency(context);

    }
    private static void jointInit(Context context) {
        store.nodes=new Nodes(context);
        loadSeedData(context);
        loadTickerValues(context);
        loadSystemMessages(context);
    }
    private static void loadSeedData(Context context) {
        loadWallet(context);
        loadAddresses(context);
        loadTransfers(context);
        loadWallets(context);
    }

    public static class TransferBalances {
        public long available;
        public long pending;
    }



    public static int getAutoAttach() {
        return store.autoAttach;
    }

    public static void loadDefaults(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            store.autoAttach = Sf.toInt(prefs.getString(Constants.PREFERENCES_MIN_ADDRESSES, "" + Constants.PREFERENCES_MIN_ADDRESSES_DEFAULT));
            store.addressSecurity = Sf.toInt(prefs.getString(Constants.PREF_ADDRESS_SECURITY, "" + Constants.PREF_ADDRESS_SECURITY_DEFAULT));
            store.minWeight = Sf.toInt(prefs.getString(Constants.PREF_MIN_WEIGHT, "" + Constants.PREF_MIN_WEIGHT_DEFAULT));
            store.balanceDisplayType = prefs.getInt(Constants.PREF_BALANCE_DISPLAY, 0);
        } catch(Exception e){
            store.autoAttach = Constants.PREFERENCES_MIN_ADDRESSES_DEFAULT;
            store.addressSecurity = Constants.PREF_ADDRESS_SECURITY_DEFAULT;
            store.minWeight = Constants.PREF_MIN_WEIGHT_DEFAULT;
            store.balanceDisplayType = 0;
        }
    }

    public static void setCacheAddress(Address address) {
        store.tmpCacheAddress=address;
    }
    public static Address getCacheAddress() {
        return store.tmpCacheAddress;
    }
    public static void setCacheTransfer(Transfer transfer) {
        store.tmpCacheTransfer=transfer;
    }
    public static Transfer getCacheTransfer() {
        return store.tmpCacheTransfer;
    }

    public static int getBalanceDisplayType() {
        return store.balanceDisplayType;
    }

    public static void setBalanceDisplayType(Context context, int id) {
        if(id<0 || id>2)
            id=0;
        store.balanceDisplayType =id;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt(Constants.PREF_BALANCE_DISPLAY,id).commit();
    }

    public static final List<Neighbor> getNeighbours() {
        return store.neighbours;
    }
    public static void setNeighbours(GetNeighborsResponse response) {

        store.neighbours.clear();
        for (Neighbor neighbor : response.getNeighbors()) {
            neighbor.setOnline(true);
            store.neighbours.add(neighbor);

        }
    }
    public static void setNodeInfo(NodeInfoResponse nodeInfo) {

        store.nodeInfo=nodeInfo;

    }
    public static final NodeInfoResponse getNodeInfo() {
        return store.nodeInfo;
    }
    public static void setNeighbours(List<Neighbor> neighbours) {
        store.neighbours=neighbours;
    }



    private static void loadSystemMessages(Context context) {
        SharedPreferences sp = context.getSharedPreferences("sysmessages", Context.MODE_PRIVATE);
        String strJson=sp.getString("msglist","[]");

        try {
            store.sysMessages.clear();
            JSONArray already = new JSONArray(strJson);
            for(int i=0; i<already.length(); i++) {
                SystemMessage msg=new SystemMessage(already.optJSONObject(i));
                if(!msg.isIsread())
                    store.sysMessages.add(msg);
            }
        } catch(Exception e) {}
    }
    public static void addSystemMessage(Context context, JSONObject message) {
        SharedPreferences sp = context.getSharedPreferences("sysmessages", Context.MODE_PRIVATE);
        String strJson=sp.getString("msglist","[]");

        try {
            JSONArray already = new JSONArray(strJson);
            already.put(message);
            sp.edit().putString("msglist",already.toString());
        } catch(Exception e) {}
    }
    public static void setAddressSecurity(Context context, int security) {
        if(security<1)
            security=1;
        if(security>3)
            security=3;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(Constants.PREF_ADDRESS_SECURITY,""+security).commit();
        store.addressSecurity=security;
    }

    private static void setMinWeight(Context context, int minWeight) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(Constants.PREF_MIN_WEIGHT,""+minWeight).commit();
        store.minWeight=minWeight;
    }

    public static final Wallet getCurrentWallet() {
        return store.currentWallet;
    }
    public static final Ticker getTicker(String currencyPair) {
        return store.tickers.get(currencyPair);
    }
    public static boolean isNodeSynced() {
        if(store.nodeInfo!=null && store.nodeInfo.getLatestMilestoneIndex() == store.nodeInfo.getLatestSolidSubtangleMilestoneIndex()) {
            return true;
        }
        return false;
    }
    private static void loadTickerValues(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_STORE, Context.MODE_PRIVATE);
        JSONArray jar=null;
        try {
            jar=new JSONArray(sp.getString(PREF_EXCHANGERATES,"[]"));
        } catch(Exception e) {}
        if(jar!=null) {
            store.tickers.clear();
            for(int i=0; i<jar.length(); i++) {
                JSONObject ob = null;
                try {
                    ob = jar.getJSONObject(i);
                } catch (Exception e) {
                }
                if(ob!=null) {
                    Ticker ticker = new Ticker(ob);
                    if(ticker.getCp()!=null) {
                        store.tickers.put(ticker.getCp(),ticker);
                    }
                }
            }

        }

    }

    public static void updateTickers(Context context, JSONObject job) {
        if(job!=null && job.optInt("result")==200 && job.has("data")) {
            store.tickers.clear();
            JSONArray jar = job.optJSONArray("data");
            if(jar!=null && jar.length()>0) {
                SharedPreferences sp = context.getSharedPreferences(SP_STORE, Context.MODE_PRIVATE);
                sp.edit().putString(PREF_EXCHANGERATES,jar.toString()).commit();

                for(int i=0; i<jar.length(); i++) {
                    JSONObject ob = null;
                    try {
                        ob=jar.getJSONObject(i);
                    } catch(Exception e) {
                        Log.e("EXCH","e2: "+e.getMessage());
                    }
                    if(ob!=null) {
                        Ticker ticker = new Ticker(ob);
                        if(ticker.getCp()!=null) {
                            store.tickers.put(ticker.getCp(),ticker);
                        }
                    }
                }
            }

        }
    }
    public static void updateTickerHist(Context context,JSONObject job) {
        if(job!=null && job.optInt("result")==200 && job.has("data")) {
            JSONArray jar = job.optJSONArray("data");
            if(jar!=null) {
                String cp=job.optString("cp");
                List<Tick> history=new ArrayList<>();
                for(int i=0; i<jar.length(); i++) {

                    Tick ticker = new Tick(jar.optJSONObject(i));
                    //Log.e("TCIK",ticker.getLast()+"--"+jar.optJSONObject(i).toString());
                    history.add(ticker);
                }
                TickerHist thist=new TickerHist();
                thist.setTicker(cp);
                thist.setStep(job.optInt("step"));
                thist.setTicks(history);
                thist.setLastUpdate(System.currentTimeMillis());
                //Log.e("STORE-THIST",cp);
                store.tickerHist.put(cp+thist.getStep(),thist);

                //JSONObject save=new JSONObject();
                SharedPreferences sp = context.getSharedPreferences(SP_STORE, Context.MODE_PRIVATE);
                String strJson = sp.getString(PREF_TICKHIST,"{}");
                if(strJson!=null) {
                    JSONObject tickerHistories = new JSONObject(strJson);
                    tickerHistories.put(cp+thist.getStep(), job);
                    tickerHistories.put("last",System.currentTimeMillis());
                    //Log.e("STL",tickerHistories.toString());
                    sp.edit().putString(PREF_TICKHIST, tickerHistories.toString()).commit();
                }
            }

        }
    }
    public static TickerHist getTickerHist(Context context,String ticker,int step) {
        TickerHist hist=store.tickerHist.get(ticker+step);
        if(hist==null) {
            SharedPreferences sp = context.getSharedPreferences(SP_STORE, Context.MODE_PRIVATE);
            String strJson = sp.getString(PREF_TICKHIST,null);
            if(strJson!=null) {
                JSONObject tickerHistories = new JSONObject(strJson);
                JSONObject gottickerhist=tickerHistories.optJSONObject(ticker+step);
                //Log.e("GTL",""+gottickerhist);
                if(gottickerhist!=null) {
                    String cp = gottickerhist.optString("cp");

                    if (!cp.isEmpty()) {
                        long last=gottickerhist.optLong("last");
                        //Log.e("STORE-THIST", "get: " + cp);
                        List<Tick> history = new ArrayList<>();
                        JSONArray jar = gottickerhist.optJSONArray("data");
                        for (int i = 0; i < jar.length(); i++) {

                            Tick aticker = new Tick(jar.optJSONObject(i));
                            //Log.e("TCIK",ticker.getLast()+"--"+jar.optJSONObject(i).toString());
                            history.add(aticker);
                        }
                        TickerHist thist = new TickerHist();
                        thist.setTicker(cp);
                        thist.setStep(gottickerhist.optInt("step"));
                        thist.setTicks(history);
                        thist.setLastUpdate(last);
                        //Log.e("STORE-THIST",cp);
                        store.tickerHist.put(cp + step, thist);
                    }
                }
            }
        }
        return store.tickerHist.get(ticker+step);
    }
    public static void setTickerHist(TickerHist hist) {
        store.tickerHist.put(hist.getTicker(),hist);
    }

    public static  synchronized void setAccountData(Context context, Seeds.Seed seed, Wallet wallet, List<Transfer> transfers, List<Address> addresses) {
        SharedPreferences sp = context.getSharedPreferences(seed.id, Context.MODE_PRIVATE);
        JSONArray trans = new JSONArray();
        for (Transfer t : transfers) {
            trans.put(t.toJson());
        }
        SpManager.setEncryptedPreference(sp, PREF_TRANSFERS, trans.toString());
        JSONArray jar = new JSONArray();
        for (Address add : addresses) {
            jar.put(add.toJson());
        }
        SpManager.setEncryptedPreference(sp, PREF_ADDRESSES, jar.toString());
        //Wallet run.wallet.monero.wallet = new Wallet(seed.id,balance,System.currentTimeMillis());
        Store.addUpdateWallet(context,seed,wallet);
        if(store.currentSeed!=null && seed.id.equals(store.currentSeed.id)) {
            reinit(context);
        }
        updateStats(context, seed, wallet, addresses, transfers);
        if(store.currentSeed!=null && seed.id.equals(store.currentSeed.id)) {
            reinit(context);
        }
    }

    public static void updateAllTransfersInSeed(Context context, Seeds.Seed seed, List<Transfer> transfers) {
        SharedPreferences sp = context.getSharedPreferences(seed.id, Context.MODE_PRIVATE);

        if(!transfers.isEmpty()) {
            JSONArray trans = new JSONArray();
            for (Transfer t : transfers) {
                trans.put(t.toJson());
            }
            //Log.e("EDATETRAN",trans.toString());
            SpManager.setEncryptedPreference(sp, PREF_TRANSFERS, trans.toString());
            if(store.currentSeed!=null && seed.id.equals(store.currentSeed.id)) {
                WalletTransfersCardAdapter.load(context,true);
                WalletAddressCardAdapter.load(context,true);
                reinit(context);

            }
        }
    }
    public static  synchronized void updateAccountData(Context context, Seeds.Seed seed, Wallet wallet, List<Transfer> transfers, List<Address> addresses) {
        if(seed!=null) {
            //Log.e("STORE","........updateAccountData()");

            SharedPreferences sp = context.getSharedPreferences(seed.id, Context.MODE_PRIVATE);

            if(!transfers.isEmpty()) {
                JSONArray trans = new JSONArray();
                for (Transfer t : transfers) {
                    trans.put(t.toJson());
                }

                SpManager.setEncryptedPreference(sp, PREF_TRANSFERS, trans.toString());

                List<Address> alreadyAddress = getAddresses(context,seed);

                for(Address add: addresses) {
                    Address already=isAlreadyAddress(add,alreadyAddress);
                    if(already==null) {
                        //add.setIndex(alreadyAddress.size());
                        alreadyAddress.add(add);

                    } else {
                        already.setAttached(add.isAttached());
                        already.setValue(add.getValue());
                        already.setPendingValue(add.getPendingValue());
                        already.setUsed(add.isUsed());
                        already.setPigInt(add.getPigInt());

                    }
                }

                    //Log.e("Store","store - UPDATING Address");
                JSONArray jar = new JSONArray();
                for (Address add : alreadyAddress) {
                    jar.put(add.toJson());
                }


                //Log.e("ADDRESSES",jar.toString());
                SpManager.setEncryptedPreference(sp, PREF_ADDRESSES, jar.toString());

                Store.addUpdateWallet(context,seed,wallet);

                if(store.currentSeed!=null && seed.id.equals(store.currentSeed.id)) {
                    reinit(context);
                    WalletAddressCardAdapter.load(context,true);
                    WalletTransfersCardAdapter.load(context,true);
                }
                //updateStats(context, seed, wallet, addresses, transfers);



            }
        }

    }

    @Deprecated
    public static synchronized void updateAccountDataOld(Context context, Seeds.Seed seed, Wallet wallet, List<Transfer> transfers, List<Address> addresses) {
        if(seed!=null) {
            //Log.e("STORE","........updateAccountData()");

            SharedPreferences sp = context.getSharedPreferences(seed.id, Context.MODE_PRIVATE);
            //String jarrayString = SpManager.getEncryptedPreference(sp, PREF_ADDRESSES, "[]");


            //Log.e("Store","store UPDATING Transfers");
            List<Transfer> alreadyTransfers = getTransfers(context,seed);
            //Collections.reverse(transfers);
            for (Transfer t : transfers) {
                Transfer already = isAlreadyTransfer(t,alreadyTransfers);
                if(already==null) {
                    alreadyTransfers.add(0,t);
                } else {
                    already.setPersistence(t.getPersistence()!=null?t.getPersistence():false);
                    already.setMarkDoubleSpend(t.isMarkDoubleSpend());
                    already.setTimestamp(t.getTimestamp());
                }
            }

            JSONArray trans = new JSONArray();
            for (Transfer t : alreadyTransfers) {
                trans.put(t.toJson());
            }
            //Log.e("TRANSFERS",trans.toString());
            SpManager.setEncryptedPreference(sp, PREF_TRANSFERS, trans.toString());


            List<Address> alreadyAddress = getAddresses(context,seed);

            for(Address add: addresses) {
                Address already=isAlreadyAddress(add,alreadyAddress);
                if(already==null) {
                    //add.setIndex(alreadyAddress.size());
                    alreadyAddress.add(add);

                } else {
                    already.setAttached(add.isAttached());
                    already.setValue(add.getValue());
                    already.setPendingValue(add.getPendingValue());
                    already.setUsed(add.isUsed());
                    //already.set
                }
            }

            //Log.e("Store","store - UPDATING Address");
            JSONArray jar = new JSONArray();
            for (Address add : alreadyAddress) {
                jar.put(add.toJson());
            }
            Log.e("ADDRESSES",jar.toString());
            SpManager.setEncryptedPreference(sp, PREF_ADDRESSES, jar.toString());

            Store.addUpdateWallet(context,seed,wallet);

            if(store.currentSeed!=null && seed.id.equals(store.currentSeed.id)) {
                reinit(context);
            }
            updateStats(context, seed, wallet, addresses, transfers);
        }

    }

    public static void setCurrentAddressPig(Context context, String address, boolean isPig) {
        for(Address add: getAddresses()) {
            //Log.e("SET-PIG",add.getAddress().startsWith(address)+"--"+add.getAddress()+"-"+address+"----");
            if(add.getAddress().startsWith(address)) {
                add.setPigUser(isPig);
            }
            SharedPreferences sp = context.getSharedPreferences(getCurrentSeed().id, Context.MODE_PRIVATE);
            JSONArray jar = new JSONArray();
            for (Address addsave : getAddresses()) {
                jar.put(addsave.toJson());
            }
            SpManager.setEncryptedPreference(sp, PREF_ADDRESSES, jar.toString());
        }

    }
    public static Transfer isAlreadyTransfer(Transfer check, List<Transfer> inlist) {
        for(Transfer compare: inlist) {
            if(compare.getHashShort().equals(check.getHashShort()))
                return compare;
        }
        return null;
    }
    public static Transfer isAlreadyTransfer(String hash, List<Transfer> inlist) {
        for(Transfer compare: inlist) {
            if(compare.getHash().equals(hash))
                return compare;
        }
        return null;
    }
    public static Address isAlreadyAddress(Address check, List<Address> inlist) {
        for(Address compare: inlist) {
            if(compare.getAddress().startsWith(check.getAddress()))
                return compare;
        }
        return null;
    }
    public static Address isAlreadyAddress(String address, List<Address> inlist) {
        for(Address compare: inlist) {
            if(compare.getAddress().startsWith(address))
                return compare;
        }
        return null;
    }

    public static boolean login(String loginPass) {
        if(loginPass==null || !loginPass.startsWith("RI") || !loginPass.endsWith("D9"))
            return false;
        store.isLoggedin=true;
        return true;
    }
    public static boolean verifyPassword(String loginPass) {
        if(loginPass==null || !loginPass.startsWith("RI") || !loginPass.endsWith("D9"))
            return false;
        return true;
    }
    public static int getAddressSecurityDefault() {
        return store.addressSecurity;

    }
    public static int getMinWeightDefaultDefault() {
        return store.minWeight;

    }
    private static void updateStats(Context context, Seeds.Seed seed, Wallet wallet, List<Address> addresses, List<Transfer> transfers) {
        SharedPreferences sp = context.getSharedPreferences("ustats", Context.MODE_PRIVATE);
        String jsonstr = sp.getString(seed.id,"{}");
        JSONObject job = new JSONObject(jsonstr);
        job.put("iota",getIotaBalanceCategory(wallet.getBalanceDisplay()));
        job.put("add",addresses.size());
        job.put("tra",transfers.size());

        sp.edit().putString(seed.id,job.toString()).commit();
    }
    private static final String period=".";
    public static final String getUaStats(Context context) {
        SharedPreferences sp = context.getSharedPreferences("ustats", Context.MODE_PRIVATE);
        StringBuilder stats=new StringBuilder();
        if(store.seeds!=null && !store.seeds.getSeeds().isEmpty()) {
            List<Seeds.Seed> seeds=Store.getSeeds().getSeeds();
            for (int i=0; i<seeds.size(); i++ ) {
                Seeds.Seed seed=seeds.get(i);
                String jsonstr = sp.getString(seed.id,"{}");
                JSONObject job = new JSONObject(jsonstr);
                stats.append(job.getInt("add"));
                stats.append(period);
                stats.append(job.getInt("tra"));
                stats.append(period);
                stats.append(job.getLong("iota"));
                if(i<seeds.size()-1)
                    stats.append(",");
            }
        }
        if(stats.toString().isEmpty())
            stats.append("0");
        // first is address count, second transfer count, 3rd wallet value as int = 1=iota, 2=kiota, 3=miota, 4=giota, etc....
        return stats.toString();
    }


    private static final long miota=  1000000;
    private static final long giota=  1000000000;
    private static final long sgiota= 10000000000L;
    private static final long ssgiota= 100000000000L;
    private static final long tiota=  1000000000000L;
    private static final long stiota=  1000000000000L;
    private static final long sstiota= 100000000000000L;
    private static final long piota=  1000000000000000L;
    private static final long spiota=  10000000000000000L;
    private static int getIotaBalanceCategory(long bal) {
        int cat=0;
        if(bal>spiota)
            cat=9;
        else if(bal>piota)
            cat=8;
        else if(bal>sstiota)
            cat=7;
        else if(bal>stiota)
            cat=6;
        else if(bal>tiota)
            cat=5;
        else if(bal>ssgiota)
            cat=4;
        else if(bal>sgiota)
            cat=3;
        else if(bal>giota)
            cat=2;
        if(bal>miota)
            cat=1;
        return cat;
    }
    public static void logout() {
        store.isLoggedin=false;
    }
    public static final boolean isLoggedIn() {
        return store.isLoggedin;
    }

    public static final List<Address> getAddresses() {
        return store.addresses;
    }
    public static final List<Address> getAddressesNotAttached() {
        List<Address> got=new ArrayList<>();
        for(Address add: store.addresses) {
            if(!add.isAttached()) {
                got.add(add);
            }
        }
        return got;
    }
    public static final List<Address> getAddressesAttached() {
        List<Address> got=new ArrayList<>();
        for(Address add: store.addresses) {
            if(add.isAttached()) {
                got.add(add);
            }
        }
        return got;
    }
    public static List<Transfer> getTransfers() {
        return store.transfers;
    }

    public static Seeds getSeeds() {
        return store.seeds;
    }

    public static List<Seeds.Seed> getSeedList() {
        if(store.seeds!=null)
            return store.seeds.getSeeds();
        return null;
    }


    public static void addSeed(Context context, char[] seedString,String name,boolean isdefault, boolean isgen) {
        if(store.seeds!=null)
            store.seeds.addSeed(context,seedString,name,isdefault,isgen);

    }
    /*
    public static void clearData() {
        store=new Store();
    }
*/
    public static synchronized void wipeAllStoreSavedData(Context context) {
        if(Validator.isValidCaller() && store.seeds!=null) {
            store.isLoggedin=false;
            for (Seeds.Seed seed : store.seeds.getSeeds()) {
                SharedPreferences sp = context.getSharedPreferences(store.currentSeed.id, Context.MODE_PRIVATE);
                sp.edit().clear().commit();
            }
            SharedPreferences sp = context.getSharedPreferences(Seeds.SP_WALLETS, Context.MODE_PRIVATE);
            sp.edit().clear().commit();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().clear().commit();
            SharedPreferences ntsp = context.getSharedPreferences(NT_STORE, Context.MODE_PRIVATE);
            ntsp.edit().clear().commit();
            store=new Store();
            Store.init(context);
            Log.e("STORE","WIPED DATA & INIT: now has seed count: "+getSeedList().size());
        }
    }
    public static List<Nodes.Node> getNodes() {
        return store.nodes.getNodes();
    }
    public static List<Nodes.Node> getOtherNodes(Context context) {
        return store.nodes.getOtherNodes(context);
    }
    //public static addSeed
    public static Nodes.Node getNode() {
        if(store.currentNode!=null)
            return store.currentNode;
        if(store.nodes!=null)
            store.currentNode=store.nodes.getNode();
        return store.currentNode;
    }
    @Nullable
    public static Nodes.Node getNextNode() {
        if(store.nodes!=null) {
            return store.nodes.getNode();
        } return null;
    }
    public static void changeNode(Nodes.Node newnode) {
        store.currentNode=newnode;
        //Log.e("SOTRE",store.currentNode.ip);
        //store.nodeInfo=null;
        store.neighbours.clear();
    }
    public static void updateNode(Context context,Nodes.Node updateNode) {
        if(store.nodes!=null) {
            store.nodes.update(context,updateNode);
        }
    }
    public static void addNode(Context context, String ip, int port, String protocol) {
        store.nodes.addNode(context,ip,port,protocol);
    }
    public static void MoveUpNode(Context context, int currindex, int toindex) {
        store.nodes.moveUp(context,currindex,toindex);
    }
    public static void removeNode(Context context, Nodes.Node removeNode) {
        store.nodes.removeNode(context, removeNode);
    }
    public static void setCurrentSeed(Context context, Seeds.Seed changeToSeed) {
        if(changeToSeed!=null) {
            store.currentSeed = changeToSeed;
            loadSeedData(context);
        }
    }
    public static Seeds.Seed getCurrentSeed() {
        if(store.currentSeed!=null) {
            //Log.e("CURR-SEED",store.currentSeed.id+"--"+store.currentSeed.value.length);
            return store.currentSeed;
        }
        store.currentSeed=getDefaultSeed();
        //Log.e("CURR-SEED-2",store.currentSeed.id+"--"+store.currentSeed.value.length);
        return store.currentSeed;
    }
    public static void removeSeed(Context context, String seedId) {
        store.seeds.removeSeed(context,seedId);
    }
    private static Seeds.Seed getDefaultSeed() {
        if(store.seeds!=null) {
            for (Seeds.Seed seed : store.seeds.getSeeds()) {
                if (seed.isdefault)
                    store.currentSeed = seed;
            }
            if (store.currentSeed == null && !store.seeds.getSeeds().isEmpty()) {
                store.currentSeed = store.seeds.getSeed(0);
            }
        }
        return store.currentSeed;
    }
    public static void setAddressesToNotAttached(Context context, Seeds.Seed seed, List<Address> setAddresses) {
        List<Address> addresses=getAddresses(context,seed);
        boolean updateAddress=false;
        if(!addresses.isEmpty()) {
            for(Address setadd: setAddresses) {
                for(Address add: addresses) {
                    if(add.getAddress().equals(setadd.getAddress())) {
                        add.setAttached(false);
                        updateAddress=true;
                    }
                }
            }

        }
        if(updateAddress) {
            //Log.e("Store","store - UPDATING Address");
            JSONArray jar = new JSONArray();
            for (Address add : addresses) {
                jar.put(add.toJson());
            }
            SharedPreferences sp = context.getSharedPreferences(seed.id, Context.MODE_PRIVATE);
            SpManager.setEncryptedPreference(sp, PREF_ADDRESSES, jar.toString());
        }
    }
    public static void updateAddress(Context context, Seeds.Seed seed, Address address) {
        List<Address> addresses=getAddresses(context,seed);
        boolean updateAddress=false;
        if(addresses.isEmpty()) {
            addresses.add(address);
            updateAddress=true;
        } else {
            for(Address add: addresses) {
                if(add.getAddress().equals(address.getAddress())) {
                    add.setValue(add.getValue()+address.getValue());
                    add.setAttached(address.isAttached());
                    add.setUsed(address.isUsed());
                    add.setLastMilestone(address.getLastMilestone());
                    add.setPigInt(address.getPigInt());
                    updateAddress=true;
                }
            }
        }
        if(updateAddress) {
            //Log.e("Store","store - UPDATING Address");
            JSONArray jar = new JSONArray();
            for (Address add : addresses) {
                jar.put(add.toJson());
            }
            SharedPreferences sp = context.getSharedPreferences(seed.id, Context.MODE_PRIVATE);
            SpManager.setEncryptedPreference(sp, PREF_ADDRESSES, jar.toString());
        }
        if(store.currentSeed.id.equals(seed.id)) {
            reinit(context);
        }
    }
    private static void loadAddresses(Context context) {
        if(store.currentSeed!=null) {
            SharedPreferences sp = context.getSharedPreferences(store.currentSeed.id, Context.MODE_PRIVATE);
            String jarrayString = SpManager.getEncryptedPreference(sp, PREF_ADDRESSES, "[]");
            //Log.e("LOAD-ADDRESS",jarrayString);
            store.addresses.clear();
            try {
                JSONArray jar = new JSONArray(jarrayString);
                for (int i = 0; i < jar.length(); i++) {
                    JSONObject job = jar.getJSONObject(i);
                    Address add = new Address(job);
                    store.addresses.add(add);
                }
            } catch (Exception e) {
            }
        }
    }
    public static List<Address> getAddresses(Context context, Seeds.Seed seed) {
        List<Address> addresses=new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(seed.id, Context.MODE_PRIVATE);
        String jarrayString = SpManager.getEncryptedPreference(sp, PREF_ADDRESSES, "[]");
        //Log.e("TMP-LOAD-ADDRESS",jarrayString);
        try {
            JSONArray jar = new JSONArray(jarrayString);
            for (int i = 0; i < jar.length(); i++) {
                JSONObject job = jar.getJSONObject(i);
                Address add = new Address(job);
                addresses.add(add);
            }
        } catch (Exception e) {
        }
        return addresses;
    }
    public static void addUpdateWallet(Context context, Seeds.Seed seed, Wallet wallet) {
        SharedPreferences sp = context.getSharedPreferences(seed.id, Context.MODE_PRIVATE);
        SpManager.setEncryptedPreference(sp, PREF_WALLET, wallet.toJson().toString());
    }
    private static void loadWallets(Context context) {
        if (store.seeds != null) {
            store.allwallets.clear();
            for (Seeds.Seed seed : store.seeds.getSeeds()) {
                Wallet wallet = getWallet(context, seed);
                if (wallet != null) {
                    store.allwallets.add(wallet);
                }
            }
        }
    }

    @Nullable
    public static Wallet getWallet(Seeds.Seed seed) {
        for(Wallet wallet: store.allwallets) {
            if(wallet.getSeedId().equals(seed.id))
                return wallet;
        }
        return null;
    }
    @Nullable
    public static Wallet getWallet(Context context, Seeds.Seed seed) {
        SharedPreferences sp = context.getSharedPreferences(seed.id, Context.MODE_PRIVATE);
        String jobString = SpManager.getEncryptedPreference(sp, PREF_WALLET, "[]");
        try {
            JSONObject job = new JSONObject(jobString);
            if(job!=null) {
                return new Wallet(job);
            }
        } catch (Exception e) {
        }
        return null;
    }
    private static void loadWallet(Context context) {
        if(store.currentSeed!=null) {
            SharedPreferences sp = context.getSharedPreferences(store.currentSeed.id, Context.MODE_PRIVATE);
            String jobString = SpManager.getEncryptedPreference(sp, PREF_WALLET, "[]");
            store.currentWallet=null;
            try {
                JSONObject job = new JSONObject(jobString);
                if(job!=null) {
                    store.currentWallet=new Wallet(job);
                }
            } catch (Exception e) {
            }
        }
    }

    public static void updateAddressFromSecurity(Context context, AddressSecurityChangeRequest request, GetNewAddressResponse response) {
        List<Address> alreadyAddress = getAddresses(context,request.getSeed());
        Address oldAddress = request.getAddress();
        String newAddressString=null;

        try {
            //Log.e("STORE"," addressSize: "+response.getAddresses().get(0).length()+" - ");
            newAddressString=response.getAddresses().get(0);
        } catch (Exception e) {
            //Log.e("STORE","bad: addressSize: ");
        }
        if(newAddressString!=null) {
            //Log.e("STORE","NEW Address is: "+newAddressString);
            for(Address address: alreadyAddress) {
                if(address.getAddress().equals(oldAddress.getAddress())) {
                    //Log.e("STORE","Good upding address: "+newAddressString);
                    address.setAddress(newAddressString);
                    address.setSecurity(request.getSecurity());
                }
            }
            JSONArray jar = new JSONArray();
            for (Address add : alreadyAddress) {
                jar.put(add.toJson());
            }
            //Log.e("ADDRESSES",jar.toString());
            SharedPreferences sp = context.getSharedPreferences(request.getSeed().id, Context.MODE_PRIVATE);
            SpManager.setEncryptedPreference(sp, PREF_ADDRESSES, jar.toString());
            if(store.currentSeed.id.equals(request.getSeed().id)) {
                loadAddresses(context);
            }

            AppService.refreshEvent();
        }

    }
    public static void addAddress(Context context, GetNewAddressRequest request, GetNewAddressResponse response) {
        if(request!=null && response!=null) {

            SharedPreferences sp = context.getSharedPreferences(request.getSeed().id, Context.MODE_PRIVATE);
            List<Address> alreadyAddress = getAddresses(context,request.getSeed());
            JSONArray jar = new JSONArray();
            for(Address already: alreadyAddress) {
                jar.put(already.toJson());
            }

            for(String add: response.getAddresses()) {
                Address address = new Address(add,false,false);
                address.setIndex(request.getIndex());
                address.setIndexName(request.getIndex()+1);
                jar.put(address.toJson());
                //Log.e("ADD-SAVE-ADDRESS","NO ACTION:   "+address.getAddress().length()+" -- "+address.toJson().toString());
            }
            //Log.e("ADD-SAVE-ADDRESS","addresses:   "+jar.toString());

            SpManager.setEncryptedPreference(sp, PREF_ADDRESSES, jar.toString());
            if(store.currentSeed.id.equals(request.getSeed().id)) {
                loadAddresses(context);
            }
        }
    }


    public static List<Transfer> addTransfers(Context context, Seeds.Seed seed, List<Transfer> transfers) {
        List<Transfer> ntransfers = new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(seed.id, Context.MODE_PRIVATE);
        String jarrayString = SpManager.getEncryptedPreference(sp, PREF_TRANSFERS, "[]");

        try {
            JSONArray jar = new JSONArray(jarrayString);
            for (int i = 0; i < jar.length(); i++) {
                JSONObject job = jar.getJSONObject(i);
                Transfer add = new Transfer(job);
                ntransfers.add(add);
            }
        } catch (Exception e) {
        }
        Collections.reverse(transfers);
        for(Transfer tran: transfers) {
            ntransfers.add(0,tran);
        }
        JSONArray trans = new JSONArray();
        for (Transfer t : ntransfers) {
            trans.put(t.toJson());
        }
        SpManager.setEncryptedPreference(sp, PREF_TRANSFERS, trans.toString());
        return transfers;
    }
    public static List<Transfer> getTransfers(Context context, Seeds.Seed seed) {
        List<Transfer> transfers = new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(seed.id, Context.MODE_PRIVATE);
        String jarrayString = SpManager.getEncryptedPreference(sp, PREF_TRANSFERS, "[]");

        try {
            JSONArray jar = new JSONArray(jarrayString);
            for (int i = 0; i < jar.length(); i++) {
                JSONObject job = jar.getJSONObject(i);
                Transfer add = new Transfer(job);
                transfers.add(add);
            }
        } catch (Exception e) {
        }
        return transfers;
    }
    private static void loadTransfers(Context context) {
        if(store.currentSeed!=null) {
            SharedPreferences sp = context.getSharedPreferences(store.currentSeed.id, Context.MODE_PRIVATE);
            String jarrayString = SpManager.getEncryptedPreference(sp, PREF_TRANSFERS, "[]");
            //String jarrayString = sp.getString(PREF_SEEDS,"[]");
            store.transfers.clear();
            try {
                JSONArray jar = new JSONArray(jarrayString);
                //Log.e("LOAD-TRANSFERS","json: "+jar.toString());
                for (int i = 0; i < jar.length(); i++) {
                    JSONObject job = jar.getJSONObject(i);
                    Transfer add = new Transfer(job);
                    store.transfers.add(add);
                }
            } catch (Exception e) {
            }
        }
    }
    public static void loadNudgeTransfers(Context context) {

        SharedPreferences sp = context.getSharedPreferences(NT_STORE, Context.MODE_PRIVATE);
        String jarrayString = SpManager.getEncryptedPreference(sp, PREF_NUDGE_TRANSFERS, "[]");
        try {
            JSONArray jar = new JSONArray(jarrayString);
            store.nudgeTransfers.clear();
            //Log.e("LOAD-NUDGE-TRANSFERS","json: "+jar.toString());
            for (int i = 0; i < jar.length(); i++) {
                JSONObject job = jar.getJSONObject(i);
                NudgeTransfer add = new NudgeTransfer(job);
                store.nudgeTransfers.add(add);
            }
        } catch (Exception e) {
            Log.e("ERR-S-01","ex: "+e.getMessage());
        }

    }
    public synchronized static void removeNudgeTransfer(Context context, NudgeTransfer transfer) {
        int remove=-1;
        //Log.e("REM-NT",store.nudgeTransfers.size()+"");
        for(int i=0; i<store.nudgeTransfers.size(); i++) {
            NudgeTransfer nt= store.nudgeTransfers.get(i);
            if(nt.transfer.getHash().equals(transfer.transfer.getHash())) {
                //Log.e("REM-NT",store.nudgeTransfers.size()+"--"+nt.transfer.getHash()+"--"+transfer.transfer.getHash());
                remove = i;
            }
        }
        store.nudgeTransfers.remove(remove);
        saveNudgeTransfers(context);

        //Log.e("REM-NT",store.nudgeTransfers.size()+"");
    }
    public static void addIfNoNudgeTransfer(Context context,Seeds.Seed seed, Transfer transfer) {
        boolean has=false;
        if(store.nudgeTransfers.isEmpty())
            loadNudgeTransfers(context);
        for(NudgeTransfer nt: store.nudgeTransfers) {
            if(nt.transfer.getHash().equals(transfer.getHash())) {
                has = true;
            }
        }
        if(!has) {
            store.nudgeTransfers.add(new NudgeTransfer(seed,transfer));
            saveNudgeTransfers(context);
        }

    }
    public static final List<NudgeTransfer> getNudgeTransfers() {
        return store.nudgeTransfers;
    }
    public static synchronized void saveNudgeTransfers(Context context) {

        SharedPreferences sp = context.getSharedPreferences(NT_STORE, Context.MODE_PRIVATE);

        JSONArray trans = new JSONArray();
        for (NudgeTransfer t : store.nudgeTransfers) {
            trans.put(t.toJson());
        }
        SpManager.setEncryptedPreference(sp, PREF_NUDGE_TRANSFERS, trans.toString());

    }
}
