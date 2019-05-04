package gallery;

import gallery.Objects.Month;

import java.io.BufferedReader;
import java.io.IOException;

import static gallery.Crawler.Crawler.cores;
import static gallery.Main.*;
import static gallery.Objects.Hash_storage.months;
import static gallery.Preprocessor.getReader;

public class readSetting {
    private static int collectOnlyAfterThisPage = Integer.MAX_VALUE;
    public static int currentYear = 2019;
    public static int galleryYear = 2016;
    static void read(String fileName) {
        // Name + UserInfo + CVList+
        try {
            BufferedReader sc = null;
            sc = getReader(fileName);
            String temp;
            sc.readLine();
            while ((temp = sc.readLine()) != null) {
                if(temp.charAt(0)=='#')continue;
                String[] entry = temp.split(",");
                String head = entry[0];
                String data = entry[1];
                switch (head) {
                    case "gallID":
                        gallID = data;
                        break;
                    case "major":
                        major = Boolean.parseBoolean(data);
                        break;
                    case "maxPage":
                        MAXPAGE = Integer.parseInt(data);
                        break;
                    case "cores":
                        cores = Integer.parseInt(data);
                        break;
                    case "kToken":
                        kToken = Integer.parseInt(data);
                        break;
                    case "minimumActiveDays":
                        minimumActiveDays = Integer.parseInt(data);
                        break;
                    case "minimumFrequency":
                        minimumFrequency = Integer.parseInt(data);
                        break;
                    case "crawlKeywords":
                        crawlKeywords = Boolean.parseBoolean(data);
                        break;
                    case "galleryYear":
                        galleryYear = Integer.parseInt(data);
                        break;
                    case "currentYear":
                        currentYear = Integer.parseInt(data);
                        break;
                    case "collectOnlyAfterThisPage":
                        collectOnlyAfterThisPage = Integer.parseInt(data);
                        if (collectOnlyAfterThisPage <= 0) collectOnlyAfterThisPage = Integer.MAX_VALUE;
                        break;
                }
                System.out.println(head + " : " + data);
            }
            sc.close();

            galleryYear-=2000;
            currentYear-=2000;
            months = new Month[currentYear - galleryYear+1][12];
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
