package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Balance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;

@Component
public class AccountJdbcDAO implements AccountDao{

    private JdbcTemplate jdbcTemplate;

    public AccountJdbcDAO(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Balance getBalance(String user) {
        Balance balance = new Balance();

        String sql = "SELECT balance FROM accounts " +
                "JOIN users ON accounts.user_id = users.user_id " +
                "WHERE users.username = ?";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, user);
        if(results.next()){
            balance.setBalance(results.getBigDecimal("balance"));
        }
        return balance;
    }

    @Override
    public int getAccountId(int userId) throws Exception {
        String sql = "SELECT account_id FROM accounts WHERE user_id = ?";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql,userId);

        if(results.next()){
            return results.getInt("account_id");
        } else {
            return -1;
        }
    }

    public boolean updateBalance(int userId, Balance newBalance) {
        try {
            String sql = "UPDATE accounts SET balance = ? WHERE user_id = ?";
            jdbcTemplate.update(sql, newBalance.getBalance().doubleValue(), userId);
            return true;
        } catch (Exception e){
            return false;
        }
    }

}
