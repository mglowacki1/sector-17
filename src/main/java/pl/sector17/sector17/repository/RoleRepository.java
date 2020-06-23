package pl.sector17.sector17.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sector17.sector17.model.Role;
import pl.sector17.sector17.model.User;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findAll();
    Role findByName(String name);
}
