package fr.univlorraine.publikfeed.model.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fr.univlorraine.publikfeed.model.app.entity.UserErrHis;



@Repository
public interface UserErrHisRepository extends JpaSpecificationExecutor<UserErrHis>, JpaRepository<UserErrHis, Integer> {

	List<UserErrHis> findAllByLogin(String login);

	@Query(value = "select user_err_his.login from user_err_his where not exists (select login from user_his where user_his.login = user_err_his.login and user_his.dat_maj > user_err_his.dat_err) ", 
		nativeQuery = true)	
	List<String> getLoginToRetry();

	public List<UserErrHis> findAllByOrderByDatErrDesc();

	public List<UserErrHis> findByLoginContainingIgnoreCaseOrTraceContainingIgnoreCaseOrderByDatErrDesc(String search, String search2);

}
