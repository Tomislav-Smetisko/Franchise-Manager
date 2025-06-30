package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unizg.foi.nwtis.podaci.Obracun;

public class ObracunDAO {
  private Connection vezaBP;

  public ObracunDAO(Connection vezaBP) {
    super();
    this.vezaBP = vezaBP;
  }

  public List<Obracun> dohvati(Long vrijemeOd, Long vrijemeDo) {
    String upit = "SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni";

    List<Obracun> obracuni = new ArrayList<>();

    try (Statement s = this.vezaBP.createStatement(); ResultSet rs = s.executeQuery(upit)) {

      while (rs.next()) {
        int partner = rs.getInt("partner");
        String id = rs.getString("id");
        boolean jelo = rs.getBoolean("jelo");
        float kolicina = rs.getFloat("kolicina");
        float cijena = rs.getFloat("cijena");
        long vrijeme = rs.getTimestamp("vrijeme").getTime();

        vrijemeOd = vrijemeOd != null ? vrijemeOd : 0;
        vrijemeDo = vrijemeDo != null ? vrijemeDo : System.currentTimeMillis();

        if (vrijeme >= vrijemeOd && vrijeme <= vrijemeDo) {
          Obracun obracun = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
          obracuni.add(obracun);
        }
      }
      return obracuni;

    } catch (SQLException ex) {
      Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public List<Obracun> dohvatiJela(Long vrijemeOd, Long vrijemeDo) {
    String upit =
        "SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE jelo = true";

    List<Obracun> obracuni = new ArrayList<>();

    try (Statement s = this.vezaBP.createStatement(); ResultSet rs = s.executeQuery(upit)) {

      while (rs.next()) {
        int partner = rs.getInt("partner");
        String id = rs.getString("id");
        boolean jelo = rs.getBoolean("jelo");
        float kolicina = rs.getFloat("kolicina");
        float cijena = rs.getFloat("cijena");
        long vrijeme = rs.getTimestamp("vrijeme").getTime();

        vrijemeOd = vrijemeOd != null ? vrijemeOd : 0;
        vrijemeDo = vrijemeDo != null ? vrijemeDo : System.currentTimeMillis();

        if (vrijeme >= vrijemeOd && vrijeme <= vrijemeDo) {
          Obracun obracun = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
          obracuni.add(obracun);
        }
      }
      return obracuni;

    } catch (SQLException ex) {
      Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public List<Obracun> dohvatiPica(Long vrijemeOd, Long vrijemeDo) {
    String upit =
        "SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE jelo = false";

    List<Obracun> obracuni = new ArrayList<>();

    try (Statement s = this.vezaBP.createStatement(); ResultSet rs = s.executeQuery(upit)) {

      while (rs.next()) {
        int partner = rs.getInt("partner");
        String id = rs.getString("id");
        boolean jelo = rs.getBoolean("jelo");
        float kolicina = rs.getFloat("kolicina");
        float cijena = rs.getFloat("cijena");
        long vrijeme = rs.getTimestamp("vrijeme").getTime();

        vrijemeOd = vrijemeOd != null ? vrijemeOd : 0;
        vrijemeDo = vrijemeDo != null ? vrijemeDo : System.currentTimeMillis();

        if (vrijeme >= vrijemeOd && vrijeme <= vrijemeDo) {
          Obracun obracun = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
          obracuni.add(obracun);
        }
      }
      return obracuni;

    } catch (SQLException ex) {
      Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public List<Obracun> dohvatiZaPartnera(int idPartner, Long vrijemeOd, Long vrijemeDo) {
    String upit =
        "SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE partner = ?";

    List<Obracun> obracuni = new ArrayList<>();

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setInt(1, idPartner);

      ResultSet rs = s.executeQuery();

      while (rs.next()) {
        int partner = rs.getInt("partner");
        String id = rs.getString("id");
        boolean jelo = rs.getBoolean("jelo");
        float kolicina = rs.getFloat("kolicina");
        float cijena = rs.getFloat("cijena");
        long vrijeme = rs.getTimestamp("vrijeme").getTime();

        vrijemeOd = vrijemeOd != null ? vrijemeOd : 0;
        vrijemeDo = vrijemeDo != null ? vrijemeDo : System.currentTimeMillis();

        if (vrijeme >= vrijemeOd && vrijeme <= vrijemeDo) {
          Obracun obracun = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
          obracuni.add(obracun);
        }

      }
      return obracuni;

    } catch (SQLException ex) {
      Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public boolean dodajObracune(List<Obracun> listaObracuna) {
    String upit = "INSERT INTO obracuni (partner, id, jelo, kolicina, cijena, vrijeme) "
        + "VALUES (?, ?, ?, ?, ?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      int brojAzuriranja = 0;

      for (var obracun : listaObracuna) {
        s.setInt(1, obracun.partner());
        s.setString(2, obracun.id());
        s.setBoolean(3, obracun.jelo());
        s.setFloat(4, obracun.kolicina());
        s.setFloat(5, obracun.cijena());
        s.setTimestamp(6, new Timestamp(obracun.vrijeme()));

        brojAzuriranja += s.executeUpdate();
      }
      return brojAzuriranja == listaObracuna.size();

    } catch (Exception ex) {
      Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

}
