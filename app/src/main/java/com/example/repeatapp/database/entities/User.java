package com.example.repeatapp.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User
{
    @PrimaryKey
    public int UserId;

    public int Points = 0;

    public int EnglishReaderSpeed = 4;

    public int PolishReaderSpeed = 4;

    public int PhraseRepeatCount = 3;

    public double ThinkTimeMultiplier = 1.2;

    public double SpeakTimeMultiplier = 1.2;
}
