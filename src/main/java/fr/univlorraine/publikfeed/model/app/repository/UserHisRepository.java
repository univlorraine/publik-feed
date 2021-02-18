package fr.univlorraine.publikfeed.model.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import fr.univlorraine.publikfeed.model.app.entity.UserHis;



@Repository
public interface UserHisRepository extends JpaSpecificationExecutor<UserHis>, JpaRepository<UserHis, String> {

}
