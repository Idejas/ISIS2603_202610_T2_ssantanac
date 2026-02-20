package co.edu.uniandes.dse.TallerPruebas.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniandes.dse.TallerPruebas.entities.AccountEntity;
import co.edu.uniandes.dse.TallerPruebas.exceptions.BusinessLogicException;
import co.edu.uniandes.dse.TallerPruebas.exceptions.EntityNotFoundException;
import co.edu.uniandes.dse.TallerPruebas.repositories.AccountRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Clase que implementa la Regla 2: Mover dinero entre dos cuentas
 */
@Slf4j
@Service
public class Regla2 {

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Transfiere dinero entre dos cuentas (Regla 2)
     * 
     * Escenarios:
     * - Éxito: transferencia entre cuentas
     * - Fallo: cuenta origen inexistente (lanza EntityNotFoundException)
     * - Fallo: misma cuenta origen y destino (lanza BusinessLogicException)
     * - Fallo: fondos insuficientes (lanza BusinessLogicException)
     * 
     * @param originAccountId id de la cuenta de origen
     * @param destinationAccountId id de la cuenta destino
     * @param amount cantidad a transferir
     * @return cuenta destino actualizada
     * @throws EntityNotFoundException si la cuenta origen no existe
     * @throws BusinessLogicException si la cuenta origen y destino son iguales o si el saldo es insuficiente
     */
    @Transactional
    public AccountEntity transferBetweenAccounts(Long originAccountId, Long destinationAccountId, Double amount) throws EntityNotFoundException, BusinessLogicException {
        log.info("Inicia proceso de transferencia entre cuentas: origen = {}, destino = {}, cantidad = {}", originAccountId, destinationAccountId, amount);
        
        // 1. Verificar que la cuenta origen existe
        Optional<AccountEntity> originAccount = accountRepository.findById(originAccountId);
        if (originAccount.isEmpty()) {
            throw new EntityNotFoundException("La cuenta origen no existe");
        }

        // 2. Verificar que la cuenta destino existe
        Optional<AccountEntity> destinationAccount = accountRepository.findById(destinationAccountId);
        if (destinationAccount.isEmpty()) {
            throw new EntityNotFoundException("La cuenta destino no existe");
        }

        // 3. Verificar que la cuenta origen y destino no sean iguales
        if (originAccountId.equals(destinationAccountId)) {
            throw new BusinessLogicException("Cuenta origen y destino no pueden ser iguales");
        }

        // 4. Verificar que la cuenta origen tiene fondos suficientes
        if (originAccount.get().getSaldo() < amount) {
            throw new BusinessLogicException("Fondos insuficientes");
        }

        // 5. Realizar la transferencia
        originAccount.get().setSaldo(originAccount.get().getSaldo() - amount);
        destinationAccount.get().setSaldo(destinationAccount.get().getSaldo() + amount);

        // 6. Guardar los cambios
        accountRepository.save(originAccount.get());
        log.info("Termina proceso de transferencia entre cuentas: origen = {}, destino = {}", originAccountId, destinationAccountId);
        return accountRepository.save(destinationAccount.get());
    }
}
