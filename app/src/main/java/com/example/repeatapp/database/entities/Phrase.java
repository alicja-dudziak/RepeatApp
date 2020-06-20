package com.example.repeatapp.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Phrase
{

    @PrimaryKey(autoGenerate = true)
    public int PhraseId;

    public long PhraseSetId;

    public String PhraseText;

    public String TranslatedPhrase;

    @Ignore
    public boolean Skipped = false;

    @Ignore
    public int RepeatedCount = 0;
}
