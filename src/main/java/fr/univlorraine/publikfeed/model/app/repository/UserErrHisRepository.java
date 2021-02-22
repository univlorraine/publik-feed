package fr.univlorraine.publikfeed.model.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import fr.univlorraine.publikfeed.model.app.entity.UserErrHis;



@Repository
public interface UserErrHisRepository extends JpaSpecificationExecutor<UserErrHis>, JpaRepository<UserErrHis, Integer> {

	List<UserErrHis> findAllByLogin(String login);

}
