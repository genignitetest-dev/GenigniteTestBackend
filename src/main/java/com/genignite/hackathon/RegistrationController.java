package com.genignite.hackathon;
import com.razorpay.*;
import jakarta.validation.Valid;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController @RequestMapping("/api")
public class RegistrationController {
  @Value("${razorpay.key-id}") String keyId;
  @Value("${razorpay.key-secret}") String secret;
  @Value("${app.fee.individual}") int feeIndividual;
  @Value("${app.fee.team}") int feeTeam;
  @Value("${app.fee.stall}") int feeStall;
  private final RegistrationRepository repo;
  public RegistrationController(RegistrationRepository repo){ this.repo=repo; }

  private static ResponseStatusException bad(String m){ return new ResponseStatusException(HttpStatus.BAD_REQUEST,m); }

  private static boolean blank(String v){ return v==null || v.isBlank(); }

  /** Step 1: validate form, create Razorpay order. Fee is always computed on the server. */
  @PostMapping("/registrations/order")
  public Map<String,Object> order(@Valid @RequestBody RegisterRequest r) throws Exception {
    String t = r.type().toUpperCase();
    boolean team = t.equals("TEAM"), stall = t.equals("STALL");
    if (!team && !stall && !t.equals("INDIVIDUAL")) throw bad("Invalid registration type");
    int n = r.members().size();
    if (team && n != 3) throw bad("A team needs exactly 3 members");
    if (stall && (n < 3 || n > 5)) throw bad("A stall needs 3 to 5 members");
    if (!team && !stall && n != 1) throw bad("Individual registration needs 1 participant");
    if ((team || stall) && blank(r.teamName())) throw bad(stall ? "Stall name is required" : "Team name is required");
    if (blank(r.abstractText()) || r.abstractText().length() > 3000) throw bad(stall ? "Describe what you will showcase" : "Abstract is required (max 3000 characters)");
    if (!stall) {
      if (blank(r.projectName())) throw bad("Project name is required");
      for (Member m : r.members()) {
        if (blank(m.college) || blank(m.branch) || blank(m.year)) throw bad("College, branch and year are required");
        if (!m.year.equals("3rd Year") && !m.year.equals("4th Year")) throw bad("Only B.Tech 3rd and 4th year students can register");
      }
    }
    int amount = stall ? feeStall : team ? feeTeam : feeIndividual;

    JSONObject o = new JSONObject();
    o.put("amount", amount * 100); o.put("currency","INR"); o.put("receipt","GZHN-"+System.currentTimeMillis());
    Order order = new RazorpayClient(keyId, secret).orders.create(o);
    String orderId = order.get("id");

    Registration reg = new Registration();
    reg.type = t; reg.teamName = (team || stall) ? r.teamName().trim() : null;
    reg.projectName = r.projectName(); reg.abstractText = r.abstractText();
    reg.techInterests = r.techInterests(); reg.amount = amount; reg.orderId = orderId; reg.members = r.members();
    repo.save(reg);
    return Map.of("orderId", orderId, "amount", amount*100, "keyId", keyId);
  }

  /** Step 2: verify Razorpay signature. Only then is the registration confirmed (PAID). */
  @PostMapping("/registrations/verify")
  public Map<String,Object> verify(@RequestBody Map<String,String> b) throws Exception {
    JSONObject p = new JSONObject();
    p.put("razorpay_order_id", b.get("razorpay_order_id"));
    p.put("razorpay_payment_id", b.get("razorpay_payment_id"));
    p.put("razorpay_signature", b.get("razorpay_signature"));
    if (!Utils.verifyPaymentSignature(p, secret)) throw bad("Payment verification failed");
    Registration reg = repo.findByOrderId(b.get("razorpay_order_id")).orElseThrow(() -> bad("Registration not found"));
    reg.status = "PAID"; reg.paymentId = b.get("razorpay_payment_id"); repo.save(reg);
    return Map.of("status","PAID","registrationId","GZHN-"+(1000+reg.id),"amount",reg.amount);
  }
}
