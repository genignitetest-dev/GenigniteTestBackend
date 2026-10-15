package com.genignite.hackathon;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
@Entity @Table(name="registrations")
public class Registration {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  public String type; public String teamName; public String projectName; public int amount;
  @Column(columnDefinition="TEXT") public String abstractText;
  @Column(length=500) public String techInterests;
  @Column(unique=true) public String orderId; public String paymentId;
  public String status="PENDING"; public Instant createdAt=Instant.now();
  @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="registration_members")
  public List<Member> members=new ArrayList<>();
}
