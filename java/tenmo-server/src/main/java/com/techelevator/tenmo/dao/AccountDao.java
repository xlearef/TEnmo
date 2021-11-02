package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Balance;

public interface AccountDao {

    //Maybe better as a int userId;
    Balance getBalance(String user);

    boolean updateBalance(int id, Balance balance);

    int getAccountId(int userId) throws Exception;

}
