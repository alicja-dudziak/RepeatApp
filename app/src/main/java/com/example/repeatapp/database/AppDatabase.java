package com.example.repeatapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.repeatapp.database.entities.Phrase;
import com.example.repeatapp.database.entities.PhraseSet;
import com.example.repeatapp.database.entities.User;
import com.example.repeatapp.database.entities.daos.PhraseDao;
import com.example.repeatapp.database.entities.daos.PhraseSetDao;
import com.example.repeatapp.database.entities.daos.UserDao;

import java.util.ArrayList;
import java.util.List;

@Database(entities = {User.class, PhraseSet.class, Phrase.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase
{
    public abstract UserDao userDao();

    public abstract PhraseDao phraseDao();

    public abstract PhraseSetDao phraseSetDao();


    private static final String DB_NAME = "appDatabase.db";
    private static volatile AppDatabase instance;

    static public AppDatabase getInstance(Context context) {
        if (instance == null)
        {
            instance = create(context);
        }

        User user = instance.userDao().GetUser();
        if(user == null)
        {
            fillDatabase();
        }

        return instance;
    }

    private static AppDatabase create(final Context context)
    {
        return Room.databaseBuilder(
                context,
                AppDatabase.class,
                DB_NAME).allowMainThreadQueries().build();
    }

    private static void fillDatabase()
    {
        insertNewUser();
        insertNewPhraseSet();
        insertPhrases();
        insertAnimals();
    }

    private static void insertNewUser()
    {
        User newUser = new User();
        newUser.UserId = 1;

        instance.userDao().Insert(newUser);
    }

    private static void insertNewPhraseSet()
    {
        PhraseSet set = new PhraseSet();
        set.Name = "List 1";

        PhraseSet animals = new PhraseSet();
        animals.Name = "Animals";

        instance.phraseSetDao().Insert(set);
        instance.phraseSetDao().Insert(animals);
    }

    private static void insertPhrases()
    {
        List<Phrase> phrases = new ArrayList<>();

        Phrase window = new Phrase();
        window.PhraseSetId = 1;
        window.PhraseText = "Window";
        window.TranslatedPhrase = "Okno";

        Phrase cigarette = new Phrase();
        cigarette.PhraseSetId = 1;
        cigarette.PhraseText = "Cigarette";
        cigarette.TranslatedPhrase = "Papieros";

        Phrase because = new Phrase();
        because.PhraseSetId = 1;
        because.PhraseText = "Because";
        because.TranslatedPhrase = "Ponieważ";

        Phrase congratulations = new Phrase();
        congratulations.PhraseSetId = 1;
        congratulations.PhraseText = "Congratulations";
        congratulations.TranslatedPhrase = "Gratulacje";

        phrases.add(window);
        phrases.add(cigarette);
        phrases.add(because);
        phrases.add(congratulations);

        instance.phraseDao().InsertAll(phrases);
    }

    private static void insertAnimals()
    {
        List<Phrase> animals = new ArrayList<>();
        Phrase parrot = new Phrase();
        parrot.PhraseSetId = 2;
        parrot.PhraseText = "Parrot";
        parrot.TranslatedPhrase = "Papuga";

        Phrase giraffe = new Phrase();
        giraffe.PhraseSetId = 2;
        giraffe.PhraseText = "Giraffe";
        giraffe.TranslatedPhrase = "Żyrafa";

        Phrase dog = new Phrase();
        dog.PhraseSetId = 2;
        dog.PhraseText = "Dog";
        dog.TranslatedPhrase = "Pies";

        Phrase monkey = new Phrase();
        monkey.PhraseSetId = 2;
        monkey.PhraseText = "Monkey";
        monkey.TranslatedPhrase = "Małpa";

        Phrase elephant = new Phrase();
        elephant.PhraseSetId = 2;
        elephant.PhraseText = "Elephant";
        elephant.TranslatedPhrase = "Słoń";

        animals.add(parrot);
        animals.add(giraffe);
        animals.add(dog);
        animals.add(monkey);
        animals.add(elephant);

        instance.phraseDao().InsertAll(animals);
    }


}
