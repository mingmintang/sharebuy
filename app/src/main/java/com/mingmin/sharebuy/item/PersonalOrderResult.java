package com.mingmin.sharebuy.item;

import com.mingmin.sharebuy.cloud.PersonalOrderDoc;

import java.io.Serializable;

public class PersonalOrderResult implements Serializable {
    public PersonalOrderDoc personalOrderDoc;
    public String imagePath;
    public String uid;

    public PersonalOrderResult(PersonalOrderDoc personalOrderDoc, String imagePath, String uid) {
        this.personalOrderDoc = personalOrderDoc;
        this.imagePath = imagePath;
        this.uid = uid;
    }
}
