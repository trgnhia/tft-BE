package org.example.core.logging;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.entities.CmsLog;

@Getter
@Setter
@AllArgsConstructor
public class RetryCmslog {
    private CmsLog cmsLog;
    private int retryCount;
}
