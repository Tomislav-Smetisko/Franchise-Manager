package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import com.google.gson.Gson;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("odabirJela")
public class OdabirJela implements Serializable {

  private static final long serialVersionUID = -4793926388710477190L;

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartner;

  @Inject
  PrijavaKorisnika prijavaKorisnika;

  private List<Jelovnik> jelovnik = new ArrayList<>();

  private String jelo;

  private int kolicina = 1;

  public String getJelo() {
    return jelo;
  }

  public void setJelo(String jelo) {
    this.jelo = jelo;
  }

  public int getKolicina() {
    return kolicina;
  }

  public void setKolicina(int kolicina) {
    this.kolicina = kolicina;
  }

  @PostConstruct
  public void ucitajJelovnik() {
    jelovnik = getJelovnik();
  }

  public List<Jelovnik> getJelovnik() {
    String odgovor = servisPartner
        .getJelovnik(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka())
        .readEntity(String.class);
    Gson gson = new Gson();
    Jelovnik[] niz = gson.fromJson(odgovor, Jelovnik[].class);
    List<Jelovnik> lista = Arrays.asList(niz);

    return lista;
  }

  public void naruci() {
    Jelovnik pronadenoJelo =
        jelovnik.stream().filter(j -> j.id().equals(jelo)).findFirst().orElse(null);

    Narudzba narudzba = new Narudzba(prijavaKorisnika.getKorisnickoIme(), jelo, true, kolicina,
        pronadenoJelo.cijena(), System.currentTimeMillis());

    servisPartner.postJelo(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka(),
        narudzba);
  }

  public List<Narudzba> getDohvatiJela() {
    List<Narudzba> novaLista = new ArrayList<>();
    try {
      String narudzba = servisPartner
          .getNarudzba(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka())
          .readEntity(String.class);

      Gson gson = new Gson();
      Narudzba[] niz = gson.fromJson(narudzba, Narudzba[].class);
      List<Narudzba> lista = Arrays.asList(niz);

      for (var n : lista) {
        if (n.jelo()) {
          Jelovnik pronadenoJelo =
              jelovnik.stream().filter(j -> j.id().equals(n.id())).findFirst().orElse(null);
          Narudzba nova = new Narudzba(n.korisnik(), pronadenoJelo.naziv(), true, kolicina,
              n.cijena(), n.vrijeme());
          novaLista.add(nova);
        }
      }
    } catch (Exception e) {
      return null;
    }

    return novaLista;
  }

}
