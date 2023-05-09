package com.android.mma.mmaproduct;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.mma.database.SpendingViewModel;

import java.util.List;

class TimeRangeAdapter extends RecyclerView.Adapter<TimeRangeAdapter.MyViewHolder> {
    private SpendingViewModel mSpendingViewModel;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public MyViewHolder(@NonNull TextView itemView) {
            super(itemView);
            textView = itemView;
        }
    }

    public TimeRangeAdapter(SpendingViewModel spendingViewModel){
        mSpendingViewModel = spendingViewModel;
    }

    @NonNull
    @Override
    public TimeRangeAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.month_item,parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String month = mSpendingViewModel.getAllMonths().getValue().get(position);
        holder.textView.setText(month);
        //mSpendingViewModel.setMonth(month);
    }

    @Override
    public int getItemCount() {
        List<String> months = mSpendingViewModel.getAllMonths().getValue();
        if(months != null){
            return months.size();
        }else {
            return 0;
        }
    }
}
