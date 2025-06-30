package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.entiteti.Obracuni;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.entiteti.Partneri;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Stateless
public class ObracuniFacade extends EntityManagerProducer implements Serializable {

  private static final long serialVersionUID = -7026916009713664621L;

  @Inject
  PartneriFacade partneriFacade;

  private CriteriaBuilder cb;

  @PostConstruct
  private void init() {
    cb = getEntityManager().getCriteriaBuilder();
  }

  public void create(Obracuni obracuni) {
    getEntityManager().persist(obracuni);
  }

  public void edit(Obracuni obracuni) {
    getEntityManager().merge(obracuni);
  }

  public void remove(Obracuni obracuni) {
    getEntityManager().remove(getEntityManager().merge(obracuni));
  }

  public Obracuni find(Object id) {
    return getEntityManager().find(Obracuni.class, id);
  }

  public List<Obracuni> dohvatiObracuneZaPartnera(int partner, Long vrijemeOd, Long vrijemeDo) {
    CriteriaQuery<Obracuni> cq = cb.createQuery(Obracuni.class);
    Root<Obracuni> root = cq.from(Obracuni.class);

    List<Predicate> predicates = new ArrayList<>();
    Partneri p = partneriFacade.find(partner);
    predicates.add(cb.equal(root.get("partneri"), p));
    predicates.add(cb.greaterThanOrEqualTo(root.get("vrijeme"), vrijemeOd));
    predicates.add(cb.lessThanOrEqualTo(root.get("vrijeme"), vrijemeDo));

    cq.select(root).where(cb.and(predicates.toArray(new Predicate[0])));

    return getEntityManager().createQuery(cq).getResultList();
  }

  public Obracun pretvori(Obracuni o) {
    if (o == null) {
      return null;
    }
    Partner partner = this.partneriFacade.pretvori(o.getPartneri());

    var oObjekt = new Obracun(partner.id(), o.getId(), o.getJelo(), o.getKolicina(), o.getCijena(),
        o.getVrijeme().getTime());

    return oObjekt;
  }

  public Obracuni pretvori(Obracun o) {
    if (o == null) {
      return null;
    }
    var oE = new Obracuni();
    oE.setCijena(o.cijena());
    oE.setId(o.id());
    oE.setJelo(o.jelo());
    oE.setKolicina(o.kolicina());
    oE.setVrijeme(new Timestamp(o.vrijeme()));

    Partneri p = this.partneriFacade.find(o.partner());
    oE.setPartneri(p);

    return oE;
  }

  public List<Obracun> pretvori(List<Obracuni> obracuniE) {
    List<Obracun> obracuni = new ArrayList<>();
    for (Obracuni oEntitet : obracuniE) {
      var oObjekt = pretvori(oEntitet);

      obracuni.add(oObjekt);
    }

    return obracuni;
  }


}
