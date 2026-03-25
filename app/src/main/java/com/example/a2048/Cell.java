package com.example.a2048;

public class Cell {
    private int val;

    public Cell(){
        this.val = 0;
    }
    public Cell(int val) { this.val = val; } // copy constructor

    public int getVal(){
        return this.val;
    }

    public void setVal(int val){
        this.val = val;
    }
}
