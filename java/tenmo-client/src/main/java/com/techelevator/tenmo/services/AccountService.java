package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Balance;
import com.techelevator.tenmo.model.User;
import org.apiguardian.api.API;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class AccountService {

    private static final String API_BASE_URL = "http://localhost:8080/";
    private RestTemplate restTemplate;
    private AuthenticatedUser currentUser;
    HttpEntity entity;


    public AccountService(RestTemplate restTemplate, AuthenticatedUser currentUser) {
        this.restTemplate = restTemplate;
        this.currentUser = currentUser;
        this.entity = new HttpEntity(makeHttpHeaders(currentUser));
    }

    public void sendingMoney(BigDecimal amountToSend, int userIdSelection){
        HttpEntity<BigDecimal> sendMoneyEntity = new HttpEntity(amountToSend, makeHttpHeaders(currentUser));
        restTemplate.exchange(API_BASE_URL + "users/" + userIdSelection, HttpMethod.PUT, sendMoneyEntity, Boolean.class).getBody();
    }

    public Balance getBalance() {
        return restTemplate.exchange(API_BASE_URL +"balance", HttpMethod.GET, entity, Balance.class).getBody();
    }

    public void displayUsers(List<User> userList){
        System.out.println("-------------------------------");
        System.out.println("Users");
        System.out.println("ID\t\tName");
        System.out.println("-------------------------------");

        for(User user : userList){
            if(!currentUser.getUser().getId().equals(user.getId())) {
                System.out.println(user.getId() + "\t" + user.getUsername());
            }
        }
        System.out.println("-----------\n\n");
    }

    public List<User> getAllUsers() {
        User[] userArray = restTemplate.exchange(API_BASE_URL + "users", HttpMethod.GET,
                this.entity,
                User[].class).getBody();
        List<User> userList = Arrays.asList(userArray);
        return userList;
    }

    public int getAccountId(int userId) {
        try{
            int accountId = restTemplate.exchange(API_BASE_URL + "users/" + userId + "/account",
                    HttpMethod.GET, this.entity, Integer.class).getBody();
            return accountId;
        } catch (Exception ex) {
            System.out.println("AccountId not found " + ex.getMessage());
            return -1;
        }
    }

    public boolean validUserCheck(int userId, List<User> userList) {
        for(User user : userList) {
            if(user.getId() == userId) {
                return true;
            }
        }
        return false;
    }

    private HttpHeaders makeHttpHeaders(AuthenticatedUser currentUser) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(currentUser.getToken());
        return httpHeaders;
    }

}
