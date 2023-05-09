package com.android.mma.mmaproduct;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

import com.android.mma.database.Spending;
import com.android.mma.database.SpendingViewModel;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import travel.ithaka.android.horizontalpickerlib.PickerLayoutManager;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment {

    private RecyclerView timeRange_rv;
    private TimeRangeAdapter timeRange_adapter;
    private PickerLayoutManager timeRange_lm;
    SnapHelper snapHelper;

    private RecyclerView list_spendings_rv;
    private ListSpendingsAdapter list_spendings_adapter;
    private RecyclerView.LayoutManager list_spendings_lm;

    private RecyclerView list_tag_rv;
    private ListTagsAdapter list_tags_adapter;
    private RecyclerView.LayoutManager list_tags_lm;

    private OnFragmentInteractionListener mListener;
    private SpendingViewModel mSpendingViewModel;

    public ListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListFragment newInstance() {
        ListFragment fragment = new ListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpendingViewModel = ViewModelProviders.of(getActivity()).get(SpendingViewModel.class);

        timeRange_lm = new PickerLayoutManager(getContext(),PickerLayoutManager.HORIZONTAL,false);
        timeRange_lm.setChangeAlpha(false);
        timeRange_lm.setScaleDownBy(0);

        timeRange_lm.setOnScrollStopListener(new PickerLayoutManager.onScrollStopListener() {
            @Override
            public void selectedView(View view) {
                String month = mSpendingViewModel.getAllMonths().getValue().get(timeRange_rv.getChildAdapterPosition(view));
                mSpendingViewModel.setMonth(month);
            }
        });

        snapHelper = new LinearSnapHelper();
        timeRange_adapter = new TimeRangeAdapter(mSpendingViewModel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        timeRange_rv = v.findViewById(R.id.recycler_view2);
        snapHelper.attachToRecyclerView(timeRange_rv);
        timeRange_rv.setAdapter(timeRange_adapter);
        timeRange_rv.setLayoutManager(timeRange_lm);
        //timeRange_rv.scrollToPosition(timeRange_adapter.getItemCount()-1);
        Button prevMonthButton = v.findViewById(R.id.to_previous_month2);
        prevMonthButton.setOnClickListener(onSelectMonthListener);

        Button nextMonthButton = v.findViewById(R.id.to_next_month2);
        nextMonthButton.setOnClickListener(onSelectMonthListener);

        list_spendings_rv = v.findViewById(R.id.recycler_view_spendings);
        list_spendings_lm = new LinearLayoutManager(getActivity());
        list_spendings_rv.setLayoutManager(list_spendings_lm);
        list_spendings_adapter = new ListSpendingsAdapter(getContext());
        list_spendings_rv.setAdapter(list_spendings_adapter);
        mSpendingViewModel.getAllMonths().observe(this, new Observer<List<String>>(){
            @Override
            public void onChanged(List<String> months) {
                timeRange_adapter.notifyDataSetChanged();
                if(months.size() != 0){
                    Log.w("data set changed",months.toString());
                    timeRange_rv.scrollToPosition(timeRange_adapter.getItemCount()-1);
                    mSpendingViewModel.setMonth(mSpendingViewModel.getAllMonths().getValue().get(timeRange_adapter.getItemCount()-1));
                }
            }
        });
        mSpendingViewModel.getFilteredSpending().observe(this,new Observer<List<Spending>>(){
            @Override
            public void onChanged(List<Spending> spending) {
                list_spendings_adapter.setSpendings(spending);
            }
        });
        list_tags_lm = new LinearLayoutManager(getActivity());
        ((LinearLayoutManager) list_tags_lm).setOrientation(LinearLayoutManager.HORIZONTAL);
        list_tag_rv = v.findViewById(R.id.chip_list_rv);
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

    private Button.OnClickListener onSelectMonthListener = new Button.OnClickListener(){

        @Override
        public void onClick(View view) {
            int index = mSpendingViewModel.getAllMonths().getValue().indexOf(mSpendingViewModel.getMonth());
            switch (view.getId()){
                case R.id.to_previous_month2:
                    timeRange_rv.scrollToPosition(Math.max(0,index - 1));
                    mSpendingViewModel.setMonth(mSpendingViewModel.getAllMonths().getValue().get(Math.max(0,index - 1)));
                    timeRange_adapter.notifyDataSetChanged();
                    return;
                case R.id.to_next_month2:
                    timeRange_rv.scrollToPosition(Math.min(index+1,timeRange_adapter.getItemCount()-1));
                    mSpendingViewModel.setMonth(mSpendingViewModel.getAllMonths().getValue().get(Math.min(index+1,timeRange_adapter.getItemCount()-1)));
                    timeRange_adapter.notifyDataSetChanged();
                    return;
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
