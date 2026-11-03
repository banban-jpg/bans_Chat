package egovframework.example.organization.dto;
import java.util.List;
import jakarta.validation.constraints.*;
public final class DirectoryModels {
 private DirectoryModels() {}
 public record EmployeeView(String employeeId, String displayName, String departmentCode, String jobTitle, String companyEmail, String extensionNo) {}
 public record Page(List<EmployeeView> items, long total, int page, int size) {}
 public record Search(String name, String departmentId, String task, int page, int size, String sort) { public int offset() { return page * size; } }
 public record Create(@NotBlank @Pattern(regexp="EMP-[0-9]{3,10}") String employeeId,
   @NotBlank @Size(max=100) String displayName, @NotBlank @Pattern(regexp="D[0-9]{2}") String departmentId,
   @NotBlank @Size(max=100) String jobTitle, @NotBlank @Email @Size(max=200) @Pattern(regexp="[^@\\s]+@bans\\.example") String companyEmail,
   @NotBlank @Pattern(regexp="[0-9-]{1,10}") String extensionNo, @NotBlank @Size(max=300) String task) { @com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String key,Object value) { throw new IllegalArgumentException("Unknown personnel field"); } }
 public record Status(@NotBlank @Pattern(regexp="ACTIVE|SUSPENDED|DEPARTED") String status) { @com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String key,Object value) { throw new IllegalArgumentException("Unknown personnel field"); } }
 public record Move(@NotBlank @Pattern(regexp="D[0-9]{2}") String departmentId) { @com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String key,Object value) { throw new IllegalArgumentException("Unknown personnel field"); } }
 public record State(String departmentCode, String status) {}
}
