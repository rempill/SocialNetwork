package repo;

import errors.RepoError;

public interface Repository<ID,E>{
    E findOne(ID id);
    Iterable<E> findAll();
    E save(E entity) throws RepoError;
    E delete(ID id);
}
