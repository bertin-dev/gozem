package com.gozem.test.model;

public class Root {

    public String type = null;
    public Content content = null;

    public Root(String type, Content content) {
        this.type = type;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }
}
