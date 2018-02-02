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

package run.wallet.iota.ui.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import run.wallet.R;
import run.wallet.iota.ui.fragment.NetworkNeighborsFragment;
import run.wallet.iota.ui.fragment.NetworkNodeInfoFragment;
import run.wallet.iota.ui.fragment.NetworkNodesFragment;


public class NetworkPagerAdapter extends FragmentPagerAdapter {
    //private static final int TAB_COUNT = 3;

    private final Context context;
    private List<Fragment> fragments = new ArrayList<>();

    public NetworkPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        fragments.add(new NetworkNodeInfoFragment());
        fragments.add(new NetworkNeighborsFragment());
        fragments.add(new NetworkNodesFragment());
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getResources().getString(R.string.fragment_node_info_title);
            case 1:
                return context.getResources().getString(R.string.fragment_neighbors_title);
            case 2:
                return context.getResources().getString(R.string.menu_all_nodes);
        }
        return null;
    }
}
