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
import java.util.Objects;

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
@Table(name="role_resp")
@Data
@NamedQuery(name="RoleResp.findAll", query="SELECT rr FROM RoleResp rr")
public class RoleResp implements Serializable {

	@Id
	@Column(name = "cod_str")
	private String codStr;
	
	@Column(name = "libelle")
	private String libelle;
	
	@Column(name = "logins")
	private String logins;
	
	@Column(name = "logins_defaut")
	private String loginsDefaut;
	
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

	/**
     * cf. https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
	@Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RoleResp)) {
            return false;
        }
        RoleResp other = (RoleResp) obj;
        return codStr != null && codStr.equals(other.getCodStr());
    }
	
	/**
     * cf. https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(getCodStr());
    }

}