package com.example.BotEng4.JPA;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;


@Entity(name = "lessonTable")
@NoArgsConstructor
@Table(name = "lesson")
public class LessonEntity {

    @Id
    private Long chatId;
    @Column(name = "Monday_time")
    private String mn;
    @Column(name = "Tuesday_time")
    private String tu;
    @Column(name = "Wednesday_time")
    private String we;
    @Column(name = "Thursday_time")
    private String th;
    @Column(name = "Friday_time")
    private String fr;
    @Column(name = "Saturday_time")
    private String st;
    @Column(name = "Sunday_time")
    private String sn;



    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getMn() {
        return mn;
    }

    public void setMn(String mn) {
        this.mn = mn;
    }

    public String getTu() {
        return tu;
    }

    public void setTu(String tu) {
        this.tu = tu;
    }

    public String getWe() {
        return we;
    }

    public void setWe(String we) {
        this.we = we;
    }

    public String getTh() {
        return th;
    }

    public void setTh(String th) {
        this.th = th;
    }

    public String getFr() {
        return fr;
    }

    public void setFr(String fr) {
        this.fr = fr;
    }

    public String getSt() {
        return st;
    }

    public void setSt(String st) {
        this.st = st;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }
}
