package com.example.repeatapp.database.entities.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.repeatapp.database.entities.User;

@Dao
public interface UserDao
{

    @Query("SELECT * FROM User")
    User GetUser();

    @Insert
    void Insert(User user);

    @Query("SELECT Points FROM User")
    int GetUserPoints();

    @Query("UPDATE User SET Points = :points")
    void UpdateUserPoints(int points);

    @Query("SELECT EnglishReaderSpeed FROM User")
    int GetEnglishReaderSpeed();

    @Query("SELECT PolishReaderSpeed FROM User")
    int GetPolishReaderSpeed();

    @Query("SELECT PhraseRepeatCount FROM User")
    int GetPhraseRepeatCount();

    @Query("SELECT SpeakTimeMultiplier FROM User")
    double GetSpeakTimeMultiplier();

    @Query("SELECT ThinkTimeMultiplier FROM User")
    double GetThinkTimeMultiplier();

    @Query("UPDATE User SET EnglishReaderSpeed = :speed")
    void SetEnglishReaderSpeed(int speed);

    @Query("UPDATE User SET PolishReaderSpeed = :speed")
    void SetPolishReaderSpeed(int speed);

    @Query("UPDATE User SET PhraseRepeatCount = :count")
    void SetPhraseRepeatCount(int count);

    @Query("UPDATE User SET SpeakTimeMultiplier = :multiplier")
    void SetSpeakTimeMultiplier(double multiplier);

    @Query("UPDATE User SET ThinkTimeMultiplier = :multiplier")
    void SetThinkTimeMultiplier(double multiplier);
}
