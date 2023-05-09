package com.android.mma.database;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import java.util.List;

public class SpendingRepository {
    private SpendingDao mSpendingDao;
    private LiveData<List<Spending>> mAllSpending;
    private LiveData<List<String>> mDistinctOrigin;
    private LiveData<List<String>> mAllMonths;
    private LiveData<Spending> mLastSpending;

    public SpendingRepository(Application application){
        AppDatabase db = AppDatabase.getDatabase(application);
        mSpendingDao = db.spendingDao();
        mAllSpending = mSpendingDao.getAll();
        mDistinctOrigin = mSpendingDao.getDistinctOrigins();
        mAllMonths = mSpendingDao.getMonths();
        mLastSpending = mSpendingDao.getLastSpending();
    }

    LiveData<List<Spending>> getAll(){
        return mAllSpending;
    }

    LiveData<List<String>> getDistinctOrigin() { return mDistinctOrigin;}

    LiveData<Spending> getLastSpending() { return mLastSpending;}

    Double getTotalForMonthAndTags(String month, List<String> origin){ return mSpendingDao.getTotalForMonthAndTags(month,origin); }

    Integer getCountForMonthAndTags(String month, List<String> origin){ return mSpendingDao.getCountForMonthAndTags(month, origin); }

    Double getMaxForMonthAndTags(String month, List<String> origin) { return mSpendingDao.getMaxForMonthAndTags(month, origin); }

    Double getTotalForMonth(String month){ return mSpendingDao.getTotalForMonth(month); }

    Integer getCountForMonth(String month){ return mSpendingDao.getCountForMonth(month); }

    Double getMaxForMonth(String month){ return mSpendingDao.getMaxForMonth(month); }

    List<Spending.MonthCost> getTotalPerMonthForTag(List<String> origin){ return mSpendingDao.getTotalPerMonthForTags(origin);}

    List<Spending.RatioTag> getRatioPerTagForMonth(String month) { return mSpendingDao.getRatioPerTagForMonth(month);}

    LiveData<List<String>> getMonths(){return mAllMonths;}

    List<Spending> getByFilter(List<String> tags,String month) {return mSpendingDao.getByFilter(tags,month);}

    public void insert (Spending... spendings){
        new insertAsyncTask(mSpendingDao).execute(spendings);
    }

    private static class insertAsyncTask extends AsyncTask<Spending, Void, Void> {
        private SpendingDao mAsyncTaskDao;

        insertAsyncTask(SpendingDao dao){
            mAsyncTaskDao = dao;
        }
        @Override
        protected Void doInBackground(Spending... spendings) {
            mAsyncTaskDao.insertAll(spendings);
            return null;
        }
    }
}
