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
import co.edu.uniandes.dse.TallerPruebas.entities.PocketEntity;
import co.edu.uniandes.dse.TallerPruebas.exceptions.BusinessLogicException;
import co.edu.uniandes.dse.TallerPruebas.exceptions.EntityNotFoundException;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@DataJpaTest
@Transactional
@Import(AccountService.class)
public class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TestEntityManager entityManager;

    private PodamFactory factory = new PodamFactoryImpl();

    private AccountEntity account;
    private PocketEntity pocket;

    @BeforeEach
    void setUp() {
        clearData();
        insertData();
    }

    private void clearData() {
        entityManager.getEntityManager().createQuery("delete from PocketEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from AccountEntity").executeUpdate();
    }

    private void insertData() {
        account = factory.manufacturePojo(AccountEntity.class);
        account.setSaldo(500.0);
        account.setEstado("ACTIVA");
        entityManager.persist(account);

        pocket = factory.manufacturePojo(PocketEntity.class);
        pocket.setSaldo(100.0);
        pocket.setAccount(account);
        entityManager.persist(pocket);
    }

    @Test
    void testTransferToPocketSuccess() throws EntityNotFoundException, BusinessLogicException {
        Double amountToTransfer = 200.0;

        PocketEntity result = accountService.transferToPocket(account.getId(), pocket.getId(), amountToTransfer);

        assertNotNull(result);
        assertEquals(300.0, result.getSaldo());
        
        AccountEntity updatedAccount = entityManager.find(AccountEntity.class, account.getId());
        assertEquals(300.0, updatedAccount.getSaldo());
    }

    @Test
    void testTransferToPocketNonExistentAccount() {
        assertThrows(EntityNotFoundException.class, () -> {
            accountService.transferToPocket(0L, pocket.getId(), 100.0);
        });
    }

    @Test
    void testTransferToPocketNonExistentPocket() {
        assertThrows(EntityNotFoundException.class, () -> {
            accountService.transferToPocket(account.getId(), 0L, 100.0);
        });
    }

    @Test
    void testTransferToPocketInsufficientBalance() {
        account.setSaldo(100.0);
        entityManager.merge(account);

        assertThrows(BusinessLogicException.class, () -> {
            accountService.transferToPocket(account.getId(), pocket.getId(), 200.0);
        });
    }

    @Test
    void testTransferToPocketExactBalance() throws EntityNotFoundException, BusinessLogicException {
        account.setSaldo(200.0);
        pocket.setSaldo(50.0);
        entityManager.merge(account);
        entityManager.merge(pocket);

        PocketEntity result = accountService.transferToPocket(account.getId(), pocket.getId(), 200.0);

        assertNotNull(result);
        assertEquals(250.0, result.getSaldo());
        
        AccountEntity updatedAccount = entityManager.find(AccountEntity.class, account.getId());
        assertEquals(0.0, updatedAccount.getSaldo());
    }
}
