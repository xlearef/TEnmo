package com.techelevator;

import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.TransferJdbcDao;
import com.techelevator.tenmo.model.Transfer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

public class TransferJdbcDaoTests extends TenmoProjectDaoTest {
    private static final Transfer TRANSFER_1 = new Transfer(9005, 2, 2,
            8005, 8006, new BigDecimal("200"));

    private Transfer testTransfer;
    private TransferJdbcDao transferDao;

    @Before
    public void setup() {
        testTransfer = new Transfer(9006, 2, 2,
                8005, 8006, new BigDecimal("200"));
        transferDao = new TransferJdbcDao(dataSource);
    }

    @Test
    public void createTransferReturnsTrue(){
        boolean actual = transferDao.createTransfer(testTransfer);
        Assert.assertEquals(true, actual);
    }

    @Test
    public void getTransfersByAccIdReturnsCorrect() {
        
    }



}
