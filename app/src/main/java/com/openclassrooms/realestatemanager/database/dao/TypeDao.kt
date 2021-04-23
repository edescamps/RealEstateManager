package com.openclassrooms.realestatemanager.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.openclassrooms.realestatemanager.database.model.Type

@Dao
interface TypeDao {

    @Insert
    suspend fun insert(type: Type)

    /**
     * When updating a row with a value already set in a column,
     * replaces the old value with the new one.
     *
     * @param type new value to write
     */
    @Update
    suspend fun updateType(type: Type)

    /**
     * Selects and returns all rows in the table,
     */
    @Query("SELECT * FROM type_table")
    fun getAllTypes(): LiveData<List<Type>>


    /**
     * Selects and returns all rows in the table,
     */
    @Query("SELECT * FROM type_table")
    fun getAllTypesStatic(): List<Type>


    @Query("DELETE FROM type_table")
    fun deleteAll()


}