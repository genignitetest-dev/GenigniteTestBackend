package com.genignite.hackathon;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record RegisterRequest(@NotBlank String type, String teamName, String projectName, String abstractText, String techInterests, @Valid @NotEmpty List<Member> members) {}
