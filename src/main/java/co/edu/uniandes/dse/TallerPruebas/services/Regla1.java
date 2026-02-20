package co.edu.uniandes.dse.TallerPruebas.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniandes.dse.TallerPruebas.entities.AccountEntity;
import co.edu.uniandes.dse.TallerPruebas.entities.PocketEntity;
import co.edu.uniandes.dse.TallerPruebas.exceptions.BusinessLogicException;
import co.edu.uniandes.dse.TallerPruebas.exceptions.EntityNotFoundException;
import co.edu.uniandes.dse.TallerPruebas.repositories.AccountRepository;
import co.edu.uniandes.dse.TallerPruebas.repositories.PocketRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Clase que implementa la Regla 1: Mover dinero de Cuenta a Bolsillo
 */
@Slf4j
@Service
public class Regla1 {

    @Autowired
    private PocketRepository pocketRepository;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Transfiere dinero de una cuenta a un bolsillo (Regla 1)
     * 
     * Escenarios:
     * - Éxito: transferencia con saldo mayor
     * - Éxito: transferencia con saldo exacto
     * - Fallo: saldo insuficiente (lanza BusinessLogicException)
     * - Fallo: bolsillo inexistente (lanza EntityNotFoundException)
     * 
     * @param accountId id de la cuenta de origen
     * @param pocketId id del bolsillo destino
     * @param amount cantidad a transferir
     * @return bolsillo actualizado
     * @throws EntityNotFoundException si la cuenta o el bolsillo no existe
     * @throws BusinessLogicException si el saldo es insuficiente
     */
    @Transactional
    public PocketEntity transferToPocket(Long accountId, Long pocketId, Double amount) throws EntityNotFoundException, BusinessLogicException {
        log.info("Inicia proceso de transferencia de cuenta con id = {} a bolsillo con id = {}, cantidad = {}", accountId, pocketId, amount);
        
        // 1. Verificar que la cuenta existe
        Optional<AccountEntity> accountEntity = accountRepository.findById(accountId);
        if (accountEntity.isEmpty()) {
            throw new EntityNotFoundException("La cuenta no existe");
        }

        // 2. Verificar que el bolsillo existe
        Optional<PocketEntity> pocketEntity = pocketRepository.findById(pocketId);
        if (pocketEntity.isEmpty()) {
            throw new EntityNotFoundException("El bolsillo no existe");
        }

        // 3. Verificar que la cuenta tiene saldo suficiente
        if (accountEntity.get().getSaldo() < amount) {
            throw new BusinessLogicException("Saldo insuficiente");
        }

        // 4. Realizar la transferencia
        accountEntity.get().setSaldo(accountEntity.get().getSaldo() - amount);
        pocketEntity.get().setSaldo(pocketEntity.get().getSaldo() + amount);

        // 5. Guardar los cambios
        accountRepository.save(accountEntity.get());
        log.info("Termina proceso de transferencia de cuenta con id = {} a bolsillo con id = {}", accountId, pocketId);
        return pocketRepository.save(pocketEntity.get());
    }
}
