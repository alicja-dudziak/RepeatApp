package com.example.repeatapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.repeatapp.database.AppDatabase;
import com.example.repeatapp.database.entities.Phrase;
import com.example.repeatapp.database.entities.PhraseSet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class PlayList extends Fragment
{

    long phraseSetId;
    List<Phrase> phrases;
    int currentPhrase = 0;
    TextView polishPhrase;
    TextView englishPhrase;
    TextToSpeech polishTts;
    TextToSpeech englishTts;
    ProgressBar progressBar;
    boolean isGameInProgress;
    Handler hdlr = new Handler();
    boolean pauseTheGameDelay;
    Instant gameStartTime;
    Instant startPauseTime;
    int pauseDuration = 0;
    View root;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        root = inflater.inflate(R.layout.activity_play_list, container, false);

        phraseSetId = getArguments().getLong("PhraseSetId", 0);

        polishPhrase = root.findViewById(R.id.polishPhrase);
        englishPhrase = root.findViewById(R.id.englishPhrase);
        progressBar = root.findViewById(R.id.progressBar);
        progressBar.setMax(100);

        SetTitle();
        GetPhrases();
        AddButtonsListeners();

        PrepareTextToSpeech();

        return root;
    }

/*    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        isGameInProgress = false;
    }*/

    private void AddButtonsListeners()
    {
        ImageView pauseButton = root.findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleTheGame();
            }
        });

        ImageView skipButton = root.findViewById(R.id.skipButton);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SkippCurrentPhrase();
            }
        });
    }

    private void SkippCurrentPhrase()
    {
        Phrase phrase = phrases.get(currentPhrase);
        phrase.Skipped = true;

        Toast.makeText(getContext(), "Current phrase will be skipped.", Toast.LENGTH_LONG).show();
    }

    private void ToggleTheGame()
    {
        if(pauseTheGameDelay)
            return;

        if(isGameInProgress)
        {
            StopTheGame();
        }
        else
        {
            ResumeTheGame();
        }

        pauseTheGameDelay = true;

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(300);
                    pauseTheGameDelay = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void StopTheGame()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            startPauseTime = Instant.now();
        }

        isGameInProgress = false;
        polishTts.stop();
        englishTts.stop();
        progressBar.setProgress(0);

        ImageView pauseButton = root.findViewById(R.id.pauseButton);
        pauseButton.setImageResource(R.drawable.play_icon);
    }

    private void ResumeTheGame()
    {
        ImageView pauseButton = root.findViewById(R.id.pauseButton);
        pauseButton.setImageResource(R.drawable.pause_icon);

        isGameInProgress = true;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            Instant endPauseTime = Instant.now();
            Duration timeElapsed = Duration.between(startPauseTime, endPauseTime);
            pauseDuration += timeElapsed.toMillis();
        }

        StartTheGame();
    }

    private void PrepareTextToSpeech()
    {
        polishTts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR)
                {
                    int polishReaderSpeed = AppDatabase.getInstance(getContext()).userDao().GetPolishReaderSpeed();

                    polishTts.setLanguage(Locale.forLanguageTag("pl"));
                    polishTts.setSpeechRate((float)0.2 * polishReaderSpeed);
                    isGameInProgress = true;

                    gameStartTime = Instant.now();
                    StartTheGame();
                }
            }
        });

        englishTts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR)
                {
                    int englishReaderSpeed = AppDatabase.getInstance(getContext()).userDao().GetEnglishReaderSpeed();

                    englishTts.setLanguage(Locale.ENGLISH);
                    englishTts.setSpeechRate((float)0.2 * englishReaderSpeed);
                }
            }
        });
    }

    private void StartTheGame()
    {
        new Thread(new Runnable() {
            public void run() {
                PolishPhrase();
                ShowProgressBar();
                EnglishPhrase();
                NextPhrase();
            }
        }).start();
    }

    private void ShowProgressBar()
    {
        if(!isGameInProgress)
        {
            return;
        }

        Phrase phrase = phrases.get(currentPhrase);
        int phraseLength = phrase.PhraseText.length();

        final int phraseDuration = GetPhraseDuration(phraseLength);

        for(int i=0; i < 100; i++)
        {
            if(!isGameInProgress)
            {
                return;
            }

            final int index = i;

            hdlr.post(new Runnable() {
                public void run() {
                    if(!isGameInProgress)
                    {
                        return;
                    }

                    progressBar.setProgress(index);
                }
            });
            try {
                Thread.sleep(phraseDuration / 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void EnglishPhrase()
    {
        hdlr.post(new Runnable() {
            public void run() {
                if(!isGameInProgress)
                {
                    return;
                }

                Phrase phrase = phrases.get(currentPhrase);
                englishPhrase.setVisibility(View.VISIBLE);
                englishPhrase.setText(phrase.PhraseText);
                englishTts.speak(phrase.PhraseText, TextToSpeech.QUEUE_ADD, null);
                phrase.RepeatedCount++;
            }
        });
    }

    private void NextPhrase()
    {
        if(!isGameInProgress)
        {
            return;
        }

        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        hdlr.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void run() {
                if(IsAllRepeated())
                {
                    EndTheGame();
                    return;
                }

                currentPhrase++;

                Phrase nextPhrase = null;

                while(nextPhrase ==null)
                {
                    if(currentPhrase >= phrases.size())
                    {
                        currentPhrase = 0;
                    }

                    Phrase newPhrase = phrases.get(currentPhrase);

                    if (!newPhrase.Skipped && newPhrase.RepeatedCount < 3) {
                        nextPhrase = newPhrase;
                    } else {
                        currentPhrase++;
                    }
                }

                StartTheGame();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void EndTheGame()
    {
        int points = AppDatabase.getInstance(getContext()).userDao().GetUserPoints();
        points += 10;
        AppDatabase.getInstance(getContext()).userDao().UpdateUserPoints(points);

        PhraseSet set = AppDatabase.getInstance(getContext()).phraseSetDao().GetPhraseSet(phraseSetId);
        set.TimesDone++;
        AppDatabase.getInstance(getContext()).phraseSetDao().Update(set);
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(gameStartTime, end);
        timeElapsed = timeElapsed.minus(pauseDuration, ChronoUnit.MILLIS);

        Intent myIntent = new Intent(getContext(), GameEnded.class);
        myIntent.putExtra("DurationTime", GetFormattedTime(timeElapsed.toMillis()));
        this.startActivity(myIntent);
        //finish();
    }

    private String GetFormattedTime(long milliSeconds)
    {
        Date date = new Date(milliSeconds);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }

    private boolean IsAllRepeated()
    {
        int shouldRepeatCount = 0;
        for(Phrase phrase : phrases)
        {
            if(!phrase.Skipped && phrase.RepeatedCount < 3)
                shouldRepeatCount++;
        }

        return shouldRepeatCount == 0;
    }

    private int GetPhraseDuration(int phraseLength)
    {
        if(phraseLength < 5)
            return 2000;

        if(phraseLength < 10)
            return 3000;

        if(phraseLength < 20)
            return 4000;

        return 5000;
    }

    private void PolishPhrase()
    {
        if(!isGameInProgress)
        {
            return;
        }

        englishPhrase.setVisibility(View.INVISIBLE);

        Phrase phrase = phrases.get(currentPhrase);
        polishPhrase.setText(phrase.TranslatedPhrase);
        polishTts.speak(phrase.TranslatedPhrase, TextToSpeech.QUEUE_ADD, null);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void SetTitle()
    {
        PhraseSet set = AppDatabase.getInstance(getContext()).phraseSetDao().GetPhraseSet(phraseSetId);
        TextView title = root.findViewById(R.id.playListName);
        title.setText(set.Name);
    }

    private void GetPhrases()
    {
        phrases = AppDatabase.getInstance(getContext()).phraseDao().GetSetPhrases(phraseSetId);
    }

}
