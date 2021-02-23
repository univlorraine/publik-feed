package fr.univlorraine.publikfeed.model.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import fr.univlorraine.publikfeed.model.app.entity.RoleAuto;



@Repository
public interface RoleAutoRepository extends JpaSpecificationExecutor<RoleAuto>, JpaRepository<RoleAuto, String> {

}
