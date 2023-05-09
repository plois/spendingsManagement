package com.android.mma.mmaproduct;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.mma.database.Spending;

import java.util.List;


class ListSpendingsAdapter extends RecyclerView.Adapter<ListSpendingsAdapter.MyViewHolder> {

    private final LayoutInflater mInflater;
    private List<Spending> spendings;

    public ListSpendingsAdapter(Context context){
        this.mInflater = LayoutInflater.from(context);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView merchant;
        private final TextView timestamp;
        private final TextView cost;
        private MyViewHolder(@NonNull View itemView) {
            super(itemView);
            merchant = itemView.findViewById(R.id.spending_item_merchant);
            timestamp = itemView.findViewById(R.id.spending_item_time);
            cost = itemView.findViewById(R.id.spending_item_price);
        }
    }

    @NonNull
    @Override
    public ListSpendingsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.list_item_spending,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListSpendingsAdapter.MyViewHolder holder, int position) {
        if(spendings != null){
            Spending current = spendings.get(position);
            holder.merchant.setText(current.getMerchant());
            holder.timestamp.setText(current.getMonthDate());
            holder.cost.setText(Double.toString(current.getCost()));
        } else {
            holder.merchant.setText("------");
            holder.timestamp.setText("0h0m0s");
            holder.cost.setText("0");
        }

    }

    public void setSpendings(List<Spending> spendings){
        this.spendings = spendings;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(spendings != null){
            return spendings.size();
        }else {
            return 0;
        }
    }
}
