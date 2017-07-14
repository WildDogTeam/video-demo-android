package com.wilddog.demo.bean;

/**
 * Created by fly on 17-6-15.
 */

public class RecordFileData {
    private String fileName;
    private String duration;
    public RecordFileData(){
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "RecordFileData{" +
                "fileName='" + fileName + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
