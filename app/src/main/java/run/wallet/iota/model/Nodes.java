package run.wallet.iota.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import jota.RunIotaAPI;
import jota.dto.response.GetNodeInfoResponse;
import run.wallet.common.Sf;
import run.wallet.common.json.JSONArray;
import run.wallet.common.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import run.wallet.iota.api.responses.NodeInfoResponse;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.JSONUrlReader;
import run.wallet.iota.helper.Num;


/**
 * Created by coops on 16/12/17.
 */

public class Nodes {
    private static List<Node> othernodes = new ArrayList<>();
    //private static final List<Node> appnodes =new ArrayList<>();
    private static final List<Node> usenodes =new ArrayList<>();

    private boolean alreadyinit=false;

    private static final String PREF_NODES = "list";
    private static final String PREF_USE_NODEs = "use";

    protected static final String SP_NODES="nodes";

    private static final String J_IP="ip";
    private static final String J_PROTO="prot";
    private static final String J_PORT="port";
    //private static final String J_ISAPP="isa";
    private static final String J_ISPREF="isp";
    private static final String J_NAME="name";
    private static final String J_LAST="last";
    private static final String J_SYNC="sync";
    private static final String J_Dead="dead";

    private static int random;
    private static final List<Node> defaultnodes=new ArrayList<>();
/*
    static {
        Node n1 = new Node();
        n1.port=14265;
        n1.name="node1";
        n1.ip="node.iotawallet.info";
        n1.protocol="http";
        n1.isappnode=true;
        defaultnodes.add(n1);
        Node n2 = new Node();
        n2.port=14265;
        n2.name="node2";
        n2.ip="node.iotawallet.info";
        n2.protocol="http";
        n2.isappnode=true;
        defaultnodes.add(n2);
        Node n3 = new Node();
        n3.port=14265;
        n3.name="node3";
        n3.ip="node.iotawallet.info";
        n3.protocol="http";
        n3.isappnode=true;
        defaultnodes.add(n3);
        Node n4 = new Node();
        n4.port=14265;
        n4.name="node4";
        n4.ip="node.iotawallet.info";
        n4.protocol="http";
        n4.isappnode=true;
        defaultnodes.add(n4);
    }
    */

    static {
        Node n1 = new Node();
        n1.port=14265;
        n1.ip="riota0.runplay.com";
        n1.protocol="http";
        n1.isappnode=true;
        defaultnodes.add(n1);
        Node n2 = new Node();
        n2.port=14265;
        n2.ip="riota1.runplay.com";
        n2.protocol="http";
        n2.isappnode=true;
        defaultnodes.add(n2);
        Node n3 = new Node();
        n3.port=14265;
        n3.ip="riota2.runplay.com";
        n3.protocol="http";
        n3.isappnode=true;
        defaultnodes.add(n3);
        Node n4 = new Node();
        n4.port=14265;
        n4.ip="riota3.runplay.com";
        n4.protocol="http";
        n4.isappnode=true;
        defaultnodes.add(n4);
    }

    protected void moveUp(Context context, int currindex, int toindex) {
        //Log.e("MOVE-UP-NODE","f: "+currindex+", to: "+toindex);
        if(currindex>0 && currindex>toindex && currindex<usenodes.size()) {
            Node modenode = usenodes.get(currindex);
            usenodes.remove(currindex);
            usenodes.add(toindex,modenode);
            save(context);
        }
    }
    protected void removeNode(Context context, Node removeNode) {
        if(removeNode!=null) {
            usenodes.remove(removeNode);
            save(context);
        }
    }

