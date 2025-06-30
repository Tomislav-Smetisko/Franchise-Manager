package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jf;

import java.io.Serializable;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("spavanje")
public class Spavanje implements Serializable {

  private static final long serialVersionUID = 9178484354159907885L;

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartner;

  private long vrijeme;

  public long getVrijeme() {
    return vrijeme;
  }

  public void setVrijeme(long vrijeme) {
    this.vrijeme = vrijeme;
  }

  public void spava() {
    servisPartner.getSpava(vrijeme).getStatus();
  }
}
