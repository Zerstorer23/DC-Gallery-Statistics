package gallery;

import gallery.Crawler.Crawler;
import gallery.Crawler.Crawler_DC_mx_title;
import gallery.Crawler.Crawler_DC_mx_title_group;
import gallery.Objects.Month;
import gallery.Objects.User;

import java.util.*;

import static gallery.Objects.Hash_storage.*;
import static gallery.Preprocessor.*;
import static gallery.readSetting.galleryYear;
import static gallery.readSetting.read;
import static java.util.stream.Collectors.toMap;

public class Main {
    public static boolean major = false;
    public static String mainPage;
    public static String gallID = "haruhiism";
    public static int MAXPAGE = 700; // 71
    public static int increment = 1;
    public static boolean mergeGonick = true;

    private static boolean verbose = false;//디버그용.true로 하면 더 많은 정보가 나온다/
    static int kToken = 3;//키워드 찾기 그룹변수. 3으로 설정한다.
    static int minimumActiveDays = 7; //유저가 너무 많을때 활동일 n일 이상만 출력한다 (그 이하 유저도 정보는 저장된다.)
    public static int minimumFrequency = 20; //유저가 너무 많을때 작성수 n 이상만 출력한다 (그 이하 유저도 정보는 저장된다.)
    static boolean crawlKeywords = true; //true 설정시 키워드 분석도 돌아간다.
    private static Crawler crawler = new Crawler_DC_mx_title_group();

    public static void main(String[] args) {
        read("setting.txt"); //설정 읽어들이고 날짜 오브젝트 생성
        if (!mergeGonick) crawler = new Crawler_DC_mx_title();
        crawler.scrollRaw(); //갤러리 스크롤
        lock(5); // 5초마다 스크롤이 완료되었는지 확인
        crawler.polishUp(); // 완료 확인시 마무리작업

        if (crawlKeywords) {
            grandKeyFrequency(50); //갤러리 전체 키워드 계산 (필수)
            printTopFrequency(100); //가장 높은 n개 키워드 출력
        }
        monthlyAnalysis();
        crawler.writeCSV();
        System.out.println(minimumActiveDays);
        System.out.println(minimumFrequency);
        // write your code here
    }


    //키워드 클러스터링 알고리즘. 나도 설명못함
    public static HashMap extractKeywords_fast(HashMap<String, Integer> passedMap, boolean verbose) {
        if (verbose) System.out.println("클러스터링 시작");
        Map<String, Integer> hash = sortHashMapByKeys(passedMap);
        ArrayList<String> word = new ArrayList<>();
        ArrayList<Integer> freq = new ArrayList<>();
        // 1 Pass
        for (Map.Entry<String, Integer> entry : hash.entrySet()) {
            word.add(entry.getKey());
            freq.add(entry.getValue());
            //  System.out.println(entry.getKey()+","+entry.getValue());
        }
        int curr = 0;
        if (verbose) System.out.println("1 순환 완료");

        //2 Pass
        while (curr < word.size()) {
            if (verbose) System.out.println(curr + " / " + word.size());
            String parent = word.get(curr);
            if (parent.length() < kToken) {
                curr++;
                continue;
            }
            int childIndex = curr + 1;

            while (childIndex < word.size()) {
                String child = word.get(childIndex);
                if (parent.length() == 0) break;
                if (child.length() > 0) {
                    if (child.charAt(0) != parent.charAt(0)) {
                        break;
                    }
                }

                int lendiff = Math.abs(parent.length() - child.length());
                if (lendiff > 3) {
                    childIndex++;
                    continue;
                }
                if (parent.length() >= kToken && child.length() >= kToken) {
                    if (parent.substring(0, kToken).equals(child.substring(0, kToken))) {
                        if (freq.get(curr) < freq.get(childIndex)) {
                            word.set(curr, word.get(childIndex));
                        }
                        freq.set(curr, freq.get(curr) + freq.get(childIndex));
                        freq.remove(childIndex);
                        word.remove(childIndex);
                    } else {
                        break;
                    }
                }
                childIndex++;
            }
            curr++;
        }
        if (verbose) System.out.println("2 순환 완료");

        //3 pass
        HashMap<String, Integer> keyHash = new HashMap<>();
        for (int i = 0; i < word.size(); i++) {
            keyHash.put(word.get(i), freq.get(i));
        }
        if (verbose) System.out.println("3 순환 완료");
        return keyHash;
    }

    /*
     * Builds Total Frequency map.
     */
    private static void grandKeyFrequency(int max) {
        verbose = true;
        for (User haruhi : Userlist) {
            for (Map.Entry<String, Integer> entry : haruhi.vocab_freq.entrySet()) {
                if (vocabHash.containsKey(entry.getKey())) {
                    vocabHash.replace(entry.getKey(), vocabHash.get(entry.getKey()) + entry.getValue());
                } else {
                    vocabHash.put(entry.getKey(), entry.getValue());
                }
            }
        }

        vocabHash = extractKeywords_fast(vocabHash, true);
        System.out.println("정렬중 ... ");
        vocabHash = vocabHash
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
        //
        //vocabHash = sortHashMapByValues(vocabHash);
        System.out.println("정렬 완료");
        verbose = false;
        int c = 0;
        for (Map.Entry<String, Integer> entry : vocabHash.entrySet()) {
            topKeywords.add(entry.getKey());
            c++;
            if (c >= max) break;
        }

    }

    /*
     * Prints out int max from total frequency
     * */
    private static void printTopFrequency(int max) {
        int c = 0;
        for (Map.Entry<String, Integer> entry : vocabHash.entrySet()) {
            System.out.println((c + 1) + ". " + entry.getKey() + "  [" + entry.getValue() + "]회");
            c++;
            if (c >= max) break;
        }

    }

    private static void monthlyAnalysis() {
        for (int y = 0; y < months.length; y++) {
            for (int m = 0; m < 12; m++) {
                Month haruhi = months[y][m];
                if (haruhi != null) {
                    System.out.println("━━━━━━━" + (galleryYear + y) + "년 " + (m + 1) + "월 통계");
                    System.out.println("━━━ 신규 유입 유저: " + haruhi.newUserIDs.size() + " 명");
                    if (haruhi.vocab_freq.size() > 0) {
                        haruhi.getInterestKeyword(5);
                        for (int i = 0; i < haruhi.newUserIDs.size(); i++) {
                            int index = UserHash.get(haruhi.newUserIDs.get(i));
                            Userlist.get(index).printInfo(minimumActiveDays);
                        }
                    }
                } else {
                    System.out.println("━━━━━━━" + (galleryYear + y) + "년 " + (m + 1) + "월 자료없음");
                }
            }
        }

    }


}
