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
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;

import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jota.utils.IotaToText;
import run.wallet.common.B;
import run.wallet.common.Currency;
import run.wallet.R;

import run.wallet.iota.api.responses.WebGetExchangeRatesHistoryResponse;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Tick;
import run.wallet.iota.model.Ticker;
import run.wallet.iota.model.TickerHist;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.adapter.ChooseSeedAdapter;

public class ChooseSeedFragment extends Fragment implements WalletTabFragment.OnFabClickListener {

    private ChooseSeedAdapter adapter;

    @BindView(R.id.choose_seed_toolbar)
    Toolbar chooseSeedToolbar;
    @BindView(R.id.wallet_addresses_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fab_seed)
    FloatingActionButton fabSeed;
    @BindView(R.id.default_seed_info_chart)
    LineChart chart;

    @BindView(R.id.default_seed_click)
    LinearLayout defaultClick;

    @BindView(R.id.default_seed_name)
    TextView seedName;
    @BindView(R.id.default_seed_value)
    TextView seedValue;
    @BindView(R.id.default_seed_currency)
    TextView seedCurrency;

    @BindView(R.id.xchange_last)
    TextView xchangeLast;
    @BindView(R.id.xchange_high)
    TextView xchangeHigh;
    @BindView(R.id.xchange_low)
    TextView xchangeLow;

    @BindView(R.id.xchange_5m)
    TextView xchange5m;
    @BindView(R.id.xchange_15m)
    TextView xchange15m;
    @BindView(R.id.xchange_30m)
    TextView xchange30m;
    @BindView(R.id.xchange_1h)
    TextView xchange1hr;
    @BindView(R.id.xchange_2h)
    TextView xchange2hr;


    @BindView(R.id.xchange_list_currencies)
    LinearLayout xchangeCurrencyList;

    private TickerHist history;
    //private List<Seeds.Seed> seedlist;
    //private Seeds seeds;

    private static int currentStep=3;
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_seed, container, false);
        unbinder = ButterKnife.bind(this, view);
        //swipeRefreshLayout = view.findViewById(R.id.wallet_addresses_swipe_container);
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(chooseSeedToolbar);
        setHasOptionsMenu(true);
        setAdapter();
        fabSeed.setVisibility(View.VISIBLE);

        setupXchangeBtns();
        defaultClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Store.getCurrentWallet()!=null) {
                    UiManager.openFragment(getActivity(), WalletTabFragment.class);
                }
            }
        });
    }

    private void goRates() {
        goChart();
    }
    private void goChart() {
        goChart.removeCallbacks(goChartRun);
        goChart.postDelayed(goChartRun,50);
    }
    private Handler goChart = new Handler();
    private Runnable goChartRun = new Runnable() {
        @Override
        public void run() {
            history = Store.getTickerHist(getActivity(),Store.getDefaultCurrency(getActivity()).getCurrencyCode(),currentStep);
            if(history==null || history.getLastUpdate()<System.currentTimeMillis()-600000) {
                AppService.updateExchangeRatesHistory(getActivity(),Store.getDefaultCurrency(getActivity()).getCurrencyCode(),currentStep);
            }
            drawChart();
        }
    };
    @Subscribe
    public void onEvent(WebGetExchangeRatesHistoryResponse response) {
        drawChart();
    }
    private DecimalFormat df=new DecimalFormat("#,###,##0.00");
    private DecimalFormat dfs=new DecimalFormat("#,###,##0.0000");
    private void drawChart() {
        if(Store.getCurrentWallet()!=null) {
            seedName.setText(Store.getCurrentSeed().name);
            seedValue.setText(IotaToText.convertRawIotaAmountToDisplayText(Store.getCurrentWallet().getBalanceDisplay(), true));
            Currency cur = Store.getDefaultCurrency(getActivity());
            Ticker ticker = Store.getTicker("IOTA:" + cur.getCurrencyCode());
            seedCurrency.setText(ticker.getIotaValString(Store.getCurrentWallet().getBalanceDisplay()) + "\n" + cur.getSymbol());
            DecimalFormat udf = df;
            if (ticker.getLast() < 0.01)
                udf = dfs;
            xchangeLast.setText(udf.format(ticker.getLast()));
            xchangeHigh.setText(udf.format(ticker.getHigh()));
            xchangeLow.setText(udf.format(ticker.getLow()));
        }
            //Log.e("THIST","go 3");
        chart.setNoDataText(getString(R.string.messages_no_chart_data));
        chart.setNoDataTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        chart.setGridBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorLight));
        Paint p = chart.getPaint(Chart.PAINT_INFO);
        p.setColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        chart.getDescription().setEnabled(false);
        chart.setDragDecelerationFrictionCoef(0.95f);
        //chart.setExtraOffsets(15.f, 15.f, 15.f, 15.f);
        chart.setHighlightPerTapEnabled(false);
        chart.setEnabled(false);
        chart.setTouchEnabled(false);
        chart.animateX(1000);
        chart.setPadding(0, 0, 0, 0);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setEnabled(false);
