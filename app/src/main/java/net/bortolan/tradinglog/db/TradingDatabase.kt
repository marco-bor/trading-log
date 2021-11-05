package net.bortolan.tradinglog.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Transaction::class], version = 1)
@TypeConverters(Converters::class)
abstract class TradingDatabase : RoomDatabase() {
    abstract fun transactionsDao(): TransactionsDao

    companion object {
        lateinit var instance: TradingDatabase

        fun init(context: Context) {
            instance = Room
                .databaseBuilder(context, TradingDatabase::class.java, "trading-log.db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}