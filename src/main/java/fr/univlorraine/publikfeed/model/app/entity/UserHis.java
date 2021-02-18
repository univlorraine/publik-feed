package fr.univlorraine.publikfeed.model.app.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import fr.univlorraine.publikfeed.converters.LocalDateTimePersistenceConverter;
import lombok.Data;


/**
 * The persistent class for the user_his database table.
 * 
 */
@Entity
@Table(name="user_his")
@Data
@NamedQuery(name="UserHis.findAll", query="SELECT u FROM UserHis u")
public class UserHis implements Serializable {
	private static final long serialVersionUID = 1L;


	@Id
	@Column(name = "login")
	private String login;

	@Column(name = "uuid")
	private String uuid;

	@Column(name = "data")
	private String data;
	
	@Convert(converter = LocalDateTimePersistenceConverter.class)
	@Column(name = "dat_maj")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
	private LocalDateTime datMaj;



}