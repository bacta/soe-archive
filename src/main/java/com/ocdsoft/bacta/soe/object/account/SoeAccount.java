package com.ocdsoft.bacta.soe.object.account;

import com.ocdsoft.network.object.account.Account;

import java.util.List;

/**
 * Created by Kyle on 4/3/14.
 */
public interface SoeAccount extends Account {

    List<CharacterInfo> getCharacterList();
    void setAuthToken(String token);
    String getAuthToken();
    void setAuthExpiration(long time);
    long getAuthExpiration();
    void setLastCharacterCreationTime(long time);
    long getLastCharacterCreationTime();
    List<CharacterInfo> getDeletedCharacterList();
    void addCharacter(CharacterInfo info);
}
