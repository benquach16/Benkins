package app.service;

import java.lang.reflect.Array;

public class Config {

    private String id;
    private String[] repos;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getRepos() {return repos;}

    public void setRepos(String[] repos) {
        for (int i = 0; i < repos.length; i++) {
            repos[i] = repos[i].replace("string:", "");
        }
        this.repos = repos;
    }
}