//chart.setZ(0.5f);
        chart.zoomOut();
        chart.setPinchZoom(false);
        chart.setPinchZoom(false);

        chart.setFocusable(false);
        Legend leg = chart.getLegend();
        leg.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        leg.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        leg.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        leg.setDrawInside(false);
        leg.setEnabled(false);

        List<Entry> entries = new ArrayList<Entry>();
        List<Entry> entriesvol = new ArrayList<Entry>();
        //Log
        history = Store.getTickerHist(getActivity(), Store.getDefaultCurrency(getActivity()).getCurrencyCode(), currentStep);
        double avg = 0D;
        if (history != null) {
            //Log.e("THIST","go 4");
            List<Tick> tickers = history.getTicks();
            Collections.reverse(tickers);
            //double min=tickers.get(0).getLast();
            double max = tickers.get(0).getLast();
            double lastvol = 0;
            double maxvol = 0;
            double minvol = 0D;
            for (int i = 0; i < tickers.size(); i++) {
                double last = tickers.get(i).getLast();
                double tmpvol = tickers.get(i).getVol();
                if (avg == 0D) {
                    avg = last;
                } else {
                    avg = (avg + last) / 2;
                }
                if (last > max)
                    max = last;
                if (tmpvol > maxvol)
                    maxvol = tmpvol;
                if (minvol == 0)
                    minvol = tmpvol;
                else if (tmpvol < minvol)
                    minvol = tmpvol;


                // turn your data into Entry objects
                Entry e = new Entry(i * 10, Double.valueOf(last).floatValue());
                entries.add(e);

            }
            max = Math.abs(max);
            double voldivide = (100 / maxvol) / 70;
            for (int i = tickers.size() - 1; i >= 0; i--) {
                double tmpvol = tickers.get(i).getVol() * voldivide;

                //Log.e("YUP", "vol: "+tickers.get(i).getVol()+" = " + tmpvol);
                Entry ev = new Entry(i * 10, Double.valueOf(tmpvol + (0.015 * history.getStep())).floatValue());
                entriesvol.add(ev);
            }
            Collections.reverse(entriesvol);


            LineDataSet dataSet = new LineDataSet(entries, "IOTA:" + Store.getDefaultCurrency(getActivity()).getCurrencyCode()); // add entries to dataset
            //dataSet.setCircleRadius(1f);
            dataSet.setDrawCircleHole(false);
            dataSet.setDrawCircles(false);
            //.setCircleColor(B.getColor(getActivity(),R.color.colorLight));
            //dataSet.setCircleColorHole(B.getColor(getActivity(),R.color.colorLight));
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(getResources().getColor(R.color.colorPrimaryDarker));
            dataSet.setFillAlpha(100);
            dataSet.setColor(B.getColor(getActivity(), R.color.colorPrimary));


            LineDataSet idataSet = new LineDataSet(entriesvol, "Vol"); // add entries to dataset
            //idataSet.setCircleRadius(1f);
            //idataSet.setCircleColor(B.getColor(getActivity(),R.color.colorPrimaryDark));
            //idataSet.setCircleColorHole(B.getColor(getActivity(),R.color.colorPrimaryDark));
            idataSet.setDrawCircleHole(false);
            idataSet.setDrawCircles(false);
            idataSet.setDrawFilled(true);
            idataSet.setFillColor(getResources().getColor(R.color.colorMiddle));
            idataSet.setColor(B.getColor(getActivity(), R.color.colorPrimaryDark));
            idataSet.setFillAlpha(100);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(dataSet);
            dataSets.add(idataSet);

            LineData lineData = new LineData(dataSets);
            chart.setData(lineData);


            //lineData.addDataSet(idataSet);
            float gomin = Double.valueOf(0).floatValue();
            float gomax = +Double.valueOf(max * 1.4).floatValue();

            chart.getAxisLeft().setAxisMinimum(gomin);
            chart.getAxisLeft().setAxisMaximum(gomax);
            chart.getAxisRight().setAxisMinimum(gomin);
            chart.getAxisRight().setAxisMaximum(gomax);

        }

        chart.invalidate(); // refresh



    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    private void setAdapter() {

        //seedlist=Store.getSeedList();
        adapter = new ChooseSeedAdapter(getActivity());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        //tvEmpty.setVisibility(seedlist.size() == 0 ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.fab_seed)
    public void onFabSeedClick() {
        if(Store.getSeedList().size()>= Constants.WALLET_MAX_ALLOW) {
            Snackbar.make(getView(), R.string.max_seeds, Snackbar.LENGTH_LONG).show();
        } else {
            UiManager.openFragmentBackStack(getActivity(),ChooseSeedAddFragment.class);
        }

    }
    @Override
    public void onFabClick() {
        //generateNewAddress();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
    @Override
    public void onResume() {
        super.onResume();
        Store.setCurrentFragment(this.getClass());
        EventBus.getDefault().register(this);
        //Log.e("CSRES","RESUME CHOOSE SEED");
        goChart();
        setAdapter();

    }
    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        //Log.e("CSRES","RESUME CHOOSE SEED");
        goChart.removeCallbacks(goChartRun);

    }

    private void setupXchangeBtns() {

        xchange5m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentStep=1;
                goRates();
            }
        });
        xchange15m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentStep=3;
                goRates();
            }
        });
        xchange30m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentStep=6;
                goRates();
            }
        });
        xchange1hr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentStep=12;
                goRates();
            }
        });
        xchange2hr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentStep=24;
                goRates();
            }
        });

    }
}
