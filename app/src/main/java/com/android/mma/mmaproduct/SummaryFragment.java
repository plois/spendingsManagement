package com.android.mma.mmaproduct;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.mma.database.Spending;
import com.android.mma.database.SpendingViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SummaryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SummaryFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private SpendingViewModel mSpendingViewModel;

    private TextView last_transaction_merchant;
    private TextView last_transaction_time;
    private TextView last_transaction_amount;
    private TextView summary_total_value;
    private TextView summary_max_value;
    private TextView summary_count_value;

    public SummaryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SummaryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SummaryFragment newInstance(String param1, String param2) {
        SummaryFragment fragment = new SummaryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mSpendingViewModel = ViewModelProviders.of(getActivity()).get(SpendingViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_summary, container, false);
        final ConstraintLayout summary_card_layout = v.findViewById(R.id.summary_card_layout);
        final LinearLayout summary_total_layout = v.findViewById(R.id.summary_total_layout);
        last_transaction_merchant = v.findViewById(R.id.last_transactions_merchant);
        last_transaction_time = v.findViewById(R.id.last_transaction_time);
        last_transaction_amount = v.findViewById(R.id.last_transaction_amount);
        summary_total_value = v.findViewById(R.id.summary_total_value);
        summary_max_value = v.findViewById(R.id.summary_max_value);
        summary_count_value = v.findViewById(R.id.summary_count_value);
        SimpleDateFormat sdf = new SimpleDateFormat("MM");
        Log.w("month", sdf.format(Calendar.getInstance().getTime()));
        mSpendingViewModel.getLastSpending().observe(this, new Observer<Spending>() {
            @Override
            public void onChanged(Spending spending) {
                if(spending != null){
                    last_transaction_merchant.setText(""+spending.getMerchant());
                    last_transaction_time.setText(""+spending.getTimestamp());
                    last_transaction_amount.setText(""+spending.getCost());
                }
            }
        });
        mSpendingViewModel.getMaxForMonth().observe(this, new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                summary_max_value.setText(""+aDouble);
            }
        });
        mSpendingViewModel.getTotalForMonth().observe(this, new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                summary_total_value.setText(""+aDouble);
            }
        });
        mSpendingViewModel.getCountForMonth().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                summary_count_value.setText(""+integer);
            }
        });
        mSpendingViewModel.getDistinctOrigins().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                if(strings != null){
                    LinearLayout previousLinearLayout = null;
                    for(int i = 0; i < strings.size(); i++){
                        LinearLayout linearLayout = new LinearLayout(summary_card_layout.getContext());
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                        TextView origin = new TextView(summary_card_layout.getContext());
                        origin.setText(strings.get(i));
                        origin.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1.0f));
                        linearLayout.addView(origin);
                        TextView amount = new TextView(summary_card_layout.getContext());
                        amount.setText("200 Y");
                        amount.setGravity(Gravity.END);
                        amount.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1.0f));
                        linearLayout.addView(amount);
                        linearLayout.setId(View.generateViewId());
                        summary_card_layout.addView(linearLayout,i,layoutParams);
                        ConstraintSet set = new ConstraintSet();
                        set.clone(summary_card_layout);
                        if(previousLinearLayout == null){
                            set.connect(linearLayout.getId(), ConstraintSet.TOP,
                                    summary_total_layout.getId(), ConstraintSet.BOTTOM, 43);
                            set.connect(linearLayout.getId(), ConstraintSet.START,
                                    summary_total_layout.getId(), ConstraintSet.START, 43);
                            set.connect(linearLayout.getId(), ConstraintSet.END,
                                    summary_total_layout.getId(), ConstraintSet.END, 43);
                        }else {
                            set.connect(linearLayout.getId(), ConstraintSet.TOP,
                                    previousLinearLayout.getId(), ConstraintSet.BOTTOM, 43);
                            set.connect(linearLayout.getId(), ConstraintSet.START,
                                    summary_total_layout.getId(), ConstraintSet.START, 43);
                            set.connect(linearLayout.getId(), ConstraintSet.END,
                                    summary_total_layout.getId(), ConstraintSet.END, 43);
                        }
                        set.applyTo(summary_card_layout);
                        previousLinearLayout = linearLayout;
                    }
                }
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
