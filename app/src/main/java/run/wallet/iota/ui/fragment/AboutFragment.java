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
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import run.wallet.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.psdev.licensesdialog.LicensesDialog;
import run.wallet.common.ActivityMan;
import run.wallet.iota.IOTA;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.QRCode;
import run.wallet.iota.model.Store;

public class AboutFragment extends Fragment {

    private static final String PACKAGE_WEBVIEW = "com.google.android.webview";

    //@BindView(R.id.about_toolbar)
    //Toolbar aboutToolbar;
    @BindView(R.id.about_version)
    TextView versionTextView;
    @BindView(R.id.about_donation_iota)
    TextView iotaNow;
    @BindView(R.id.about_donation_iota_address)
    TextView iotaAddress;


    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    private void initAppVersion() {
        try {
            String versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            versionTextView.setText(getString(R.string.about_version, versionName));
        } catch (PackageManager.NameNotFoundException e) {
            versionTextView.setText(R.string.about_version_unknown);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //((AppCompatActivity) getActivity()).setSupportActionBar(aboutToolbar);
        initAppVersion();
        iotaAddress.setText(Constants.DONATION_ADDRESS);
    }
    @Override
    public void onResume() {
        super.onResume();
        Store.setCurrentFragment(this.getClass());
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    private void openPlayStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + AboutFragment.PACKAGE_WEBVIEW)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + AboutFragment.PACKAGE_WEBVIEW)));
        }
    }

    @OnClick(R.id.about_terms)
    public void onAboutTermsClick() {
        ActivityMan.openAndroidBrowserUrl(getActivity(),Constants.WWW_RUN_PLAY+"/terms");
    }
    @OnClick(R.id.about_privacy)
    public void onAboutPrivacyClick() {
        ActivityMan.openAndroidBrowserUrl(getActivity(),Constants.WWW_RUN_PLAY+"/privacy");
    }
    @OnClick(R.id.about_donation_iota)
    public void onAboutDonationIotaClick() {
        if (Store.getCurrentWallet() != null) {

            QRCode qrCode = new QRCode();
            qrCode.setAddress(Constants.DONATION_ADDRESS);
            qrCode.setTag(Constants.DONATION_TAG);

            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.QRCODE, qrCode);

            Intent pickContactIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("iota://send"));
            pickContactIntent.putExtra(Constants.QRCODE,qrCode);

            getActivity().setResult(Constants.DONATE_NOW,pickContactIntent);

            getActivity().finish();
        } else
            onAboutDonationIotaAddressClick();
    }
    @OnClick(R.id.about_donation_iota_address)
    public void onAboutDonationIotaAddressClick() {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getActivity().getString(R.string.seed), Constants.DONATION_ADDRESS);
        clipboard.setPrimaryClip(clip);
        Snackbar.make(getView(), getString(R.string.messages_iota_donation), Snackbar.LENGTH_LONG).show();
    }
    @OnClick(R.id.about_licenses)
    public void onAboutLicensesClick() {
        try {
            new LicensesDialog.Builder(getActivity())
                    .setNotices(R.raw.licenses)
                    .setTitle(R.string.about_licenses)
                    .setIncludeOwnLicense(true)
                    .setCloseText(R.string.buttons_ok)
                    .build()
                    .showAppCompat();
        } catch (AndroidRuntimeException e) {
            View contentView = getActivity().getWindow().getDecorView();
            Snackbar snackbar = Snackbar.make(contentView,
                    R.string.message_open_licenses_error, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.message_install_web_view, v -> openPlayStore());
            snackbar.show();
        }
    }

}
