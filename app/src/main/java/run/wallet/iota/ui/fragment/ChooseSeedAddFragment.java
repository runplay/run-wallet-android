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

package run.wallet.iota.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import jota.utils.SeedRandomGenerator;
import run.wallet.R;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.SeedValidator;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.dialog.CopySeedDialog;

public class ChooseSeedAddFragment extends Fragment {

    private static final String SEED = "seed";
    @BindView(R.id.add_seed_toolbar)
    Toolbar addSeedToolbar;

    @BindView(R.id.seed_login_seed_text_input_layout)
    TextInputLayout seedEditTextLayout;

    @BindView(R.id.seed_login_seed_input)
    TextInputEditText seedEditText;

    private String generatedSeed;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_add, container, false);
        unbinder = ButterKnife.bind(this, view);

        //UiManager.setActionBarBackOnly(getActivity(),getString(R.string.seed_add),null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity) getActivity()).setSupportActionBar(addSeedToolbar);
        setHasOptionsMenu(false);
        addSeedToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.md_nav_back));
        addSeedToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("cek", "home selected");
                getActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        UiManager.setActionBarBackOnly(getActivity(),getString(R.string.seed_add),null);
        generatedSeed=null;
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    @OnClick(R.id.seed_add_button)
    public void onSeedLoginClick() {
        addSeedToStore();
    }

    @OnClick(R.id.seed_add_generate_seed)
    public void onSeedLoginGenerateSeedClick() {
        generatedSeed = SeedRandomGenerator.generateNewSeed();
        seedEditText.setText(generatedSeed);
        Bundle bundle = new Bundle();
        bundle.putString("generatedSeed", generatedSeed);
        CopySeedDialog dialog = new CopySeedDialog();
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), null);
    }

    @OnEditorAction(R.id.seed_login_seed_input)
    public boolean onSeedLoginSeedInputEditorAction(int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE)
                || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            addSeedToStore();
        }
        return true;
    }

    private void addSeedToStore() {
        if(Store.getSeedList().size()>= Constants.WALLET_MAX_ALLOW) {
            Snackbar.make(getView(), R.string.max_seeds, Snackbar.LENGTH_LONG).show();
            return;
        }
        String seed = SeedValidator.getSeed(seedEditText.getText().toString());


        if (seed.isEmpty()) {
            seedEditTextLayout.setError(getString(R.string.messages_empty_seed));
            if (seedEditTextLayout.getError() != null)
                return;
        }
        String badSeed=SeedValidator.isSeedValid(getActivity(),seed);
        if(badSeed!=null) {
            Snackbar.make(getView(),badSeed, Snackbar.LENGTH_LONG).show();
            return;
        }
        List<Seeds.Seed> seeds=Store.getSeedList();
        for(Seeds.Seed tmp: seeds) {
            if(seed.equals(String.valueOf(tmp.value))) {
                Snackbar.make(getView(), R.string.message_seed_duplicate, Snackbar.LENGTH_LONG).show();
                return;

            }
        }

        addSeed(seed);


        getActivity().onBackPressed();
        //UiManager.openFragment(getActivity(),ChooseSeedFragment.class);
    }

    private void addSeed(String seed) {
        boolean isgen = false;
        if(generatedSeed!=null && generatedSeed.equals(seed))
            isgen=true;
        //Log.e("adding","seed: "+isgen);
        Store.addSeed(getActivity(),seed.toCharArray(),getSeedName(),false,isgen);
    }

    private String getSeedName() {
        return getString(R.string.wallet)+"-"+Store.getSeedList().size();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SEED, seedEditText.getText().toString());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            seedEditText.setText(savedInstanceState.getString(SEED));
        }
    }
}
