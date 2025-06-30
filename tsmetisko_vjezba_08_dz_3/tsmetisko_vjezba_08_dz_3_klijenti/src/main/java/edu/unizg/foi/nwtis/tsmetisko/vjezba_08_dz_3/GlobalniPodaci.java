package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GlobalniPodaci {
  private int brojObracuna = 0;

  private final Map<Integer, Integer> brojOtvorenihNarudzbi = new ConcurrentHashMap<>();
  private final Map<Integer, Integer> brojRacuna = new ConcurrentHashMap<>();


  public int getBrojObracuna() {
    return brojObracuna;
  }

  public void setBrojObracuna(int brojObracuna) {
    this.brojObracuna = brojObracuna;
  }

  public Map<Integer, Integer> getBrojOtvorenihNarudzbi() {
    return brojOtvorenihNarudzbi;
  }

  public Map<Integer, Integer> getBrojRacuna() {
    return brojRacuna;
  }

  public synchronized void povecajBrojObracuna() {
    brojObracuna++;
  }

  public synchronized void smanjiBrojObracuna() {
    if (brojObracuna > 0) {
      brojObracuna--;
    }
  }

  public void povecajBrojOtvorenihNarudzbi(int partnerId) {
    int brojOtvorenihNarudzbiPartnera = brojOtvorenihNarudzbi.getOrDefault(partnerId, 0);
    brojOtvorenihNarudzbiPartnera++;
    brojOtvorenihNarudzbi.put(partnerId, brojOtvorenihNarudzbiPartnera);
  }

  public void smanjiBrojOtvorenihNarudzbi(int partnerId) {
    int brojOtvorenihNarudzbiPartnera = brojOtvorenihNarudzbi.getOrDefault(partnerId, 0);
    if (brojOtvorenihNarudzbiPartnera > 0) {
      brojOtvorenihNarudzbiPartnera--;
      brojOtvorenihNarudzbi.put(partnerId, brojOtvorenihNarudzbiPartnera);
    }
  }

  public void povecajBrojRacuna(int partnerId) {
    int brojRacunaPartnera = brojRacuna.getOrDefault(partnerId, 0);
    brojRacunaPartnera++;
    brojRacuna.put(partnerId, brojRacunaPartnera);
  }

  public void smanjiBrojRacuna(int partnerId) {
    int brojRacunaPartnera = brojRacuna.getOrDefault(partnerId, 0);
    if (brojRacunaPartnera > 0) {
      brojRacunaPartnera--;
      brojRacuna.put(partnerId, brojRacunaPartnera);
    }
  }


}
