package com.example.repeatapp.database.entities.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.repeatapp.database.entities.Phrase;

import java.util.List;

@Dao
public interface PhraseDao
{

    @Query("SELECT * FROM Phrase WHERE PhraseSetId = :phraseSetId")
    List<Phrase> GetSetPhrases(long phraseSetId);

    @Insert
    void InsertPhrase(Phrase phrase);

    @Insert
    void InsertAll(List<Phrase> phrases);

    @Delete
    void DeletePhrase(Phrase phrase);

    @Query("DELETE FROM Phrase WHERE PhraseId = :phraseId")
    void DeletePhrase(int phraseId);

    @Query("DELETE FROM Phrase WHERE PhraseSetId = :phraseSetId")
    void DeleteSetPhrases(long phraseSetId);

    @Update
    void UpdatePhrase(Phrase phrase);

}
