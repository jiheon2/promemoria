package kopo.analyzeservice.dto;

public class DateCountDTO {
    private String date;
    private long count;

    public DateCountDTO(String date, long count) {
        this.date = date;
        this.count = count;
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}

