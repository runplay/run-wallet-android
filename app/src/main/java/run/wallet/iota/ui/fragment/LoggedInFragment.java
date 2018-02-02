package run.wallet.iota.ui.fragment;

import android.app.Fragment;

import run.wallet.iota.model.Store;

/**
 * Created by coops on 11/01/18.
 */

public class LoggedInFragment extends Fragment {

    @Override
    public void onResume() {
        super.onResume();
        if(!Store.isLoggedIn())
            getActivity().recreate();
    }
}
