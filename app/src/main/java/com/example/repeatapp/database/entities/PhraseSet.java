package com.example.repeatapp.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PhraseSet
{
    @PrimaryKey(autoGenerate = true)
    public long PhraseSetId;

    public String Name;

    public int TimesDone = 0;

    public boolean IsFavourite = false;

}
