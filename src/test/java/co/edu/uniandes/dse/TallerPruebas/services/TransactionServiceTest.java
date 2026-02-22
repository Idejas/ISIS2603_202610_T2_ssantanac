package co.edu.uniandes.dse.TallerPruebas.services;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import co.edu.uniandes.dse.TallerPruebas.entities.AccountEntity;
import co.edu.uniandes.dse.TallerPruebas.exceptions.BusinessLogicException;
import co.edu.uniandes.dse.TallerPruebas.exceptions.EntityNotFoundException;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@DataJpaTest
@Transactional
@Import(TransactionService.class)
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TestEntityManager entityManager;

    private PodamFactory factory = new PodamFactoryImpl();

    private AccountEntity originAccount;
    private AccountEntity destinationAccount;

    @BeforeEach
    void setUp() {
        clearData();
        insertData();
    }

    private void clearData() {
        entityManager.getEntityManager().createQuery("delete from AccountEntity").executeUpdate();
    }

    private void insertData() {
        originAccount = factory.manufacturePojo(AccountEntity.class);
        originAccount.setSaldo(500.0);
        originAccount.setEstado("ACTIVA");
        entityManager.persist(originAccount);

        destinationAccount = factory.manufacturePojo(AccountEntity.class);
        destinationAccount.setSaldo(300.0);
        destinationAccount.setEstado("ACTIVA");
        entityManager.persist(destinationAccount);
    }

    @Test
    void testTransferBetweenAccountsSuccess() throws EntityNotFoundException, BusinessLogicException {
        Double amountToTransfer = 200.0;

        AccountEntity result = transactionService.transferBetweenAccounts(originAccount.getId(), destinationAccount.getId(), amountToTransfer);

        assertNotNull(result);
        assertEquals(500.0, result.getSaldo());
        
        AccountEntity updatedOrigin = entityManager.find(AccountEntity.class, originAccount.getId());
        assertEquals(300.0, updatedOrigin.getSaldo());
    }

    @Test
    void testTransferBetweenAccountsNonExistentOrigin() {
        assertThrows(EntityNotFoundException.class, () -> {
            transactionService.transferBetweenAccounts(0L, destinationAccount.getId(), 100.0);
        });
    }

    @Test
    void testTransferBetweenAccountsSameAccount() {
        assertThrows(BusinessLogicException.class, () -> {
            transactionService.transferBetweenAccounts(originAccount.getId(), originAccount.getId(), 100.0);
        });
    }

    @Test
    void testTransferBetweenAccountsInsufficientFunds() {
        originAccount.setSaldo(100.0);
        entityManager.merge(originAccount);

        assertThrows(BusinessLogicException.class, () -> {
            transactionService.transferBetweenAccounts(originAccount.getId(), destinationAccount.getId(), 300.0);
        });
    }

    @Test
    void testTransferBetweenAccountsNonExistentDestination() {
        assertThrows(EntityNotFoundException.class, () -> {
            transactionService.transferBetweenAccounts(originAccount.getId(), 0L, 100.0);
        });
    }

    @Test
    void testTransferBetweenAccountsExactBalance() throws EntityNotFoundException, BusinessLogicException {
        originAccount.setSaldo(250.0);
        destinationAccount.setSaldo(150.0);
        entityManager.merge(originAccount);
        entityManager.merge(destinationAccount);

        AccountEntity result = transactionService.transferBetweenAccounts(originAccount.getId(), destinationAccount.getId(), 250.0);

        assertNotNull(result);
        assertEquals(400.0, result.getSaldo());
        
        AccountEntity updatedOrigin = entityManager.find(AccountEntity.class, originAccount.getId());
        assertEquals(0.0, updatedOrigin.getSaldo());
    }
}
