package de.cweyermann.ber.playerratings.control.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.cweyermann.ber.playerratings.boundary.Repository;
import de.cweyermann.ber.playerratings.entity.Player;

/**
 * Only supports {@link #findById(String)} and {@link #save(Player)}
 * 
 * @author chris
 *
 */
public class InMemoryPlayerRepo implements Repository {

    public Map<String, Player> backendMap = new HashMap<>();

    @Override
    public <S extends Player> S save(S entity) {
        String id = entity.getId();

        backendMap.put(id, entity);

        return entity;
    }

    @Override
    public <S extends Player> Iterable<S> saveAll(Iterable<S> entities) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<Player> findById(String id) {

        Player player = backendMap.get(id);
        return Optional.ofNullable(player);
    }

    @Override
    public boolean existsById(String id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterable<Player> findAll() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<Player> findAllById(Iterable<String> ids) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long count() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void deleteById(String id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void delete(Player entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteAll(Iterable<? extends Player> entities) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteAll() {
        // TODO Auto-generated method stub

    }
    
    public void clear() {
        backendMap.clear();
    }

}
