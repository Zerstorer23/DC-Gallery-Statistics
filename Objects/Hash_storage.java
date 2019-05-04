package gallery.Objects;

import java.util.ArrayList;
import java.util.HashMap;

public class Hash_storage {
    public static HashMap<String,Integer> UserHash = new HashMap<>(); // 아래의 유저리스트에서 유저 index를 빨리찾게 돕는 해쉬
    public static ArrayList<User> Userlist = new ArrayList<>(); //유저 리스트
    static ArrayList<String> foundUsers = new ArrayList<>(); //뭔지 까먹음
    public static Month[][] months = new Month[3][12];//3 12값은 설정단계에서 알아서 바뀝니다.
                                      //[Year:(갤러리 설립년도 = 0)] [Month: 1월 = 0]

    public static HashMap<String,Integer> vocabHash = new HashMap<>();
    public static ArrayList<String> topKeywords = new ArrayList<>();

    public static ArrayList<String> outTextBox = new ArrayList<>();
    public static void console(String  a){
        outTextBox.add(a);
        System.out.println(a);
    }

}
