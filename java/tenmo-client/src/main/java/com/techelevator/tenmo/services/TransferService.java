package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TransferService {
    //need entity/body/ - need curruser for this
    private static final String API_BASE_URL = "http://localhost:8080/";
    private static final int TRANSFER_STATUS_PENDING = 1; // pending
    private static final int TRANSFER_STATUS_APPROVED = 2;
    private static final int TRANSFER_STATUS_REJECTED = 3;
    private static final int TRANSFER_TYPE_REQUEST = 1;
    private static final int TRANSFER_TYPE_SEND = 2;


    private RestTemplate restTemplate;
    HttpEntity entity;
    AuthenticatedUser currentUser;
    private Map<Integer,String> transferTypeMap = Map.of(1,"Request", 2,"Send");
    private Map<Integer,String> transferStatusMap = Map.of(1,"Pending",2,"approved",3,"Rejected");

    public TransferService(RestTemplate restTemplate, AuthenticatedUser currentUser) {
        this.restTemplate = restTemplate;
        this.currentUser = currentUser;
        this.entity = new HttpEntity(makeHttpHeaders(currentUser));
    }


    public boolean requestingMoney(Transfer transferRequest, AuthenticatedUser currentUser, int userIdSelection){
        HttpEntity<Transfer> requestMoneyEntity = new HttpEntity(transferRequest, makeHttpHeaders(currentUser));
        return restTemplate.exchange(API_BASE_URL + "user/request", HttpMethod.POST, requestMoneyEntity, Boolean.class).getBody();
    }


    public void listAllUserTransfers(Map<Integer, List<Transfer>> transfersMap) {
        User user = null;
        System.out.println(
                "-------------------------------------------\n" +
                "Transfers\n" +
                "ID\t\tFrom/To\t\tAmount\n" +
                "-------------------------------------------");
        for(Map.Entry<Integer, List<Transfer>> entry : transfersMap.entrySet()) {
            for(int i = 0; i < entry.getValue().size(); i++) {
                Transfer currentTransfer = entry.getValue().get(i);
                Integer accountId = entry.getKey();

                if(accountId != currentTransfer.getAccountIdTo()){
                    user = getUserByAccountId(currentTransfer.getAccountIdTo());
                    System.out.println(currentTransfer.getTransferId() + "\tTo: " + user.getUsername() + "\t$" +
                            currentTransfer.getAmount());
                } else if(accountId != currentTransfer.getAccountIdFrom()){
                    user = getUserByAccountId(currentTransfer.getAccountIdFrom());
                    System.out.println(currentTransfer.getTransferId() + "\tFrom: " + user.getUsername() + "\t$" +
                            currentTransfer.getAmount());
                }

            }

        }
    }

    public void listPendingTransfers(Map<Integer, List<Transfer>> transfersMap){
        User user = null;
        System.out.println(
                "-------------------------------------------\n" +
                        "Transfers\n" +
                        "ID\t\tTo\t\tAmount\n" +
                        "-------------------------------------------");
        for(Map.Entry<Integer, List<Transfer>> entry : transfersMap.entrySet()) {
            for(int i = 0; i < entry.getValue().size(); i++) {
                Transfer currentTransfer = entry.getValue().get(i);
                Integer accountId = entry.getKey();
                if(currentTransfer.getTransferStatusId() == TRANSFER_STATUS_PENDING) {
                    if (accountId != currentTransfer.getAccountIdTo()) {
                        user = getUserByAccountId(currentTransfer.getAccountIdTo());
                        System.out.println(currentTransfer.getTransferId() + "\tTo: " + user.getUsername() + "\t$" +
                                currentTransfer.getAmount());
                    }
                }
            }
        }
    }

    public void showTransferDetails(List<Transfer> transferList, int transferId){
        User sender = null;
        User recipient = null;
        System.out.println(
                "--------------------------------------------\n" +
                "Transfer Details\n" +
                "--------------------------------------------\n"
                );
        for(Transfer transfer : transferList){

            if(transfer.getTransferId() == transferId){
                sender = getUserByAccountId(transfer.getAccountIdFrom());
                recipient = getUserByAccountId(transfer.getAccountIdTo());

                System.out.println("Id: " + transferId);
                System.out.println("From: " + sender.getUsername());
                System.out.println("To: " + recipient.getUsername());
                System.out.println("Type: " + transferTypeMap.get(transfer.getTransferTypeId()));
                System.out.println("Status: " + transferStatusMap.get(transfer.getTransferStatusId()));
                System.out.println("Amount: $" + transfer.getAmount());
            }
        }
    }

    public boolean updateRequest(List<Transfer> transferList, int transferId, int newStatus) {
        for(Transfer transfer : transferList) {
            if(transfer.getTransferId() == transferId) {
                transfer.setTransferStatusId(newStatus);
                HttpEntity<Transfer> transferRequestEntity = new HttpEntity(transfer, makeHttpHeaders(currentUser));
                boolean accepted = restTemplate.exchange(API_BASE_URL + "user/transfers/" + transferId, HttpMethod.PUT,
                        transferRequestEntity, Boolean.class).getBody();
                return accepted;
            }
        }
        return false;
    }


    public boolean isValidTransfer(int transferId, List<Transfer> transferList, int accountId) {
        for(Transfer transfer : transferList) {
            if(transfer.getTransferId() == transferId) {
                if(accountId != transfer.getAccountIdTo()){
                    return true;
                }
            }
        }
        return false;
    }


    public Map<Integer, List<Transfer>> getTransfersMap() {
        ParameterizedTypeReference<Map<Integer, List<Transfer>>> responseType =
                new ParameterizedTypeReference<Map<Integer, List<Transfer>>>() {};
        return restTemplate.exchange(API_BASE_URL + "user/transfers", HttpMethod.GET, entity, responseType).getBody();
    }

    public User getUserByAccountId(int id){
        User user = restTemplate.exchange( API_BASE_URL + "users/account/" + id, HttpMethod.GET,
                this.entity,
                User.class).getBody();
        return user;
    }


    private HttpHeaders makeHttpHeaders(AuthenticatedUser currentUser) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(currentUser.getToken());
        return httpHeaders;
    }
}
