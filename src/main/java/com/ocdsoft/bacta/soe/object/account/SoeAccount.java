package com.ocdsoft.bacta.soe.object.account;

import com.ocdsoft.bacta.engine.object.account.Account;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyle on 4/3/14.
 */
@Data
public class SoeAccount implements Account {

    @Setter(AccessLevel.NONE)
    private int id;
    private String username;
    private String password;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private List<CharacterInfo> characterList = new ArrayList<>();

    String authToken = "";
    long authExpiration;
    long lastCharacterCreationTime;

    @Getter(AccessLevel.NONE)
    final String type = "account";

    public SoeAccount() {}

    public SoeAccount(int id) {
        this.id = id;
    }

    public List<CharacterInfo> getCharacterList() {
        List<CharacterInfo> newList = new ArrayList<>();
        for(CharacterInfo info : characterList) {
            if(!info.isDisabled()) {
                newList.add(info);
            }
        }
        return newList;
    }

    public List<CharacterInfo> getDeletedCharacterList() {
        List<CharacterInfo> newList = new ArrayList<>();
        for(CharacterInfo info : characterList) {
            if(info.isDisabled()) {
                newList.add(info);
            }
        }
        return newList;    }

    public void addCharacter(CharacterInfo info) {
        characterList.add(info);
    }
}
