/**
 *
 * Copyright (c) 2022 Université de Lorraine, 18/02/2021
 *
 * dn-sied-dev@univ-lorraine.fr
 *
 * Ce logiciel est un programme informatique servant à alimenter Publik depuis des groupes LDAP.
 *
 * Ce logiciel est régi par la licence CeCILL 2.1 soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL 2.1, et que vous en avez accepté les
 * termes.
 *
 */
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