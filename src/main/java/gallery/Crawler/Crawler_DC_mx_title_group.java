package gallery.Crawler;

import gallery.Objects.Date;
import gallery.Objects.Month;
import gallery.Objects.User;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Map;

import static gallery.Main.*;
import static gallery.Objects.Hash_storage.*;
import static gallery.Preprocessor.getPrinter;
import static gallery.readSetting.currentYear;
import static gallery.readSetting.galleryYear;


public class Crawler_DC_mx_title_group extends Crawler {


    /**
     * 갤러리의 모든 글 제목을 긁어내는 코드입니다.
     * 유지보수 쟁점
     * 고닉의 data-uid-nick를 체크해 같은 계정은 묶고 닉변한 계정은 가장 최근의 닉을 보여준다.
     */
    public void scrollRaw() {
        if (major) {
            mainPage = "http://gall.dcinside.com/board/lists/?id=" + gallID + "&list_num=100&sort_type=N&search_head=&page=";
        } else {
            mainPage = "http://gall.dcinside.com/mgallery/board/lists/?id=" + gallID + "&list_num=100&sort_type=N&search_head=&page=";
        }
        initCores();
        for (int c = 0; c < cores; c++) {
            int finalC = c;
            Thread go = new Thread(() -> {
                while (!CORES.get(finalC).isEmpty()) {
                    String tempPage = CORES.get(finalC).poll();
                    try {
                        Document doc = Jsoup.connect(tempPage).get();
                        Elements posts = doc.select("tbody").first().select("tr[class=ub-content],tr[class=ub-content us-post]");
                        //System.out.println("Frequency: "+posts.size());
                        assert posts != null;
                        for (Element post : posts) {
                            String type = post.select("td[class=gall_num]").first().text();
                            String mtype = post.select("td[class=gall_subject]").first().text();
                            if(mtype.equals("공지"))continue;
                            if (!isValidIndex(type)) continue;
                            String writer = post.select("td[class=gall_writer ub-writer]").attr("data-nick");
                            String writerUID = post.select("td[class=gall_writer ub-writer]").attr("data-uid-nick");
                            writer = cleanseNick(writer);
                            String ip = post.select("span[class=ip]").text();
                            String view = post.select("td[class=gall_count]").text();
                            //Replies
                            Element ReplyBox = post.select("span[class=reply_num]").first();
                            String replies = "0";
                            if (ReplyBox != null) {
                                replies = ReplyBox.text();
                                replies = removeChars(replies).split(" ")[0];
                            }

                            String date = post.select("td[class=gall_date]").text();
                        //    System.out.println(date+" date");
                            String[] dateToken = date.split("\\.");
                            //   System.out.println(date);
                            int year, month, day;
                            if(date.contains(":")) {
                                year = cal.get(Calendar.YEAR) - galleryYear -2000;
                           //     System.out.println(year+"="+cal.get(Calendar.YEAR)+" - "+galleryYear);
                                month = cal.get(Calendar.MONTH) + 1;
                                day = cal.get(Calendar.DATE);
                            }else if (dateToken.length < 3) {
                                year = currentYear - galleryYear;
                                month = Integer.parseInt(dateToken[0]);
                                day = Integer.parseInt(dateToken[1]);
                                //      System.out.println(year+","+month);
                            } else {
                                year = Integer.parseInt(dateToken[0]) - galleryYear;
                                month = Integer.parseInt(dateToken[1]);
                                day = Integer.parseInt(dateToken[2]);
                                //  System.out.println(year+","+month);
                            }
                            Date time = new Date(year, month, day);
                            String title = post.select("td[class=gall_tit ub-word],td[class=gall_tit ub-word voice_tit]").select("a").first().text();
                            title = removeChars(title);
                         /*   if(year-1 <0){
                                year++;
                                System.out.println("Found "+time.toString()+" / "+date);
                            }*/

                            if (months[year][month - 1] == null) {
                                months[year][month - 1] = new Month(year, month);
                                //    System.out.println("Found "+time.toString());
                            }
                            months[year][month - 1].incrementPost();
                            months[year][month - 1].appendReply(Integer.parseInt(replies));
                            months[year][month - 1].appendView(Integer.parseInt(view));
                            String UID;
                            if (ip == null) {
                                //고닉들. 고유 갤로그 id로 정리한다.
                                UID = writerUID;
                            } else if(writer.equals("ㅇㅇ")){
                                //ㅇㅇ 유동들. ip로 정리한다
                                writer = writer+ip;
                                UID = writer;
                            } else{
                                //유동인데 ㅇㅇ이 아닌녀석들. 닉으로 정리한다
                                //null로 보내면 오브젝트 생성단계에서 자동 이름 설정
                                UID = writer;
                            }
                            //User
                            if (!UserHash.containsKey(UID)) {
                                User user = new User(writer,UID);
                                user.addSentence(title);
                                user.updateDate(time);
                                UserHash.put(user.UID, Userlist.size());
                                Userlist.add(user);
                            } else {
                                int index = UserHash.get(UID);
                                Userlist.get(index).addSentence(title);
                                Userlist.get(index).updateDate(time);
                                Userlist.get(index).updateName(writer);
                                Userlist.get(index).totalWrite++;
                            }
                            months[year][month - 1].appendUser(UID);
                            months[year][month - 1].addSentence(title);
                        }
                    } catch (HttpStatusException e) {
                        e.printStackTrace();
                        readd(finalC, tempPage);
                    } catch (Exception e) {
                        e.printStackTrace();
                        readd(finalC, tempPage);
                    }
                }
            });
            go.start();
        }
    }

