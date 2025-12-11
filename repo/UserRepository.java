package repo;

 import domain.Duck;
import domain.TipRata;
import domain.User;
import util.PageResult;
/**
 * Repository interface for User entities.
 */
public interface UserRepository extends Repository<Integer, User>{
    // Additional user-specific methods can be added here
    // Good for SRP :) dont want to need to remake repo when i wanna add
    //new functionality to user repo and i only have the generic
    //repo interface
    Iterable<Duck> findAllDucks();
    PageResult<User> findPage(int pageIndex, int pageSize);
    PageResult<Duck> findDuckPage(int pageIndex, int pageSize, TipRata filter);
    User findByEmail(String email);
    boolean emailExists(String email);
    void updatePassword(int userId, String hashedPassword);
}
