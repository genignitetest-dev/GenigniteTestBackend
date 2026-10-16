package com.genignite.hackathon;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    @Value("${app.fee.individual}")
    private int feeIndividual;

    @Value("${app.fee.team}")
    private int feeTeam;

    @Value("${app.fee.stall}")
    private int feeStall;

    private final RegistrationRepository repo;

    public RegistrationController(RegistrationRepository repo) {
        this.repo = repo;
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST, message
        );
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Register a participant, team, or stall without online payment.
     * The fee is calculated on the server.
     */

    /** Records a user's claimed UPI reference; this does not verify or capture payment. */
    @PostMapping("/registrations/{id}/payment-reference")
    public Map<String, Object> submitPaymentReference(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Registration reg = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found"));
        String reference = body == null ? null : body.get("reference");
        if (blank(reference) || reference.trim().length() > 100) {
            throw bad("A valid UPI transaction/reference number is required");
        }
        reg.paymentReference = reference.trim();
        reg.paymentStatus = "PENDING_VERIFICATION";
        repo.save(reg);
        return Map.of(
                "registrationId", "GZHN-" + (1000 + reg.id),
                "paymentStatus", reg.paymentStatus,
                "message", "Payment reference submitted for manual verification"
        );
    }

    @PostMapping("/registrations/order")
    public Map<String, Object> order(
            @Valid @RequestBody RegisterRequest r) {

        if (r.type() == null || r.members() == null
                || r.members().isEmpty()) {
            throw bad("Registration type and members are required");
        }

        String t = r.type().trim().toUpperCase();

        boolean team = t.equals("TEAM");
        boolean stall = t.equals("STALL");
        boolean individual = t.equals("INDIVIDUAL");

        if (!team && !stall && !individual) {
            throw bad("Invalid registration type");
        }

        int n = r.members().size();

        if (team && n != 3) {
            throw bad("A team needs exactly 3 members");
        }

        if (stall && (n < 3 || n > 5)) {
            throw bad("A stall needs 3 to 5 members");
        }

        if (individual && n != 1) {
            throw bad("Individual registration needs 1 participant");
        }

        if ((team || stall) && blank(r.teamName())) {
            throw bad(stall
                    ? "Stall name is required"
                    : "Team name is required");
        }

        if (blank(r.abstractText())
                || r.abstractText().length() > 3000) {
            throw bad(stall
                    ? "Describe what you will showcase"
                    : "Abstract is required (max 3000 characters)");
        }

        if (!stall) {
            if (blank(r.projectName())) {
                throw bad("Project name is required");
            }

            for (Member m : r.members()) {
                if (m == null
                        || blank(m.college)
                        || blank(m.branch)
                        || blank(m.year)) {
                    throw bad("College, branch and year are required");
                }

                if (!m.year.equals("3rd Year")
                        && !m.year.equals("4th Year")) {
                    throw bad(
                            "Only B.Tech 3rd and 4th year students can register"
                    );
                }
            }
        }

        int amount = stall
                ? feeStall
                : team ? feeTeam : feeIndividual;

        Registration reg = new Registration();

        reg.type = t;
        reg.teamName = (team || stall)
                ? r.teamName().trim()
                : null;
        reg.projectName = r.projectName();
        reg.abstractText = r.abstractText();
        reg.techInterests = r.techInterests();
        reg.amount = amount;
        reg.status = "PENDING";
        reg.orderId = null;
        reg.paymentId = null;
        reg.members = r.members();

        Registration saved = repo.save(reg);

        return Map.of(
                "status", "PENDING",
                "registrationId", "GZHN-" + (1000 + saved.id),
                "databaseId", saved.id,
                "amount", amount,
                "message", "Registration submitted successfully"
        );
    }
}
