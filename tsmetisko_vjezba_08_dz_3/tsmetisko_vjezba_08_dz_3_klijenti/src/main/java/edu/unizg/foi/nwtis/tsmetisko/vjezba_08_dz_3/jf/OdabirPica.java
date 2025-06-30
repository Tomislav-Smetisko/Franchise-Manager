package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import com.google.gson.Gson;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("odabirPica")
public class OdabirPica implements Serializable {

  private static final long serialVersionUID = 6262961632337388879L;

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartner;

  @Inject
  PrijavaKorisnika prijavaKorisnika;

  private List<KartaPica> kartaPica = new ArrayList<>();

  private String pice;

  private int kolicina = 1;

  public String getPice() {
    return pice;
  }

  public void setPice(String pice) {
    this.pice = pice;
  }

  public int getKolicina() {
    return kolicina;
  }

  public void setKolicina(int kolicina) {
    this.kolicina = kolicina;
  }

  @PostConstruct
  public void ucitajJelovnik() {
    kartaPica = getKartaPica();
  }

  public List<KartaPica> getKartaPica() {
    String odgovor = servisPartner
        .getKartaPica(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka())
        .readEntity(String.class);
    Gson gson = new Gson();
    KartaPica[] niz = gson.fromJson(odgovor, KartaPica[].class);
    List<KartaPica> lista = Arrays.asList(niz);

    return lista;
  }

  public void naruci() {
    KartaPica pronadenoPice =
        kartaPica.stream().filter(j -> j.id().equals(pice)).findFirst().orElse(null);

    Narudzba narudzba = new Narudzba(prijavaKorisnika.getKorisnickoIme(), pice, false, kolicina,
        pronadenoPice.cijena(), System.currentTimeMillis());

    servisPartner.postPice(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka(),
        narudzba);
  }

  public List<Narudzba> getDohvatiPica() {
    List<Narudzba> novaLista = new ArrayList<>();
    try {
      String narudzba = servisPartner
          .getNarudzba(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka())
          .readEntity(String.class);
      Gson gson = new Gson();
      Narudzba[] niz = gson.fromJson(narudzba, Narudzba[].class);
      List<Narudzba> lista = Arrays.asList(niz);

      for (var n : lista) {
        if (!n.jelo()) {
          KartaPica pronadenoJelo =
              kartaPica.stream().filter(j -> j.id().equals(n.id())).findFirst().orElse(null);
          Narudzba nova = new Narudzba(n.korisnik(), pronadenoJelo.naziv(), false, kolicina,
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
