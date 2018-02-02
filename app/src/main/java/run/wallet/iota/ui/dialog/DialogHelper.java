package run.wallet.iota.ui.dialog;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;

import android.util.Log;
import android.widget.Adapter;
import android.widget.ArrayAdapter;

import run.wallet.R;
import run.wallet.iota.model.Nodes;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.adapter.NodesListAdapter;
import run.wallet.iota.ui.fragment.NetworkNodesFragment;

/**
 * Created by coops on 17/01/18.
 */

public class DialogHelper {

    public static void showAddressSecurityDialog(Activity activity, DialogInterface.OnDismissListener dismiss) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(activity);
        builderSingle.setIcon(R.drawable.tran_primary_dark);
        builderSingle.setTitle(activity.getString(R.string.settings_address_security));
        //builderSingle.setMessage(activity.getString(R.string.settings_address_security_summary));


        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(activity, android.R.layout.select_dialog_item);
        arrayAdapter.add(activity.getString(R.string.settings_password_protection_title)+" 1");
        arrayAdapter.add(activity.getString(R.string.settings_password_protection_title)+" 2");
        arrayAdapter.add(activity.getString(R.string.settings_password_protection_title)+" 3");

        builderSingle.setOnDismissListener(dismiss);
        builderSingle.setNegativeButton(activity.getString(R.string.buttons_cancel),new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick (DialogInterface dialog,int which){
                dialog.dismiss();
            }
        });
        builderSingle.setNeutralButton(activity.getString(R.string.address_security_default_is)+" 2", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick (DialogInterface dialog,int which){
                    //fragment.onResume();
                    if(Store.getAddressSecurityDefault()!=2)
                        Store.setAddressSecurity(activity,2);

                }
            });
        builderSingle.setAdapter(arrayAdapter,new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick (DialogInterface dialog,int which){
                //fragment.onResume();
                Store.setAddressSecurity(activity,which++);
            }
        });
        builderSingle.show();


    }
    public static void showNodeListItemDialog(Activity activity, final NodesListAdapter adapter, int position,DialogInterface.OnDismissListener dismiss) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(activity);
        builderSingle.setIcon(R.drawable.menu_neighbors);
        builderSingle.setTitle(activity.getString(R.string.menu_node_title));
        //builderSingle.

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(activity, android.R.layout.select_dialog_item);
        arrayAdapter.add(activity.getString(R.string.menu_node_connect));
        arrayAdapter.add(activity.getString(R.string.menu_node_move_top));
        arrayAdapter.add(activity.getString(R.string.menu_node_move_one));
        arrayAdapter.add(activity.getString(R.string.menu_node_remove));

        builderSingle.setOnDismissListener(dismiss);
        builderSingle.setNegativeButton(activity.getString(R.string.buttons_cancel),new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick (DialogInterface dialog,int which){
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter,new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick (DialogInterface dialog,int which){

                switch(which) {
                    case 0:
                        Store.changeNode(adapter.getItem(position));
                        AppService.getNodeInfo(activity,true);
                        break;
                    case 1:
                        Store.MoveUpNode(activity,position,0);
                        break;
                    case 2:
                        if(position>0)
                            Store.MoveUpNode(activity,position,position-1);
                        break;
                    case 3:
                        if(Store.getNodes().size()<2) {
                            Snackbar.make(activity.findViewById(R.id.drawer_layout), activity.getString(R.string.menu_node_no_remove), Snackbar.LENGTH_LONG).show();
                        } else {
                            Store.removeNode(activity, adapter.getItem(position));
                        }
                        break;
                }
                dismiss.onDismiss(dialog);
                //fragment.onResume();
            }
        });
        builderSingle.show();


    }

}
