package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.entiteti;

import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;

@Entity
@NamedQuery(name = "Uloge.findAll", query = "SELECT u FROM Uloge u")
public class Uloge implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  private String korisnik;

  private String grupa;

  public Uloge() {

  }

  public String getKorisnik() {
    return korisnik;
  }

  public void setKorisnik(String korisnik) {
    this.korisnik = korisnik;
  }

  public String getGrupa() {
    return grupa;
  }

  public void setGrupa(String grupa) {
    this.grupa = grupa;
  }



}
