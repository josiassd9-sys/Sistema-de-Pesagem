package com.josias.pesagempaginainicial.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PesagemDao {
    @Query("SELECT * FROM pesagens ORDER BY timestamp DESC")
    fun getAll(): List<Pesagem>

    @Insert
    fun insert(pesagem: Pesagem): Long

    @Query("DELETE FROM pesagens")
    fun deleteAll(): Int
}
