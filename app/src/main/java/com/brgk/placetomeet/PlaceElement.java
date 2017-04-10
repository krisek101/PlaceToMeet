package com.brgk.placetomeet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlaceElement {

    private String name;
    private int id;
    private int img;
    private List<String> categories;
    private boolean checked;

    public PlaceElement(String name, int id, int img, String[] categories) {
        this.name = name;
        this.id = id;
        this.img = img;
        this.categories = Arrays.asList(categories);
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

    public List<String> getCategories() {
        return this.categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

}