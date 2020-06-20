package com.example.repeatapp.file;

import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class FileCSV
{
    public List Read(InputStream stream)
    {
        List result = new ArrayList();
        String delimiter = null;

        BufferedReader fileReader = new BufferedReader(new InputStreamReader(stream));
        try
        {
            String csvLine;
            while ((csvLine = fileReader.readLine()) != null)
            {
                if(delimiter == null)
                {
                    if (csvLine.contains(","))
                        delimiter = ",";
                    else if (csvLine.contains(";"))
                        delimiter = ";";
                }

                String[] row = csvLine.split(delimiter);
                result.add(row);
            }
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Error while reading CSV file: "+ex);
        }
        finally
        {
            try
            {
                stream.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error with CSV file: "+e);
            }
        }

        return result;
    }

    public boolean Write(String fileName, List<String[]> data)
    {
        CSVWriter writer = null;
        try
        {
            FileOutputStream stream = new FileOutputStream("/storage/emulated/0/Documents/" + fileName + ".csv");

            writer = new CSVWriter(new OutputStreamWriter(stream, "UTF-8"));
            writer.writeAll(data);
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error while writing to csv file.\n" + e.getMessage());
        }

        return true;
    }

}
