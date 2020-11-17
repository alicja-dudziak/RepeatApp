package com.example.repeatapp.ui.importfile;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.repeatapp.EditList;
import com.example.repeatapp.MainActivity;
import com.example.repeatapp.R;
import com.example.repeatapp.file.ExcelExporter;
import com.example.repeatapp.file.FileCSV;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ImportFragment extends Fragment
{

    View root;
    List resultList;
    String fileName;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        root = inflater.inflate(R.layout.fragment_importfile, container, false);

        Button selectFileButton = root.findViewById(R.id.selectFileButton);
        selectFileButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OnSelectButtonClick();
            }
        });

        return root;
    }

    private void OnSelectButtonClick()
    {
        Intent intent = new Intent().setAction(Intent.ACTION_GET_CONTENT);

        intent.setType("*/*");
        String[] mimeTypes = {"application/excel", "text/csv", "text/xls", "application/vnd.ms-excel", "application/x-excel", "application/x-msexcel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 123 && resultCode == RESULT_OK)
        {
            if(resultList != null)
            {
                resultList.clear();
            }

            Uri selectedFileUri = data.getData();
            try
            {
                ContentResolver cR = root.getContext().getContentResolver();
                String mime = cR.getType(selectedFileUri);
                fileName = GetFileName(selectedFileUri);
                if(mime.equals("text/csv"))
                {
                    InputStream fileInputStream = root.getContext().getContentResolver().openInputStream(selectedFileUri);

                    FileCSV csvFile = new FileCSV();
                    resultList = csvFile.Read(fileInputStream);
                    GoToEditListView();
                }
                else
                {
                    InputStream fileInputStream = root.getContext().getContentResolver().openInputStream(selectedFileUri);
                    resultList = ExcelExporter.Import(fileInputStream);
                    GoToEditListView();
                }
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void GoToEditListView()
    {
        List<String[]> csvList = resultList;

        if(resultList == null || resultList.size() == 0 || csvList.get(0).length != 2)
        {
            Toast.makeText(root.getContext(), "Invalid file selected.", Toast.LENGTH_LONG).show();
        }
        else
        {
            Bundle bundle = new Bundle();
            bundle.putSerializable("ImportedList", (Serializable)resultList);
            bundle.putString("FileName", fileName);

            MainActivity.navController.navigate(R.id.nav_edit_list, bundle);
        }
    }

    public String GetFileName(Uri uri)
    {
        String result = null;
        if (uri.getScheme().equals("content"))
        {
            Cursor cursor = root.getContext().getContentResolver().query(uri, null, null, null, null);
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

}