package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AccountService;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.tenmo.services.TransferService;
import com.techelevator.view.ConsoleService;
/*import org.apiguardian.api.API;*/
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.ls.LSOutput;

import java.math.BigDecimal;
import java.util.*;


public class App {

	private static final String API_BASE_URL = "http://localhost:8080/";

	private static final String MENU_OPTION_EXIT = "Exit";
	private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String APPROVE_TRANSFER = "Approve";
	private static final String REJECT_TRANSFER = "Reject";
	private static final String DONT_APPROVE_OR_REJECT = "Don't approve or reject";
	private static final int TRANSFER_STATUS_PENDING = 1; // pending
	private static final int TRANSFER_STATUS_APPROVED = 2;
	private static final int TRANSFER_STATUS_REJECTED = 3;
	private static final int TRANSFER_TYPE_REQUEST = 1;
	private static final int TRANSFER_TYPE_SEND = 2;


	private AuthenticatedUser currentUser;
	private ConsoleService console;
	private AuthenticationService authenticationService;
	private RestTemplate restTemplate;
	private AccountService accountService;
	private TransferService transferService;

	public static void main(String[] args) {
		App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL));
		app.run();
	}

	public App(ConsoleService console, AuthenticationService authenticationService) {
		this.console = console;
		this.authenticationService = authenticationService;
		this.restTemplate = new RestTemplate();
	}


	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");

		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() {
		while(true) {
			String choice = (String)console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if(MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				viewCurrentBalance();
			} else if(MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory();
			} else if(MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				viewPendingRequests();
			} else if(MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if(MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if(MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else {
				// the only other option on the main menu is to exit
				exitProgram();
			}
		}
	}

	private void viewCurrentBalance() {
		// TODO Auto-generated method stub
		Balance balance = accountService.getBalance();
		System.out.println("Your current account balance: $"+balance.getBalance());
	}

	private void viewTransferHistory() {
		// TODO Auto-generated method stub
		Map<Integer, List<Transfer>> transfersMap = transferService.getTransfersMap();
		transferService.listAllUserTransfers(transfersMap);
		Collection<List<Transfer>> collectionOfTransfers = transfersMap.values();
		List<List<Transfer>> listOfTransfers = new ArrayList<>(collectionOfTransfers);
		//listOfTransfers contains 1 key-value pair with accountId => List<Transfer>
		Integer transferId = console.getUserInputInteger("Please enter Transfer ID to view details (0 to cancel)");
		if(transferId != 0) {
			transferService.showTransferDetails(listOfTransfers.get(0), transferId);
		}
	}

	private void viewPendingRequests() {
		// TODO Auto-generated method stub
		Map<Integer, List<Transfer>> transfersMap = transferService.getTransfersMap();
		transferService.listPendingTransfers(transfersMap);
		Collection<List<Transfer>> collectionOfTransfers = transfersMap.values();
		List<List<Transfer>> listOfTransfers = new ArrayList<>(collectionOfTransfers);
		Integer transferId = console.getUserInputInteger("Please enter Transfer ID to approve/reject (0 to cancel)");
		int currentUserAccountId = accountService.getAccountId(this.currentUser.getUser().getId());
		if(transferService.isValidTransfer(transferId, listOfTransfers.get(0), currentUserAccountId)) {
			boolean approved = false;
			boolean rejected = false;
			String[] optionArray = {APPROVE_TRANSFER, REJECT_TRANSFER, DONT_APPROVE_OR_REJECT};
			String choice = (String) console.getChoiceFromOptions(optionArray);
			if (choice.equals(APPROVE_TRANSFER)) {
				//change transfer status to approve
				approved = transferService.updateRequest(listOfTransfers.get(0), transferId, TRANSFER_STATUS_APPROVED);

			} else if (choice.equals(REJECT_TRANSFER)) {
				//change transfer status to reject
				rejected = transferService.updateRequest(listOfTransfers.get(0), transferId, TRANSFER_STATUS_REJECTED);
			} else {
				//leave
			}
			if(approved) {
				System.out.println("Transfer was approved");
			} else if(rejected) {
				System.out.println("Transfer was rejected");
			} else {
				System.out.println("Transfer was denied");
			}
		} else {
			System.out.println("Not a valid Transfer. Please try again");
		}

	}

	private void sendBucks() {
		// TODO Auto-generated method stub
		List<User> userList = accountService.getAllUsers();
		accountService.displayUsers(userList);
		int userIdSelection = console.getUserInputInteger("Enter ID of user you are sending to (0 to cancel)");
		boolean validUserInput = accountService.validUserCheck(userIdSelection, userList);
		while (validUserInput == false && userIdSelection != 0) {
			userIdSelection = console.getUserInputInteger("Please enter a valid ID of user you are sending to (0 to cancel)");
			validUserInput = accountService.validUserCheck(userIdSelection, userList);
		}
		if(userIdSelection != 0 && userIdSelection != currentUser.getUser().getId()){
			String amountAsString = console.getUserInput("Enter amount");
			try {
				Double.parseDouble(amountAsString);
				BigDecimal amountToSendAsBigDecimal = new BigDecimal(amountAsString);
				accountService.sendingMoney(amountToSendAsBigDecimal, userIdSelection);
			} catch (NumberFormatException ex) {
				System.out.println("\nPlease enter a valid amount.");
			} catch (Exception ex) {

			}
		} else {
			System.out.println("Please enter a valid User ID (Cannot be your own)");
		}
	}

	private void requestBucks() {
		// TODO Auto-generated method stub
		List<User> userList = accountService.getAllUsers();
		accountService.displayUsers(userList);
		int userIdSelection = console.getUserInputInteger("Enter User ID to request from");
		if(userIdSelection != currentUser.getUser().getId() && accountService.validUserCheck(userIdSelection, userList)) {
			Integer amount = console.getUserInputInteger("Enter amount to request");
			BigDecimal amountAsBd = new BigDecimal(amount.toString());
			Transfer transfer = new Transfer(TRANSFER_TYPE_REQUEST, TRANSFER_STATUS_PENDING,
					userIdSelection, currentUser.getUser().getId(), amountAsBd);
			transferService.requestingMoney(transfer, currentUser, userIdSelection);
		} else {
			System.out.println("Please enter a valid User ID (Cannot be your own)");
		}


	}

	private void exitProgram() {
		System.exit(0);
	}



	private void registerAndLogin() {
		while(!isAuthenticated()) {
			String choice = (String)console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}

	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
		while (!isRegistered) //will keep looping until user is registered
		{
			UserCredentials credentials = collectUserCredentials();
			try {
				authenticationService.register(credentials);
				isRegistered = true;
				System.out.println("Registration successful. You can now login.");
			} catch(AuthenticationServiceException e) {
				System.out.println("REGISTRATION ERROR: "+e.getMessage());
				System.out.println("Please attempt to register again.");
			}
		}
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) //will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();

			try {
				currentUser = authenticationService.login(credentials);

				//Create services
				this.accountService = new AccountService(this.restTemplate, currentUser);
				this.transferService = new TransferService(this.restTemplate, currentUser);
				System.out.println("Welcome " + credentials.getUsername() + "!");

			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: Invalid credentials.");
				String input = console.getUserInput("Would you like to try logging in again? (Y/N)");
				if(input.equalsIgnoreCase("N")){
					break;
				}
			}
		}
	}

	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}

	private HttpHeaders makeHttp(AuthenticatedUser currentUser){
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.setBearerAuth(currentUser.getToken());
		return httpHeaders;
	}
}
