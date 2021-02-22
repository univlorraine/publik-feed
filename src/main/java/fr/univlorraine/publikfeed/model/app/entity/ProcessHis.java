package fr.univlorraine.publikfeed.model.app.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import fr.univlorraine.publikfeed.converters.LocalDateTimePersistenceConverter;


/**
 * The persistent class for the process_his database table.
 * 
 */
@SuppressWarnings("serial")
@Entity
@Table(name="process_his")
@NamedQuery(name="ProcessHis.findAll", query="SELECT r FROM ProcessHis r")
public class ProcessHis implements Serializable {


	@EmbeddedId
	private ProcessHisPK id;


	@Convert(converter = LocalDateTimePersistenceConverter.class)
	@Column(name = "dat_fin")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
	private LocalDateTime datFin;
	
	@Column(name = "nb_obj_traite")
	private int nbObjTraite;
	
	@Column(name = "nb_obj_total")
	private int nbObjTotal;
	
	@Column(name = "nb_obj_erreur")
	private int nbObjErreur;

	public ProcessHis() {
		super();
	}

	public ProcessHisPK getId() {
		return id;
	}

	public void setId(ProcessHisPK id) {
		this.id = id;
	}

	public LocalDateTime getDatFin() {
		return datFin;
	}

	public void setDatFin(LocalDateTime datFin) {
		this.datFin = datFin;
	}

	public int getNbObjTraite() {
		return nbObjTraite;
	}

	public void setNbObjTraite(int nbObjTraite) {
		this.nbObjTraite = nbObjTraite;
	}

	public int getNbObjTotal() {
		return nbObjTotal;
	}

	public void setNbObjTotal(int nbObjTotal) {
		this.nbObjTotal = nbObjTotal;
	}

	public int getNbObjErreur() {
		return nbObjErreur;
	}

	public void setNbObjErreur(int nbObjErreur) {
		this.nbObjErreur = nbObjErreur;
	}
	
	

}