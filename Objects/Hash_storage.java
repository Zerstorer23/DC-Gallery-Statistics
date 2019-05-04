package gallery.Objects;

import java.util.ArrayList;
import java.util.HashMap;

public class Hash_storage {
    public static HashMap<String,Integer> UserHash = new HashMap<>();
    public static ArrayList<User> Userlist = new ArrayList<>();
    static ArrayList<String> foundUsers = new ArrayList<>();
    public static Month[][] months = new Month[3][12];
                                      //[Year:(갤러리 설립년도 = 0)] [Month: 1월 = 0]

    public static HashMap<String,Integer> vocabHash = new HashMap<>();
    public static ArrayList<String> topKeywords = new ArrayList<>();


}
