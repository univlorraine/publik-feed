package fr.univlorraine.publikfeed.model.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import fr.univlorraine.publikfeed.model.app.entity.UserRole;
import fr.univlorraine.publikfeed.model.app.entity.UserRolePK;



@Repository
public interface UserRoleRepository extends JpaSpecificationExecutor<UserRole>, JpaRepository<UserRole, UserRolePK> {


}
