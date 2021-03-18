package fr.univlorraine.publikfeed.model.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import fr.univlorraine.publikfeed.model.app.entity.UserHis;



@Repository
public interface UserHisRepository extends JpaSpecificationExecutor<UserHis>, JpaRepository<UserHis, String> {

	public List<UserHis> findAllByOrderByLogin();

	/*@Query(value = "select u from UserHis u where u.login like %$1%", nativeQuery = false)
	public List<UserHis> findAllForKey(String search);*/
	
	public List<UserHis> findByLoginContainingIgnoreCaseOrDataContainingIgnoreCase(String search,String search2);

	public List<UserHis> findAllByDatSupNull();

}
