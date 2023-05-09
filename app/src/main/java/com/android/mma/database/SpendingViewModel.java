package com.android.mma.database;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class SpendingViewModel extends AndroidViewModel {
    /** live data **/
    private SpendingRepository mRepository;
    private LiveData<List<String>> mDistinctOrigins;
    private LiveData<Spending> mLastSpending;
    private LiveData<List<String>> mAllMonths;

    /** live data based on filters **/
    private MediatorLiveData<List<Spending>> mSpendingFilter;
    private MediatorLiveData<List<Spending.MonthCost>> mTotalPerMonthForTag;
    private MediatorLiveData<List<Spending.RatioTag>> mRatiosPerTagForMonth;
    private MediatorLiveData<Double> mTotal;
    private MediatorLiveData<Integer> mCount;
    private MediatorLiveData<Double> mMax;
    private MediatorLiveData<Double> mTotalForMonth;
    private MediatorLiveData<Integer> mCountForMonth;
    private MediatorLiveData<Double> mMaxForMonth;

    /** filters **/
    private MediatorLiveData<List<String>> tags;
    private MediatorLiveData<String> month; //in format "01"..."12"
    private MediatorLiveData<String> lastMonth;

    public SpendingViewModel(@NonNull Application application) {
        super(application);
        mRepository = new SpendingRepository(application);
        mDistinctOrigins = mRepository.getDistinctOrigin();
        mLastSpending = mRepository.getLastSpending();
        mAllMonths = mRepository.getMonths();
        tags = new MediatorLiveData<>();
        String[] tagsList = new String[]{};
        tags.setValue(Arrays.asList(tagsList));
        setInitialTags();
        month = new MediatorLiveData<>();
        month.setValue("");
        setInitialMonth();
        getBarsForTags();
        SimpleDateFormat sdf = new SimpleDateFormat("MM");
        lastMonth = new MediatorLiveData<>();
        lastMonth.setValue(sdf.format(Calendar.getInstance().getTime()));
        //Log.w("mDistincOrigins",mDistinctOrigins.getValue().toString());
        //Log.w("mAllMonths",mAllMonths.getValue().toString());
    }

    public LiveData<List<String>> getDistinctOrigins() {return mDistinctOrigins;}

    public LiveData<List<String>>  getAllMonths() {
        return mAllMonths;
    }

    private void setInitialTags(){
        tags.addSource(mDistinctOrigins, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                if(strings != null && strings.size() != 0){
                    Log.w("onChanged",strings.toString());
                    tags.setValue(strings);
                    tags.removeSource(mAllMonths);
                }
            }
        });
    }

    private void setInitialMonth(){
        month.addSource(mAllMonths, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                if(strings != null && strings.size() != 0){
                    month.setValue(strings.get(strings.size()-1));
                    month.removeSource(mAllMonths);
                }
            }
        });
    }

    public LiveData<Double> getTotalForMonthAndTags(){
        if(mTotal == null){
            mTotal = new MediatorLiveData<>();
            mTotal.addSource(tags, new Observer<List<String>>(){
                @Override
                public void onChanged(List<String> strings) {
                    new getTotalAsyncTask(mRepository).execute(new MyFilters(month.getValue(),strings));
                }
            });
            mTotal.addSource(month, new Observer<String>(){
                @Override
                public void onChanged(String s) {
                    new getTotalAsyncTask(mRepository).execute(new MyFilters(s,tags.getValue()));
                }
            });
        }
        return mTotal;
    }

    public LiveData<Double> getMaxForMonthAndTags(){
        if(mMax == null){
            mMax = new MediatorLiveData<>();
            mMax.addSource(tags, new Observer<List<String>>(){
                @Override
                public void onChanged(List<String> strings) {
                    new getMaxAsyncTask(mRepository).execute(new MyFilters(month.getValue(),strings));
                }
            });
            mMax.addSource(month, new Observer<String>(){
                @Override
                public void onChanged(String s) {
                    new getMaxAsyncTask(mRepository).execute(new MyFilters(s,tags.getValue()));
                }
            });
        }
        return mMax;
    }

    public LiveData<Integer> getCountForMonthAndTags(){
        if(mCount == null){
            mCount = new MediatorLiveData<>();
            mCount.addSource(tags, new Observer<List<String>>() {
                @Override
                public void onChanged(List<String> strings) {
                    new getCountAsyncTask(mRepository).execute(new MyFilters(month.getValue(),strings));
                }
            });
            mCount.addSource(month, new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    new getCountAsyncTask(mRepository).execute(new MyFilters(s,tags.getValue()));
                }
            });
        }
        return mCount;
    }

    public LiveData<Double> getTotalForMonth(){
        if(mTotalForMonth == null){
            mTotalForMonth = new MediatorLiveData<>();
            mTotalForMonth.addSource(lastMonth, new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    new getTotalMonthAsyncTask(mRepository).execute(s);
                }
            });
            mTotalForMonth.addSource(mLastSpending, new Observer<Spending>() {
                @Override
                public void onChanged(Spending spending) {
                    Log.w("last spending changed","total last month");
                    if(spending != null){
                        Log.w("total last month",spending.getMonthRaw());
                        new getTotalMonthAsyncTask(mRepository).execute(spending.getMonthRaw());
                    }
                }
            });
        }
        return mTotalForMonth;
    }

    public LiveData<Integer> getCountForMonth(){
        if(mCountForMonth == null){
            mCountForMonth = new MediatorLiveData<>();
            mCountForMonth.addSource(lastMonth, new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    new getCountMonthAsyncTask(mRepository).execute(s);
                }
            });
            mCountForMonth.addSource(mLastSpending, new Observer<Spending>() {
                @Override
                public void onChanged(Spending spending) {
                    if(spending != null){
                        new getCountMonthAsyncTask(mRepository).execute(spending.getMonthRaw());
                    }
                }
            });
        }
        return mCountForMonth;
    }

    public LiveData<Double> getMaxForMonth(){
        if(mMaxForMonth == null){
            mMaxForMonth = new MediatorLiveData<>();
            mMaxForMonth.addSource(lastMonth, new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    new getMaxMonthAsyncTask(mRepository).execute(s);
                }
            });
            mMaxForMonth.addSource(mLastSpending, new Observer<Spending>() {
                @Override
                public void onChanged(Spending spending) {
                    if(spending != null){
                        new getMaxMonthAsyncTask(mRepository).execute(spending.getMonthRaw());
                    }
                }
            });
        }
        return mMaxForMonth;
    }

    //get list of month/total for all months and based on user selected tag
    public LiveData<List<Spending.MonthCost>> getBarsForTags(){
        if(mTotalPerMonthForTag == null){
            mTotalPerMonthForTag = new MediatorLiveData<>();
            mTotalPerMonthForTag.addSource(tags, new Observer<List<String>>() {
                @Override
                public void onChanged(List<String> strings) {
                    new getBarForTagAsyncTask(mRepository).execute(strings);
                }
            });
            mTotalPerMonthForTag.addSource(mAllMonths, new Observer<List<String>>() {
                @Override
                public void onChanged(List<String> strings) {
                    new getBarForTagAsyncTask(mRepository).execute(tags.getValue());
                }
            });
        }
        return mTotalPerMonthForTag;
    }

    //get lists of ratio/tag for all tags based on user selected month
    public LiveData<List<Spending.RatioTag>> getRatiosForMonth(){
        if(mRatiosPerTagForMonth == null){
            mRatiosPerTagForMonth = new MediatorLiveData<>();
            mRatiosPerTagForMonth.addSource(month, new Observer<String>(){
                @Override
                public void onChanged(String s) {
                    new getRatiosForMonthAsyncTask(mRepository).execute(s);
                }
            });
            mRatiosPerTagForMonth.addSource(mDistinctOrigins, new Observer<List<String>>() {
                @Override
                public void onChanged(List<String> strings) {
                    new getRatiosForMonthAsyncTask(mRepository).execute(month.getValue());
                }
            });
        }
        return mRatiosPerTagForMonth;
    }

    public LiveData<List<Spending>> getFilteredSpending(){
        if(mSpendingFilter == null){
            mSpendingFilter = new MediatorLiveData<>();
            mSpendingFilter.addSource(mLastSpending, new Observer<Spending>() {
                @Override
                public void onChanged(Spending value) {
                    new getByFilterAsyncTask(mRepository).execute(new MyFilters(month.getValue(),tags.getValue()));
                }
            });
            mSpendingFilter.addSource(tags,new Observer<List<String>>(){
                @Override
                public void onChanged(List<String> list) {
                    if(list != null){
                        new getByFilterAsyncTask(mRepository).execute(new MyFilters(month.getValue(),list));
                    }
                }
            });
            mSpendingFilter.addSource(month,new Observer<String>(){
                @Override
                public void onChanged(String s) {
                    if(s != null){
                        new getByFilterAsyncTask(mRepository).execute(new MyFilters(s,tags.getValue()));
                    }
                }
            });
        }
        return mSpendingFilter;
    }

    private class getByFilterAsyncTask extends AsyncTask<MyFilters, Void, List<Spending>> {
        private SpendingRepository mRepository;

        getByFilterAsyncTask(SpendingRepository repo){
            mRepository = repo;
        }

        @Override
        protected List<Spending> doInBackground(MyFilters... myFilters) {
            return mRepository.getByFilter(myFilters[0].tags,myFilters[0].month);
        }

        @Override
        protected void onPostExecute(List<Spending> spendings) {
            mSpendingFilter.setValue(spendings);
        }
    }

    private class getRatiosForMonthAsyncTask extends AsyncTask<String, Void, List<Spending.RatioTag>> {

        getRatiosForMonthAsyncTask(SpendingRepository repo){ mRepository = repo; }

        @Override
        protected List<Spending.RatioTag> doInBackground(String... strings) {
            return mRepository.getRatioPerTagForMonth(strings[0]);
        }

        @Override
        protected void onPostExecute(List<Spending.RatioTag> ratioTags) {
            mRatiosPerTagForMonth.setValue(ratioTags);
        }
    }

    private class getTotalAsyncTask extends AsyncTask<MyFilters, Void, Double>{

        getTotalAsyncTask(SpendingRepository repo) { mRepository = repo; }
        @Override
        protected Double doInBackground(MyFilters... myFilters) {
            return mRepository.getTotalForMonthAndTags(myFilters[0].month,myFilters[0].tags);
        }

        @Override
        protected void onPostExecute(Double aDouble) {
            mTotal.setValue(aDouble);
        }
    }

    private class getMaxAsyncTask extends AsyncTask<MyFilters, Void, Double>{

        getMaxAsyncTask(SpendingRepository repo) { mRepository = repo; }
        @Override
        protected Double doInBackground(MyFilters... myFilters) {
            return mRepository.getMaxForMonthAndTags(myFilters[0].month,myFilters[0].tags);
        }

        @Override
        protected void onPostExecute(Double aDouble) {
            mMax.setValue(aDouble);
        }
    }

    private class getCountAsyncTask extends AsyncTask<MyFilters, Void, Integer>{

        getCountAsyncTask(SpendingRepository repo) { mRepository = repo; }
        @Override
        protected Integer doInBackground(MyFilters... myFilters) {
            return mRepository.getCountForMonthAndTags(myFilters[0].month,myFilters[0].tags);
        }

        @Override
        protected void onPostExecute(Integer aInteger) {
            mCount.setValue(aInteger);
        }
    }

    private class getTotalMonthAsyncTask extends AsyncTask<String, Void, Double>{

        getTotalMonthAsyncTask(SpendingRepository repo){ mRepository = repo; }

        @Override
        protected Double doInBackground(String... strings) {
            return mRepository.getTotalForMonth(strings[0]);
        }

        @Override
        protected void onPostExecute(Double aDouble) {
            mTotalForMonth.setValue(aDouble);
        }
    }

    private class getCountMonthAsyncTask extends AsyncTask<String, Void, Integer>{

        getCountMonthAsyncTask(SpendingRepository repo){ mRepository = repo; }

        @Override
        protected Integer doInBackground(String... strings) {
            return mRepository.getCountForMonth(strings[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            mCountForMonth.setValue(integer);
        }
    }

    private class getMaxMonthAsyncTask extends AsyncTask<String, Void, Double>{

        getMaxMonthAsyncTask(SpendingRepository repo){ mRepository = repo; }

        @Override
        protected Double doInBackground(String... strings) {
            return mRepository.getMaxForMonth(strings[0]);
        }

        @Override
        protected void onPostExecute(Double aDouble) {
            mMaxForMonth.setValue(aDouble);
        }
    }



    private class getBarForTagAsyncTask extends AsyncTask<List<String>, Void, List<Spending.MonthCost>> {

        getBarForTagAsyncTask(SpendingRepository repo){ mRepository = repo; }

        @Override
        protected List<Spending.MonthCost> doInBackground(List<String>... lists) {
            return mRepository.getTotalPerMonthForTag(lists[0]);
        }

        @Override
        protected void onPostExecute(List<Spending.MonthCost> monthCosts){
            List<Spending.MonthCost> cleanMonthCostList = new ArrayList<>();
            List<String> allMonths = mAllMonths.getValue();
            Log.w("monthcosts",monthCosts.toString());
            if(allMonths != null && allMonths.size() >= monthCosts.size() ){
                int j = 0;
                for(int i = 0; i < allMonths.size(); i++){
                    if(j > monthCosts.size()-1){
                        cleanMonthCostList.add(new Spending.MonthCost(0.0,allMonths.get(i)));
                    }
                    else if(allMonths.get(i).equals(monthCosts.get(j).getMonth())){
                        cleanMonthCostList.add(monthCosts.get(j));
                        Log.w("monthcosts-month",monthCosts.get(j).getMonth());
                        Log.w("monthcosts-cost",""+monthCosts.get(j).getCost());
                        j++;
                    }else{
                        cleanMonthCostList.add(new Spending.MonthCost(0.0,allMonths.get(i)));
                    }
                }
                mTotalPerMonthForTag.setValue(cleanMonthCostList);
            }
        }


    }

    private class MyFilters {
        String month;
        List<String> tags;

        MyFilters(String month, List<String> tags){
            this.month = month;
            this .tags = tags;
        }
    }

    public void setTag(List<String> tags){
        Log.w("list of tags",tags.toString());
        this.tags.setValue(tags);
    }

    public LiveData<List<String>> getTags(){
        Log.w("list of tags",tags.getValue().toString());
        return tags;
    }

    public void setMonth(String month){
        Log.w("set month",month);
        this.month.setValue(month);
    }

    public String getMonth(){
        Log.w("get month",month.getValue());
        return this.month.getValue();
    }

    public LiveData<Spending> getLastSpending() {
        return mLastSpending;
    }
}
