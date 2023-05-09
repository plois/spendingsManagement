package com.android.mma.mmaproduct;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.android.mma.database.Spending;
import com.android.mma.database.SpendingViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BarFragment extends Fragment {
    private BarChart chart;
    private BarData barData;
    private SpendingViewModel mSpendingViewModel;

    private RecyclerView list_tag_rv;
    private ListTagsAdapter list_tags_adapter;
    private RecyclerView.LayoutManager list_tags_lm;

    private TextView total_value;
    private TextView n_transaction_value;
    private TextView max_transaction_value;

    private OnFragmentInteractionListener mListener;

    public BarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BarFragment newInstance() {
        BarFragment fragment = new BarFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpendingViewModel = ViewModelProviders.of(getActivity()).get(SpendingViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_bar, container, false);
        total_value = v.findViewById(R.id.total_value);
        mSpendingViewModel.getTotalForMonthAndTags().observe(this, new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                total_value.setText(""+aDouble);
            }
        });
        n_transaction_value = v.findViewById(R.id.n_transaction_value);
        mSpendingViewModel.getCountForMonthAndTags().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                n_transaction_value.setText(""+integer);
            }
        });
        max_transaction_value = v.findViewById(R.id.max_transaction_value);
        mSpendingViewModel.getMaxForMonthAndTags().observe(this, new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                max_transaction_value.setText(""+aDouble);
            }
        });
        chart = v.findViewById(R.id.bar_chart);
        chart.setFitBars(false);
        chart.setScaleEnabled(false);
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                mSpendingViewModel.setMonth(chart.getXAxis().getFormattedLabel((int)h.getX()));
            }

            @Override
            public void onNothingSelected() {
                return;
            }
        });
        YAxis left = chart.getAxisLeft();
        left.setDrawAxisLine(false);
        left.setDrawGridLines(false);
        left.setDrawZeroLine(true);
        left.setDrawLabels(false);
        chart.getAxisRight().setEnabled(false);
        final XAxis xaxis = chart.getXAxis();
        xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xaxis.setGranularity(1f);
        xaxis.setDrawAxisLine(false);
        xaxis.setDrawGridLines(false);
        Legend legend = chart.getLegend();
        legend.setEnabled(false);
        Description description = chart.getDescription();
        description.setEnabled(false);
        mSpendingViewModel.getBarsForTags().observe(this, new Observer<List<Spending.MonthCost>>() {
            @Override
            public void onChanged(List<Spending.MonthCost> monthCosts) {
                List<BarEntry> entries = new ArrayList<>();
                final String[] labels = new String[monthCosts.size()];
                for(int i=0; i<monthCosts.size(); i++){
                    BarEntry newEntry = new BarEntry(i,(float)monthCosts.get(i).getCost());
                    entries.add(newEntry);
                    labels[i] = monthCosts.get(i).getMonth();
                }
                BarDataSet dataSet = new BarDataSet(entries,"label");
                barData = new BarData(dataSet);
                barData.setBarWidth(0.6f);
                chart.setData(barData);
                ValueFormatter formatter = new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        if((int)value >= labels.length){
                            return ""+value;
                        }else{
                            return labels[(int)value];
                        }
                    }
                };
                chart.getXAxis().setValueFormatter(formatter);
                chart.animateY(300);
                chart.invalidate();
            }
        });
        list_tags_lm = new LinearLayoutManager(getActivity());
        ((LinearLayoutManager) list_tags_lm).setOrientation(LinearLayoutManager.HORIZONTAL);
        list_tag_rv = v.findViewById(R.id.bar_tag_list_rv);
        list_tag_rv.setLayoutManager(list_tags_lm);
        list_tags_adapter = new ListTagsAdapter(new Chip.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                List<String> currentTags = new ArrayList<String>(mSpendingViewModel.getTags().getValue());
                if(b && !currentTags.contains((compoundButton.getText().toString()))){
                    currentTags.add(compoundButton.getText().toString());
                    mSpendingViewModel.setTag(currentTags);
                }else if(!b){
                    currentTags.remove(currentTags.indexOf(compoundButton.getText().toString()));
                    mSpendingViewModel.setTag(currentTags);
                }
            }
        },mSpendingViewModel);
        list_tag_rv.setAdapter(list_tags_adapter);
        mSpendingViewModel.getDistinctOrigins().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                list_tags_adapter.notifyDataSetChanged();
            }
        });
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
