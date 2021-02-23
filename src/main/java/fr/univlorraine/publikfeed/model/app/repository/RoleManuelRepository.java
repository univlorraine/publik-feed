package fr.univlorraine.publikfeed.model.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import fr.univlorraine.publikfeed.model.app.entity.RoleManuel;



@Repository
public interface RoleManuelRepository extends JpaSpecificationExecutor<RoleManuel>, JpaRepository<RoleManuel, String> {

	public List<RoleManuel> findByDatSupNotNull();

}
