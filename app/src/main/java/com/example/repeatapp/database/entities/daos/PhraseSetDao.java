package com.example.repeatapp.database.entities.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.repeatapp.database.entities.PhraseSet;

import java.util.List;

@Dao
public interface PhraseSetDao
{

    @Query("SELECT * FROM PhraseSet ORDER BY IsFavourite DESC")
    List<PhraseSet> GetAllSets();

    @Query("SELECT * FROM PhraseSet WHERE PhraseSetId = :phraseSetId")
    PhraseSet GetPhraseSet(long phraseSetId);

    @Insert
    long Insert(PhraseSet phraseSet);

    @Update
    void Update(PhraseSet phraseSet);

    @Delete
    void Delete(PhraseSet phraseSet);

}
