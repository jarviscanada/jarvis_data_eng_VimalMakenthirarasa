package ca.jrvs.apps.stockquote.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Date;
import java.sql.Timestamp;

public class Quote {
    private String ticker;
    private double open;
    private double high;
    private double low;
    private double price;
    private int volume;
    private Date latestTradingDay;
    private double previousClose;
    private double change;
    private String changePercent;
    private Timestamp timestamp;

    @JsonProperty("01. symbol")
    public String getTicker() {
        return ticker;
    }

    @JsonProperty("01. symbol")
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    @JsonProperty("02. open")
    public double getOpen() {
        return open;
    }

    @JsonProperty("02. open")
    public void setOpen(double open) {
        this.open = open;
    }

    @JsonProperty("03. high")
    public double getHigh() {
        return high;
    }

    @JsonProperty("03. high")
    public void setHigh(double high) {
        this.high = high;
    }

    @JsonProperty("04. low")
    public double getLow() {
        return low;
    }

    @JsonProperty("04. low")
    public void setLow(double low) {
        this.low = low;
    }

    @JsonProperty("05. price")
    public double getPrice() {
        return price;
    }

    @JsonProperty("05. price")
    public void setPrice(double price) {
        this.price = price;
    }

    @JsonProperty("06. volume")
    public int getVolume() {
        return volume;
    }

    @JsonProperty("06. volume")
    public void setVolume(int volume) {
        this.volume = volume;
    }

    @JsonProperty("07. latest trading day")
    public Date getLatestTradingDay() {
        return latestTradingDay;
    }

    @JsonProperty("07. latest trading day")
    public void setLatestTradingDay(Date latestTradingDay) {
        this.latestTradingDay = latestTradingDay;
    }

    @JsonProperty("08. previous close")
    public double getPreviousClose() {
        return previousClose;
    }

    @JsonProperty("08. previous close")
    public void setPreviousClose(double previousClose) {
        this.previousClose = previousClose;
    }

    @JsonProperty("09. change")
    public double getChange() {
        return change;
    }

    @JsonProperty("09. change")
    public void setChange(double change) {
        this.change = change;
    }

    @JsonProperty("10. change percent")
    public String getChangePercent() {
        return changePercent;
    }

    @JsonProperty("10. change percent")
    public void setChangePercent(String changePercent) {
        this.changePercent = changePercent;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}