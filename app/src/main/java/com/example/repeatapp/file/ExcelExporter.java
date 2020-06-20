package com.example.repeatapp.file;

import android.os.Environment;

import com.example.repeatapp.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class ExcelExporter {

    public static void Export(String filePath, List<String[]> data)
    {
        try {
            File file = new File(filePath);
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale(Locale.forLanguageTag("pl").getLanguage(), Locale.forLanguageTag("pl").getCountry()));
            WritableWorkbook workbook;
            workbook = Workbook.createWorkbook(file, wbSettings);

            WritableSheet sheet = workbook.createSheet( "RepeatApp", 0);

            int rowIndex = 0;
            for(String[] row : data)
            {
                sheet.addCell(new Label(0, rowIndex, row[0]));
                sheet.addCell(new Label(1, rowIndex, row[1]));
                rowIndex++;
            }

            workbook.write();
            workbook.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static List Import(InputStream stream)
    {
        Workbook workbook= null;
        List result = new ArrayList();

        try
        {
            workbook = Workbook.getWorkbook(stream);
            Sheet sheet = workbook.getSheet(0);
            int rows = sheet.getRows();

            for(int i = 0; i < rows; i++)
            {
                String polishPhrase = sheet.getCell(0, i).getContents();
                String englishPhrase = sheet.getCell(1, i).getContents();
                String[] row = {polishPhrase, englishPhrase};

                result.add(row);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (BiffException e)
        {
            e.printStackTrace();
        }

        return result;
    }
}