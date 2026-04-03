package org.example.repositories;

import org.example.entities.CmsLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CmsLogRepository  extends JpaRepository<CmsLog, Long> {

}
