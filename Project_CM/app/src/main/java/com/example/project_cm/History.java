package com.example.project_cm;

import java.io.Serializable;
import java.util.Date;

public class History implements Serializable {

    private Date date;
    private int portionSize;

    public History () {}

    public History (Date date, int portionSize) {
        this.date = date;
        this.portionSize = portionSize;
    }

    public Date getDate() {
        return date;
    }

    public int getPortionSize() {
        return portionSize;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setPortionSize(int portionSize) {
        this.portionSize = portionSize;
    }
}
