/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.logic.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Project implements Serializable {
  @Id
  @GeneratedValue
  private Integer id;

  @Column(nullable = false)
  private String name;

  @ManyToOne
  @JoinColumn(nullable = false)
  private Currency grantCurrency;

  @ManyToOne
  @JoinColumn(nullable = false)
  private Currency accountCurrency;

  public Project() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * @return the grantCurrency
   */
  public Currency getGrantCurrency() {
    return grantCurrency;
  }

  /**
   * @param grantCurrency the grantCurrency to set
   */
  public void setGrantCurrency(Currency grantCurrency) {
    this.grantCurrency = grantCurrency;
  }

  /**
   * @return the accountCurrency
   */
  public Currency getAccountCurrency() {
    return accountCurrency;
  }

  /**
   * @param accountCurrency the accountCurrency to set
   */
  public void setAccountCurrency(Currency accountCurrency) {
    this.accountCurrency = accountCurrency;
  }
}
