package Models;

import java.time.LocalDate;

public class User {

    private int id;
    private String nom;
    private String prenom;
    private String number;
    private String mail;
    private String password;
    private String role;

    public User(int id, String nom, String prenom, String number, String mail, String password, String role) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.number = number;
        this.mail = mail;
        this.password = password;
        this.role = role;
    }
    public User(String nom, String prenom, String number, String mail, String password, String role) {
        this.nom = nom;
        this.prenom = prenom;
        this.number = number;
        this.mail = mail;
        this.password = password;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", number='" + number + '\'' +
                ", mail='" + mail + '\'' +
                ", password='********'" +
                ", role='" + role + '\'' +
                '}';
    }
}
