package fr.univlorraine.publikfeed.model.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import fr.univlorraine.publikfeed.model.app.entity.RoleResp;



@Repository
public interface RoleRespRepository extends JpaSpecificationExecutor<RoleResp>, JpaRepository<RoleResp, String> {

	public List<RoleResp> findByDatSupNull();

	public List<RoleResp> findByDatSupNotNullAndUuidNotNullAndDatSupPublikNull();

	public List<RoleResp> findAll();

}
