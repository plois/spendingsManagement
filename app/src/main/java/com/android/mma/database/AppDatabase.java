package com.android.mma.database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.Calendar;

@Database(entities = {Spending.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    public abstract SpendingDao spendingDao();

    private static volatile AppDatabase INSTANCE;

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){
                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };

    static AppDatabase getDatabase(final Context context){
        if( INSTANCE == null){
            synchronized (AppDatabase.class){
                if( INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),AppDatabase.class,"app_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final SpendingDao mDao;

        PopulateDbAsync(AppDatabase db){
            mDao = db.spendingDao();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            mDao.deleteAll();
            Spending spending1 = new Spending("2019-09-05 20:05:12","merchant1","Alipay",35.0);
            Calendar calendar = Calendar.getInstance();
            calendar.set(2019,8,27,15,32);
            Spending spending2 = new Spending("2019-09-27 15:32:00","merchant2","Wechat",15.0);
            calendar.set(2019,7,12,10,3);
            Spending spending3 = new Spending("2019-08-14 23:56:34","merchant3","Wechat",20.0);
            Spending spending4 = new Spending("2019-07-03 02:22:39","merchant4","Alipay",9.0);
            mDao.insertAll(spending1);
            mDao.insertAll(spending2);
            mDao.insertAll(spending3);
            mDao.insertAll(spending4);
            return null;
        }
    }

}
