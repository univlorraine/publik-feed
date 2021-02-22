package fr.univlorraine.publikfeed.model.app.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonFormat;

import fr.univlorraine.publikfeed.converters.LocalDateTimePersistenceConverter;

/**
 * The primary key class for the process_his database table.
 * 
 */
@SuppressWarnings("serial")
@Embeddable
public class ProcessHisPK implements Serializable {

	@Column(name="cod_process")
	private String codProcess;

	
	@Convert(converter = LocalDateTimePersistenceConverter.class)
	@Column(name = "dat_deb")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
	private LocalDateTime datDeb;
	


	public ProcessHisPK() {
	}
	public String getCodProcess() {
		return this.codProcess;
	}
	public void setCodProcess(String codProcess) {
		this.codProcess = codProcess;
	}
	public LocalDateTime getDatDeb() {
		return this.datDeb;
	}
	public void setDatDeb(LocalDateTime datDeb) {
		this.datDeb = datDeb;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ProcessHisPK)) {
			return false;
		}
		ProcessHisPK castOther = (ProcessHisPK)other;
		return 
			this.codProcess.equals(castOther.codProcess)
			&& this.datDeb.equals(castOther.datDeb);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.codProcess.hashCode();
		hash = hash * prime + this.datDeb.hashCode();
		
		return hash;
	}
}