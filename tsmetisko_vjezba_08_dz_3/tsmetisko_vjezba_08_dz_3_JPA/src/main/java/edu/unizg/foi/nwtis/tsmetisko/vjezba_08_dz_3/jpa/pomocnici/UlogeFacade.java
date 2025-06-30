package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.entiteti.Uloge;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.criteria.CriteriaBuilder;

@Stateless
public class UlogeFacade extends EntityManagerProducer implements Serializable {

  private static final long serialVersionUID = -7307326770346764528L;

  private CriteriaBuilder cb;

  @PostConstruct
  private void init() {
    cb = getEntityManager().getCriteriaBuilder();
  }

  public void create(Uloge uloga) {
    getEntityManager().persist(uloga);
  }

}