    private static final int timeout=120000;
    protected Nodes.Node getNode() {
        long time = System.currentTimeMillis();
        for(Node node: usenodes) {
            if(node.syncVal<2 || (node.lastused<time-timeout))
                return node;
        }

        return usenodes.get(0);
    }
    protected Nodes(Context context) {
        othernodes.clear();

        load(context);
    }
    private Node getRandomDefault() {
        random= Num.getRandom(0,defaultnodes.size()-1);
        return defaultnodes.get(random);
    }
    private void load(Context context) {

        usenodes.add(getRandomDefault());

        //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sp = context.getSharedPreferences(SP_NODES,Context.MODE_PRIVATE);

        String uarrayString=sp.getString(PREF_USE_NODEs,"[]");
        //String jarrayString = sp.getString(PREF_SEEDS,"[]");
        try {
            JSONArray jar = new JSONArray(uarrayString);
            if(jar.length()!=0) {
                usenodes.clear();
                for (int i = 0; i < jar.length(); i++) {
                    JSONObject job = jar.getJSONObject(i);
                    Node node = new Node();
                    node.ip = job.optString(J_IP);
                    node.port = job.optInt(J_PORT);
                    node.syncVal = job.optInt(J_SYNC);
                    node.protocol = job.optString(J_PROTO);
                    node.name = job.optString(J_NAME);
                    node.lastused = job.optLong(J_LAST);
                    node.deadcount=job.optInt(J_Dead);
                    usenodes.add(node);
                }
            } else {
                //Log.e("NODES","init()");
                if(!alreadyinit) {
                    init(context);
                }
            }
        } catch (Exception e) {}
        //Log.e("REFRESH-NODE","nodes size: "+usenodes.size());
    }
    private void loadOthers(Context context) {
        //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sp = context.getSharedPreferences(SP_NODES,Context.MODE_PRIVATE);
        othernodes.clear();
        String uarrayString=sp.getString(PREF_NODES,"[]");
        //String jarrayString = sp.getString(PREF_SEEDS,"[]");
        try {
            JSONArray jar = new JSONArray(uarrayString);
            if(jar.length()!=0) {
                for (int i = 0; i < jar.length(); i++) {
                    JSONObject job = jar.getJSONObject(i);
                    Node node = new Node();
                    node.ip = job.optString(J_IP);
                    node.port = job.optInt(J_PORT);
                    node.syncVal = job.optInt(J_SYNC);
                    node.protocol = job.optString(J_PROTO);
                    node.name = job.optString(J_NAME);
                    node.lastused = job.optLong(J_LAST);
                    node.deadcount=job.optInt(J_Dead);
                    othernodes.add(node);
                }
            }
        } catch (Exception e) {}
        //Log.e("REFRESH-OTHER-NODE","nodes size: "+othernodes.size());
    }
    protected void saveDownloadNodes(Context context, List<Node> nodes) {
        //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sp = context.getSharedPreferences(SP_NODES,Context.MODE_PRIVATE);
        JSONArray jar = new JSONArray();
        try {
            for(Node node: nodes) {
                JSONObject job = new JSONObject();
                job.put(J_IP,node.ip);
                job.put(J_PORT,node.port);
                job.put(J_SYNC,node.syncVal);
                job.put(J_PROTO,node.protocol);
                job.put(J_NAME,node.name);
                job.put(J_LAST,node.lastused);
                job.put(J_Dead,node.deadcount);
                jar.put(job);
            }
        } catch (Exception e) {}
        if(jar.length()>0) {
            sp.edit().putString(PREF_NODES,jar.toString()).commit();
        }
    }
    protected List<Node> getOtherNodes(Context context) {
        if(othernodes.isEmpty())
            loadOthers(context);
        return othernodes;
    }

    protected final List<Node> getNodes() {

        return usenodes;
    }
    protected final int size() {
        return usenodes.size();
    }
    protected void addNode(Context context, String ip,int port,String protocol) {
            Node node = new Node();
            node.ip=ip;
            node.protocol=protocol;
            node.port=port;
            usenodes.add(node);
            save(context);
    }

    protected void save(Context context) {
        //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sp = context.getSharedPreferences(SP_NODES,Context.MODE_PRIVATE);
        JSONArray jar = new JSONArray();
        try {
            for(Node node: usenodes) {
                JSONObject job = new JSONObject();
                job.put(J_IP,node.ip);
                job.put(J_PORT,node.port);
                job.put(J_SYNC,node.syncVal);
                job.put(J_PROTO,node.protocol);
                job.put(J_NAME,node.name);
                job.put(J_LAST,node.lastused);
                job.put(J_Dead,node.deadcount);
                jar.put(job);
            }
        } catch (Exception e) {}
        if(jar.length()>0) {
            sp.edit().putString(PREF_USE_NODEs,jar.toString()).commit();
        }
    }
    protected void update(Context context, Node updatenode) {
        //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sp = context.getSharedPreferences(SP_NODES,Context.MODE_PRIVATE);
        JSONArray jar = new JSONArray();
        try {
            for(Node usenode: usenodes) {
                Node node=usenode;
                if(node.ip.equals(updatenode.ip) && node.port==updatenode.port) {
                    node=updatenode;
                }
                JSONObject job = new JSONObject();
                job.put(J_IP,node.ip);
                job.put(J_PORT,node.port);
                job.put(J_SYNC,node.syncVal);
                job.put(J_PROTO,node.protocol);
                job.put(J_NAME,node.name);
                job.put(J_LAST,node.lastused);
                job.put(J_Dead,node.deadcount);
                jar.put(job);
            }
        } catch (Exception e) {}
        if(jar.length()>0) {
            sp.edit().putString(PREF_USE_NODEs,jar.toString()).commit();
            load(context);
        }
    }
    public static class Node {
        public String ip="";
        public int port;
        public String name="";
        public String protocol="";
        public boolean isappnode;
        public long syncVal;
        public long lastused;
        public int deadcount;
        public String getName() {
            if(ip!=null && (ip.contains(".runplay.com"))) {
                return "iotanode.runplay.com";
            }
            return ip;
        }
        public Node() {

        }
    }






