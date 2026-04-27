package com.budget.ms.repository;

import com.budget.ms.entity.AppSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<AppSettings,Long> {
}
