package gallery.Objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static gallery.Objects.Hash_storage.Userlist;
import static gallery.Objects.Hash_storage.topKeywords;
import static gallery.Objects.Hash_storage.vocabHash;
import static gallery.Preprocessor.sortHashMapByValues;

public class User {
    public String name;
    public String UID;
    public Date firstWrite;
    public Date lastWrite;
    public Date activeMonths;
    int activeDays;
    public int totalWrite;
    //  ArrayList<String> sentences = new ArrayList<>();
    public HashMap<String, Integer> vocab_freq = new HashMap<>();
    private HashMap<String, Integer> name_freq = new HashMap<>();

    public User(String nick, String UID) {
        if (UID == null) {
            this.name = nick;
        } else {
            this.UID = UID;
        }
        this.totalWrite = 1;
        name_freq.put(nick, 1);
    }

    public void addSentence(String content) {
        //      sentences.add(content);
        String[] token = content.split(" ");
        for (int i = 0; i < token.length; i++) {
            if (token[i].length() <= 1) continue;
            if (Pattern.matches("[a-zA-Z]+", token[i])) continue;
            if (vocab_freq.containsKey(token[i])) {
                vocab_freq.replace(token[i], vocab_freq.get(token[i]) + 1);
            } else {
                vocab_freq.put(token[i], 1);
            }
        }
    }

    public void calcActivity() {
        int firstDays = firstWrite.year * 365 + firstWrite.month * 31 + firstWrite.day;
        int lastDays = lastWrite.year * 365 + lastWrite.month * 31 + lastWrite.day;
        int active = lastDays - firstDays;
        this.activeDays = active;
        int y, m, d;
        y = active / 365;
        active = active % 365;
        m = active / 31;
        active = active % 31;
        d = active;
        activeMonths = new Date(y, m, d);
        //     System.out.println(lastWrite.toString()+" - "+firstWrite.toString()+" = "+activeMonths.toString());
    }

    public void updateDate(Date nd) {
        if (firstWrite == null || lastWrite == null) {
            firstWrite = nd;
            lastWrite = nd;
        } else {
            int firstDays = firstWrite.year * 365 + firstWrite.month * 31 + firstWrite.day;
            int lastDays = lastWrite.year * 365 + lastWrite.month * 31 + lastWrite.day;
            int newdays = nd.year * 365 + nd.month * 31 + nd.day;
            if (newdays < firstDays) firstWrite = nd;
            if (newdays > lastDays) lastWrite = nd;
        }
    }

    private void sortMap() {
        this.vocab_freq = sortHashMapByValues(this.vocab_freq);
    }

    public void setName() {
        if (name !=null) return;
        Map.Entry<String, Integer> maxEntry = null;
        for (Map.Entry<String, Integer> entry : name_freq.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        if (maxEntry != null) {
            this.name = maxEntry.getKey();
        }
    }

    public void getInterestKeyword(int max) {
        sortMap();
        int c = 0;
        for (Map.Entry<String, Integer> entry : vocab_freq.entrySet()) {
            System.out.print((c + 1) + ". " + entry.getKey());
            if (topKeywords.contains(entry.getKey())) {
                System.out.println(" (갤러리 공통 키워드)");
                max++;
            } else {
                System.out.println(" ");
            }
            c++;
            if (c >= max) break;
        }
    }

    public void printInfo(int minimum) {
        if (this.activeDays < minimum) return;
        int period = 0;
        if (activeDays > 0) {
            period = activeDays / totalWrite;
        }
        System.out.println(this.name + " " + this.UID);
        System.out.println("├━━활동기간: " + this.activeMonths.readableString());
        System.out.println("├━━글 작성:" + this.totalWrite);
        if (period > 0) {
            System.out.println("└━━" + period + "일에 글 하나");
        } else {
            System.out.println("└━━ 매일");
        }


        System.out.println(" ");
    }

    public void updateName(String writer) {
        if (name_freq.containsKey(writer)) {
            int freq = name_freq.get(writer) + 1;
            name_freq.replace(writer, freq);
        } else {
            name_freq.put(writer, 1);
        }
    }
}
