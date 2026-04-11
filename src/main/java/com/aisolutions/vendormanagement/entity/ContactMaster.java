package com.aisolutions.vendormanagement.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "m03ContactMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactMaster {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "Code", nullable = false, updatable = false)
  public Long uniqId;

  @Column(name = "ContType")
  public String contactType;

  @Column(name = "ContID")
  public String contactId;

  @Column(name = "ContName")
  public String contactName;

  @Column(name = "ContRegId")
  public String contactRegId;

  @Column(name = "ContRegDate")
  public LocalDateTime dateJoin;

  @Column(name = "InActive")
  public Boolean inActive;

  @Column(name = "EntryStaff")
  public String entryStaff;

  @Column(name = "EntryDate")
  public LocalDateTime entryDate;

  @Column(name = "LastAddEditStaff")
  public String lastEditStaff;

  @Column(name = "LastAddEditDate")
  public LocalDateTime lastEditDate;
}
