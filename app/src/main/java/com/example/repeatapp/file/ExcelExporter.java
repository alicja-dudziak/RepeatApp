package com.example.repeatapp.file;

import android.os.Environment;

import com.example.repeatapp.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class ExcelExporter {

    public static void Export(String filePath, List<String[]> data)
    {

    }
    /*
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
            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("UTF-8");
            workbook = Workbook.getWorkbook(stream, ws);
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
    }*/

    public static List Import(InputStream stream) {
        List result = new ArrayList();

        try{
            Workbook wb = WorkbookFactory.create(stream);
            Sheet mySheet = wb.getSheetAt(0);

            Iterator<Row> rowIterator = mySheet.rowIterator();

            while(rowIterator.hasNext()){
                if(mySheet instanceof XSSFSheet) {
                    XSSFRow row = (XSSFRow) rowIterator.next();
                    String polishPhrase = row.getCell(0).getStringCellValue();
                    String englishPhrase = row.getCell(1).getStringCellValue();
                    String[] rowArray = {polishPhrase, englishPhrase};
                    result.add(rowArray);
                }
                else if(mySheet instanceof HSSFSheet) {
                    HSSFRow row = (HSSFRow) rowIterator.next();
                    String polishPhrase = row.getCell(0).getStringCellValue();
                    String englishPhrase = row.getCell(1).getStringCellValue();
                    String[] rowArray = {polishPhrase, englishPhrase};
                    result.add(rowArray);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }

}