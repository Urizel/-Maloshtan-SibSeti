package com.example.al.sibirski;

// XXX mutable model
public class ContainerPayment {
    private String date;
    private String text;
    private String sum;
    private boolean plus;

    public ContainerPayment(String date, boolean plus, String sum, String text) {
        this.date = date;
        this.plus = plus;
        this.sum = sum;
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isPlus() {
        return plus;
    }

    public void setPlus(boolean plus) {
        this.plus = plus;
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
