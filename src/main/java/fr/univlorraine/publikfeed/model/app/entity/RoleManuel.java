package fr.univlorraine.publikfeed.model.app.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import fr.univlorraine.publikfeed.converters.LocalDateTimePersistenceConverter;
import lombok.Data;


/**
 * The persistent class for the role_manuel database table.
 * 
 */
@SuppressWarnings("serial")
@Entity
@Table(name="role_manuel")
@Data
@NamedQuery(name="RoleManuel.findAll", query="SELECT rm FROM RoleManuel rm")
public class RoleManuel implements Serializable {

	@Id
	@Column(name = "id")
	private String id;
	
	@Column(name = "libelle")
	private String libelle;
	
	@Column(name = "logins")
	private String logins;
	
	@Column(name = "filtre")
	private String filtre;
	
	@Column(name = "uuid")
	private String uuid;
	
	@Column(name = "slug")
	private String slug;
	
	@Column(name = "ou")
	private String ou;

	@Column(name = "hash")
	private String hash;
	
	@Convert(converter = LocalDateTimePersistenceConverter.class)
	@Column(name = "dat_maj")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
	private LocalDateTime datMaj;
	
	@Convert(converter = LocalDateTimePersistenceConverter.class)
	@Column(name = "dat_sup")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
	private LocalDateTime datSup;

	@Convert(converter = LocalDateTimePersistenceConverter.class)
	@Column(name = "dat_cre_publik")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
	private LocalDateTime datCrePublik;
	
	@Convert(converter = LocalDateTimePersistenceConverter.class)
	@Column(name = "dat_maj_publik")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
	private LocalDateTime datMajPublik;
	
	@Convert(converter = LocalDateTimePersistenceConverter.class)
	@Column(name = "dat_sup_publik")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
	private LocalDateTime datSupPublik;


}