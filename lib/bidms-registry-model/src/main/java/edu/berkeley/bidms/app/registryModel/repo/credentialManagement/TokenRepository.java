package edu.berkeley.bidms.app.registryModel.repo.credentialManagement;

import edu.berkeley.bidms.app.registryModel.model.Person;
import edu.berkeley.bidms.app.registryModel.model.credentialManagement.BaseToken;

import java.util.Date;
import java.util.List;

public interface TokenRepository<T extends BaseToken> {
    void delete(T tokenObject);

    void deleteAll(Iterable<? extends T> entities);

    T findByTokenAndPerson(String token, Person person);

    List<T> findAllByPerson(Person person);

    T findByToken(String token);

    List<T> findAllByExpiryDateLessThanEqual(Date date);
}
