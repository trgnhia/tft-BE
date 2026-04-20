package org.example.dto.upload;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.common.constant.Constants;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteUploadRequest {
    @NotBlank(message = "{" + Constants.MessageKey.UPLOAD_URL_REQUIRED + "}")
    private String url;
}
