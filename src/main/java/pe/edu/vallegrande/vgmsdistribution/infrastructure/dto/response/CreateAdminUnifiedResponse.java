package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdminUnifiedResponse {
	private boolean success;
	private String message;
	private DataSection data;
	private String timestamp;

	@lombok.Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DataSection {
		private String accessToken;
		private String refreshToken;
		private String tokenType;
		private Integer expiresIn;
		private Integer refreshExpiresIn;
		private UserInfo userInfo;
	}

	@lombok.Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserInfo {
		private String userId;
		private String username;
		private String email;
		private String firstName;
		private String lastName;
		private String organizationId;
		private List<String> roles;
		private Boolean mustChangePassword;
		private String lastLogin;
	}
} 