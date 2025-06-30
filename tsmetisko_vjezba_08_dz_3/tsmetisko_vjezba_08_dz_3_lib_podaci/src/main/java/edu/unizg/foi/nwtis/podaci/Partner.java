package edu.unizg.foi.nwtis.podaci;

public record Partner(int id, String naziv, String vrstaKuhinje, String adresa, int mreznaVrata,
    float gpsSirina, float gpsDuzina, int mreznaVrataKraj, String sigurnosniKod, String adminKod) {
  public Partner partnerBezKodova() {
    return new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, gpsSirina, gpsDuzina,
        mreznaVrataKraj, "******", "******");
  }
}
