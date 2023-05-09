package com.android.mma.mmaproduct;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.mma.database.Spending;
import com.android.mma.database.SpendingViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;

import travel.ithaka.android.horizontalpickerlib.PickerLayoutManager;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PieFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PieFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PieFragment extends Fragment {

    private PieChart chart;
    private PieData pieData;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private PickerLayoutManager pickerLayoutManager;
    private SnapHelper snapHelper;
    private SpendingViewModel mSpendingViewModel;

    private TextView total_value;
    private TextView n_transaction_value;
    private TextView max_transaction_value;

    private OnFragmentInteractionListener mListener;

    public PieFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PieFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PieFragment newInstance() {
        PieFragment fragment = new PieFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpendingViewModel = ViewModelProviders.of(getActivity()).get(SpendingViewModel.class);

        pickerLayoutManager = new PickerLayoutManager(getContext(),PickerLayoutManager.HORIZONTAL,false);
        pickerLayoutManager.setChangeAlpha(false);
        pickerLayoutManager.setScaleDownBy(0);

        pickerLayoutManager.setOnScrollStopListener(new PickerLayoutManager.onScrollStopListener() {
            @Override
            public void selectedView(View view) {
                String month = mSpendingViewModel.getAllMonths().getValue().get(recyclerView.getChildAdapterPosition(view));
                mSpendingViewModel.setMonth(month);
            }
        });

        snapHelper = new LinearSnapHelper();
        mAdapter = new TimeRangeAdapter(mSpendingViewModel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pie, container, false);

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
        recyclerView = v.findViewById(R.id.recycler_view);
        snapHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(pickerLayoutManager);
        Button prevMonthButton = v.findViewById(R.id.to_previous_month);
        prevMonthButton.setOnClickListener(onSelectMonthListener);

        Button nextMonthButton = v.findViewById(R.id.to_next_month);
        nextMonthButton.setOnClickListener(onSelectMonthListener);
        mSpendingViewModel.getAllMonths().observe(this, new Observer<List<String>>(){
            @Override
            public void onChanged(List<String> months) {
                mAdapter.notifyDataSetChanged();
                if(months.size() != 0){
                    recyclerView.scrollToPosition(mAdapter.getItemCount()-1);
                    mSpendingViewModel.setMonth(mSpendingViewModel.getAllMonths().getValue().get(mAdapter.getItemCount()-1));
                }
            }
        });
        chart = v.findViewById(R.id.pie_chart);
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry mPieEntry = (PieEntry) e;
                List<String> tags = new ArrayList<>();
                tags.add(mPieEntry.getLabel());
                mSpendingViewModel.setTag(tags);
            }

            @Override
            public void onNothingSelected() {
                return;
            }
        });
        Legend legend = chart.getLegend();
        legend.setEnabled(false);
        Description description = chart.getDescription();
        description.setEnabled(false);
        mSpendingViewModel.getRatiosForMonth().observe(this, new Observer<List<Spending.RatioTag>>() {
            @Override
            public void onChanged(List<Spending.RatioTag> ratioTags) {
                List<PieEntry> entries = new ArrayList<>();
                for(int i=0; i<ratioTags.size(); i++){
                    PieEntry newEntry = new PieEntry((float)ratioTags.get(i).getRatio(),ratioTags.get(i).getTag());
                    entries.add(newEntry);

                }
                PieDataSet dataSet = new PieDataSet(entries,"label");
                pieData = new PieData(dataSet);
                chart.setData(pieData);
                chart.animateXY(300,300);
                chart.invalidate();
            }
        });
        return v;
    }

    private Button.OnClickListener onSelectMonthListener = new Button.OnClickListener(){

        @Override
        public void onClick(View view) {
            int index = mSpendingViewModel.getAllMonths().getValue().indexOf(mSpendingViewModel.getMonth());
            switch (view.getId()){
                case R.id.to_previous_month:
                    recyclerView.scrollToPosition(Math.max(0,index - 1));
                    mSpendingViewModel.setMonth(mSpendingViewModel.getAllMonths().getValue().get(Math.max(0,index - 1)));
                    mAdapter.notifyDataSetChanged();
                    return;
                case R.id.to_next_month:
                    recyclerView.scrollToPosition(Math.min(index+1,mAdapter.getItemCount()-1));
                    mSpendingViewModel.setMonth(mSpendingViewModel.getAllMonths().getValue().get(Math.min(index+1,mAdapter.getItemCount()-1)));
                    mAdapter.notifyDataSetChanged();
            }
        }
    };


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
