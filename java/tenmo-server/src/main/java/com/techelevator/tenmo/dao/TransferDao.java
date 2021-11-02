package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {

    int getTransferId();

    int getTransferTypeId(); // 1 = request, 2 = send

    int getTransferStatusId(); // 1 = pending, 2 = approved, 3 = rejected

    int getAccountFromId(); // shows id of sender

    int getAccountToId(); // shows id of recipient

    BigDecimal getAmount();

    boolean createTransfer(Transfer transfer);

    List<Transfer> getAllTransfersByAccountId(int accountId);

    boolean updateTransfer(Transfer transfer);

}
