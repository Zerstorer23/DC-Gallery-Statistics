package gallery.Crawler;

import gallery.Objects.Month;
import gallery.Objects.User;

import java.io.PrintWriter;
import java.util.*;

import static gallery.Main.*;
import static gallery.Main.gallID;
import static gallery.Main.minimumFrequency;
import static gallery.Objects.Hash_storage.*;
import static gallery.Preprocessor.getPrinter;
import static gallery.readSetting.galleryYear;

/**
 * 갤러리 정보 긁는 크롤러
 * abstract 클래스로 만들어서 새로운 형태의 크롤러 제작시
 * 간단하게 복사 교체가능
 */
public abstract class Crawler {

    /*
    * 멀티스레드로
    * 여러개의 스레드가 각각 특정 페이지를 담당하여 긁는 원리
    * */

    //스레드별로 담당하는 갤러리 페이지 url을 큐로 저장

    static ArrayList<Queue<String>> CORES = new ArrayList<>();
    private static int[] core_sizes;
    public static int cores = 10;
    private static HashMap<String, Integer> attempts = new HashMap<>();
    void initCores() {
        for (int i = 0; i < cores; i++) {
            Queue<String> lists = new LinkedList<>();
            CORES.add(lists);
        }
        String tempPage;
        for (int p = 1; p <= MAXPAGE; p = p + increment) {
            tempPage = mainPage + p;
            //  System.out.println(tempPage);
            int index = p % cores;
            CORES.get(index).add(tempPage);
        }

        core_sizes = new int[cores];
        for (int i = 0; i < cores; i++) {
            core_sizes[i] = CORES.get(i).size();
        }
    }

    void readd(int coreID, String page) {
        System.out.println("Status exception: Re-add");
        if (attempts.containsKey(page)) {
            int tried = attempts.get(page);
            System.out.println(page + "  Attempts:" + tried);
            if (tried < 12) {
                CORES.get(coreID).add(page);
                attempts.replace(page, tried + 1);
            } else {
                System.out.println("Stop readding");
            }
        } else {
            CORES.get(coreID).add(page);
            attempts.put(page, 1);
            System.out.println("[NEW]" + page + "  Attempts:" + 1);
        }
        System.out.println("  Attempt SIZE=" + attempts.size());
    }
    private static String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";

    public abstract void scrollRaw();

    public abstract void polishUp();

    public abstract void writeCSV();

    private static String regexNumber ="\\d+";
    static boolean isValidIndex(String aa){
        return aa.matches(regexNumber);
    }
    // Collects sentences. need
    //          writeToFileRaw("network-"+gallID+"-Raw.txt");
    //         buildNetwork("network-"+gallID+"-Raw.txt");
    //         writeToFile("network-"+gallID+".txt");
    static String removeChars(String title) {
        title = title.replaceAll(match, " ");
        title = title.replace("dc", "");
        title = title.replace("official", "");
        title = title.replace("app", "");
        title = title.replace("앱에서", "");
        title = title.replace("작성", "");
        title = title.replace("http", "");
        title = title.replace("/", "");
        title = title.replace("\n", "");
        title = title.trim().replaceAll(" +", " ");
        return title.toLowerCase();
    }

    static String cleanseNick(String nick) {
        nick = nick.replace(",", ".");
        nick = nick.replace("/", ".");
        nick = nick.replace("\n", " ");
        nick = nick.trim().replaceAll(" +", " ");
        return nick;
    }

    public static boolean isReadyToBuild() {
        boolean ready = true;
        System.out.println(" ");
        for (int i = 0; i < cores; i++) {
            double total = core_sizes[i];
            int completed = (int) total - CORES.get(i).size();
            System.out.println("[CORE #" + i + "] Completed: " + completed + " | " + (int) ((completed / total) * 100) + "%");
            if (!CORES.get(i).isEmpty()) ready = false;
        }
        if (ready) {
            for (Map.Entry<String, Integer> attempt : attempts.entrySet()) {
                    int tried = attempt.getValue();
                    System.out.println(attempt.getKey()+" : "+attempt.getValue());
                    if (tried < 1) {
                        ready = false;
                        break;
                    }
            }
        }
        System.out.println(" ");
        return ready;
    }


    public void flushConsole() {
        String mFilename = "console_" + gallID + "_output.txt";
        PrintWriter mpw = getPrinter(mFilename);
        //Write Months
       for(String s : outTextBox){
           mpw.write(s+"\n");
       }
        mpw.close();
    }

}
