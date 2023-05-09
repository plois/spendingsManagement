package com.android.mma.mmaproduct;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.mma.database.SpendingViewModel;
import com.google.android.material.chip.Chip;

import java.util.List;

public class ListTagsAdapter extends RecyclerView.Adapter<ListTagsAdapter.MyViewHolder> {

    //private List<String> tags;

    private final Chip.OnCheckedChangeListener mOnToggleTagListener;
    private SpendingViewModel mSpendingViewModel;

    public ListTagsAdapter(Chip.OnCheckedChangeListener onToggleTagListener, SpendingViewModel spendingViewModel) {
        mOnToggleTagListener = onToggleTagListener;
        mSpendingViewModel = spendingViewModel;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private Chip chip;
        private MyViewHolder(@NonNull Chip chip) {
            super(chip);
            this.chip = chip;
        }
    }

    @NonNull
    @Override
    public ListTagsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Chip chip = new Chip(parent.getContext());
        chip.setCheckable(true);
        chip.setOnCheckedChangeListener(mOnToggleTagListener);
        return new MyViewHolder(chip);
    }

    @Override
    public void onBindViewHolder(@NonNull ListTagsAdapter.MyViewHolder holder, int position) {
        List<String> origins = mSpendingViewModel.getDistinctOrigins().getValue();
        List<String> tags = mSpendingViewModel.getTags().getValue();
        if(tags != null){
            String current = origins.get(position);
            holder.chip.setText(current);
            if(tags.contains(origins.get(position))){
                holder.chip.setChecked(true);
            }else{
                holder.chip.setChecked(false);
            }
        } else {
            holder.chip.setText("no data");

        }
    }

    @Override
    public int getItemCount() {
        List<String> origins = mSpendingViewModel.getDistinctOrigins().getValue();
        if(origins != null){
            return origins.size();
        }else {
            return 0;
        }
    }
}
