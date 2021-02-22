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
 * The persistent class for the user_role database table.
 * 
 */
@SuppressWarnings("serial")
@Entity
@Table(name="user_role")
@NamedQuery(name="UserRole.findAll", query="SELECT ur FROM UserRole ur")
public class UserRole implements Serializable {


	@EmbeddedId
	private UserRolePK id;


	@Convert(converter = LocalDateTimePersistenceConverter.class)
	@Column(name = "dat_maj")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
	private LocalDateTime datMaj;
	
	@Convert(converter = LocalDateTimePersistenceConverter.class)
	@Column(name = "dat_sup")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
	private LocalDateTime datSup;

	public UserRole() {
		super();
	}

	public UserRolePK getId() {
		return id;
	}

	public void setId(UserRolePK id) {
		this.id = id;
	}

	public LocalDateTime getDatMaj() {
		return datMaj;
	}

	public void setDatMaj(LocalDateTime datMaj) {
		this.datMaj = datMaj;
	}

	public LocalDateTime getDatSup() {
		return datSup;
	}

	public void setDatSup(LocalDateTime datSup) {
		this.datSup = datSup;
	}
	

}