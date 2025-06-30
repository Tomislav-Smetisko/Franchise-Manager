package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Stateless
public class ZapisiFacade extends EntityManagerProducer implements Serializable {

  private static final long serialVersionUID = 5639101021587647972L;

  private CriteriaBuilder cb;

  @PostConstruct
  private void init() {
    cb = getEntityManager().getCriteriaBuilder();
  }

  public void create(Zapisi zapisi) {
    getEntityManager().persist(zapisi);
  }

  public void edit(Zapisi zapisi) {
    getEntityManager().merge(zapisi);
  }

  public void remove(Zapisi zapisi) {
    getEntityManager().remove(getEntityManager().merge(zapisi));
  }

  public List<Zapisi> dohvatiZapiseZaPartnera(String partner, Long vrijemeOd, Long vrijemeDo) {
    CriteriaQuery<Zapisi> cq = cb.createQuery(Zapisi.class);
    Root<Zapisi> root = cq.from(Zapisi.class);

    List<Predicate> predicates = new ArrayList<>();
    predicates.add(cb.equal(root.get("korisnickoime"), partner));
    predicates.add(cb.greaterThanOrEqualTo(root.get("vrijeme"), vrijemeOd));
    predicates.add(cb.lessThanOrEqualTo(root.get("vrijeme"), vrijemeDo));

    cq.select(root).where(cb.and(predicates.toArray(new Predicate[0])));

    return getEntityManager().createQuery(cq).getResultList();
  }

}