    private void init(Context context) {

        if(goOther.getStatus()== AsyncTask.Status.PENDING) {
            goOther.setContext(context);
            goOther.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
        }

    }

    private GoOtherNodes goOther = new GoOtherNodes();
    private ChooseOtherNodes goChoose;
    private class GoOtherNodes extends AsyncTask<Boolean, Boolean, Boolean> {
        private Context context;
        protected void setContext(Context context) {
            this.context=context;
        }
        @Override
        protected Boolean doInBackground(Boolean... booleans) {
            alreadyinit=true;
            JSONArray jar = JSONUrlReader.readJsonArrayFromUrl(context, Constants.WWW_RUN_IOTA+"/node_table.json");
            //Log.e("LOAD-NODE",Constants.WWW_RUN_IOTA+"/node_table.json");
            if(jar!=null) {
                List<Nodes.Node> nodes=new ArrayList<>();
                for(int i=0; i<jar.length(); i++) {
                    JSONObject job = jar.optJSONObject(i);
                    try {
                        Nodes.Node node = new Nodes.Node();
                        String address = job.getString("host");
                        String usehttp="http";
                        if(address.contains("https"))
                            usehttp="https";
                        address = address.replace("http:", "").replace("https:", "")
                                .replaceAll("/","").replaceAll("\\/", "");

                        node.ip = address.split(":")[0];
                        node.protocol = usehttp;
                        node.port = Sf.toInt(address.split(":")[1]);
                        nodes.add(node);


                    } catch (Exception e) {}
                }
                if(!nodes.isEmpty()) {
                    saveDownloadNodes(context,nodes);
                    othernodes=nodes;
                }

            }
            load(context);
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            goChoose = new ChooseOtherNodes();
            goChoose.setContext(context);
            goChoose.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);

        }
    };
    private class ChooseOtherNodes extends AsyncTask<Boolean, Boolean, Boolean> {
        private Context context;
        protected void setContext(Context context) {
            this.context=context;
        }
        @Override
        protected Boolean doInBackground(Boolean... booleans) {
            try {
                int start = Num.getRandom(0, othernodes.size() - 16);
                if (start < 0)
                    start = 0;
                List<Node> tp10 = othernodes.subList(start, start + 15);
                List<Node> checkedOk = new ArrayList<>();
                tp10.add(0, getRandomDefault());
                Collections.reverse(tp10);
                for (Node node : tp10) {

                    GetNodeInfoResponse res = null;

                    try {
                        RunIotaAPI api = new RunIotaAPI.Builder().protocol(node.protocol).host(node.ip).port(((Integer) node.port).toString()).build();
                        res = api.getNodeInfo();
                    } catch (Exception e) {
                    }

                    if (res != null) {
                        NodeInfoResponse response = new NodeInfoResponse(res);
                        //Log.e("NODES", node.ip+":"+response.isSyncOk());
                        if (response.isSyncOk()) {
                            checkedOk.add(node);
                            if (checkedOk.size() > 3)
                                break;
                        }
                    }
                }

                usenodes.clear();
                usenodes.add(getRandomDefault());
                usenodes.addAll(checkedOk);
                save(context);
                //Log.e("NODES","Finished load and test, othernodes size: "+usenodes.size());
                load(context);
            } catch(Exception e){}
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result) {

        }
    };
}
