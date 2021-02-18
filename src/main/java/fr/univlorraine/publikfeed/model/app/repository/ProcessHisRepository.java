package fr.univlorraine.publikfeed.model.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import fr.univlorraine.publikfeed.model.app.entity.ProcessHis;
import fr.univlorraine.publikfeed.model.app.entity.ProcessHisPK;



@Repository
public interface ProcessHisRepository extends JpaSpecificationExecutor<ProcessHis>, JpaRepository<ProcessHis, ProcessHisPK> {

	List<ProcessHis> findAllByIdCodProcessOrderByDatFinDesc(String processName);

	List<ProcessHis> findAllByIdCodProcessOrderByIdDatDebDesc(String processName);

}
