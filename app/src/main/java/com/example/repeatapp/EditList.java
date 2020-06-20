package com.example.repeatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.repeatapp.database.AppDatabase;
import com.example.repeatapp.database.entities.Phrase;
import com.example.repeatapp.database.entities.PhraseSet;
import com.example.repeatapp.file.ExcelExporter;
import com.example.repeatapp.file.FileCSV;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditList extends Fragment {

    List<Pair<Integer, LinearLayout>> phrases = new ArrayList<>();
    Set<Integer> deletedPhrases = new HashSet<>();
    ProgressDialog mDialog;
    long phraseSetId;
    View root;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = inflater.inflate(R.layout.activity_edit_list, container, false);

        Bundle arguments = getArguments();
        phraseSetId = arguments == null ? 0 : arguments.getLong("PhraseSetId", 0);

        AddButtonsListeners();

        mDialog = new ProgressDialog(getContext());
        mDialog.setMessage("Saving...");
        mDialog.setCancelable(false);

        if(phraseSetId != 0)
        {
            ShowRemoveButton();
            FillPhraseSet(phraseSetId);
        }

        List importedList = arguments == null ? null : (List) arguments.getSerializable("ImportedList");
        if(importedList != null)
        {
            String fileName = arguments.getString("FileName");
            CreateListFromImportedFile(importedList, fileName);
        }

        onSharedIntent();

        return root;
    }

    private boolean VerifyStoragePermissions(Activity activity)
    {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        return permission == PackageManager.PERMISSION_GRANTED;
    }

    private void onSharedIntent()
    {
        Intent receivedIntent = getActivity().getIntent();
        String receivedAction = receivedIntent.getAction();
        String receivedType = receivedIntent.getType();

        if (receivedAction != null && receivedAction.equals(Intent.ACTION_SEND))
        {
            Bundle bundle = receivedIntent.getExtras();
            Uri selectedFileUri = (Uri)bundle.get(Intent.EXTRA_STREAM);

            List resultList = null;
            if (receivedType.equals("text/csv"))
            {
                InputStream fileInputStream = null;
                try {
                    fileInputStream = getContext().getContentResolver().openInputStream(selectedFileUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                FileCSV csvFile = new FileCSV();
                resultList = csvFile.Read(fileInputStream);
            }
            else
            {
                InputStream fileInputStream = null;
                try {
                    fileInputStream = getContext().getContentResolver().openInputStream(selectedFileUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                resultList = ExcelExporter.Import(fileInputStream);
            }

            String fileName = GetFileName(selectedFileUri);
            CreateListFromImportedFile(resultList, fileName);
        }
    }

    private void CreateListFromImportedFile(List data, String fileName)
    {
        CreateImportedPhrases(data);

        EditText listName = root.findViewById(R.id.listName);
        listName.setText(fileName);
    }

    public String GetFileName(Uri uri)
    {
        String result = null;
        if (uri.getScheme().equals("content"))
        {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }

        int dotIndex = result.indexOf('.');
        String fileWithoutExtension = result.substring(0, dotIndex);

        return fileWithoutExtension;
    }

    private boolean CreateImportedPhrases(List phraseList)
    {
        List<String[]> csvList = phraseList;

        if(csvList == null || csvList.size() == 0 || csvList.get(0).length != 2)
        {
            Toast.makeText(getContext(), "Invalid file selected.", Toast.LENGTH_LONG).show();
            return false;
        }
        List<Phrase> importedPhrases = new ArrayList<>();

        for (String[] row: csvList)
        {
            Phrase phrase = new Phrase();
            phrase.TranslatedPhrase = row[0];
            phrase.PhraseText = row[1];
            importedPhrases.add(phrase);
        }

        for(Phrase phrase : importedPhrases)
        {
            CreateNewPhraseInputs(phrase);
        }

        return true;
    }

    private void ShowRemoveButton()
    {
        ImageView removeButton = root.findViewById(R.id.deleteButton);
        removeButton.setVisibility(View.VISIBLE);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowRemoveConfirmDialog();
            }
        });
    }

    private void ShowRemoveConfirmDialog()
    {
        new AlertDialog.Builder(getContext(), R.style.MyDialogTheme)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Removing list")
                .setMessage("Are you sure you want to remove list?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RemovePhraseSet();
                        //finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    private void RemovePhraseSet()
    {
        PhraseSet set = AppDatabase.getInstance(getContext()).phraseSetDao().GetPhraseSet(phraseSetId);
        AppDatabase.getInstance(getContext()).phraseSetDao().Delete(set);
        AppDatabase.getInstance(getContext()).phraseDao().DeleteSetPhrases(phraseSetId);
    }

    private void FillPhraseSet(long phraseSetId)
    {
        PhraseSet set = AppDatabase.getInstance(getContext()).phraseSetDao().GetPhraseSet(phraseSetId);
        EditText listName = root.findViewById(R.id.listName);
        listName.setText(set.Name);

        List<Phrase> setPhrases = AppDatabase.getInstance(getContext()).phraseDao().GetSetPhrases(phraseSetId);

        for(Phrase phrase : setPhrases)
        {
            CreateNewPhraseInputs(phrase);
        }
    }

    private void AddButtonsListeners()
    {
        CreateAddPhraseListener();
        CreateSaveButtonListener();
        CreateExportButtonListener();
    }

    private void CreateExportButtonListener()
    {
        Button exportButton = root.findViewById(R.id.exportButton);

        if(phraseSetId != 0)
        {
            exportButton.setVisibility(View.VISIBLE);
            exportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExportList();
                }
            });
        }
    }

    private void CreateSaveButtonListener()
    {
        ImageView saveButton = root.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveSet();
            }
        });
    }

    private void ExportList()
    {
        if(VerifyStoragePermissions(getActivity()) && Validate())
        {
            FileCSV csv = new FileCSV();

            EditText listName = root.findViewById(R.id.listName);
            String fileName = listName.getText().toString();
            List<String[]> phrases = GetPhrases();

            ExcelExporter.Export("/storage/emulated/0/Documents/" + fileName + ".xls", phrases);
            //if(csv.Write(fileName, phrases))
            //{
                Toast.makeText(getContext(), "List successfully exported to Documents folder", Toast.LENGTH_LONG).show();
            //}
        }
    }

    private List<String[]> GetPhrases()
    {
        List<String[]> result = new ArrayList<>();

        for(Pair<Integer, LinearLayout> pair : phrases)
        {
            EditText polishPhrase = (EditText) pair.second.getChildAt(0);
            EditText englishPhrase = (EditText) pair.second.getChildAt(1);

            String polish = polishPhrase.getText().toString();
            String english = englishPhrase.getText().toString();
            try
            {
                polish = new String(polish.getBytes("UTF-8"));
                english = new String(english.getBytes("UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }

            String[] row = { polish, english };
            result.add(row);
        }

        return result;
    }

    private void SaveSet()
    {
        if(Validate())
        {
            mDialog.show();
            DeleteRemovedPhrases();
            long newPhraseSetId = SavePhraseSet();
            SavePhrases(newPhraseSetId);
            mDialog.cancel();
            Intent myIntent = new Intent(getContext(), MainActivity.class);
            this.startActivity(myIntent);
            //finish();
        }
    }

    private void SavePhrases(long phraseSetId)
    {
        for(Pair<Integer, LinearLayout> pair : phrases)
        {
            if(pair.first == 0)
            {
                InsertNewPhrase(phraseSetId, pair.second);
            }
            else
            {
                UpdatePhrase(phraseSetId, pair.first, pair.second);
            }
        }
    }

    private void UpdatePhrase(long phraseSetId, int phraseId, LinearLayout phraseLine)
    {
        EditText polishPhrase = (EditText) phraseLine.getChildAt(0);
        EditText englishPhrase = (EditText) phraseLine.getChildAt(1);

        Phrase phraseToUpdate = new Phrase();
        phraseToUpdate.PhraseId = phraseId;
        phraseToUpdate.PhraseSetId = phraseSetId;
        phraseToUpdate.PhraseText = englishPhrase.getText().toString();
        phraseToUpdate.TranslatedPhrase = polishPhrase.getText().toString();

        AppDatabase.getInstance(getContext()).phraseDao().UpdatePhrase(phraseToUpdate);
    }

    private void InsertNewPhrase(long phraseSetId, LinearLayout phraseLine)
    {
        EditText polishPhrase = (EditText) phraseLine.getChildAt(0);
        EditText englishPhrase = (EditText) phraseLine.getChildAt(1);

        Phrase newPhrase = new Phrase();
        newPhrase.PhraseSetId = phraseSetId;
        newPhrase.PhraseText = englishPhrase.getText().toString();
        newPhrase.TranslatedPhrase = polishPhrase.getText().toString();

        AppDatabase.getInstance(getContext()).phraseDao().InsertPhrase(newPhrase);
    }

    private long SavePhraseSet()
    {
        EditText listName = root.findViewById(R.id.listName);

        if(phraseSetId != 0)
        {
            PhraseSet setToUpdate = AppDatabase.getInstance(getContext()).phraseSetDao().GetPhraseSet(phraseSetId);
            setToUpdate.Name = listName.getText().toString();
            AppDatabase.getInstance(getContext()).phraseSetDao().Update(setToUpdate);
        }
        else
        {
            PhraseSet newSet = new PhraseSet();
            newSet.Name = listName.getText().toString();
            phraseSetId = AppDatabase.getInstance(getContext()).phraseSetDao().Insert(newSet);
        }

        return phraseSetId;
    }

    private void DeleteRemovedPhrases()
    {
        if(deletedPhrases.size() > 0)
        {
            for(int phraseId : deletedPhrases)
            {
                if(phraseId != 0)
                {
                    AppDatabase.getInstance(getContext()).phraseDao().DeletePhrase(phraseId);
                }
            }
        }
    }

    private boolean Validate()
    {
        boolean isValidListName = ValidateListName();
        boolean areValidInputs = ValidateInputs();

        return isValidListName && areValidInputs;
    }

    private boolean ValidateListName()
    {
        EditText listName = root.findViewById(R.id.listName);

        if(listName.getText().toString().isEmpty())
        {
            listName.setHintTextColor(getResources().getColor(R.color.validationColor));

            return false;
        }

        return true;
    }

    private boolean ValidateInputs()
    {
        boolean isValid = true;

        if(phrases.size() == 0)
        {
            Toast toast = Toast.makeText(getContext(), "Add at least one phrase", Toast.LENGTH_LONG);
            TextView v = toast.getView().findViewById(android.R.id.message);
            v.setTextColor(getResources().getColor(R.color.validationColor));
            toast.show();

            return false;
        }

        for(Pair<Integer, LinearLayout> pair : phrases)
        {
            EditText polishPhrase = (EditText) pair.second.getChildAt(0);
            EditText englishPhrase = (EditText) pair.second.getChildAt(1);

            if(polishPhrase.getText().toString().isEmpty())
            {
                isValid = false;
                polishPhrase.setHintTextColor(getResources().getColor(R.color.validationColor));
            }

            if(englishPhrase.getText().toString().isEmpty())
            {
                isValid = false;
                englishPhrase.setHintTextColor(getResources().getColor(R.color.validationColor));
            }
        }

        return isValid;
    }

    private void CreateAddPhraseListener()
    {
        Button addPhrase = root.findViewById(R.id.addPhrase);
        addPhrase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewPhraseInputs(null);
            }
        });
    }

    private void CreateNewPhraseInputs(Phrase phrase)
    {
        LinearLayout phraseList = root.findViewById(R.id.phraseList);

        final LinearLayout phraseLine = new LinearLayout(getContext());
        phraseLine.setOrientation(LinearLayout.HORIZONTAL);
        phraseLine.setBackground(getContext().getDrawable(R.drawable.play_list_edit_text));
        LinearLayout.LayoutParams phraseLineParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        phraseLineParams.bottomMargin = 30;
        phraseLine.setLayoutParams(phraseLineParams);

        EditText polishPhrase = CreateEditText("Polish phrase");
        EditText englishPhrase = CreateEditText("English phrase");

        ImageView removeButton = new ImageView(getContext());
        removeButton.setImageResource(R.drawable.delete_icon);
        LinearLayout.LayoutParams removeButtonParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        removeButtonParams.weight = (float) 0.1;
        removeButtonParams.setMargins(20, 0, 20, 0);
        removeButton.setLayoutParams(removeButtonParams);
        removeButton.setPadding(0, 30, 0 ,30);

        phraseLine.addView(polishPhrase);
        phraseLine.addView(englishPhrase);
        phraseLine.addView(removeButton);

        phraseList.addView(phraseLine);
        final Pair<Integer, LinearLayout> pair;

        if(phrase != null)
        {
            polishPhrase.setText(phrase.TranslatedPhrase);
            englishPhrase.setText(phrase.PhraseText);

            pair = new Pair<>(phrase.PhraseId, phraseLine);

            phrases.add(pair);
        }
        else
        {
            pair = new Pair<>(0, phraseLine);
            phrases.add(pair);
        }

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemovePhrase(pair);
            }
        });

        if(phrase == null)
        {
            FocusEditText(polishPhrase);
        }
    }

    private EditText CreateEditText(String hint)
    {
        final EditText editText = new EditText(getContext());
        editText.setTextColor(Color.WHITE);
        editText.setTextSize(18);
        editText.setHint(hint);
        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        editTextParams.weight = (float) 0.5;
        editTextParams.setMargins(20, 0, 20, 0);

        editText.setLayoutParams(editTextParams);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                editText.setHintTextColor(Color.DKGRAY);
            }
        });

        return editText;
    }

    private void RemovePhrase(Pair<Integer, LinearLayout> pair)
    {
        LinearLayout phraseList = root.findViewById(R.id.phraseList);
        phraseList.removeView(pair.second);

        phrases.remove(pair);
        deletedPhrases.add(pair.first);
    }

    private void FocusEditText(EditText edit)
    {
        edit.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
    }
}
