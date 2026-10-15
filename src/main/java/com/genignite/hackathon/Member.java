package com.genignite.hackathon;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
@Embeddable
public class Member {
  @NotBlank public String name;
  @NotBlank @Email public String email;
  @Pattern(regexp="\\d{10}", message="Phone must be 10 digits") public String phone;
  public String college;
  public String branch;
  @Column(name="study_year") public String year;
  @NotBlank public String tshirt;
  @NotBlank public String food;
}
