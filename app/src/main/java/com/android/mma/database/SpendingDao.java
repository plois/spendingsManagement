package com.android.mma.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SpendingDao {
    @Query("SELECT * FROM Spending")
    LiveData<List<Spending>> getAll();

    @Query("SELECT sum(cost) FROM Spending where strftime('%m',timestamp) like :month and origin in (:origin)")
    Double getTotalForMonthAndTags(String month, List<String> origin);

    @Query("SELECT count(cost) FROM Spending where strftime('%m',timestamp) like :month and origin in (:origin)")
    Integer getCountForMonthAndTags(String month, List<String> origin);

    @Query("SELECT max(cost) FROM Spending where strftime('%m',timestamp) like :month and origin in (:origin)")
    Double getMaxForMonthAndTags(String month, List<String> origin);

    @Query("SELECT sum(cost) FROM Spending where strftime('%m',timestamp) like :month")
    Double getTotalForMonth(String month);

    @Query("SELECT count(cost) FROM Spending where strftime('%m',timestamp) like :month")
    Integer getCountForMonth(String month);

    @Query("SELECT max(cost) FROM Spending where strftime('%m',timestamp) like :month")
    Double getMaxForMonth(String month);

    @Query("with cte(month,cost) as (SELECT strftime('%m',timestamp),sum(cost) FROM Spending where origin IN (:origin) group by strftime('%m',timestamp) order by uid DESC) select * from cte")
    List<Spending.MonthCost> getTotalPerMonthForTags(List<String> origin);

    @Query("SELECT t1.cost / t2.sum as ratio,origin as tag from ((SELECT sum(cost) as cost,origin from Spending where strftime('%m',timestamp) like :month group by origin) t1) CROSS JOIN (SELECT sum(cost) as sum FROM Spending where strftime('%m',timestamp) like :month) t2")
    List<Spending.RatioTag> getRatioPerTagForMonth(String month);

    @Query("SELECT * FROM Spending where strftime('%m',timestamp) like :month and origin  IN (:origin) order by uid DESC")
    List<Spending> getByFilter(List<String> origin, String month);

    @Query("SELECT DISTINCT origin FROM Spending")
    LiveData<List<String>> getDistinctOrigins();

    @Query("SELECT * FROM Spending ORDER BY date(timestamp) DESC LIMIT 1")
    LiveData<Spending> getLastSpending();

    @Query("SELECT DISTINCT strftime('%m',timestamp) FROM Spending order by uid DESC")
    LiveData<List<String>> getMonths();

    @Query("SELECT DISTINCT strftime('%m',timestamp) FROM Spending order by uid DESC LIMIT 1")
    LiveData<String> getLastMonth();

    @Insert
    void insertAll(Spending... spendings);

    @Delete
    void delete(Spending spending);

    @Query("DELETE FROM Spending")
    void deleteAll();
}
