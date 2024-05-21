package com.example.applicationgestinemployes.controller;

import com.example.applicationgestinemployes.model.Conge;
import com.example.applicationgestinemployes.model.Employe;
import com.example.applicationgestinemployes.model.Responsable;
import com.example.applicationgestinemployes.service.CongeService;
import com.example.applicationgestinemployes.service.EmployeService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@RequestScoped
public class CongeController implements Serializable {

    @Inject
    private CongeService congeService;

    @Inject
    private EmployeService employeService;
    private Conge selectedConge = new Conge();
    private Conge newConge = new Conge();
    private List<Conge> pendingConges;

    @PostConstruct
    public void init() {
        pendingConges = congeService.findAllPending();
        System.out.println("Nombre de demandes de congé en attente: " + pendingConges.size());
        for (Conge conge : pendingConges) {
            System.out.println("Conge: " + conge);
        }
    }

    public String createConge() {
        Employe employe = getLoggedInEmploye();
        if (employe == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Employé non trouvé."));
            return "failure";
        }

        Responsable responsable = employe.getResponsable();
        if (responsable == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Responsable non défini pour l'employé."));
            return "failure";
        }

        newConge.setEmploye(employe);
        newConge.setStatut("EN_ATTENTE");
        congeService.create(newConge);

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Demande de congé soumise avec succès."));
        newConge = new Conge();

        return "success";
    }
    public List<Conge> getHistoriqueCongesUtilisateurConnecte() {
        Employe employe = getLoggedInEmploye();
        if (employe != null) {
            List<Conge> tousConges = congeService.getAllConges();
            List<Conge> historiqueConges = new ArrayList<>();
            for (Conge conge : tousConges) {
                if (conge.getEmploye() != null && conge.getEmploye().getIdEmploye() != null && conge.getEmploye().getIdEmploye().equals(employe.getIdEmploye())) {
                    historiqueConges.add(conge);
                }
            }
            return historiqueConges;
        } else {
            return null;
        }
    }

    public void approuverConge(Long congeId) {
        congeService.approuverConge(congeId);
        pendingConges = congeService.findAllPending();
    }

    public void rejeterConge(Long congeId) {
        congeService.rejeterConge(congeId);
        pendingConges = congeService.findAllPending();
    }

    public Employe getLoggedInEmploye() {
        String username = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("username");
        if (username != null) {
            List<Employe> employes = employeService.getAllEmployes();
            for (Employe employe : employes) {
                if (employe.getUsername().equals(username)) {
                    return employe;
                }
            }
        }
        return null;
    }

    public Conge getNewConge() {
        return newConge;
    }
    public Conge getSelectedConge() {
        return selectedConge;
    }

    public void setSelectedConge(Conge selectedConge) {
        this.selectedConge = selectedConge;
    }
    public void setNewConge(Conge newConge) {
        this.newConge = newConge;
    }

    public List<Conge> getPendingConges() {
        return pendingConges;
    }

    public void setPendingConges(List<Conge> pendingConges) {
        this.pendingConges = pendingConges;
    }
}