    @Override
    public void polishUp() {
        for (User aUserlist : Userlist) {
            aUserlist.calcActivity();
            int month, year;
            year = aUserlist.firstWrite.year;
            month = aUserlist.firstWrite.month - 1;
            months[year][month].newUserIDs.add(aUserlist.name);
        }
    }

    @Override
    public void writeCSV() {
        String mFilename = "excel_" + gallID + "_month.csv";
        String uFIlename = "excel_" + gallID + "_users.csv";
        String kFIlename = "excel_" + gallID + "_keys.csv";
        PrintWriter mpw = getPrinter(mFilename);
        //Write Months
        mpw.write("Date,totalPost,totalReply,totalView,newUser,avgStay\n");
        for (int y = 0; y < months.length; y++) {
            for (int m = 0; m < 12; m++) {
                Month haruhi = months[y][m];
                if (haruhi != null) {
                    haruhi.calculateAvgStay();
                    String content = (y + galleryYear + 2000) + "-" + (m + 1) + "-01," + haruhi.totalPost + "," + haruhi.totalReply + "," + haruhi.totalView + "," + haruhi.newUserIDs.size() + "," + haruhi.avgStay;
                    // System.out.println(content);
                    content = content + "\n";
                    mpw.write(content);
                }
            }
        }
        mpw.close();

        PrintWriter pw = getPrinter(uFIlename);
        pw.write("Name,fDate,lDate,activeDays,totalWrite\n");
        for (User haruhi : Userlist) {
            haruhi.calcActivity();
            String content = haruhi.name + "," + haruhi.firstWrite.toString() + "," + haruhi.lastWrite.toString() + "," + haruhi.activeMonths.toStringinDays() + "," + haruhi.totalWrite;
            content = content + "\n";
            pw.write(content);
        }
        pw.close();
        PrintWriter kpw = getPrinter(kFIlename);
        kpw.write("vocab,Freq\n");
        for (Map.Entry<String, Integer> entry : vocabHash.entrySet()) {
            if (entry.getValue() > minimumFrequency)
                kpw.write(entry.getKey() + "," + entry.getValue() + "\n");
        }
        kpw.close();
    }


}
