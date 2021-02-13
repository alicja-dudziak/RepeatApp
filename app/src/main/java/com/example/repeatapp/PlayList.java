package com.example.repeatapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
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
import com.example.repeatapp.ui.settings.MultiplierValue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class PlayList extends Fragment
{

    private long phraseSetId;
    private List<Phrase> phrases;
    private int currentPhrase = 0;
    private TextView polishPhrase;
    private TextView englishPhrase;
    private TextView info;
    private TextToSpeech polishTts;
    private TextToSpeech englishTts;
    private ProgressBar progressBar;
    private boolean isGameInProgress;
    private Handler hdlr = new Handler();
    private boolean pauseTheGameDelay;
    private Instant gameStartTime;
    private Instant startPauseTime;
    private int pauseDuration = 0;
    private View root;
    private Thread gameThread;
    private boolean isWaitingForSpeaker = false;
    private HashMap<String, String> polishTtsParams = new HashMap<>();
    private HashMap<String, String> englishTtsParams = new HashMap<>();
    private boolean isInitialStart = true;
    private Instant speakingStartTime;
    private Instant speakingEndTime;
    private int repeatCount;
    private int counter = 1;
    private UUID loopGuid;
    private double thinkTimeMultiplier;
    private double speakTimeMultiplier;
    private boolean isSecondTimeEnglishPhrase = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        root = inflater.inflate(R.layout.activity_play_list, container, false);

        phraseSetId = getArguments().getLong("PhraseSetId", 0);

        polishPhrase = root.findViewById(R.id.polishPhrase);
        englishPhrase = root.findViewById(R.id.englishPhrase);
        info = root.findViewById(R.id.whatToDo);
        progressBar = root.findViewById(R.id.progressBar);
        progressBar.setMax(100);

        repeatCount = AppDatabase.getInstance(getContext()).userDao().GetPhraseRepeatCount();
        UpdateRepeatCounter();
        thinkTimeMultiplier = MultiplierValue.GetMultiplierRaisedValue(AppDatabase.getInstance(getContext()).userDao().GetThinkTimeMultiplier());
        speakTimeMultiplier = MultiplierValue.GetMultiplierRaisedValue(AppDatabase.getInstance(getContext()).userDao().GetSpeakTimeMultiplier());

        polishTtsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "polishUtteranceId");
        englishTtsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "englishUtteranceId");

        SetTitle();
        GetPhrases();
        AddButtonsListeners();

        PrepareTextToSpeech();

        return root;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        gameThread.interrupt();
        hdlr.removeCallbacksAndMessages(null);
        polishTts.shutdown();
        englishTts.shutdown();
    }

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

    private void UpdateRepeatCounter()
    {
        TextView repeatCounter = root.findViewById(R.id.playListCounter);
        repeatCounter.setText(counter + "/" +repeatCount);
    }

    private void SkippCurrentPhrase()
    {
        Phrase phrase = phrases.get(currentPhrase);

        if(phrase.Skipped)
        {
            return;
        }

        phrase.Skipped = true;

        SetSkippButtonColor(true);
        Toast.makeText(getContext(), "Current phrase will be skipped.", Toast.LENGTH_LONG).show();
    }

    private void SetSkippButtonColor(boolean clicked)
    {
        ImageView skipButton = root.findViewById(R.id.skipButton);

        if(clicked)
        {
            skipButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.buttonColor), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
        else
        {
            skipButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorText), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
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

        if(gameThread != null) {
            gameThread.interrupt();
        }

        hdlr.removeCallbacksAndMessages(null);

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
        loopGuid = UUID.randomUUID();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            Instant endPauseTime = Instant.now();
            Duration timeElapsed = Duration.between(startPauseTime, endPauseTime);
            pauseDuration += timeElapsed.toMillis();
        }

        Phrase phrase = phrases.get(currentPhrase);
        if(phrase.Skipped)
        {
            NextPhrase(loopGuid);
        }
        else
        {
            StartTheGame();
        }
    }

    private void PrepareTextToSpeech()
    {
        polishTts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR)
                {
                    polishTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            speakingStartTime = Instant.now();
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            OnPolishWordEndSpeaking();
                        }

                        @Override
                        public void onError(String utteranceId) { }
                    });

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
                    englishTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            speakingStartTime = Instant.now();
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            OnEnglishWordEndSpeaking();
                        }

                        @Override
                        public void onError(String utteranceId) { }
                    });

                    int englishReaderSpeed = AppDatabase.getInstance(getContext()).userDao().GetEnglishReaderSpeed();

                    //englishTts.setLanguage(Locale.ENGLISH);
                    englishTts.setSpeechRate((float)0.2 * englishReaderSpeed);
                }
            }
        });
    }

    private void OnPolishWordEndSpeaking()
    {
        hdlr.post(new Runnable() {   // Omija błąd: Only the original thread that created a view hierarchy can touch its views.
            public void run() {
                info.setText("Think...");
            }
        });

        speakingEndTime = Instant.now();
        Duration timeElapsed = Duration.between(speakingStartTime, speakingEndTime);
        long milliSeconds = Math.round(timeElapsed.toMillis() * thinkTimeMultiplier);

        ShowProgressBar(milliSeconds);
        EnglishPhrase();
    }

    private void OnEnglishWordEndSpeaking()
    {
        hdlr.post(new Runnable() {   // Omija błąd: Only the original thread that created a view hierarchy can touch its views.
            public void run() {
                info.setText("Talk...");
            }
        });

        UUID currentLoopGuid = loopGuid;
        speakingEndTime = Instant.now();
        Duration timeElapsed = Duration.between(speakingStartTime, speakingEndTime);
        long milliSeconds = Math.round(timeElapsed.toMillis() * speakTimeMultiplier);

        ShowProgressBar(milliSeconds);

        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        if(!isSecondTimeEnglishPhrase) {
            EnglishPhrase();
            isSecondTimeEnglishPhrase = true;
        }
        else {
            NextPhrase(currentLoopGuid);
            isSecondTimeEnglishPhrase = false;
        }
    }

    private void StartTheGame()
    {
        info.setText("Listen...");
        if(getContext() == null)
        {
            return;
        }

        loopGuid = UUID.randomUUID();
        gameThread = new Thread(new Runnable() {
            public void run() {
                PolishPhrase();
            }
        });
        gameThread.start();
    }

    private void ShowProgressBar(long milliSeconds)
    {
        if(!isGameInProgress)
        {
            return;
        }

        if(milliSeconds < 1500)
        {
            milliSeconds = 1500;
        }

        for(int i=0; i <= 100; i++)
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
                Thread.sleep(milliSeconds / 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void EnglishPhrase()
    {
       hdlr.post(new Runnable() {   // Omija błąd: Only the original thread that created a view hierarchy can touch its views.
            public void run() {
                if(!isGameInProgress)
                {
                    return;
                }

                if(!polishTts.isSpeaking()) {
                    englishTts.setLanguage(Locale.ENGLISH);
                    Phrase phrase = phrases.get(currentPhrase);
                    englishPhrase.setVisibility(View.VISIBLE);
                    info.setText("Listen...");
                    englishPhrase.setText(phrase.PhraseText);
                    englishTts.speak(phrase.PhraseText, TextToSpeech.QUEUE_ADD, englishTtsParams);

                    phrase.RepeatedCount++;
                }
                else {
                    isWaitingForSpeaker = true;
                }
            }
        });
    }

    private void NextPhrase(final UUID loopId)
    {
        if(!isGameInProgress)
        {
            return;
        }

        hdlr.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void run() {
                if(!isGameInProgress || loopId != loopGuid)
                {
                    return;
                }

                if(IsAllRepeated())
                {
                    EndTheGame();
                    return;
                }

                SelectNextPhrase();
                StartTheGame();
            }
        });
    }

    private void SelectNextPhrase()
    {
        currentPhrase++;

        Phrase nextPhrase = null;

        while(nextPhrase ==null)
        {
            if(currentPhrase >= phrases.size())
            {
                counter++;
                currentPhrase = 0;
                UpdateRepeatCounter();
            }

            Phrase newPhrase = phrases.get(currentPhrase);

            if (!newPhrase.Skipped && newPhrase.RepeatedCount < repeatCount)
            {
                nextPhrase = newPhrase;
            }
            else
            {
                currentPhrase++;
            }
        }
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

        this.getFragmentManager().popBackStack();
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
            if(!phrase.Skipped && phrase.RepeatedCount < repeatCount)
                shouldRepeatCount++;
        }

        return shouldRepeatCount == 0;
    }

    private void PolishPhrase()
    {
        if(!isGameInProgress)
        {
            return;
        }

        if(isInitialStart || !englishTts.isSpeaking()) {
            polishTts.setLanguage(Locale.forLanguageTag("pl"));
            SetSkippButtonColor(false);
            isInitialStart = false;
            englishPhrase.setVisibility(View.INVISIBLE);

            Phrase phrase = phrases.get(currentPhrase);
            polishPhrase.setText(phrase.TranslatedPhrase);
            polishTts.speak(phrase.TranslatedPhrase, TextToSpeech.QUEUE_ADD, polishTtsParams);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
        }
        else {
            isWaitingForSpeaker = true;
        }

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
