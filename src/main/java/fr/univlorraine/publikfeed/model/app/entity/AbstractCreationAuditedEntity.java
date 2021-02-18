package fr.univlorraine.publikfeed.model.app.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;

/**
 * Superclass d'Entité suivie.
 * @author Adrien Colson
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@SuppressWarnings("serial")
public abstract class AbstractCreationAuditedEntity implements Serializable {

	/** Date de création. */
	@CreatedDate
	private LocalDateTime createdDate;

	/** Utilisateur qui a fait la création. */
	@CreatedBy
	@ManyToOne(optional = true)
	@JoinColumn(name = "created_by")
	@NotFound(action = NotFoundAction.IGNORE)
	private Utilisateur createdBy;

}
