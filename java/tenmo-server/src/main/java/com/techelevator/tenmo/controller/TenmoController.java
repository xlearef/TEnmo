package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.*;
import com.techelevator.tenmo.model.Balance;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@PreAuthorize("isAuthenticated()")
public class TenmoController {

    @Autowired
    AccountDao accountDao;
    @Autowired
    UserDao userDao;
    @Autowired
    TransferDao transferDao;

    private static final int TRANSFER_STATUS_PENDING = 1; // pending
    private static final int TRANSFER_STATUS_APPROVED = 2;
    private static final int TRANSFER_STATUS_REJECTED = 3;
    private static final int TRANSFER_TYPE_REQUEST = 1;
    private static final int TRANSFER_TYPE_SEND = 2;


    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public Balance getBalance(Principal principal) {
        System.out.println(principal.getName());
        return accountDao.getBalance(principal.getName());
    }
    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> listUsers(){
        return userDao.findAll();
    }

    @RequestMapping(path = "/users/{id}", method = RequestMethod.GET)
    public User getUserById(@PathVariable int id) {
        User user = userDao.findUserById(id);
        return user;
    }
    @RequestMapping(path = "/users/account/{id}", method = RequestMethod.GET)
    public User getUserByAccountId(@PathVariable int id){
        User user = userDao.findUserByAccountId(id);
        return user;
    }

    @RequestMapping(path = "/users/{id}/account", method = RequestMethod.GET)
    public int getAccountIdByUserId(@PathVariable int id) throws Exception{
        int accountId = accountDao.getAccountId(id);
        return accountId;

    }

    @RequestMapping(path = "/users/{id}", method = RequestMethod.PUT)
    public boolean sendMoney(@PathVariable int id, Principal sender, @RequestBody BigDecimal transferAmount){
        System.out.println(id + " " + sender.getName() + " " + transferAmount);
        int senderId = userDao.findIdByUsername(sender.getName());
        int senderAccountId;
        int recipientAccountId;

        Balance recipientBalance = accountDao.getBalance(userDao.findUserById(id).getUsername());
        Balance senderBalance = accountDao.getBalance(sender.getName());

        try {
            senderAccountId = accountDao.getAccountId(senderId);
            recipientAccountId = accountDao.getAccountId(id);

            if(senderBalance.getBalance().compareTo(transferAmount) >= 0 && transferAmount.compareTo(new BigDecimal("0")) >= 0){
                senderBalance.setBalance(senderBalance.getBalance().subtract(transferAmount));
                recipientBalance.setBalance(recipientBalance.getBalance().add(transferAmount)); // math for balances

                accountDao.updateBalance(id, recipientBalance); // updateBalance sql update
                accountDao.updateBalance(senderId, senderBalance);

                System.out.println(senderBalance.getBalance() + " " + recipientBalance.getBalance());
                Transfer transfer = new Transfer(TRANSFER_TYPE_SEND, TRANSFER_STATUS_APPROVED, senderAccountId, recipientAccountId, transferAmount);
                transferDao.createTransfer(transfer);
                return true;
            }

            System.out.println("Rec: " + recipientBalance.getBalance());
            System.out.println("Send: " + senderBalance.getBalance());
        } catch (Exception e){
            System.out.println("Please enter a valid Account ID");
        }


        return false;
    }

    @RequestMapping(path = "/user/transfers",method = RequestMethod.GET)
    public Map<Integer, List<Transfer>> getUserTransfers(Principal user) {
        try{
            int userId = userDao.findIdByUsername(user.getName());
            int accountId = accountDao.getAccountId(userId);
            List<Transfer> transfers = transferDao.getAllTransfersByAccountId(accountId);
            Map<Integer, List<Transfer>> transfersMap = new HashMap<>();
            transfersMap.put(accountId, transfers);
            return transfersMap;

        } catch(Exception ex) {
            System.out.println("User id not found : " + ex.getMessage());
            return null;
        }
    }
    @RequestMapping(path = "user/request", method = RequestMethod.POST)
    public boolean createTransferRequest(@RequestBody Transfer transfer){

        try{
            transfer.setAccountIdFrom(accountDao.getAccountId(transfer.getAccountIdFrom())); // convert user id to account id
            transfer.setAccountIdTo(accountDao.getAccountId(transfer.getAccountIdTo()));
            transferDao.createTransfer(transfer);
            return true;
        } catch(Exception e){
            return false;
        }
    }
    @RequestMapping(path = "user/transfers/{id}" , method = RequestMethod.PUT)
    public boolean updateTransfer(@RequestBody Transfer transfer, @PathVariable int id){
        BigDecimal amount = transfer.getAmount();

        String fromUsername = userDao.findUserByAccountId(transfer.getAccountIdFrom()).getUsername();
        Balance fromBalance = accountDao.getBalance(fromUsername);
        Balance newFromBalance = new Balance(amount.subtract(fromBalance.getBalance()));

        String toUsername = userDao.findUserByAccountId(transfer.getAccountIdTo()).getUsername();
        Balance toBalance = accountDao.getBalance(toUsername);
        Balance newToBalance = new Balance(amount.add(toBalance.getBalance()));
        if(transfer.getTransferStatusId() == TRANSFER_STATUS_REJECTED){
            transferDao.updateTransfer(transfer);
            return true;
        }
        if(fromBalance.getBalance().compareTo(amount) >= 0 && amount.compareTo(new BigDecimal("0")) >= 0 &&
                transfer.getTransferStatusId() == TRANSFER_STATUS_APPROVED){
            //update transfer table with:
            transferDao.updateTransfer(transfer);
            //if transfer status is approved update accounts
            if(transfer.getTransferStatusId() == TRANSFER_STATUS_APPROVED) {
                //update From
                accountDao.updateBalance(transfer.getAccountIdFrom(), newFromBalance);
                //update To
                accountDao.updateBalance(transfer.getAccountIdTo(), newToBalance);
            }
            return true;
        }
        return false;
    }


}
