package com.brgk.placetomeet.models;

public class CategoryElement {

    private String name;
    private int id;
    private int img;
    private boolean checked;

    public CategoryElement(String name, int id, int img) {
        this.name = name;
        this.id = id;
        this.img = img;
        this.checked = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

}