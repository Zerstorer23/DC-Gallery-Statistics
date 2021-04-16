package gallery.Objects;

import java.util.*;
import java.util.regex.Pattern;

import static gallery.Main.extractKeywords_fast;
import static gallery.Objects.Hash_storage.*;
import static gallery.Preprocessor.sortHashMapByValues;
import static java.util.stream.Collectors.toMap;

public class Month {
    private Date date;
    public int totalView;
    public int totalPost;
    public int totalReply;
    public int avgStay;
    public ArrayList<String> newUserIDs = new ArrayList<>();
    public HashMap<String, Integer> vocab_freq = new HashMap<>();

    public Month() {

    }

    public Month(int y, int m) {
        date = new Date(y, m, 0);
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

    public void appendView(int a) {
        this.totalView = this.totalView + a;
    }

    public void incrementPost() {
        this.totalPost++;

    }

    public void appendReply(int a) {
        this.totalReply = this.totalReply + a;
    }

    public void appendUser(String ID) {
        if (!foundUsers.contains(ID)) {
            foundUsers.add(ID);
            //  newUserIDs.add(ID);
        }
    }
    public void calculateAvgStay(){
        int total = 0;
        for (String newUserID : newUserIDs) {
            int index = UserHash.get(newUserID);
            total = total + Userlist.get(index).activeDays;
        }
        if(newUserIDs.size()==0){
            this.avgStay = 0;
        }else{
            this.avgStay = total / (newUserIDs.size());

        }
    }

    public String monthToString() {
        return " ";
    }

    public void getInterestKeyword(int max) {
        //  vocab_freq = sortHashMapByValues(vocab_freq);
        vocab_freq = extractKeywords_fast(vocab_freq,false);
        vocab_freq = vocab_freq
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
        int c = 0;
        for (Map.Entry<String, Integer> entry : vocab_freq.entrySet()) {
            if (entry.getKey().length() > 2) {
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
    }
}
