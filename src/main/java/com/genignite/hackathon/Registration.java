package com.genignite.hackathon;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "registrations")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String type;

    public String teamName;

    public String projectName;

    public int amount;

    @Column(columnDefinition = "TEXT")
    public String abstractText;

    @Column(length = 500)
    public String techInterests;

    @Column(unique = true, nullable = true)
    public String orderId;

    @Column(nullable = true)
    public String paymentId;

    @Column(length = 100)
    public String paymentReference;

    @Column(length = 40)
    public String paymentStatus = "NOT_REPORTED";

    public String status = "PENDING";

    public Instant createdAt = Instant.now();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "registration_members",
            joinColumns = @JoinColumn(name = "registration_id")
    )
    public List<Member> members = new ArrayList<>();
}