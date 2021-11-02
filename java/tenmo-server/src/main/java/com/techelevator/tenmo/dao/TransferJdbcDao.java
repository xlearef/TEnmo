package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@PreAuthorize("isAuthenticated()")
public class TransferJdbcDao implements TransferDao {


    JdbcTemplate jdbcTemplate;

    public TransferJdbcDao(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean createTransfer(Transfer transfer){
        //transfer id, transfer_type_id, transfer_status_id, account_from, account_to, amount

        try {
            String sql = "INSERT INTO transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                    "VALUES(?,?,?,?,?)";
            jdbcTemplate.update(sql, transfer.getTransferTypeId(), transfer.getTransferStatusId(),
                    transfer.getAccountIdFrom(), transfer.getAccountIdTo(), transfer.getAmount());
            return true;
        } catch(Exception e){
            System.out.println("Create Transfer Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Transfer> getAllTransfersByAccountId(int accountId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                "FROM transfers WHERE account_from = ? OR account_to = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId, accountId);
        while(results.next()) {
            transfers.add(mapRowToTransfer(results));
        }
        return transfers;
    }

    @Override
    public boolean updateTransfer(Transfer transfer){
        try {
            String sql = "UPDATE transfers SET transfer_type_id = ?, transfer_status_id = ? " +
                    "WHERE transfer_id = ?";
            jdbcTemplate.update(sql, transfer.getTransferTypeId(), transfer.getTransferStatusId(), transfer.getTransferId());
            return true;
        } catch(Exception ex){
            return false;
        }
    }


    @Override
    public int getTransferId() {
        return 0;
    }

    @Override
    public int getTransferTypeId() {
        return 0;
    }

    @Override
    public int getTransferStatusId() {
        return 0;
    }

    @Override
    public int getAccountFromId() {
        return 0;
    }

    @Override
    public int getAccountToId() {
        return 0;
    }

    @Override
    public BigDecimal getAmount() {
        return null;
    }

    private Transfer mapRowToTransfer(SqlRowSet rowSet) {
        int transferId = rowSet.getInt("transfer_id");
        int transferTypeId = rowSet.getInt("transfer_type_id");
        int transferStatusId = rowSet.getInt("transfer_status_id");
        int accountFrom = rowSet.getInt("account_from");
        int accountTo = rowSet.getInt("account_to");
        BigDecimal amount = new BigDecimal(rowSet.getString("amount"));
        Transfer transfer = new Transfer(transferId, transferTypeId, transferStatusId, accountFrom, accountTo, amount);
        return transfer;
    }


}
