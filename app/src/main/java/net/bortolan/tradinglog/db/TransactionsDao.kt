package net.bortolan.tradinglog.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TransactionsDao {

    @Query("SELECT * FROM `Transaction` ORDER BY date DESC")
    fun getAll(): LiveData<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(transactions: List<Transaction>)
}