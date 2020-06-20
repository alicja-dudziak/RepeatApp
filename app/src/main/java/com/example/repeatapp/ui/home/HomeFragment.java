package com.example.repeatapp.ui.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import com.example.repeatapp.EditList;
import com.example.repeatapp.MainActivity;
import com.example.repeatapp.PlayList;
import com.example.repeatapp.R;
import com.example.repeatapp.database.AppDatabase;
import com.example.repeatapp.database.entities.PhraseSet;

import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);

        CreateSets();
        AddButtonListener();

        return root;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        Refresh();
        MainActivity.RefreshPoints();
    }

    private void AddButtonListener()
    {
        Button addList = root.findViewById(R.id.addNewList);

        addList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenAddNewListActivity();
            }
        });
    }

    private void Refresh()
    {
        LinearLayout setsContainer = root.findViewById(R.id.sets);
        setsContainer.removeAllViews();

        CreateSets();
    }

    private void OpenAddNewListActivity()
    {
        MainActivity.navController.navigate(R.id.nav_edit_list);
    }

    private void CreateSets()
    {
        LinearLayout setsContainer = root.findViewById(R.id.sets);
        Context context = root.getContext();
        List<PhraseSet> sets = AppDatabase.getInstance(context).phraseSetDao().GetAllSets();

        for(PhraseSet set : sets)
        {
            setsContainer.addView(CreateSetLine(set, context));
            setsContainer.addView(CreateSeparator(context));
        }
    }

    private LinearLayout CreateSeparator(Context context)
    {
        LinearLayout separator = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(620, 4);
        params.setMargins(150,0,20,60);
        separator.setLayoutParams(params);
        separator.setBackgroundColor(Color.WHITE);

        return separator;
    }

    private LinearLayout CreateSetLine(final PhraseSet set, final Context context)
    {
        final LinearLayout setLine = new LinearLayout(context);
        setLine.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100);
        lineParams.setMargins(30,10,150,20);
        setLine.setLayoutParams(lineParams);

        final ImageView favouriteIcon = new ImageView(context);
        if(set.IsFavourite)
        {
            favouriteIcon.setImageResource(R.drawable.full_star_icon);
        }
        else
        {
            favouriteIcon.setImageResource(R.drawable.star_icon);
        }
        LinearLayout.LayoutParams favouriteIconParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        favouriteIconParams.gravity = Gravity.BOTTOM;
        favouriteIconParams.weight = (float) 0.1;
        favouriteIconParams.rightMargin = 30;
        favouriteIcon.setLayoutParams(favouriteIconParams);
        favouriteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnFavouriteClick(set, context, favouriteIcon);
            }
        });

        TextView setName = new TextView(context);

        if(set.TimesDone > 0)
        {
            setName.setText(set.Name + " (" + set.TimesDone +")");
            setName.setTextColor(Color.argb(128, 255, 255, 255));
        }
        else
        {
            setName.setText(set.Name);
            setName.setTextColor(Color.WHITE);
        }

        setName.setTextSize(22);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        nameParams.weight = (float) 0.7;
        setName.setLayoutParams(nameParams);

        ImageView editButton = new ImageView(context);
        editButton.setImageResource(R.drawable.edit_icon);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        iconParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        iconParams.weight = (float) 0.1;
        iconParams.rightMargin = 50;
        editButton.setLayoutParams(iconParams);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPhraseSet(set, context);
            }
        });

        ImageView playButton = new ImageView(context);
        playButton.setImageResource(R.drawable.play_icon);
        LinearLayout.LayoutParams playIconParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        playIconParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        playIconParams.weight = (float) 0.1;
        playIconParams.rightMargin = 50;
        playButton.setLayoutParams(playIconParams);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaySet(set, context);
            }
        });

        setLine.addView(favouriteIcon);
        setLine.addView(setName);
        setLine.addView(editButton);
        setLine.addView(playButton);

        return setLine;
    }

    private void OnFavouriteClick(PhraseSet set, Context context, ImageView icon)
    {
        PhraseSet clickedSet = AppDatabase.getInstance(context).phraseSetDao().GetPhraseSet(set.PhraseSetId);
        clickedSet.IsFavourite = !clickedSet.IsFavourite;

        AppDatabase.getInstance(context).phraseSetDao().Update(clickedSet);
        Refresh();
    }


    private void PlaySet(PhraseSet set, Context context)
    {
        Bundle bundle = new Bundle();
        bundle.putLong("PhraseSetId", set.PhraseSetId);

        MainActivity.navController.navigate(R.id.nav_play_list, bundle);
    }

    private void EditPhraseSet(PhraseSet set, Context context)
    {
        Bundle bundle = new Bundle();
        bundle.putLong("PhraseSetId", set.PhraseSetId);

        MainActivity.navController.navigate(R.id.nav_edit_list, bundle);
    }
}