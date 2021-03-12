package fr.univlorraine.publikfeed.model.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import fr.univlorraine.publikfeed.model.app.entity.RoleManuel;



@Repository
public interface RoleManuelRepository extends JpaSpecificationExecutor<RoleManuel>, JpaRepository<RoleManuel, String> {

	public List<RoleManuel> findByDatSupNull();

	public List<RoleManuel> findByDatSupNotNullAndUuidNotNullAndDatSupPublikNull();

	public List<RoleManuel> findAll();

	public List<RoleManuel> findAllByOrderByDatMajDesc();

	public List<RoleManuel> findByLibelleContainingIgnoreCaseOrIdContainingIgnoreCase(String search, String search2);

}
