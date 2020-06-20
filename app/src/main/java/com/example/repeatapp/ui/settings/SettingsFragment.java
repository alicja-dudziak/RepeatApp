package com.example.repeatapp.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.repeatapp.R;
import com.example.repeatapp.database.AppDatabase;

public class SettingsFragment extends Fragment
{
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        root = inflater.inflate(R.layout.fragment_settings, container, false);

        SetUpValues();
        CreateListeners();

        return root;
    }

    private void SetUpValues()
    {
        SeekBar englishSpeed = root.findViewById(R.id.englishSpeed);
        TextView englishProgress = root.findViewById(R.id.englishSpeedProgress);
        int englishReaderSpeed = AppDatabase.getInstance(root.getContext()).userDao().GetEnglishReaderSpeed();

        englishSpeed.setProgress(englishReaderSpeed);
        englishProgress.setText(englishReaderSpeed +"/"+englishSpeed.getMax());

        SeekBar polishSpeed = root.findViewById(R.id.polishSpeed);
        TextView polishProgress = root.findViewById(R.id.polishSpeedProgress);
        int polishReaderSpeed = AppDatabase.getInstance(root.getContext()).userDao().GetPolishReaderSpeed();

        polishSpeed.setProgress(polishReaderSpeed);
        polishProgress.setText(polishReaderSpeed +"/"+polishSpeed.getMax());
    }

    private void CreateListeners()
    {
        CreateEnglishReaderSpeedListener();
        CreatePolishReaderSpeedListener();
    }

    private void CreateEnglishReaderSpeedListener()
    {
        SeekBar englishSpeed = root.findViewById(R.id.englishSpeed);
        final TextView englishProgress = root.findViewById(R.id.englishSpeedProgress);
        englishSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                englishProgress.setText(progress + "/" + seekBar.getMax());
                AppDatabase.getInstance(root.getContext()).userDao().SetEnglishReaderSpeed(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void CreatePolishReaderSpeedListener()
    {
        SeekBar polishSpeed = root.findViewById(R.id.polishSpeed);
        final TextView polishProgress = root.findViewById(R.id.polishSpeedProgress);
        polishSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                polishProgress.setText(progress + "/" + seekBar.getMax());
                AppDatabase.getInstance(root.getContext()).userDao().SetPolishReaderSpeed(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }
}
